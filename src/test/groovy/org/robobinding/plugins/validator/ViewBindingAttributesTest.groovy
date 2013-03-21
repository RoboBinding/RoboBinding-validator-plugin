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
