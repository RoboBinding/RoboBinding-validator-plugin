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
import groovy.util.slurpersupport.GPathResult

import java.io.File;

import javax.xml.namespace.QName;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlWithBindingAttributes {

	XmlLineNumberDecorator xmlLineNumberDecorator
	
	List<ViewNameAndAttributes> findViewsWithBindings(String xml, String bindingPrefix) {
		List<ViewNameAndAttributes> viewNamesAndAttributes = []
		String xmlWithLineNumbers = xmlLineNumberDecorator.embedLineNumbers(xml, bindingPrefix)
		
		GPathResult rootNode = new XmlSlurper().parseText(xmlWithLineNumbers)
		
		rootNode.children().eachWithIndex { it, index ->
			processViewNode(it, viewNamesAndAttributes) 
		}
		
		viewNamesAndAttributes
	}
	
	def processViewNode(viewNode, viewNamesAndAttributes) {
		ViewName viewName = new ViewName(value: viewNode.name(), lineNumber: getLineNumber(viewNode))
		def rawBindingAttributesMap = getBindingAttributesForNode(viewNode)
		addBindingAttributesToList(rawBindingAttributesMap, viewName, viewNamesAndAttributes)
		
		viewNode.children().each { 
			processViewNode(it, viewNamesAndAttributes) 
		}
	}

	private getBindingAttributesForNode(viewNode) {
		def viewAttributes = viewNode.attributes()
		def nodeField = viewNode.getClass().getDeclaredField("node")
		nodeField.setAccessible(true)
		def node = nodeField.get(viewNode)

		def attributesWithRoboBindingNamespace = node.@attributeNamespaces.findAll { it.value == FilesWithBindingAttributes.ROBOBINDING_NAMESPACE }
		def bindingAttributeNames = attributesWithRoboBindingNamespace*.key
		viewAttributes.subMap(bindingAttributeNames)
	}
	
	private int getLineNumber(viewNode) {
		viewNode.attributes()[XmlLineNumberDecorator.LINE_NUMBER_ATTRIBUTE].toInteger()
	}
	
	private addBindingAttributesToList(rawBindingAttributesMap, ViewName viewName, viewNamesAndAttributes) {
		List<BindingAttribute> bindingAttributes = getBindingAttributes(rawBindingAttributesMap)
		if (bindingAttributes) {
			
			def viewBindingAttributes = new ViewNameAndAttributes(
					viewName: viewName,
					bindingAttributes: bindingAttributes)
			
			viewNamesAndAttributes << viewBindingAttributes
		}
	}
	
	private List<BindingAttribute> getBindingAttributes(rawBindingAttributesMap) {
		def bindingAttributes = []
		
		rawBindingAttributesMap.each { attributeQName, attributeValue ->
			String attributeName = QName.valueOf(attributeQName).localPart
			bindingAttributes << createBindingAttribute(attributeName, attributeValue)
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
