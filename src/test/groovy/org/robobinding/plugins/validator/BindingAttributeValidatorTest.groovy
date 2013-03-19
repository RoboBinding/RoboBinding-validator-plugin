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
class BindingAttributeValidatorTest extends Specification {

	ErrorReporter errorReporter = Mock()
	BindingAttributeValidator bindingAttributeValidator = new BindingAttributeValidator(
		errorReporter: errorReporter)
	
	def "when validating, first clear errors in files"() {
		given: 
		def viewBindingsForFile = getViewBindingsForFile()
		
		when: 
		bindingAttributeValidator.validate(viewBindingsForFile)
		
		then:
		viewBindingsForFile.each { file, viewNameAndAttributes ->
			1 * errorReporter.clearErrorsFor(file)
		}
	}
	
	def "given an error occurs whilst validating the file, then report error in file"() {
		
	}
	
	def "given multiple errors occur whilst validating the files, then report errors in files"() {
		
	}
	
	def getViewBindingsForFile() {
		def viewBindingsForFile = [:]
		10.times {
			def file = Mock(File.class)
			viewBindingsForFile[file] = new ViewNameAndAttributes()
		}
		viewBindingsForFile
	}
	
}
