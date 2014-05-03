package org.robobinding.plugins.validator

import groovy.util.slurpersupport.GPathResult

import javax.xml.namespace.QName

import org.mockito.Mockito
import org.robobinding.ViewNameResolver

import android.view.View

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlWithBindingAttributes {

	XmlLineNumberDecorator xmlLineNumberDecorator
	ViewNameResolver viewNameResolver

	List<ViewBindingAttributes> findViewsWithBindings(String xml, String bindingPrefix) {
		List<ViewBindingAttributes> viewBindingAttributes = []
		String xmlWithLineNumbers = xmlLineNumberDecorator.embedLineNumbers(xml, bindingPrefix)

		GPathResult rootNode = new XmlSlurper().parseText(xmlWithLineNumbers)

		rootNode.children().eachWithIndex { it, index ->
			processViewNode(it, viewBindingAttributes)
		}

		viewBindingAttributes
	}

	def processViewNode(viewNode, viewNamesAndAttributes) {
		String viewName = viewNameResolver.getViewNameFromLayoutTag(viewNode.name())

		if (viewName.startsWith("android")) {
			View view = instanceOf(viewName)
			int viewLineNumber = getLineNumber(viewNode)
			def rawBindingAttributesMap = getBindingAttributesForNode(viewNode)
			addBindingAttributesToList(rawBindingAttributesMap, view, viewLineNumber, viewNamesAndAttributes)
		}

		viewNode.children().each {
			processViewNode(it, viewNamesAndAttributes)
		}
	}

	def instanceOf(fullyQualifiedViewName) {
		Class viewClass = Class.forName(fullyQualifiedViewName)
		Mockito.mock(viewClass)
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

	private addBindingAttributesToList(rawBindingAttributesMap, View view, int viewLineNumber, viewNamesAndAttributes) {
		Map<String, BindingAttribute> bindingAttributes = getBindingAttributes(rawBindingAttributesMap)
		if (bindingAttributes) {

			def viewBindingAttributes = new ViewBindingAttributes(
					view: view,
					viewLineNumber: viewLineNumber,
					bindingAttributes: bindingAttributes)

			viewNamesAndAttributes << viewBindingAttributes
		}
	}

	private Map<String, BindingAttribute> getBindingAttributes(rawBindingAttributesMap) {
		def bindingAttributes = [:]

		rawBindingAttributesMap.each { attributeQName, attributeValue ->
			String rawAttributeName = QName.valueOf(attributeQName).localPart
			addBindingAttributeToMap(rawAttributeName, attributeValue, bindingAttributes)
		}

		bindingAttributes
	}

	private addBindingAttributeToMap(String rawAttributeName, String attributeValue, Map bindingAttributes) {
		String[] attributeDetails = rawAttributeName.split('_')
		bindingAttributes[attributeDetails[0]] = new BindingAttribute(
				attributeName: attributeDetails[0],
				attributeValue: attributeValue,
				lineNumber: attributeDetails[1].toInteger())
	}

}
