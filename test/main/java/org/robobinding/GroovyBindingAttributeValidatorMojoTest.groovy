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
	def void givenXmlContainsRoboBindingNamespace_whenCheckingIfNamespaceIsDeclared_thenReturnTrue() {
		def xmlWithRoboBindingNamespaceDeclaration = 
			'''<?xml version="1.0" encoding="utf-8"?>
				<LinearLayout
					xmlns:android="http://schemas.android.com/apk/res/android"
					xmlns:bind="http://robobinding.org/android"
					android:orientation="horizontal"></LinearLayout>'''
		
		assertTrue validatorMojo.containsRoboBindingNamespaceDeclaration(xmlWithRoboBindingNamespaceDeclaration)
	}
	
	@Test
	def void givenXmlDoesNotContainRoboBindingNamespace_whenCheckingIfNamespaceIsDeclared_thenReturnFalse() {
		def xmlWithoutRoboBindingNamespaceDeclaration =
			'''<?xml version="1.0" encoding="utf-8"?>
				<LinearLayout
					xmlns:android="http://schemas.android.com/apk/res/android"
					android:orientation="horizontal"></LinearLayout>'''
		
		assertFalse validatorMojo.containsRoboBindingNamespaceDeclaration(xmlWithoutRoboBindingNamespaceDeclaration)
	}
	
	@Test
	def void testXmlSlurperNamespaceQuerying() {	
		def xml = '''<?xml version="1.0" encoding="utf-8"?>
					<LinearLayout
						xmlns:android="http://schemas.android.com/apk/res/android"
						xmlns:bind="http://robobinding.org/android"
						android:orientation="horizontal">

		  <nons>test</nons>
		  
					</LinearLayout>'''
		
		def result = new XmlSlurper().parseText(xml)
		def result2 = new XmlSlurper().parseText(xml)
		
		println result.'**'.each {println it.namespaceURI()}
		
		println "Tag hints: ${result.@namespaceTagHints}"
		def xmlClass = result.getClass()
		def gpathClass = xmlClass.getSuperclass()
		def namespaceTagHints = gpathClass.getDeclaredField("namespaceTagHints")
		namespaceTagHints.setAccessible(true)
		println namespaceTagHints.get(result)
		println namespaceTagHints.get(result2)
		
		println "Tag hints: ${result.@namespaceTagHints}"
		
		def s = '''<?xml version="1.0" encoding="utf-8"?>
		<Root 
			xmlns:a="http://a.example" 
			xmlns:b="http://b.example" 
			xmlns:c="http://c.example" 
			xmlns:d="http://d.example"
		a:orientation="crap">
		  <a:name>Test A</a:name>
		  <b:name>Test B</b:name>
		  <b:stuff>
			  <c:foo>bar</c:foo>
			  <c:baz>
				  <d:foo2/>
			  </c:baz>
		  </b:stuff>
		  <nons>test</nons>
		  <c:test/>
		</Root>'''
		
		def xmls = new XmlSlurper().parseText(xml)
		
		def namespaceList = xmls.'**'.collect { it.namespaceURI() }.unique()
		
		println namespaceList
		
//		println result.lookupNamespace("android")
//		println result.lookupNamespace("bind")
//		println result.namespaceTagHints
//		println result.namespacePrefix
//		println result.namespaceMap
		
//		def namespaceList = result.'**'.collect { it.namespaceURI() }.unique()
//		
//		println namespaceList
		
//		def bindingNamespace = result.'**'.find { it.namespaceURI().equals('http://robobinding.org/android') }
//		
//		println bindingNamespace
		
		//println "${result} ${result.name} namespaces: ${result.namespaceMap} ${result.size()} ${result[0]} ${result.'@android:orientation'}"
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