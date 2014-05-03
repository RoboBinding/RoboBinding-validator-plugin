package org.robobinding.plugins.validator

import groovy.transform.Immutable

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class FilesWithBindingAttributes {

	static final def ROBOBINDING_NAMESPACE = 'http://robobinding.org/android'
	static final def NO_VIEWS_WITH_BINDINGS_FOUND = []
	XmlWithBindingAttributes xmlWithBindingAttributes
	
	List<ViewBindingAttributes> findViewsWithBindings(File xmlFile) {
		String xml = xmlFile.text
		def robobindingNamespaceDeclarationPrefix = getRoboBindingNamespaceDeclarationPrefix(xml)
		
		if (robobindingNamespaceDeclarationPrefix) {
			return xmlWithBindingAttributes.findViewsWithBindings(xml, robobindingNamespaceDeclarationPrefix)
		}
		
		return NO_VIEWS_WITH_BINDINGS_FOUND
	}
	
	private getRoboBindingNamespaceDeclarationPrefix(String xml) {
		def rootNode = new XmlSlurper().parseText(xml)

		def xmlClass = rootNode.getClass()
		def gpathClass = xmlClass.getSuperclass()
		def namespaceTagHintsField = gpathClass.getDeclaredField("namespaceTagHints")
		namespaceTagHintsField.setAccessible(true)

		def namespaceDeclarations = namespaceTagHintsField.get(rootNode)

		return namespaceDeclarations.find { key, value ->
			value == ROBOBINDING_NAMESPACE
		}?.key
	}

}
