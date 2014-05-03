package org.robobinding.plugins.validator.mojo

import org.robobinding.plugins.validator.FileChangeChecker
import org.sonatype.plexus.build.incremental.BuildContext;

import java.io.File;

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class MojoFileChangeChecker implements FileChangeChecker
{
	BuildContext buildContext
	
	boolean hasFileChangedSinceLastBuild(File file) {
		buildContext.hasDelta(file)
	}
}
