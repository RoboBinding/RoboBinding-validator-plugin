package org.robobinding.plugins.validator.mojo

import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.codehaus.mojo.groovy.GroovyMojo
import org.robobinding.binder.BindingAttributeResolver
import org.robobinding.ViewNameResolver
import org.robobinding.plugins.validator.BindingAttributesValidator
import org.robobinding.plugins.validator.ErrorReporter
import org.robobinding.plugins.validator.FileChangeChecker
import org.robobinding.plugins.validator.FilesWithBindingAttributes
import org.robobinding.plugins.validator.LayoutXmlValidator
import org.robobinding.plugins.validator.XmlLineNumberDecorator
import org.robobinding.plugins.validator.XmlWithBindingAttributes
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
	requiresDependencyResolution=ResolutionScope.COMPILE_PLUS_RUNTIME,
	threadSafe=true)
class BindingAttributesValidatorMojo extends GroovyMojo
{
	@Parameter(property='basedir',required=true)
	public File baseFolder

	@Component
	public BuildContext buildContext;

	void execute()
	{
		log.info("Validating binding attributes...")

		ErrorReporter errorReporter = new MojoErrorReporter(buildContext: buildContext)
		BindingAttributesValidator bindingAttributeValidator = createBindingAttributeValidator(errorReporter)
		LayoutXmlValidator layoutXmlValidator = createLayoutXmlValidator(bindingAttributeValidator)
		layoutXmlValidator.validate()

		if (errorReporter.errorsReported)
		   throw new MojoFailureException("Binding attributes validation failed.")

		log.info("Done!")
	}

	private BindingAttributesValidator createBindingAttributeValidator(ErrorReporter errorReporter) {
		BindingAttributeResolver bindingAttributeResolver = new BindingAttributeResolver()
		new BindingAttributesValidator(bindingAttributeResolver: bindingAttributeResolver, errorReporter: errorReporter)
	}

	private LayoutXmlValidator createLayoutXmlValidator(BindingAttributesValidator bindingAttributeValidator) {
		FileChangeChecker fileChangeChecker = new MojoFileChangeChecker(buildContext: buildContext)
		XmlLineNumberDecorator xmlLineNumberDecorator = new XmlLineNumberDecorator()
		ViewNameResolver viewNameResolver = new ViewNameResolver()
		XmlWithBindingAttributes xmlWithBindingAttributes = new XmlWithBindingAttributes(xmlLineNumberDecorator: xmlLineNumberDecorator, viewNameResolver: viewNameResolver)
		FilesWithBindingAttributes filesWithBindingAttributes = new FilesWithBindingAttributes(xmlWithBindingAttributes: xmlWithBindingAttributes)
		new LayoutXmlValidator(resFolder: new File(baseFolder, "res"), fileChangeChecker: fileChangeChecker, filesWithBindingAttributes: filesWithBindingAttributes, bindingAttributeValidator: bindingAttributeValidator)
	}
}
