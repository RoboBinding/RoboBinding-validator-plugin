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
import org.mockito.Mockito
import org.robobinding.PendingAttributesForView
import org.robobinding.PendingAttributesForViewImpl
import org.robobinding.ViewResolutionErrorsException
import org.robobinding.binder.BindingAttributeResolver
import org.robobinding.binder.ViewNameResolver
import org.robobinding.binder.ViewResolutionResult

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class ViewBindingAttributes {

	ViewNameResolver viewNameResolver = new ViewNameResolver()
	BindingAttributeResolver bindingAttributeResolver = new BindingAttributeResolver();
	ErrorReporter errorReporter
	File xmlFile
	String viewName
	int viewLineNumber
	Map<String, String> attributes
	Map<String, Integer> attributeLineNumbers
	
	def validate() {
		errorReporter.clearErrorsFor(xmlFile)
		
		performViewValidation({ validateView(getFullyQualifiedViewName(viewName), attributes) })
	}
	
	def performViewValidation(Closure viewValidator) {
		try {
			viewValidator.call()
		}
		catch (ViewResolutionErrorsException e) {
			e.attributeErrors.each { attributeResolutionException ->
				reportUnrecognizedBindingAttribute(attributeResolutionException.attributeName)
			}
			e.missingRequiredAttributeErrors.each { missingRequiredAttributesException ->
				reportMissingRequiredAttributes(missingRequiredAttributesException.missingAttributes)
			}
		}
	}
	
	def validateView(String fullyQualifiedViewName, attributes) {
		if (!fullyQualifiedViewName.startsWith("android"))
			return

		def view = instanceOf(fullyQualifiedViewName)
		PendingAttributesForView pendingAttributes = new PendingAttributesForViewImpl(view, attributes)
		ViewResolutionResult result = bindingAttributeResolver.resolve(pendingAttributes)
		result.assertNoErrors()
	}

	def instanceOf(fullyQualifiedViewName) {
		Class viewClass = Class.forName(fullyQualifiedViewName)
		Mockito.mock(viewClass)
	}
	
	def reportUnrecognizedBindingAttribute(attributeName) {
		errorReporter.errorIn(xmlFile, attributeLineNumbers[attributeName], "Unrecognized binding attribute on ${getFullyQualifiedViewName(viewName)}: $attributeName\n\n")
	}
	
	def reportMalformedBindingAttribute(attributeName,errorMessage) {
		errorReporter.errorIn(xmlFile, attributeLineNumbers[attributeName], errorMessage)
	}
	
	def reportMissingRequiredAttributes(missingAttributes) {
		errorReporter.errorIn(xmlFile, viewLineNumber, "Missing required attribute${missingAttributes.size() > 1 ? 's' : ''} on ${getFullyQualifiedViewName(viewName)}: ${missingAttributes.join(', ')}\n\n")
	}
	
	def getFullyQualifiedViewName(viewName) {
		viewNameResolver.getViewNameFromLayoutTag(viewName)
	}
	
//	def getBindingAttributeProcessor() {
//		if (bindingAttributeProcessor == null)
//			bindingAttributeProcessor = new BindingAttributeProcessor(null, true)
//
//		bindingAttributeProcessor
//	}
}
