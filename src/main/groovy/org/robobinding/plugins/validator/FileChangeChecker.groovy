package org.robobinding.plugins.validator

import java.io.File;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
interface FileChangeChecker {
	
	boolean hasFileChangedSinceLastBuild(File file)
}
