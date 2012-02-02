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

import org.apache.maven.plugin.MojoFailureException
import org.codehaus.groovy.maven.mojo.GroovyMojo

/**
 *
 * @goal validate-xml
 * @phase validate
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
	
	void execute()
	{
		log.info("Validating binding attributes...")
		
		inEachLayoutFolder {
			inEachXmlFileWithBindings(it) { xmlFile ->
				forEachViewWithBindingAttributes(xmlFile.text) { viewName, attributes ->
					
					//5. For each binding attribute declared, check the corresponding view against the candidate providers (BindingAttributeProviderResolver.getCandidateProviders()
					
					//6. If attribute is not resolved, throw exception
					
					if (attributes.contains("mistake"))
						throw new MojoFailureException("${viewName} does not support attribute 'mistake'")
				}
			}
		}
		
		log.info("Done.")
	}

	
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
		
		def nodeField = viewNode.getClass().getDeclaredField("node")
		nodeField.setAccessible(true)
		def node = nodeField.get(viewNode)
		def attributeNamespacesField = node.getClass().getDeclaredField("attributeNamespaces")
		attributeNamespacesField.setAccessible(true)
		def attributeNamespaces = attributeNamespacesField.get(node)
		
		def bindingAttributes = attributeNamespaces.findAll { it.value == 'http://robobinding.org/android' }
		c.call(viewName, bindingAttributes*.key)
		
		viewNode.children().each {
			processViewNode(it, c)
		}
	}
	
	def getResFolder() {
		if (resFolder == null)
			resFolder = new File(baseFolder, "res")
			
		resFolder	
	}
}
