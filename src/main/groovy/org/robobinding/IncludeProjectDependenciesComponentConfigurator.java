package org.robobinding;

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
 * A custom ComponentConfigurator which adds the project's runtime classpath
 * elements to the
 * 
 * @author Brian Jackson
 * @since Aug 1, 2008 3:04:17 PM
 * 
 * @plexus.component 
 *                   role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="include-project-dependencies"
 * @plexus.requirement role=
 *                     "org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                     role-hint="default"
 */
public class IncludeProjectDependenciesComponentConfigurator extends
		AbstractComponentConfigurator {

   protected MavenProject project;
	
	public void configureComponent(Object component,
			PlexusConfiguration configuration,
			ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm,
			ConfigurationListener listener)
			throws ComponentConfigurationException {

		
		addProjectDependenciesToClassRealm(expressionEvaluator, containerRealm);

		converterLookup.registerConverter(new ClassRealmConverter(
				containerRealm));

		ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

		converter.processConfiguration(converterLookup, component,
				containerRealm.getClassLoader(), configuration,
				expressionEvaluator, listener);
	}

	private void addProjectDependenciesToClassRealm(
			ExpressionEvaluator expressionEvaluator, ClassRealm containerRealm)
			throws ComponentConfigurationException {
		try {
			project = (MavenProject)expressionEvaluator
					.evaluate("${project}");
		} catch (ExpressionEvaluationException e) {
			throw new ComponentConfigurationException(
					"There was a problem evaluating: ${project.runtimeClasspathElements}",
					e);
		}

		List<URL> urls = getProjectDependencies(project);
		for (URL url : urls) {
			containerRealm.addConstituent(url);
		}
	}

	private List<URL> getProjectDependencies(MavenProject project) {
		
		List<URL> projectDependencyURLs = new ArrayList<URL>();
		
		try
		{
		for (Object path : project.getRuntimeClasspathElements())
			projectDependencyURLs.add(new File((String)path).toURI().toURL());
		
		for (Object artifact : project.getDependencyArtifacts())
			projectDependencyURLs.add(((Artifact)artifact).getFile().toURI().toURL());
		
		} catch (Exception e){}
		
		return projectDependencyURLs;
	}
	
}
