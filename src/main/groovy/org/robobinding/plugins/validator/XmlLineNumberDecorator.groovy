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
package org.robobinding.plugins.validator

/**
 *
 * @since 1.0
 * @version $Revision: 1.0 $
 * @author Robert Taylor
 */
class XmlLineNumberDecorator {

	static final String LINE_NUMBER_ATTRIBUTE = "line_number"
	
	String embedLineNumbers(String xml, String bindingPrefix) {
		def decoratedLines = []
		
		xml.eachLine { line, lineNumber ->
			def viewTagMatcher = line =~ "<([a-zA-Z0-9-_\\.]+)"
			
			if(viewTagMatcher) {
				viewTagMatcher.each { viewMatches ->
					def viewName = viewMatches[1]
					line = line.replaceAll(viewName, "$viewName $LINE_NUMBER_ATTRIBUTE=\"${lineNumber + 1}\"")
				}
			}
			else {
				def bindingAttributeMatcher = line =~ "${bindingPrefix}:(\\w+)="
				
				if (bindingAttributeMatcher) {
					bindingAttributeMatcher.each { attributeMatches ->
						def attributeName = attributeMatches[1]
						line = line.replaceFirst(attributeName, "${attributeName}_${lineNumber + 1}")
					}
				}
			}
			
			decoratedLines << line
		}
		
		decoratedLines.join("\n")
	}
	
	def getLineNumber(viewNode) {
		viewNode.attributes()[LINE_NUMBER_ATTRIBUTE].toInteger()
	}
	
	def getBindingAttributeDetails(attributeValue) {
		def attributeDetails = attributeValue.split('_')
		[attributeDetails[0], attributeDetails[1].toInteger()]
	}
	
	def getBindingAttributeDetailsMaps(bindingAttributesMap) {
		def actualBindingAttributes = [:]
		def bindingAttributeLineNumbers = [:]
		
		bindingAttributesMap.each { key, value ->
			def (attributeName, lineNumber) = getBindingAttributeDetails(key)
			actualBindingAttributes[attributeName] = value
			bindingAttributeLineNumbers[attributeName] = lineNumber
		}
		
		[actualBindingAttributes, bindingAttributeLineNumbers]
	}
}
