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

import groovy.lang.Closure;
import groovy.transform.Immutable;

import java.io.File;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class LayoutXmlValidator {

	static final def LAYOUT_FOLDER = ~/[layout].*/
	static final def XML_FILE = ~/.*[.xml]/
	File resFolder
	FilesWithChanges filesWithChanges
	BindingAttributeValidator bindingAttributeValidator
	
	void validate() {
		def fileToViewBindingsMap = [:]
		
		inEachLayoutFolder { layoutFolder ->
			inEachXmlFile(layoutFolder) { xmlFile ->
				fileToViewBindingsMap[xmlFile] = filesWithChanges.findUpdatedViewsWithBindings(xmlFile)
			}
		}
		
		bindingAttributeValidator.validate(fileToViewBindingsMap)
	}
	
	def inEachLayoutFolder (Closure c) {
		resFolder.eachDirMatch(LAYOUT_FOLDER) { c.call(it) }
	}

	def inEachXmlFile(File folder, Closure c) {
		folder.eachFileMatch(XML_FILE) { c.call(it) }
	}
}
