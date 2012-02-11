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
package org.robobinding.validator

import org.mockito.Mockito
import org.robobinding.binder.BindingAttributeProcessor

import android.util.AttributeSet
import android.view.View


/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class BindingAttributeValidatorTest extends GroovyTestCase {

	private static final String TEMP_PATH = "."
	
	def validator
	def resFolder
	def layoutFoldersCount
	def xmlFilesCount
	
	def void test_whenProcessingEachLayoutFolder_thenInvokeTheClosureOnEachFolder() {
		createLayoutFolders()

		def layoutFoldersProcessed = 0
		validator.inEachLayoutFolder { folder ->
			
			assertTrue(folder.isDirectory())
			layoutFoldersProcessed++
		}
		
		assertEquals(layoutFoldersCount, layoutFoldersProcessed)
	}
	
	def void test_whenProcessingEachXmlFile_thenInvokeTheClosureOnEachXmlFile() {
		createLayoutXmlFiles()

		def xmlFilesProcessed = 0
		validator.inEachXmlFile(resFolder) { file ->
			
			assertTrue(file.isFile())
			xmlFilesProcessed++
		}
		
		assertEquals(xmlFilesCount, xmlFilesProcessed)
	}
	
	def void test_givenXmlContainsRoboBindingNamespace_whenCheckingIfNamespaceIsDeclared_thenReturnName() {
		def xmlWithRoboBindingNamespaceDeclaration = 
			'''<?xml version="1.0" encoding="utf-8"?>
				<LinearLayout
					xmlns:android="http://schemas.android.com/apk/res/android"
					xmlns:bind="http://robobinding.org/android"
					android:orientation="horizontal"></LinearLayout>'''
		
		assertNotNull validator.getRoboBindingNamespaceDeclaration(xmlWithRoboBindingNamespaceDeclaration)
	}
	
	def void test_givenXmlDoesNotContainRoboBindingNamespace_whenCheckingIfNamespaceIsDeclared_thenReturnNull() {
		def xmlWithoutRoboBindingNamespaceDeclaration =
			'''<?xml version="1.0" encoding="utf-8"?>
				<LinearLayout
					xmlns:android="http://schemas.android.com/apk/res/android"
					android:orientation="horizontal"></LinearLayout>'''
		
		assertNull validator.getRoboBindingNamespaceDeclaration(xmlWithoutRoboBindingNamespaceDeclaration)
	}
	
	def void test_givenXmlWithBindingAttributes_whenProcessingEachTag_thenInvokeClosure() {
		def xml = '''<?xml version="1.0" encoding="utf-8"?>
			<LinearLayout
				xmlns:android="http://schemas.android.com/apk/res/android"
				xmlns:bind="http://robobinding.org/android"
				android:orientation="horizontal">
				<EditText
					android:layout_width="fill_parent"
					android:layout_height="wrap_content"
					bind:enabled="{firstnameInputEnabled}"
					bind:text="${firstname}" />
			</LinearLayout>'''
		
		def viewFound, attributesFound
		validator.forEachViewWithBindingAttributesInThe(xml) {viewName, attributes ->
			viewFound = viewName
			attributesFound = attributes
		}
		
		assertEquals ("EditText", viewFound) 
		assertEquals ([enabled: '{firstnameInputEnabled}', text: '${firstname}'], attributesFound)
	}
	
	def void test_givenXmlWithNestedBindingAttributes_whenProcessingEachTag_thenInvokeClosure() {
		def xml = '''<?xml version="1.0" encoding="utf-8"?>
			<LinearLayout
				xmlns:android="http://schemas.android.com/apk/res/android"
				xmlns:bind="http://robobinding.org/android"
				android:orientation="horizontal">
				<RadioGroup
					android:layout_width="fill_parent"
					android:layout_height="wrap_content"
					bind:enabled="{enabled}" >
					
					<RadioButton
						android:layout_width="fill_parent"
						android:layout_height="wrap_content"
						bind:visibility="{visible}" />

				</RadioGroup>
			</LinearLayout>'''
		
		def viewsFound = []
		def attributesFound = [:]
		validator.forEachViewWithBindingAttributesInThe(xml) {viewName, attributes ->
			viewsFound << viewName
			attributesFound[viewName] = attributes
		}
		
		assertEquals (["RadioGroup", "RadioButton"], viewsFound)
		assertEquals ([RadioGroup: [enabled:"{enabled}"], RadioButton: [visibility:"{visible}"]], attributesFound)
	}
	
	def void test_givenCustomView_whenValidatingView_thenAccept() {
		def view = "org.robobinding.CustomView"
		def attributes = Mockito.mock(AttributeSet.class)
		
		def errorMessage = validator.validateView(view, attributes)
		
		assertNull(errorMessage)
	}
	
	def void test_givenAndroidViewWithValidAttributes_whenValidatingView_thenReturnEmptyErrorMessage() {
		def view = Mockito.mock(View.class)
		def attributes = [:]
		mockBindingAttributeProcessor()
		
		def errorMessage = validator.validateView(view, attributes)
		
		assertTrue(errorMessage.size() == 0)
	}
	
	def void test_givenAndroidViewWithInvalidAttributes_whenValidatingView_thenReturnErrorMessage() {
		def view = Mockito.mock(View.class)
		def attributes = [:]
		mockFailingBindingAttributeProcessor()
		
		def errorMessage = validator.validateView(view, attributes)
		
		assertTrue(errorMessage.size() > 0)
	}
	
	def mockBindingAttributeProcessor() {
		def bindingAttributeProcessor = Mockito.mock(BindingAttributeProcessor.class)
		validator.bindingAttributeProcessor = bindingAttributeProcessor
	}
	
	def mockFailingBindingAttributeProcessor() {
		mockBindingAttributeProcessor()
		Mockito.doThrow(new RuntimeException()).when(validator.bindingAttributeProcessor).process(org.mockito.Matchers.any(View.class), org.mockito.Matchers.any(AttributeSet.class))
	}
	
	def void setUp() {
		resFolder = new File("${TEMP_PATH}/res")
		resFolder.mkdir()
		
		validator = new BindingAttributeValidator(new File(TEMP_PATH))
	}
	
	def createLayoutFolders() {
		layoutFoldersCount = anyNumber()
		
		def layoutFolderIndex = 0
		layoutFoldersCount.times {
			new File(resFolder, "layout${layoutFolderIndex++}").mkdir()
		}
	}
	
	def createLayoutXmlFiles() {
		xmlFilesCount = anyNumber()
		def xmlFileIndex = 0
		xmlFilesCount.times {
			new File(resFolder, "${xmlFileIndex++}.xml").createNewFile()
		}
		
		anyNumber().times {
			new File(resFolder, "${xmlFileIndex++}.txt").createNewFile()
		}
	}
	
	def void tearDown() {
		resFolder.deleteDir()
	}
	
	def anyNumber() {
		return new Random().nextInt(10) + 1
	}
}