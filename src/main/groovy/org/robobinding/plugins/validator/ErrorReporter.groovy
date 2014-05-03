package org.robobinding.plugins.validator

import java.io.File;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
interface ErrorReporter {
	
	void errorIn(File file, int lineNumber, String errorMessage)
	
	void clearErrorsFor(File file)
	
	boolean errorsReported()
	
}
