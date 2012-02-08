/**
 * Copyright 2012 Cheng Wei, Robert Taylor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.robobinding.validator

import groovy.lang.Closure

import org.robobinding.binder.BindingAttributeProcessor
import org.robobinding.binder.ViewNameResolver

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class BindingAttributeValidator {

	def resFolder
	def bindingAttributeProcessor
	def viewNameResolver

	BindingAttributeValidator(baseFolder) {
		resFolder = new File(baseFolder, "res")
		viewNameResolver = new ViewNameResolver()
	}

	def validate() {
		def errorMessages = []

		inEachLayoutFolder { layoutFolder ->
			
			inEachXmlFileWithBindings(layoutFolder) { xmlFile ->
				
				forEachViewWithBindingAttributes(xmlFile.text) { viewName, attributes ->

					def fullyQualifiedViewName = getFullQualifiedViewName(viewName)
					def errorMessage = validateView(fullyQualifiedViewName, attributes)

					if (errorMessage)
						errorMessages << errorMessage
						
				}
			}
		}

		errorMessages
	}

	def inEachLayoutFolder (Closure c) {
		resFolder.eachDirMatch(~/[layout].*/) { c.call(it) }
	}

	def inEachXmlFile(folder, Closure c) {
		folder.eachFileMatch(~/.*[.xml]/) { c.call(it) }
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

		rootNode.children().each { processViewNode(it, c) }
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

		viewNode.children().each { processViewNode(it, c) }
	}

	def getFullQualifiedViewName(viewName) {
		viewNameResolver.getViewNameFromLayoutTag(viewName)
	}

	def validateView(fullyQualifiedViewName, attributes) {
		if (!fullyQualifiedViewName.startsWith("android"))
			return

		try {
			getBindingAttributeProcessor().process(instanceOf(fullyQualifiedViewName), attributes)
		}
		catch (RuntimeException e) {
			return "${fullyQualifiedViewName} in ${xmlFile.name} has binding errors:\n\n${e.message}"
		}
	}

	def instanceOf(fullyQualifiedViewName) {
		Class viewClass = Class.forName(fullyQualifiedViewName)
		org.mockito.Mockito.mock(viewClass)
	}
	
	def getBindingAttributeProcessor() {
		if (bindingAttributeProcessor == null)
			bindingAttributeProcessor = new BindingAttributeProcessor(null, true)

		bindingAttributeProcessor
	}
}
