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

import static org.mockito.Mockito.*

import org.robobinding.PendingAttributesForView
import org.robobinding.UnrecognizedAttributeException
import org.robobinding.ViewResolutionErrorsException
import org.robobinding.binder.BindingAttributeResolver

import android.util.AttributeSet
import android.view.View

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class ViewBindingAttributesTest extends GroovyTestCase {

	ErrorReporter errorReporter
	File xmlFile = new File("")
	String viewName = "View"
	int viewLineNumber = 5
	Map<String, String> attributes = [text: "value"]
	Map<String, Integer> attributeLineNumbers = [text: 1]
	String attributeName = "text"
	ViewBindingAttributes viewBindingAttributes
	
	def void setUp() {
		errorReporter = mock(ErrorReporter.class)
		viewBindingAttributes = new ViewBindingAttributes(errorReporter: errorReporter, 
			xmlFile: xmlFile, 
			viewName: viewName, 
			viewLineNumber: viewLineNumber, 
			attributes: attributes, 
			attributeLineNumbers: attributeLineNumbers)
	}
	
	def void test_whenValidating_thenClearErrorsForXmlFile() {
		viewBindingAttributes.validate()
		
		verify(errorReporter).clearErrorsFor(xmlFile)
	}
	
	def void test_givenCustomView_whenValidatingView_thenAccept() {
		def view = "org.robobinding.CustomView"
		def attributes = mock(AttributeSet.class)
		
		viewBindingAttributes.validateView(view, attributes)
	}
	
	def void test_givenAndroidViewWithValidAttributes_whenValidatingView_thenAccept() {
		def viewName = View.class.name
		def attributes = [:]
		mockBindingAttributeResolver()
		
		viewBindingAttributes.validateView(viewName, attributes)
	}
	
	def void test_whenPerformingViewValidationAndBindingAttributeExceptionIsThrown_thenReportUnrecognizedBindingAttributes() {
		def attributeName = "text"
		ViewResolutionErrorsException viewResolutionErrorsException = new ViewResolutionErrorsException(mock(View.class))
		viewResolutionErrorsException.addUnrecognizedAttributes(attributeName)
		
		viewBindingAttributes.performViewValidation({ throw viewResolutionErrorsException })
		
		verify(errorReporter).errorIn(xmlFile, attributeLineNumbers[attributeName], "Unrecognized binding attribute on android.view.View: $attributeName\n\n")
	}
	
//	def void test_whenPerformingViewValidationAndBindingAttributeExceptionIsThrown_thenReportMalformedBindingAttributes() {
//		def errorMessage = "{text is malformed"
//		def malformedBindingAttributes = [text: errorMessage]
//		def attributeName = "text"
//		def bindingAttributeException = new BindingAttributeException([:], malformedBindingAttributes, "android.view.View")
//		
//		viewBindingAttributes.performViewValidation({ throw bindingAttributeException })
//
//		verify(errorReporter).errorIn(xmlFile, attributeLineNumbers[attributeName], errorMessage)
//	}
//	
//	def void test_whenPerformingViewValidationAndMissingRequiredBindingAttributeExceptionIsThrown_thenReportMissingAttributes() {
//		def missingAttributes = ["source", "itemLayout"]
//		def missingRequiredBindingAttributeException = new MissingRequiredBindingAttributeException(missingAttributes, "android.view.View")
//		
//		viewBindingAttributes.performViewValidation({ throw missingRequiredBindingAttributeException })
//		
//		verify(errorReporter).errorIn(xmlFile, viewLineNumber, "Missing required attributes on android.view.View: ${missingAttributes.join(', ')}\n\n")
//	}
	
	def mockBindingAttributeResolver() {
		viewBindingAttributes.bindingAttributeResolver = mock(BindingAttributeResolver.class)
	}
	
	def mockFailingBindingAttributeResolver() {
		mockBindingAttributeResolver()
		doThrow(new RuntimeException()).when(viewBindingAttributes.bindingAttributeResolver).resolve(org.mockito.Matchers.any(PendingAttributesForView.class))
	}
}
