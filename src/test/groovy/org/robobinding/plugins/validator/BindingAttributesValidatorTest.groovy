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
import org.robobinding.attribute.MalformedAttributeException;
import org.robobinding.attribute.MissingRequiredAttributesException;
import org.robobinding.binder.BindingAttributeResolver
import org.robobinding.binder.ViewResolutionResult

import spock.lang.Specification

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class BindingAttributesValidatorTest extends Specification {

	ErrorReporter errorReporter = Mock()
	BindingAttributeResolver bindingAttributeResolver = Mock()
	BindingAttributesValidator bindingAttributeValidator = new BindingAttributesValidator(
		bindingAttributeResolver: bindingAttributeResolver,
		errorReporter: errorReporter)
	
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
		File xmlFile = Mock()
		int unrecognizedAttributeLineNumber = 10
		viewBindingsForFile[xmlFile] = [new ViewBindingAttributes(bindingAttributes: [attributeName: new BindingAttribute(attributeName: "attributeName", lineNumber: unrecognizedAttributeLineNumber)])]
		UnrecognizedAttributeException unrecognizedAttributeException = new UnrecognizedAttributeException("attributeName")
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> newResolutionResult(unrecognizedAttributeException)
		
		when:
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		1 * errorReporter.errorIn(xmlFile, unrecognizedAttributeLineNumber, unrecognizedAttributeException.getMessage())
	}
	
	def "given a malformed attribute error occurs whilst validating a file, then report error in the file"() {
		given:
		def viewBindingsForFile = [:]
		File xmlFile = Mock()
		int malformedAttributeLineNumber = 20
		viewBindingsForFile[xmlFile] = [new ViewBindingAttributes(bindingAttributes: [attributeName: new BindingAttribute(attributeName: "attributeName", lineNumber: malformedAttributeLineNumber)])]
		MalformedAttributeException malformedAttributeException = new MalformedAttributeException("attributeName", "")
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> newResolutionResult(malformedAttributeException)
		
		when:
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		1 * errorReporter.errorIn(xmlFile, malformedAttributeLineNumber, malformedAttributeException.getMessage())
	}
	
	def "given a missing required binding attributes error occurs whilst validating a file, then report error in the file"() {
		given:
		def viewBindingsForFile = [:]
		File xmlFile = Mock()
		int viewLineNumber = 30
		viewBindingsForFile[xmlFile] = [new ViewBindingAttributes(viewLineNumber: viewLineNumber, bindingAttributes: [attributeName: new BindingAttribute(attributeName: "attributeName")])]
		MissingRequiredAttributesException missingAttributeException = new MissingRequiredAttributesException(["attributeName1", "attributeName2"])
		bindingAttributeResolver.resolve(_ as PendingAttributesForView) >> newResolutionResult(missingAttributeException)
		
		when:
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		1 * errorReporter.errorIn(xmlFile, viewLineNumber, missingAttributeException.getMessage())
	}
	
	def newResolutionResult(AttributeResolutionException... attributeResolutionExceptions) {
		ViewResolutionErrorsException viewResolutionErrors = new ViewResolutionErrorsException(null)
		attributeResolutionExceptions.each {
			viewResolutionErrors.addAttributeError(it)
		}
		new ViewResolutionResult(null, viewResolutionErrors)
	}
	
	def newResolutionResult(MissingRequiredAttributesException... missingRequiredAttributesException) {
		ViewResolutionErrorsException viewResolutionErrors = new ViewResolutionErrorsException(null)
		missingRequiredAttributesException.each {
			viewResolutionErrors.addMissingRequiredAttributeError(it)
		}
		new ViewResolutionResult(null, viewResolutionErrors)
	}
	
	def "given multiple errors occur whilst validating the files, then report errors in files"() {
		
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
