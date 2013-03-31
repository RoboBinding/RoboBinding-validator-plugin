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
