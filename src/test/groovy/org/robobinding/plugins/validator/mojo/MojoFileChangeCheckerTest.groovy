package org.robobinding.plugins.validator.mojo

import org.mockito.Mockito
import org.sonatype.plexus.build.incremental.BuildContext

import spock.lang.Specification


/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class MojoFileChangeCheckerTest extends Specification {

	BuildContext buildContext = Mock(BuildContext.class)
	MojoFileChangeChecker mojoFileChangeChecker = new MojoFileChangeChecker(buildContext: buildContext)
	
	def "when checking if file has changed then delegate to BuildContext"() {
		given:
		def aFile = new File("")
		def fileHasChanged = trueOrFalse()
		buildContext.hasDelta(aFile) >> fileHasChanged
		
		when:
		def result = mojoFileChangeChecker.hasFileChangedSinceLastBuild(aFile)
		
		then:
		fileHasChanged == result
	}
	
	def trueOrFalse() {
		new Random().nextInt(2) == 0 ? true : false
	}
}