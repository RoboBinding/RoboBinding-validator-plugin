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

package org.robobinding

import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

/**
*
* @since 1.0
* @version $Revision: 1.0 $
* @author Robert Taylor
*/
class GroovyBindingAttributeValidatorMojoTest {

	private static final String TEMP_PATH = "."
	
	def validatorMojo
	def resFolder
	def layoutFoldersCount
	def xmlFilesCount
	
	@Test
	def void whenProcessingEachLayoutFolder_thenInvokeTheClosureOnEachFolder() {
		createLayoutFolders()

		def layoutFoldersProcessed = 0
		validatorMojo.inEachLayoutFolder { folder ->
			
			assertTrue(folder.isDirectory())
			layoutFoldersProcessed++
		}
		
		assertEquals(layoutFoldersCount, layoutFoldersProcessed)
	}
	
	@Test
	def void whenProcessingEachXmlFile_thenInvokeTheClosureOnEachXmlFile() {
		createLayoutXmlFiles()

		def xmlFilesProcessed = 0
		validatorMojo.inEachXmlFile(resFolder) { file ->
			
			assertTrue(file.isFile())
			xmlFilesProcessed++
		}
		
		assertEquals(xmlFilesCount, xmlFilesProcessed)
	}
	
	@Test
	def void givenXmlContainsRoboBindingNamespace_whenCheckingIfNamespaceIsDeclared_thenReturnName() {
		def xmlWithRoboBindingNamespaceDeclaration = 
			'''<?xml version="1.0" encoding="utf-8"?>
				<LinearLayout
					xmlns:android="http://schemas.android.com/apk/res/android"
					xmlns:bind="http://robobinding.org/android"
					android:orientation="horizontal"></LinearLayout>'''
		
		assertNotNull validatorMojo.containsRoboBindingNamespaceDeclaration(xmlWithRoboBindingNamespaceDeclaration)
	}
	
	@Test
	def void givenXmlDoesNotContainRoboBindingNamespace_whenCheckingIfNamespaceIsDeclared_thenReturnNull() {
		def xmlWithoutRoboBindingNamespaceDeclaration =
			'''<?xml version="1.0" encoding="utf-8"?>
				<LinearLayout
					xmlns:android="http://schemas.android.com/apk/res/android"
					android:orientation="horizontal"></LinearLayout>'''
		
		assertNull validatorMojo.containsRoboBindingNamespaceDeclaration(xmlWithoutRoboBindingNamespaceDeclaration)
	}
	
	@Test
	def void givenXmlWithBindingAttributes_whenProcessingEachTag_thenInvokeClosure() {
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
		validatorMojo.forEachViewWithBindingAttributes() {viewName, attributes ->
			viewFound = viewName
			attributesFound = attributes
		}
		
		assertEquals ("EditText", viewFound) 
		assertEquals (["enabled", "text"], attributesFound)
	}
	
	@Test
	def void testGettingAllAttributes() {
		
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
		
		def rootNode = new XmlSlurper().parseText(xml)//.declareNamespace(bind: "http://robobinding.org/android",
			//android: "http://schemas.android.com/apk/res/android")
		
		
		rootNode.children().each {
			
			it.each { a ->
				println a.getClass()
				println a
				
				def xmlClass = a.getClass()
				//def gpathClass = xmlClass.getSuperclass()
				def nodeField = xmlClass.getDeclaredField("node")
				nodeField.setAccessible(true)
				def node = nodeField.get(a)
				def nodeClass = node.getClass();
				def attributeNamespaces = nodeClass.getDeclaredField("attributeNamespaces");
				attributeNamespaces.setAccessible(true)
				println "node: ${attributeNamespaces.get(node)}"
				
				a.each { b ->
					println b.getClass()
					println b
				}
			}
			
			println it.nodeIterator().next().name()
			//println it.getClass()
				//def nodeClass = rootNode.getClass()
				//def node = nodeClass.getDeclaredField("node")
				//println node.get(nodeClass)
				//it.attributes().each{key, value ->
				//	println it."@bind:${key}"
				//}
				//println it.title
//				println key
//				println value
			} 
		
		//println "Nodes ${nodes}"
	}
	
	@Before
	def void setUp() {
		resFolder = new File("${TEMP_PATH}/res")
		resFolder.mkdir()
		
		validatorMojo = new GroovyBindingAttributeValidatorMojo()
		validatorMojo.baseFolder = new File(TEMP_PATH)
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
	
	@After
	def void tearDown() {
		resFolder.deleteDir()
	}
	
	def anyNumber() {
		return new Random().nextInt(10) + 1
	}
}