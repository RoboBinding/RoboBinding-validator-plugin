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