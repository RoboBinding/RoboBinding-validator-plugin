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

import spock.lang.Specification

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlWithBindingAttributesTest extends Specification {

	XmlLineNumberDecorator xmlLineNumberDecorator = Mock()
	XmlWithBindingAttributes xmlWithBindingAttributes = new XmlWithBindingAttributes(
		xmlLineNumberDecorator: xmlLineNumberDecorator)
	
	def "given xml with binding attributes then determine view name and line numbers"() {
		given:
		String rawXml = "<raw-xml/>"
		String xmlWithLineNumbers = 
				  '''<?xml version="1.0" encoding="utf-8"?>
					 <LinearLayout
			            xmlns:android="http://schemas.android.com/apk/res/android"
			            xmlns:bind="http://robobinding.org/android"
			            android:orientation="horizontal">
					    
					    <TextView line_number="6" android:id="@+id/some_id"
					       android:layout_width="fill_parent"
					       android:layout_height="wrap_content"
					       bind:text_9="{name}"/>

						<RelativeLayout line_number="11"
						   android:layout_width="fill_parent"
					       android:layout_height="wrap_content">
					       
						   <EditText line_number="15"
					          android:layout_width="fill_parent"
					          android:layout_height="wrap_content"
					          bind:visibility_18="{nameVisible}" >
					       </EditText>

						 </RelativeLayout>
					 </LinearLayout>'''
		xmlLineNumberDecorator.embedLineNumbers(rawXml, 'bind') >> xmlWithLineNumbers
		
		when:
		List<ViewNameAndAttributes> results = xmlWithBindingAttributes.findViewsWithBindings(rawXml, 'bind')
		
		then:
		results.size() == 2
		results[0].viewName.value == 'TextView'
		results[0].viewName.lineNumber == 6
		results[0].bindingAttributes[0].attributeName == 'text'
		results[0].bindingAttributes[0].attributeValue == '{name}'
		results[0].bindingAttributes[0].lineNumber == 9
		results[1].viewName.value == 'EditText'
		results[1].viewName.lineNumber == 15
		results[1].bindingAttributes[0].attributeName == 'visibility'
		results[1].bindingAttributes[0].attributeValue == '{nameVisible}'
		results[1].bindingAttributes[0].lineNumber == 18
	}
}
