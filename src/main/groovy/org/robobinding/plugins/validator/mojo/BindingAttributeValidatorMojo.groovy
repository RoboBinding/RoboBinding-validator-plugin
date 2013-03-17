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

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.codehaus.mojo.groovy.GroovyMojo
import org.robobinding.plugins.validator.BindingAttributesValidator
import org.robobinding.plugins.validator.ErrorReporter
import org.robobinding.plugins.validator.FileChangeChecker
import org.sonatype.plexus.build.incremental.BuildContext

/**
 *
 * @goal validate-bindings
 * @phase compile
 * @configurator include-project-dependencies
 * 
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
@Mojo(name="validate-bindings", 
	defaultPhase=LifecyclePhase.COMPILE, 
	configurator="include-project-dependencies")
class BindingAttributeValidatorMojo extends GroovyMojo
{
	/**
	 * @parameter expression="${basedir}"
	 * @required
	 */
	@Parameter(property='basedir',required=true)
	public File baseFolder
	
	@Component
	public BuildContext buildContext;
	
	void execute()
	{
		log.info("Validating binding attributes...")
		
		FileChangeChecker fileChangeChecker = new MojoFileChangeChecker(buildContext: buildContext)
		ErrorReporter errorReporter = new MojoErrorReporter(buildContext: buildContext)
		new BindingAttributesValidator(baseFolder, fileChangeChecker, errorReporter).validate()
		
		if (errorReporter.errorMessages)
		   throw new MojoFailureException(describe(errorReporter.errorMessages))
		
		log.info("Done!")
	}
	
	def describe(errorMessages) {
		"${errorMessages.join('\n\n')}\n\n"
	}
}
