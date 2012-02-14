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

import groovy.util.slurpersupport.NodeChild;

import org.mockito.Mockito

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlLineNumberDecoratorTest extends GroovyTestCase {

	static final String BINDING_PREFIX = "bind"
	
	def xmlDecorator = new XmlLineNumberDecorator()
	
	def void test_whenDecoratingViewTags_thenAppendLineNumber() {

		def xml = '''<?xml version="1.0" encoding="utf-8"?>
					 <TextView android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <EditText
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </EditText>'''

		def expectedXml = '''<?xml version="1.0" encoding="utf-8"?>
					 <TextView line_number="2" android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <EditText line_number="6"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </EditText>'''

		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		assertEquals(expectedXml, decoratedXml)
	}
	
	def void test_whenDecoratingFullyQualifiedViewTags_thenAppendLineNumber() {
		
		def xml = '''<?xml version="1.0" encoding="utf-8"?>
					 <android.widget.TextView android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <android.widget.EditText
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </android.widget.EditText>'''

		def expectedXml = '''<?xml version="1.0" encoding="utf-8"?>
					 <android.widget.TextView line_number="2" android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" />

					 <android.widget.EditText line_number="6"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content" >
					 </android.widget.EditText>'''

		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		assertEquals(expectedXml, decoratedXml)
	}
	
	def void test_whenDecoratingMultipleViewTagsOnSameLine_thenAppendLineNumberToEach() {
		
		def xml = '<?xml version="1.0" encoding="utf-8"?><TextView android:id="@+id/some_id"' + 
						' android:layout_width="fill_parent" android:layout_height="wrap_content" />' + 
						'<EditText android:id="@+id/some_other_id" android:layout_width="fill_parent" ' +
						'android:layout_height="wrap_content" />'
						
		def expectedXml = '<?xml version="1.0" encoding="utf-8"?><TextView line_number="1" android:id="@+id/some_id"' + 
						' android:layout_width="fill_parent" android:layout_height="wrap_content" />' + 
						'<EditText line_number="1" android:id="@+id/some_other_id" android:layout_width="fill_parent" ' +
						'android:layout_height="wrap_content" />'

		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		assertEquals(expectedXml, decoratedXml)
	}

	def void test_whenDecoratingBindingAttributes_thenAppendLineNumber() {

		def xml = '''android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:text="${value}"/>

					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:onTextChange="textChanged" />'''

		def expectedXml = '''android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:text_3="${value}"/>

					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:onTextChange_7="textChanged" />'''

		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		assertEquals(expectedXml, decoratedXml)
	}
	
	def void test_whenDecoratingMultipleBindingAttributesOnSameLine_thenAppendLineNumberToEach() {
		
		def xml = 'android:layout_width="fill_parent" android:layout_height="wrap_content" />' +
					'bind:text="${value}" android:layout_width="fill_parent" ' +
					'android:layout_height="wrap_content" bind:onTextChange="textChanged" />'
						
		def expectedXml = 'android:layout_width="fill_parent" android:layout_height="wrap_content" />' +
					'bind:text_1="${value}" android:layout_width="fill_parent" ' +
					'android:layout_height="wrap_content" bind:onTextChange_1="textChanged" />'

		def decoratedXml = xmlDecorator.embedLineNumbers(xml, BINDING_PREFIX)

		assertEquals(expectedXml, decoratedXml)
	}
	
	def void test_givenDecoratedViewTag_thenReturnViewNameAndLineNumber() {
		Node node = Mockito.mock(Node.class)
		Mockito.when(node.attributes()).thenReturn([line_number:"2"])
		
		def lineNumber = xmlDecorator.getLineNumber(node)
		
		assertEquals(2, lineNumber)
	}
	
	def void test_givenDecoratedBindingAttribute_thenReturnAttributeNameAndLineNumber() {
		
		def attributeValue = "text_12"
		
		def (attributeName, lineNumber) = xmlDecorator.getBindingAttributeDetails(attributeValue)
		
		assertEquals("text", attributeName)
		assertEquals(12, lineNumber)
	}
	
	def void test_givenDecoratedBindingAttributeMap_thenReturnBindingAttributesMapAndLineNumbersMaps() {
		
		def bindingAttributesMap = [text_12:"{text}", enabled_13:"{value}"]
		
		def (actualBindingAttributes, bindingAttributeLineNumbers) = xmlDecorator.getBindingAttributeDetailsMaps(bindingAttributesMap)
		
		assertEquals([text:"{text}", enabled:"{value}"], actualBindingAttributes)
		assertEquals([text:12, enabled:13], bindingAttributeLineNumbers)
	}
}
