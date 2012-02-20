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
import org.robobinding.binder.ViewNameResolver
import org.robobinding.viewattribute.MissingRequiredBindingAttributeException

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class ViewBindingAttributes {

	def viewNameResolver
	def errorReporter
	def xmlFile
	def viewName
	def viewLineNumber
	def attributes
	def attributeLineNumbers
	def bindingAttributeProcessor
	
	def ViewBindingAttributes(errorReporter,xmlFile,viewName,viewLineNumber,attributes,attributeLineNumbers) {
		this.errorReporter = errorReporter
		this.xmlFile = xmlFile
		this.viewName = viewName
		this.viewLineNumber = viewLineNumber
		this.attributes = attributes
		this.attributeLineNumbers = attributeLineNumbers
		this.viewNameResolver = new ViewNameResolver()
	}
	
	def validate() {
		errorReporter.clearErrorsFor(xmlFile)
		
		performViewValidation({ validateView(getFullyQualifiedViewName(viewName), attributes) })
	}
	
	def performViewValidation(Closure viewValidator) {
		try {
			viewValidator.call()
		}
		catch (BindingAttributeException e) {
			e.unrecognizedBindingAttributes.each { attributeName, attributeValue ->
				reportUnrecognizedBindingAttribute(attributeName)
			}
			e.malformedBindingAttributes.each { attributeName, errorMessage ->
				reportMalformedBindingAttribute(attributeName, errorMessage)
			}
		}
		catch (MissingRequiredBindingAttributeException e) {
			reportMissingRequiredAttributes(e.missingAttributes)
		}
	}
	
	def validateView(String fullyQualifiedViewName, attributes) {
		if (!fullyQualifiedViewName.startsWith("android"))
			return

		def view = instanceOf(fullyQualifiedViewName)
		getBindingAttributeProcessor().process(view, attributes)
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
	
	def getBindingAttributeProcessor() {
		if (bindingAttributeProcessor == null)
			bindingAttributeProcessor = new BindingAttributeProcessor(null, true)

		bindingAttributeProcessor
	}
}
