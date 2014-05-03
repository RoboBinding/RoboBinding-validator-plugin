package org.robobinding.plugins.validator

import org.robobinding.PendingAttributesForViewImpl

import spock.lang.Specification
import android.view.View

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class ViewBindingAttributesTest extends Specification {

	def "subscript operator should delegate to binding attributes"() {
		given:
		def bindingAttribute = new BindingAttribute()
		def bindingAttributes = ["attributeName" : bindingAttribute]
		def viewBindingAttributes = new ViewBindingAttributes(bindingAttributes: bindingAttributes)
		
		when:
		def result = viewBindingAttributes["attributeName"]
		
		then:
		result == bindingAttribute
	}
	
	def "should return as PendingAttributesForView"() {
		given:
		View view = Mock()
		def bindingAttribute1 = new BindingAttribute(attributeName: "attributeName1", attributeValue: "attributeValue1")
		def bindingAttribute2 = new BindingAttribute(attributeName: "attributeName2", attributeValue: "attributeValue2")
		def bindingAttributes = ["attributeName1" : bindingAttribute1, "attributeName2" : bindingAttribute2]
		def viewBindingAttributes = new ViewBindingAttributes(view: view, bindingAttributes: bindingAttributes)
		
		when:
		PendingAttributesForViewImpl result = viewBindingAttributes.asPendingAttributesForView()
	
		then:
		result.view == view
		result.@attributeMappings == [attributeName1: "attributeValue1", attributeName2: "attributeValue2"]
	}
	
}
