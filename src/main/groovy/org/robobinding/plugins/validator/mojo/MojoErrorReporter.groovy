package org.robobinding.plugins.validator.mojo

import java.io.File;

import org.robobinding.plugins.validator.ErrorReporter
import org.sonatype.plexus.build.incremental.BuildContext



/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class MojoErrorReporter implements ErrorReporter
{
	BuildContext buildContext
	boolean errorsReported
	
	void errorIn(File file, int lineNumber, String errorMessage) {
		buildContext.addMessage(file, lineNumber, 0, "$errorMessage", BuildContext.SEVERITY_ERROR, null)
		errorsReported = true
	}
	
	void clearErrorsFor(File file) {
		buildContext.removeMessages(file)
	}
	
	boolean errorsReported() {
		errorsReported
	}
}
