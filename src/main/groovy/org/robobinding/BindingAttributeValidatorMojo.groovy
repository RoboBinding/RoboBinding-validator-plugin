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
package org.robobinding

import java.util.logging.Logger

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.project.MavenProject
import org.codehaus.classworlds.ClassRealm
import org.codehaus.groovy.maven.mojo.GroovyMojo
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator
import org.codehaus.plexus.component.configurator.ComponentConfigurationException
import org.codehaus.plexus.component.configurator.ConfigurationListener
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator
import org.codehaus.plexus.configuration.PlexusConfiguration
import org.robobinding.binder.BindingAttributeProcessor
import org.robobinding.binder.ViewNameResolver

/**
 *
 * @goal validate-bindings
 * @phase compile
 * @configurator include-project-dependencies
 * 
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class BindingAttributeValidatorMojo extends GroovyMojo
{
	/**
	 * @parameter expression="${basedir}"
	 * @required
	 */
	def baseFolder
	
	def resFolder
	def bindingAttributeProcessor
	
//	/**
//	* The maven project.
//	*
//	* @parameter expression="${project}"
//	* @required
//	*/
//   protected MavenProject project;
//   
//   private ClassLoader classLoader;
//   
//   protected ClassLoader getClassLoader() {
//	 synchronized (BindingAttributeValidatorMojo.class) {
//	   if (classLoader != null)
//		 return classLoader;
//	 }
//	 synchronized (BindingAttributeValidatorMojo.class) {
//	   List<URL> urls = new ArrayList<URL>();
//	   for (Object object : project.getCompileClasspathElements()) {
//		 String path = (String) object;
//		 urls.add(new File(path).toURL());
//	   }
//	   
//	   println "Dependencies: ${project.getDependencies()}"
//	   println "Compile Dependencies: ${project.getCompileDependencies()}"
//	   
//	   for (Object object : project.getDependencies()) {
//		   String path = (String) object;
//		   urls.add(new File(path).toURL());
//		 }
//	   
//	   URL[] urlArray = urls
//	   classLoader = new URLClassLoader(urlArray /*, parentClassLoader */);
//	   // Thread.currentThread().setContextClassLoader(classLoader); // if needed
//	   return classLoader;
//	 }
//   }
	
	void execute()
	{
		log.info("Validating binding attributes...")
		
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
						//throw new MojoFailureException("\n\n${fullyQualifiedViewName} in ${xmlFile.name} has binding errors:\n\n${e.message}")
						errorMessages << "\n\n${fullyQualifiedViewName} in ${xmlFile.name} has binding errors:\n\n${e.message}"
					}
				}
//					def view = getViewInstance(viewName)
//					def errorMessage = validateViewAttributes(view, attributes)
//					if (errorMessage)
//						errorMessages << errorMessage
				//}
			}
		}
		
		if (errorMessages)
			throw new MojoFailureException(describe(errorMessages))
		
		log.info("Done!")
	}

	def describe(errorMessages) {
		def message
		
		errorMessages.each {
			message += "\n\n${it}"
		}
		
		message
	}
	
//	protected Class<?> getClass(String className) {
//		return getClassLoader().loadClass(className);
//	  }
	
//	def getViewInstance(viewName) {
//		def fullyQualifiedViewName = new ViewNameResolver().getViewNameFromLayoutTag(viewName)
//		Class viewClass = getClass(fullyQualifiedViewName)
//		return org.mockito.Mockito.mock(viewClass)
//	}
//	
//	def validateViewAttributes(view, attributes) {
//		def errorMessage
//		
//		try {
//			getBindingAttributeProcessor().process(view, attributes)
//		}
//		catch (RuntimeException e) {
//			errorMessage = "\n\n${fullyQualifiedViewName} in ${xmlFile.name} has binding errors:\n\n${e.message}"
//		}
//		
//		return errorMessage
//	}
	
	def inEachLayoutFolder (Closure c) {
		getResFolder().eachDirMatch(~/[layout].*/) {
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
	
	def getResFolder() {
		if (resFolder == null)
			resFolder = new File(baseFolder, "res")
			
		resFolder	
	}
	
	def getBindingAttributeProcessor() {
		if (bindingAttributeProcessor == null)
			bindingAttributeProcessor = new BindingAttributeProcessor(null, true)
	
		bindingAttributeProcessor		
	}
	
}
