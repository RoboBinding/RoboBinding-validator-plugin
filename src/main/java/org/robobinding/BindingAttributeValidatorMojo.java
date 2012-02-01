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
package org.robobinding;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
public class BindingAttributeValidatorMojo extends AbstractMojo
{
	public void execute() throws MojoExecutionException, MojoFailureException
	{
		getLog().info("Validating binding attributes...");

		//1. Locate layout folder
		
		//2. Iterate through each file
		
		//3. Parse the xml into data structure
		
		//4. Does file use the robobinding namespace?
		
		//5. For each binding attribute declared, check the corresponding view agains tthe candidate providers (BindingAttributeProviderResolver.getCandidateProviders()
		
		//6. If attribute is not resolved, add it to the list
	}
}
