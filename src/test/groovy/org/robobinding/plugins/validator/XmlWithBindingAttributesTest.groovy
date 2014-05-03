package org.robobinding.plugins.validator

import org.robobinding.ViewNameResolver

import spock.lang.Specification
import android.widget.EditText
import android.widget.TextView

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlWithBindingAttributesTest extends Specification {

	XmlLineNumberDecorator xmlLineNumberDecorator = Mock()
	XmlWithBindingAttributes xmlWithBindingAttributes = new XmlWithBindingAttributes(
		xmlLineNumberDecorator: xmlLineNumberDecorator,
		viewNameResolver: new ViewNameResolver())

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
		List<ViewBindingAttributes> results = xmlWithBindingAttributes.findViewsWithBindings(rawXml, 'bind')

		then:
		results.size() == 2
		results[0].view instanceof TextView
		results[0].viewLineNumber == 6
		results[0].bindingAttributes['text'].attributeName == 'text'
		results[0].bindingAttributes['text'].attributeValue == '{name}'
		results[0].bindingAttributes['text'].lineNumber == 9
		results[1].view instanceof EditText
		results[1].viewLineNumber == 15
		results[1].bindingAttributes['visibility'].attributeName == 'visibility'
		results[1].bindingAttributes['visibility'].attributeValue == '{nameVisible}'
		results[1].bindingAttributes['visibility'].lineNumber == 18
	}

	def "should not [yet] process custom views"() {
		given:
		String rawXml = "<raw-xml/>"
		String xmlWithLineNumbers =
				  '''<?xml version="1.0" encoding="utf-8"?>
					 <LinearLayout
			            xmlns:android="http://schemas.android.com/apk/res/android"
			            xmlns:bind="http://robobinding.org/android"
			            android:orientation="horizontal">

					    <org.custom.TextView line_number="6" android:id="@+id/some_id"
					       android:layout_width="fill_parent"
					       android:layout_height="wrap_content"
					       bind:text_9="{name}"/>

						<RelativeLayout line_number="11"
						   android:layout_width="fill_parent"
					       android:layout_height="wrap_content">

						   <org.custom.EditText line_number="15"
					          android:layout_width="fill_parent"
					          android:layout_height="wrap_content"
					          bind:visibility_18="{nameVisible}" >
					       </org.custom.EditText>

						 </RelativeLayout>
					 </LinearLayout>'''
		xmlLineNumberDecorator.embedLineNumbers(rawXml, 'bind') >> xmlWithLineNumbers

		when:
		List<ViewBindingAttributes> results = xmlWithBindingAttributes.findViewsWithBindings(rawXml, 'bind')

		then:
		results.isEmpty()
	}

	def "should process android views inside custom containers"() {
		given:
		String rawXml = "<raw-xml/>"
		String xmlWithLineNumbers =
				  '''<?xml version="1.0" encoding="utf-8"?>
					 <org.custom.LinearLayout
			            xmlns:android="http://schemas.android.com/apk/res/android"
			            xmlns:bind="http://robobinding.org/android"
			            android:orientation="horizontal">

					    <TextView line_number="6" android:id="@+id/some_id"
					       android:layout_width="fill_parent"
					       android:layout_height="wrap_content"
					       bind:text_9="{name}"/>

						<org.custom.RelativeLayout line_number="11"
						   android:layout_width="fill_parent"
					       android:layout_height="wrap_content">

						   <EditText line_number="15"
					          android:layout_width="fill_parent"
					          android:layout_height="wrap_content"
					          bind:visibility_18="{nameVisible}" >
					       </EditText>

						 </org.custom.RelativeLayout>
					 </org.custom.LinearLayout>'''
		xmlLineNumberDecorator.embedLineNumbers(rawXml, 'bind') >> xmlWithLineNumbers

		when:
		List<ViewBindingAttributes> results = xmlWithBindingAttributes.findViewsWithBindings(rawXml, 'bind')

		then:
		results.size() == 2
	}
}
