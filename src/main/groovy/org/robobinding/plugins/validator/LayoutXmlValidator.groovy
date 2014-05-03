package org.robobinding.plugins.validator

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
	FileChangeChecker fileChangeChecker
	FilesWithBindingAttributes filesWithBindingAttributes
	BindingAttributesValidator bindingAttributeValidator
	
	void validate() {
		def fileToViewBindingsMap = [:]

		inEachLayoutFolder { layoutFolder ->
			inEachXmlFile(layoutFolder) { xmlFile ->
				inEachFileWithChanges(xmlFile) { layoutFileWithChanges ->
					fileToViewBindingsMap[xmlFile] = filesWithBindingAttributes.findViewsWithBindings(layoutFileWithChanges)
				}
			}
		}
		
		bindingAttributeValidator.validate(fileToViewBindingsMap)
	}
	
	def inEachLayoutFolder (Closure c) {
		resFolder.eachDirMatch(LAYOUT_FOLDER) { c.call(it) }
	}

	def inEachXmlFile(File file, Closure c) {
		file.eachFileMatch(XML_FILE) { c.call(it) }
	}
	
	def inEachFileWithChanges(File file, Closure c) {
		if (fileChangeChecker.hasFileChangedSinceLastBuild(file)) c.call(file)
	}
}
