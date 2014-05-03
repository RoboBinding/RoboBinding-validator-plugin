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
class MojoErrorReporterTest extends Specification {

	BuildContext buildContext = Mock(BuildContext.class)
	File file = new File("a_file.xml")
	MojoErrorReporter mojoErrorReporter = new MojoErrorReporter(buildContext: buildContext)
	def lineNumber = 13
	def errorMessage = "Error!"
	
	def "when reporting an error then delegate to build context"() {
		when:
		mojoErrorReporter.errorIn(file, lineNumber, errorMessage)
		
		then:
		1 * buildContext.addMessage(file, lineNumber, 0, "$errorMessage", BuildContext.SEVERITY_ERROR, null)
	}
	
	def "when clearing errors then delegate to build context"() {
		when:
		mojoErrorReporter.clearErrorsFor(file)
		
		then:
		1 * buildContext.removeMessages(file)
	}
	
	def "after reporting an error, then errors reported should be true"() {
		assert !mojoErrorReporter.errorsReported()
		
		when:
		mojoErrorReporter.errorIn(file, lineNumber, errorMessage)
		
		then:
		mojoErrorReporter.errorsReported()
	}
}