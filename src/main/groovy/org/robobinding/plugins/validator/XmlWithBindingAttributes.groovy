/**
 * Copyright 2013 Cheng Wei, Robert Taylor
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
package org.robobinding.plugins.validator

import groovy.lang.Closure;
import groovy.util.Node;

import java.io.File;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlWithBindingAttributes {

	//TODO remove this duplication
	static final def ROBOBINDING_NAMESPACE = 'http://robobinding.org/android'
	static final String LINE_NUMBER_ATTRIBUTE = "line_number"
	XmlLineNumberDecorator xmlLineNumberDecorator
	
	List<ViewNameAndAttributes> findViewsWithBindings(String xml, String bindingPrefix) {
		String xmlWithLineNumbers = xmlLineNumberDecorator.embedLineNumbers(xml, bindingPrefix)
		List<ViewNameAndAttributes> viewNamesAndAttributes = []
		
		def rootNode = new XmlSlurper().parseText(xmlWithLineNumbers)
		rootNode.children().each { 
			processViewNode(it, viewNamesAndAttributes) 
		}
	}
	
	def processViewNode(viewNode, viewNamesAndAttributes) {
		ViewName viewName = new ViewName(viewName: viewNode.name(), lineNumber: getLineNumber(viewNode))
		def viewAttributes = viewNode.attributes()

		def nodeField = viewNode.getClass().getDeclaredField("node")
		nodeField.setAccessible(true)
		def node = nodeField.get(viewNode)

		def attributesWithRoboBindingNamespace = node.@attributeNamespaces.findAll { it.value == ROBOBINDING_NAMESPACE }
		def bindingAttributeNames = attributesWithRoboBindingNamespace*.key
		def rawBindingAttributesMap = viewAttributes.subMap(bindingAttributeNames)
		
		Map<String, BindingAttribute> bindingAttributes = getBindingAttributes(rawBindingAttributesMap)
		
		def viewBindingAttributes = new ViewNameAndAttributes(
			viewName: viewName,
			bindingAttributes: bindingAttributes)
		
		viewNamesAndAttributes << viewBindingAttributes

		viewNode.children().each { processViewNode(it, viewNamesAndAttributes) }
	}
	
	//TODO remove these from XmlLineNumberDecorator
	private int getLineNumber(viewNode) {
		viewNode.attributes()[LINE_NUMBER_ATTRIBUTE].toInteger()
	}
	
	Map<String, BindingAttribute> getBindingAttributes(rawBindingAttributesMap) {
		def bindingAttributes = [:]
		
		rawBindingAttributesMap.each { attributeName, attributeValue ->
			bindingAttributes[attributeName] = createBindingAttribute(attributeName, attributeValue)
		}
		
		bindingAttributes
	}
	
	private BindingAttribute createBindingAttribute(String attributeName, String attributeValue) {
		String[] attributeDetails = attributeName.split('_')
		new BindingAttribute(attributeName: attributeDetails[0],
			 attributeValue: attributeValue,
			 lineNumber: attributeDetails[1].toInteger())
	}
}
