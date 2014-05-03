package org.robobinding.plugins.validator

import org.robobinding.AttributeResolutionException
import org.robobinding.UnrecognizedAttributeException
import org.robobinding.ViewResolutionErrorsException
import org.robobinding.binder.BindingAttributeResolver
import org.robobinding.binder.ViewResolutionResult

import android.view.View


/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class BindingAttributesValidator {

	BindingAttributeResolver bindingAttributeResolver
	ErrorReporter errorReporter

	void validate(Map<File, List<ViewBindingAttributes>> viewBindingsForFile) {
		viewBindingsForFile.each { xmlFile, viewBindingAttributesList ->
			errorReporter.clearErrorsFor(xmlFile)

			resolveAllBindingAttributesInFile(viewBindingAttributesList, xmlFile)
		}
	}

	private resolveAllBindingAttributesInFile(List<ViewBindingAttributes> viewBindingAttributesList, File xmlFile) {
		viewBindingAttributesList.each { ViewBindingAttributes viewBindingAttributes ->
			ViewResolutionResult result = bindingAttributeResolver.resolve(viewBindingAttributes.asPendingAttributesForView())

			try {
				result.assertNoErrors()
			} catch (ViewResolutionErrorsException e) {
				reportResolutionErrors(e, viewBindingAttributes, xmlFile)
			}
		}
	}

	private reportResolutionErrors(ViewResolutionErrorsException e, ViewBindingAttributes viewBindingAttributes, File xmlFile) {
		e.attributeErrors.each { attributeResolutionException ->
			def bindingAttribute = viewBindingAttributes[attributeResolutionException.attributeName]
			reportAttributeResolutionError(xmlFile, bindingAttribute, errorMessageFor(attributeResolutionException, e.view))
		}
		e.missingRequiredAttributeErrors.each { missingRequiredAttributesException ->
			reportMissingRequiredAttributesError(xmlFile, viewBindingAttributes.viewLineNumber, "$missingRequiredAttributesException.message for ${getViewSimpleName(e.view)}")
		}
	}

	def errorMessageFor(AttributeResolutionException e, View view) {
		if (e instanceof UnrecognizedAttributeException)
			return "$e.message for ${getViewSimpleName(view)}"
		
		"$e.message"
	}
	
	def getViewSimpleName(View view) {
		String simpleName = view.getClass().getSimpleName()
		simpleName[0..simpleName.indexOf('$') - 1]
	}
	
	def reportAttributeResolutionError(File xmlFile, BindingAttribute bindingAttribute, String errorMessage) {
		errorReporter.errorIn(xmlFile, bindingAttribute.lineNumber, errorMessage)
	}
	
	def reportMissingRequiredAttributesError(File xmlFile, int viewLineNumber, String errorMessage) {
		errorReporter.errorIn(xmlFile, viewLineNumber, errorMessage)
	}
}
