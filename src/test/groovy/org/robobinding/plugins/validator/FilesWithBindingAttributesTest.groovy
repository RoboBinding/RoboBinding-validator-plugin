package org.robobinding.plugins.validator

import spock.lang.Specification

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class FilesWithBindingAttributesTest extends Specification {

	File xmlFile
	String xml
	XmlWithBindingAttributes xmlWithBindingAttributes = Mock()
	FilesWithBindingAttributes filesWithBindingAttributes = new FilesWithBindingAttributes(
			xmlWithBindingAttributes: xmlWithBindingAttributes)

	def "given xml with the RoboBinding namespace declaration, then search for views with bindings using binding prefix"() {
		given:
		xmlFileWith(
		'''<?xml version="1.0" encoding="utf-8"?>
		      <LinearLayout
		         xmlns:android="http://schemas.android.com/apk/res/android"
		         xmlns:bind="http://robobinding.org/android"
		         android:orientation="horizontal"/>''')
		def viewsAndAttributes = [new ViewBindingAttributes(), new ViewBindingAttributes()]
		xmlWithBindingAttributes.findViewsWithBindings(xml, 'bind') >> viewsAndAttributes
		
		when:
		def result = filesWithBindingAttributes.findViewsWithBindings(xmlFile)
		
		then:
		result == viewsAndAttributes
	}

	def "given xml without the RoboBinding namespace declaration, then shouldn't find any views with bindings"() {
		given:
		xmlFileWith(
		'''<?xml version="1.0" encoding="utf-8"?>
		      <LinearLayout
		         xmlns:android="http://schemas.android.com/apk/res/android"
		         android:orientation="horizontal"/>''')
		
		when:
		def result = filesWithBindingAttributes.findViewsWithBindings(xmlFile)
		
		then:
		result.isEmpty()
	}
	
	def setup() {
		createTempFolder()
	}

	private createTempFolder() {
		new File("test-tmp").mkdir()
	}
	
	private xmlFileWith(String xml) {
		this.xml = xml
		xmlFile = new File("test-tmp/file_with_changes")
		xmlFile.createNewFile()
		xmlFile.text = xml
	}
	
	def cleanup() {
		new File("test-tmp").deleteDir()
	}
}
