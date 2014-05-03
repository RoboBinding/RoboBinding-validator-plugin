package org.robobinding.plugins.validator

import org.robobinding.PendingAttributesForView
import org.robobinding.PendingAttributesForViewImpl

import android.view.View

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class ViewBindingAttributes {

	View view
	int viewLineNumber
	Map<String, BindingAttribute> bindingAttributes
	
	BindingAttribute getAt(String attributeName) {
		bindingAttributes[attributeName]
	}
	
	PendingAttributesForView asPendingAttributesForView() {
		def attributeMappings = [:]
		
		bindingAttributes.each { attributeName, bindingAttribute ->
			attributeMappings[attributeName] = bindingAttribute.attributeValue
		}
		
		new PendingAttributesForViewImpl(view, attributeMappings)
	}
}
