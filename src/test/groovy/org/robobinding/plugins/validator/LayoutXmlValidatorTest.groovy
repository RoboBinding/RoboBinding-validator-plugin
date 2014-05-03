package org.robobinding.plugins.validator

import spock.lang.Specification


/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class LayoutXmlValidatorTest extends Specification {

	FileChangeChecker fileChangeChecker = Mock()
	FilesWithBindingAttributes filesWithBindingAttributes = Mock()
	BindingAttributesValidator bindingAttributeValidator = Mock()
	File resFolder = new File("test-tmp/res")
	List<File> layoutFiles = []
	Map<File, ViewBindingAttributes> fileToViewBindingsMap = [:]
	LayoutXmlValidator layoutXmlValidator = new LayoutXmlValidator(
		resFolder: resFolder, 
		fileChangeChecker: fileChangeChecker,
		filesWithBindingAttributes: filesWithBindingAttributes,
		bindingAttributeValidator: bindingAttributeValidator)
	
	def "should validate all updated views and bindings"() {
		given:
		layoutFiles.each { layoutFile ->
			filesWithBindingAttributes.findViewsWithBindings(layoutFile) >> viewBindingsFor(layoutFile)
			fileChangeChecker.hasFileChangedSinceLastBuild(layoutFile) >> true
		}
		
		when:
		layoutXmlValidator.validate()
		
		then:
		1 * bindingAttributeValidator.validate(fileToViewBindingsMap)
	}
	
	def "should not validate files without changes"() {
		given:
		layoutFiles.each { layoutFile ->
			filesWithBindingAttributes.findViewsWithBindings(layoutFile) >> viewBindingsFor(layoutFile)
			fileChangeChecker.hasFileChangedSinceLastBuild(layoutFile) >> false
		}
		
		when:
		layoutXmlValidator.validate()
		
		then:
		0 * bindingAttributeValidator.validate(fileToViewBindingsMap)
	}
	
	List<ViewBindingAttributes> viewBindingsFor(File layoutFile) {
		def viewNameAndAttributesList = []
		
		anyNumber().times {
			viewNameAndAttributesList << randomViewNameAndAttributes()
		}
		
		fileToViewBindingsMap[layoutFile] = viewNameAndAttributesList
		viewNameAndAttributesList
	}
	
	def randomViewNameAndAttributes() {
		new ViewBindingAttributes()
	}
	
	def setup() {
		resFolder.mkdirs()
		layoutFiles = []
		
		def layoutFolders = createLayoutFolders()
		createLayoutXmlFiles(layoutFolders)
		createNonXmlFiles(layoutFolders)
	}
	
	def createLayoutFolders() {
		int layoutFoldersCount = anyNumber()
		def layoutFolders = []
		
		layoutFoldersCount.times {
			def layoutFolder = new File(resFolder, "layout${layoutFolders.size()++}")
			layoutFolder.mkdir()
			layoutFolders << layoutFolder
		}
		
		layoutFolders
	}
	
	def createLayoutXmlFiles(def layoutFolders) {
		int xmlFilesCount = anyNumber()
		def xmlFileIndex = 0
		
		layoutFolders.each { layoutFolder ->
			xmlFilesCount.times {
				def layoutFile = new File(layoutFolder, "${xmlFileIndex++}.xml")
				layoutFile.createNewFile()
				layoutFiles << layoutFile
			}
		}
	}
	
	def createNonXmlFiles(def layoutFolders) {
		int nonXmlFileIndex = 0
		
		layoutFolders.each { layoutFolder ->
			anyNumber().times {
				new File(layoutFolder, "${nonXmlFileIndex++}.txt").createNewFile()
			}
		}
	}
	
	def void cleanup() {
		resFolder.deleteDir()
	}
	
	def anyNumber() {
		return new Random().nextInt(10) + 1
	}
}
