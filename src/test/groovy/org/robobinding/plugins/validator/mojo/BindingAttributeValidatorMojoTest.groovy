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

import org.robobinding.binder.BindingAttributeProcessor

import android.util.AttributeSet
import android.view.View


/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class BindingAttributeValidatorMojoTest extends GroovyTestCase {

	def void test_givenErrorMessagesList_whenDescribingErrors_theReturnMessageInCorrectFormat() {
		
		def mojo = new BindingAttributeValidatorMojo()
		def errorMessages = ["error1", "error2", "error3"]
		
		def errorMessage = mojo.describe(errorMessages)
		
		assertEquals("error1\n\nerror2\n\nerror3\n\n", errorMessage)
	}
}