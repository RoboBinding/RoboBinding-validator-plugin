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

import static org.mockito.Mockito.*

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner

import spock.lang.Specification;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class FilesWithChangesTest extends Specification {

	String fileWithChangesText = "file_with_changes_text"
	String fileWithoutChangesText = "file_without_changes_text"
	File fileWithChanges, fileWithoutChanges
	FileChangeChecker fileChangeChecker = Mock()
	FilesWithBindingAttributes filesWithBindingAttributes = Mock()
	FilesWithChanges filesWithChanges = new FilesWithChanges(
		fileChangeChecker: fileChangeChecker,
		filesWithBindingAttributes: filesWithBindingAttributes)
	
	def "when evaluating file with changes, then find views with binding attributes"() {
		given:
		def viewsAndAttributes = [new ViewNameAndAttributes(), new ViewNameAndAttributes()]
		filesWithBindingAttributes.findViewsWithBindings(fileWithChangesText) >> viewsAndAttributes
		
		when:
		def result = filesWithChanges.findUpdatedViewsWithBindings(fileWithChanges)
		
		then:
		result == viewsAndAttributes
	}
	
	def "when evaluating a file without changes, then shouldn't find any views with binding attributes "() {
		when:
		def result = filesWithChanges.findUpdatedViewsWithBindings(fileWithoutChanges)
		
		then:
		result.isEmpty()
		0 * filesWithBindingAttributes.findViewsWithBindings(fileWithoutChangesText)
	}
	
	def setup() {
		createTempFolder()
		configureFileWithChanges()
		configureFileWithoutChanges()
	}

	private createTempFolder() {
		new File("test-tmp").mkdir()
	}
	
	private configureFileWithChanges() {
		fileWithChanges = new File("test-tmp/file_with_changes")
		fileWithChanges.createNewFile()
		fileWithChanges.text = fileWithChangesText
		fileChangeChecker.hasFileChangedSinceLastBuild(fileWithChanges) >> true
	}
	
	private configureFileWithoutChanges() {
		fileWithoutChanges = new File("test-tmp/file_without_changes")
		fileWithoutChanges.createNewFile()
		fileWithoutChanges.text = fileWithoutChangesText
		fileChangeChecker.hasFileChangedSinceLastBuild(fileWithoutChanges) >> false
	}
	
	def cleanup() {
		new File("test-tmp").deleteDir()
	}
}
