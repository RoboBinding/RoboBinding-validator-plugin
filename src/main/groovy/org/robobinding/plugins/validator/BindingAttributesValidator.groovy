/**
 * Copyright 2013 Cheng Wei, Robert Taylor
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
