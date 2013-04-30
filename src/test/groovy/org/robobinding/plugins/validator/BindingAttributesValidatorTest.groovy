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

import org.robobinding.AttributeResolutionException
import org.robobinding.PendingAttributesForView
import org.robobinding.UnrecognizedAttributeException
import org.robobinding.ViewResolutionErrorsException
import org.robobinding.attribute.MalformedAttributeException
import org.robobinding.attribute.MissingRequiredAttributesException
import org.robobinding.binder.BindingAttributeResolver
import org.robobinding.binder.ViewBindingErrors
import org.robobinding.binder.ViewResolutionResult
import org.robobinding.viewattribute.AttributeBindingException

import spock.lang.Specification
import android.view.View
import android.widget.ListView
import android.widget.TextView

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class BindingAttributesValidatorTest extends Specification {

	ErrorReporter errorReporter = Mock()
	TextView textView = Mock()
	ListView listView = Mock()
	File xmlFile = Mock()
	BindingAttributeResolver bindingAttributeResolver = Mock()
	BindingAttributeBinder bindingAttributeBinder = Mock()
	ViewBindingErrors viewBindingErrors = new ViewBindingErrors(null)
	BindingAttributesValidator bindingAttributeValidator = new BindingAttributesValidator(
		bindingAttributeResolver: bindingAttributeResolver,
		bindingAttributeBinder: bindingAttributeBinder,
		errorReporter: errorReporter)
	
	def setup() {
		bindingAttributeBinder.bind(_,_) >> viewBindingErrors
	}
	
	def "when validating, first clear errors in files"() {
		given:
		def viewBindingsForFile = getViewBindingsForFile()
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> newResolutionResult()
		
		when: 
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		viewBindingsForFile.each { file, viewNameAndAttributes ->
			1 * errorReporter.clearErrorsFor(file)
		}
	}
	
	def "given an unrecognized attribute error occurs whilst validating a file, then report error in the file"() {
		given:
		def viewBindingsForFile = [:]
		int unrecognizedAttributeLineNumber = 10
		viewBindingsForFile[xmlFile] = [new ViewBindingAttributes(bindingAttributes: [text: new BindingAttribute(attributeName: "text", lineNumber: unrecognizedAttributeLineNumber)])]
		UnrecognizedAttributeException unrecognizedAttributeException = new UnrecognizedAttributeException("text")
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> newResolutionResult(textView, unrecognizedAttributeException)
		
		when:
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		1 * errorReporter.errorIn(xmlFile, unrecognizedAttributeLineNumber, "$unrecognizedAttributeException.message for TextView")
	}
	
	def "given a malformed attribute error occurs whilst validating a file, then report error in the file"() {
		given:
		def viewBindingsForFile = [:]
		int malformedAttributeLineNumber = 20
		viewBindingsForFile[xmlFile] = [new ViewBindingAttributes(bindingAttributes: [text: new BindingAttribute(attributeName: "text", lineNumber: malformedAttributeLineNumber)])]
		MalformedAttributeException malformedAttributeException = new MalformedAttributeException("text", "")
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> newResolutionResult(textView, malformedAttributeException)
		
		when:
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		1 * errorReporter.errorIn(xmlFile, malformedAttributeLineNumber, "$malformedAttributeException.message")
	}
	
	def "given a missing required binding attributes error occurs whilst validating a file, then report error in the file"() {
		given:
		def viewBindingsForFile = [:]
		int viewLineNumber = 30
		viewBindingsForFile[xmlFile] = [new ViewBindingAttributes(viewLineNumber: viewLineNumber, bindingAttributes: [attributeName: new BindingAttribute(attributeName: "text")])]
		MissingRequiredAttributesException missingAttributeException = new MissingRequiredAttributesException(["attributeName1", "attributeName2"])
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> newResolutionResult(listView, missingAttributeException)
		
		when:
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		1 * errorReporter.errorIn(xmlFile, viewLineNumber, "$missingAttributeException.message for ListView")
	}
	
	def "given an attribute binding exceptiosn occurs whilst validating a file, then report error"() {
		given:
		def viewBindingsForFile = [:]
		def attributeWithBindingErrorLineNumber = 20
		viewBindingsForFile[xmlFile] = [new ViewBindingAttributes(bindingAttributes: [text: new BindingAttribute(attributeName: "text", lineNumber: attributeWithBindingErrorLineNumber)])]
		ViewResolutionResult viewResolutionResult = newResolutionResult()
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> viewResolutionResult
		AttributeBindingException bindingException = Mock()
		bindingException.attribute >> "text"
		bindingException.toString() >> "errorMessage"
		viewBindingErrors.addAttributeError(bindingException)
		bindingAttributeBinder.bind(viewResolutionResult, xmlFile) >> viewBindingErrors
		
		when:
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		1 * errorReporter.errorIn(xmlFile, attributeWithBindingErrorLineNumber, "errorMessage")
	}
	
	def newResolutionResult() {
		ViewResolutionErrorsException viewResolutionErrors = new ViewResolutionErrorsException(null)
		new ViewResolutionResult(null, viewResolutionErrors)
	}
	
	def newResolutionResult(View view, AttributeResolutionException... attributeResolutionExceptions) {
		ViewResolutionErrorsException viewResolutionErrors = new ViewResolutionErrorsException(view)
		attributeResolutionExceptions.each {
			viewResolutionErrors.addAttributeError(it)
		}
		new ViewResolutionResult(null, viewResolutionErrors)
	}
	
	def newResolutionResult(View view, MissingRequiredAttributesException... missingRequiredAttributesException) {
		ViewResolutionErrorsException viewResolutionErrors = new ViewResolutionErrorsException(view)
		missingRequiredAttributesException.each {
			viewResolutionErrors.addMissingRequiredAttributeError(it)
		}
		new ViewResolutionResult(null, viewResolutionErrors)
	}
	
	def getViewBindingsForFile() {
		def viewBindingsForFile = [:]
		10.times {
			def file = Mock(File.class)
			viewBindingsForFile[file] = [new ViewBindingAttributes()]
		}
		viewBindingsForFile
	}
}
