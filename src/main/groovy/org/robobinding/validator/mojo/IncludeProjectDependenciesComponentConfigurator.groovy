package org.robobinding.validator.mojo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.converters.special.ClassRealmConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * @plexus.component 
 *                   role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="include-project-dependencies"
 * @plexus.requirement role=
 *                     "org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                     role-hint="default"
 */
class IncludeProjectDependenciesComponentConfigurator extends AbstractComponentConfigurator {

	def void configureComponent(Object component, PlexusConfiguration configuration, ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm,
		ConfigurationListener listener) {

		addProjectDependenciesToClassRealm(expressionEvaluator, containerRealm)

		converterLookup.registerConverter(new ClassRealmConverter(containerRealm))

		def converter = new ObjectWithFieldsConverter()
		converter.processConfiguration(converterLookup, component, containerRealm.getClassLoader(), configuration,
				expressionEvaluator, listener)
	}

	def addProjectDependenciesToClassRealm(ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm) {

		def project = expressionEvaluator.evaluate('${project}')

		for (URL url : getProjectDependencies(project))
			containerRealm.addConstituent(url)
	}

	def getProjectDependencies(project) {

		def projectDependencyURLs = []

		project.runtimeClasspathElements.each { path ->
			projectDependencyURLs << new File(path).toURI().toURL()
		}
		
		project.dependencyArtifacts.each { artifact ->
			projectDependencyURLs << artifact.file.toURI().toURL()
		}
		
		projectDependencyURLs
	}
}
