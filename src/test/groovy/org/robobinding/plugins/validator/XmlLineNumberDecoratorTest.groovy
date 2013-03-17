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
package org.robobinding.plugins.validator

import org.mockito.Mockito

import spock.lang.Specification

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlLineNumberDecoratorTest extends Specification {

	static final String BINDING_PREFIX = 'bind'
	XmlLineNumberDecorator xmlDecorator = new XmlLineNumberDecorator()
	
	def "should append line number to view tags"() {
		given:
		def xml = '''<?xml version="1.0" encoding="utf-8"?>
					 <TextView android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <EditText
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </EditText>'''

		when:
		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		then:
		decoratedXml == '''<?xml version="1.0" encoding="utf-8"?>
					 <TextView line_number="2" android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <EditText line_number="6"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </EditText>'''
	}
	
	def "should append line number to fully qualified view tags"() {
		given:
		def xml = '''<?xml version="1.0" encoding="utf-8"?>
					 <android.widget.TextView android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <android.widget.EditText
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </android.widget.EditText>'''

		when:
		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		then:
		decoratedXml == '''<?xml version="1.0" encoding="utf-8"?>
					 <android.widget.TextView line_number="2" android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <android.widget.EditText line_number="6"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </android.widget.EditText>'''
	}
	
	def "should append line numbers to every view tag on the same line"() {
		given:
		def xml = '<?xml version="1.0" encoding="utf-8"?><TextView android:id="@+id/some_id"' + 
						' android:layout_width="fill_parent" android:layout_height="wrap_content" />' + 
						'<EditText android:id="@+id/some_other_id" android:layout_width="fill_parent" ' +
						'android:layout_height="wrap_content" />'
						
		when:
		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		then:
		decoratedXml == '<?xml version="1.0" encoding="utf-8"?><TextView line_number="1" android:id="@+id/some_id"' + 
						' android:layout_width="fill_parent" android:layout_height="wrap_content" />' + 
						'<EditText line_number="1" android:id="@+id/some_other_id" android:layout_width="fill_parent" ' +
						'android:layout_height="wrap_content" />'
	}

	def "should append line numbers to binding attributes "() {
		given:
		def xml = '''android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:text="${value}"/>

					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:onTextChange="textChanged" />'''

		when:
		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		then:
		decoratedXml == '''android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:text_3="${value}"/>

					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:onTextChange_7="textChanged" />'''
	}
	
	def "should append line numbers to every binding attribute on the same line"() {
		given:
		def xml = 'android:layout_width="fill_parent" android:layout_height="wrap_content" />' +
					'bind:text="${value}" android:layout_width="fill_parent" ' +
					'android:layout_height="wrap_content" bind:onTextChange="textChanged" />'
						
		when:
		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		then:
		decoratedXml == 'android:layout_width="fill_parent" android:layout_height="wrap_content" />' +
					'bind:text_1="${value}" android:layout_width="fill_parent" ' +
					'android:layout_height="wrap_content" bind:onTextChange_1="textChanged" />'
	}
	
	def "should get line number for decorated view tag"() {
		given:
		Node node = Mock()
		node.attributes() >> [line_number:"2"]
		
		when:
		def lineNumber = xmlDecorator.getLineNumber(node)
		
		then:
		lineNumber == 2
	}
	
	def "should get line number and attribute name for decorated binding attribute"() {
		given:
		def attributeValue = 'text_12'
		
		when:
		def (attributeName, lineNumber) = xmlDecorator.getBindingAttributeDetails(attributeValue)
		
		then:
		attributeName == 'text'
		lineNumber == 12
	}
	
	def "should get line numbers and attribute names from a map of decorated binding attributes"() {
		given:
		def bindingAttributesMap = [text_12:"{text}", enabled_13:"{value}"]
		
		when:
		def (actualBindingAttributes, bindingAttributeLineNumbers) = xmlDecorator.getBindingAttributeDetailsMaps(bindingAttributesMap)
		
		then:
		actualBindingAttributes == [text:"{text}", enabled:"{value}"]
		bindingAttributeLineNumbers == [text:12, enabled:13]
	}
}
