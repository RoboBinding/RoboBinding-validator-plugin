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
}
