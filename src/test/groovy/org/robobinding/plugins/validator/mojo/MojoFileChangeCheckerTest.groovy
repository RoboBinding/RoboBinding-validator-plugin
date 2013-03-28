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