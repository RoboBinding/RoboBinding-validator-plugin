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


/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class MojoErrorReporterTest extends GroovyTestCase {

	def buildContext
	def file
	def mojoErrorReporter
	
	def void setUp() {
		buildContext = Mockito.mock(BuildContext.class)
		file = new File("")
		mojoErrorReporter = new MojoErrorReporter(buildContext: buildContext)
	}
	
	def void test_whenReportingAnError_thenDelegateToBuildContext() {
		
		def lineNumber = 13
		def errorMessage = "Error!"
		
		mojoErrorReporter.errorIn(file, lineNumber, errorMessage)
		
		Mockito.verify(buildContext).addMessage(file, lineNumber, 0, errorMessage, BuildContext.SEVERITY_ERROR, null)
	}
	
	def void test_whenClearingErrors_thenDelegateToBuildContext() {
		
		mojoErrorReporter.clearErrorsFor(file)
		
		Mockito.verify(buildContext).removeMessages(file)
	}
}