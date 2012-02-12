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

import org.sonatype.plexus.build.incremental.BuildContext;



/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class MojoErrorReporter
{
	def buildContext
	
	def errorIn(file,lineNumber,errorMessage) {
		buildContext.addMessage(file, lineNumber, 0, errorMessage, BuildContext.SEVERITY_ERROR, null)
	}
	
	def clearErrorsFor(file) {
		buildContext.removeMessages(file)
	}
}
