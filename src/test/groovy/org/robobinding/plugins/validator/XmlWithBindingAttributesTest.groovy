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
	
	def "given xml with binding attributes then the view name and line numbers"() {
		given:
		String xml
		String xmlWithLineNumbers = '''<?xml version="1.0" encoding="utf-8"?>
					 <LinearLayout
			         xmlns:android="http://schemas.android.com/apk/res/android"
			         xmlns:bind="http://robobinding.org/android"
			         android:orientation="horizontal">
					 <TextView line_number="5" android:id="@+id/some_id"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:text_8="{name}"/>

					 <EditText line_number="10"
					 android:layout_width="fill_parent"
					 android:layout_height="wrap_content"
					 bind:text_13="{age}" >
					 </EditText>
					 </LinearLayout>'''
		xmlLineNumberDecorator.embedLineNumbers(xml, 'bind') >> xmlWithLineNumbers
		
		when:
		def result = xmlWithBindingAttributes.findViewsWithBindings(xml, 'bind')
		
		then:
		result.size() == 2
	}
}
