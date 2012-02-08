package org.robobinding.validator

import groovy.lang.Closure

import org.robobinding.binder.BindingAttributeProcessor
import org.robobinding.binder.ViewNameResolver

class BindingAttributeValidator {

   def resFolder
   def bindingAttributeProcessor
   
   BindingAttributeValidator(baseFolder) {
	   resFolder = new File(baseFolder, "res")
   }
   
   def validate()
   {
	   def errorMessages = []
	   
	   inEachLayoutFolder {
		   inEachXmlFileWithBindings(it) { xmlFile ->
			   forEachViewWithBindingAttributes(xmlFile.text) { viewName, attributes ->
				   def fullyQualifiedViewName = new ViewNameResolver().getViewNameFromLayoutTag(viewName)
				   
				   if (!fullyQualifiedViewName.startsWith("android"))
					   return
				   
				   Class viewClass = Class.forName(fullyQualifiedViewName)
				   def view = org.mockito.Mockito.mock(viewClass)
				   
				   try {
					   getBindingAttributeProcessor().process(view, attributes)
				   }
				   catch (RuntimeException e) {
					   errorMessages << "${fullyQualifiedViewName} in ${xmlFile.name} has binding errors:\n\n${e.message}"
				   }
			   }
		   }
	   }
	   
	   errorMessages
   }

   def inEachLayoutFolder (Closure c) {
	   resFolder.eachDirMatch(~/[layout].*/) {
		   c.call(it)
	   }
   }
   
   def inEachXmlFile(folder, Closure c) {
	   folder.eachFileMatch(~/.*[.xml]/) {
		   c.call(it)
	   }
   }
   
   def inEachXmlFileWithBindings(folder, Closure c) {
	   inEachXmlFile(folder) {
		   if (getRoboBindingNamespaceDeclaration(it.text)) {
			   c.call(it)
		   }
	   }
   }
   
   def getRoboBindingNamespaceDeclaration(xml) {
	   def rootNode = new XmlSlurper().parseText(xml)
	   
	   def xmlClass = rootNode.getClass()
	   def gpathClass = xmlClass.getSuperclass()
	   def namespaceTagHints = gpathClass.getDeclaredField("namespaceTagHints")
	   namespaceTagHints.setAccessible(true)
	   
	   def namespaceDeclarations = namespaceTagHints.get(rootNode)
	   
	   for (String name : namespaceDeclarations.keySet()) {
		   if (namespaceDeclarations.get(name) == 'http://robobinding.org/android') {
			   return name
		   }
	   }
   }
   
   def forEachViewWithBindingAttributes(xml, Closure c) {
	   def rootNode = new XmlSlurper().parseText(xml)
	   
	   rootNode.children().each {
		   processViewNode(it, c)
	   }
   }
   
   def processViewNode(viewNode, Closure c) {
	   def viewName = viewNode.name()
	   def viewAttributes = viewNode.attributes()
	   
	   def nodeField = viewNode.getClass().getDeclaredField("node")
	   nodeField.setAccessible(true)
	   def node = nodeField.get(viewNode)
	   def attributeNamespacesField = node.getClass().getDeclaredField("attributeNamespaces")
	   attributeNamespacesField.setAccessible(true)
	   def attributeNamespaces = attributeNamespacesField.get(node)
	   
	   def bindingAttributes = attributeNamespaces.findAll { it.value == 'http://robobinding.org/android' }
	   def bindingAttributeNames = bindingAttributes*.key
	   c.call(viewName, viewAttributes.subMap(bindingAttributeNames))
	   
	   viewNode.children().each {
		   processViewNode(it, c)
	   }
   }
   
   def getBindingAttributeProcessor() {
	   if (bindingAttributeProcessor == null)
		   bindingAttributeProcessor = new BindingAttributeProcessor(null, true)
   
	   bindingAttributeProcessor
   }
	
}
