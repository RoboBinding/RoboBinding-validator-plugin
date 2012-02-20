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
import org.robobinding.binder.BindingAttributeException
import org.robobinding.binder.BindingAttributeProcessor
import org.robobinding.viewattribute.MissingRequiredBindingAttributeException

import android.util.AttributeSet
import android.view.View

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class ViewBindingAttributesTest extends GroovyTestCase {

	def errorReporter
	def xmlFile = new File("")
	def viewName = "View"
	def viewLineNumber = 5
	def attributes = [text: "value"]
	def attributeLineNumbers = [text: 1]
	def attributeName = "text"
	def viewBindingAttributes
	
	def void setUp() {
		errorReporter = Mockito.mock(ErrorReporter.class)
		viewBindingAttributes = new ViewBindingAttributes(errorReporter, xmlFile, viewName, viewLineNumber, attributes, attributeLineNumbers)
	}
	
	def void test_whenValidating_thenClearErrorsForXmlFile() {
		viewBindingAttributes.validate()
		
		Mockito.verify(errorReporter).clearErrorsFor(xmlFile)
	}
	
	def void test_givenCustomView_whenValidatingView_thenAccept() {
		def view = "org.robobinding.CustomView"
		def attributes = Mockito.mock(AttributeSet.class)
		
		viewBindingAttributes.validateView(view, attributes)
	}
	
	def void test_givenAndroidViewWithValidAttributes_whenValidatingView_thenAccept() {
		def viewName = View.class.name
		def attributes = [:]
		mockBindingAttributeProcessor()
		
		viewBindingAttributes.validateView(viewName, attributes)
	}
	
	def void test_whenPerformingViewValidationAndBindingAttributeExceptionIsThrown_thenReportUnrecognizedBindingAttributes() {
		def unrecognizedBindingAttributes = [text: "{text}"]
		def attributeName = "text"
		def bindingAttributeException = new BindingAttributeException(unrecognizedBindingAttributes, [:], "android.view.View")
		
		viewBindingAttributes.performViewValidation({ throw bindingAttributeException })
		
		Mockito.verify(errorReporter).errorIn(xmlFile, attributeLineNumbers[attributeName], "Unrecognized binding attribute on android.view.View: $attributeName\n\n")
	}
	
	def void test_whenPerformingViewValidationAndBindingAttributeExceptionIsThrown_thenReportMalformedBindingAttributes() {
		def errorMessage = "{text is malformed"
		def malformedBindingAttributes = [text: errorMessage]
		def attributeName = "text"
		def bindingAttributeException = new BindingAttributeException([:], malformedBindingAttributes, "android.view.View")
		
		viewBindingAttributes.performViewValidation({ throw bindingAttributeException })

		Mockito.verify(errorReporter).errorIn(xmlFile, attributeLineNumbers[attributeName], errorMessage)
	}
	
	def void test_whenPerformingViewValidationAndMissingRequiredBindingAttributeExceptionIsThrown_thenReportMissingAttributes() {
		def missingAttributes = ["source", "itemLayout"]
		def missingRequiredBindingAttributeException = new MissingRequiredBindingAttributeException(missingAttributes, "android.view.View")
		
		viewBindingAttributes.performViewValidation({ throw missingRequiredBindingAttributeException })
		
		Mockito.verify(errorReporter).errorIn(xmlFile, viewLineNumber, "Missing required attributes on android.view.View: ${missingAttributes.join(', ')}\n\n")
	}
	
	def mockBindingAttributeProcessor() {
		def bindingAttributeProcessor = Mockito.mock(BindingAttributeProcessor.class)
		viewBindingAttributes.bindingAttributeProcessor = bindingAttributeProcessor
	}
	
	def mockFailingBindingAttributeProcessor() {
		mockBindingAttributeProcessor()
		Mockito.doThrow(new RuntimeException()).when(viewBindingAttributes.bindingAttributeProcessor).process(org.mockito.Matchers.any(View.class), org.mockito.Matchers.any(AttributeSet.class))
	}
}
