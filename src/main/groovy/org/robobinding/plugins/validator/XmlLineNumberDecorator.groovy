package org.robobinding.plugins.validator

import groovy.transform.Immutable;

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
}
