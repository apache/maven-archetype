package org.apache.maven.archetype.mojos;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.ui.creation.ArchetypeCreationConfigurator;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * Creates an archetype project from the current project.
 * </p>
 * <p>
 * This goal reads your source and resource files, the values of its parameters,
 * and properties you specify in a <code>.property</code> file, and uses them to
 * create a Maven archetype project using the maven-archetype packaging. 
 * If you build the resulting project, it will create the archetype. You can then
 * use this archetype to create new projects that resemble the original.  
 * </p>
 * <p>
 * The maven-archetype-plugin uses Velocity to expand template files, and this documentation
 * talks about 'Velocity Properties', which are values substituted into Velocity templates.
 * See <a href="http://velocity.apache.org/engine/devel/user-guide.html">The Velocity User's Guide</a>
 * for more information.
 * </p>
 * <p>
 * This goal modifies the text of the files of the current project to form the Velocity template files
 * that make up the archetype.
 * </p>
 * <dl>
 * <dt>GAV</dt><dd>The GAV values for the current project are replaced by properties: groupId, artifactId, and version.
 * The user chooses new values for these when generating a project from the archetype.</dd>
 * <dt>package</dt><dd>All the files under one specified Java (or cognate) package are relocated to a project 
 * that the user chooses when generating a project. References to the class name are replaced by a property reference. For
 * example, if the current project's sources are in the package <code>org.apache.saltedpeanuts</code>, then 
 * any example of the string <code>org.apache.saltedpeanuts</code> is replaced with the Velocity property
 * reference <code>${packageName}</code>. When the user generates a project, this is in turn replaced by
 * his or her choice of a package.  
 * </dd>
 * <dt>custom properties</dt><dd>You may identify additional strings that should be replaced by parameters. 
 * To add custom properties, you must use the <code>propertyFile</code> parameter to specify a property file.
 * See the documentation for <code>propertyFile</code> for the details.
 * </dl>
 * <p>
 * Note that you may need to edit the results of this goal. This goal has no way to exclude unwanted files,
 * or add copyright notices to the Velocity templates, or add more complex elements to the archetype metadata file.
 * </p>
 * <p>
 * This goal also generates a simple integration-test that exercises the generated archetype.
 * </p>
 *
 * @author rafale
 * @requiresProject true
 * @goal create-from-project
 * @execute phase="generate-sources"
 * @aggregator
 */
public class CreateArchetypeFromProjectMojo
    extends AbstractMojo
{
    /** @component */
    private ArchetypeCreationConfigurator configurator;

    /**
     * Enable the interactive mode to define the archetype from the project.
     *
     * @parameter expression="${interactive}" default-value="false"
     */
    private boolean interactive;

    /** @component */
    private ArchetypeManager manager;

    /**
     * File extensions which are checked for project's text files (vs binary files).
     *
     * @parameter expression="${archetype.filteredExtentions}"
     */
    private String archetypeFilteredExtentions;

    /**
     * Directory names which are checked for project's sources main package.
     *
     * @parameter expression="${archetype.languages}"
     */
    private String archetypeLanguages;

    /**
     * The location of the registry file.
     *
     * @parameter expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * Velocity templates encoding.
     *
     * @parameter default-value="UTF-8" expression="${archetype.encoding}"
     */
    private String defaultEncoding;

    /**
     * Create a partial archetype.
     *
     * @parameter expression="${archetype.partialArchetype}"
     */
    private boolean partialArchetype = false;

    /**
     * Create pom's velocity templates with CDATA preservation. This uses the <code>String.replaceAll()</code>
     * method and risks to have some overly replacement capabilities (beware of '1.0' value).
     *
     * @parameter expression="${archetype.preserveCData}"
     */
    private boolean preserveCData = false;

    /** @parameter expression="${localRepository}"
     * @readonly
     **/
    private ArtifactRepository localRepository;

    /**
     * POMs in archetype are created with their initial parent.
     * This property is ignored when preserveCData is true.
     *
     * @parameter expression="${archetype.keepParent}"
     */
    private boolean keepParent = true;

    /**
     * The Maven project to create an archetype from.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The property file that holds the plugin configuration. If this is provided, then
     * the plugin reads properties from here. The properties in here can be standard
     * properties listed below or custom properties for this archetype. The standard properties
     * are below. Several of them overlap parameters of this goal; it's better to just
     * set the parameter.
     * 
     *  <dl><dt>package</dt><dd>See the packageName parameter.</dd>
     *  <dt>archetype.languages</dt><dd>See the archetypeLanguages parameter.</dd>
     *  <dt>groupId</dt><dd>The default groupId of the generated project.</dd>
     *  <dt>artifactId</dt><dd>The default artifactId of the generated project.</dd> 
     *  <dt>version</dt><dd>The default version of the generated project.</dd>
     *  <dt>archetype.filteredExtensions</dt><dd>See the filteredExensions parameter.</dd>
     *  </dl>
     *  <strong>Custom Properties</strong>
     *  <p>
     *  Custom properties allow you to replace some constant values in the project's files
     *  with Velocity macro references. When a user generates a project from your archetype
     *  he or she gets the opportunity to replace the value from the source project. 
     *  </p>
     *  <p>
     *  Custom property names <strong>may not contain the '.' character</strong>.
     *  </p>
     *  <p>
     *  For example, if you include a line like the following in your property file:
     *  <pre>
     *  	cxf-version=2.5.1-SNAPSHOT
     *  </pre>
     *  the plugin will search your files for the string <pre>2.5.1-SNAPSHOT</pre> and
     *  replace them with references to a velocity macro <pre>cxf-version</pre>. It will 
     *  then list <pre>cxf-version</pre> as a <pre>requiredProperty</pre> in the 
     *  archetype-metadata.xml, with <pre>2.5.1-SNAPSHOT</pre> as the default value.
     *  </p>
     *  
     *
     * @parameter expression="${archetype.properties}"
     */
    private File propertyFile;

    /**
     * The property telling which phase to call on the generated archetype.
     * Interesting values are: <code>package</code>, <code>integration-test</code>, <code>install</code> and <code>deploy</code>.
     *
     * @parameter expression="${archetype.postPhase}" default-value="package"
     */
    private String archetypePostPhase;

    /**
     * The directory where the archetype should be created.
     * 
     * @parameter expression="${project.build.directory}/generated-sources/archetype"
     */
    private File outputDirectory;

    /** @parameter expression="${testMode}" */
    private boolean testMode;

    /** 
     * The package name for Java source files to be incorporated in the archetype and 
     * and relocated to the package that the user selects.
     * 
     * @parameter expression="${packageName}" */
    private String packageName; //Find a better way to resolve the package!!! enforce usage of the configurator

    /**
     *  @parameter expression="${session}"
     *  @readonly
     */
    private MavenSession session;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Properties executionProperties = session.getExecutionProperties();
        try
        {
            if ( propertyFile != null )
            {
                propertyFile.getParentFile().mkdirs();
            }

            List<String> languages = getLanguages( archetypeLanguages, propertyFile );

            Properties properties =
                configurator.configureArchetypeCreation( project, Boolean.valueOf( interactive ), executionProperties,
                                                         propertyFile, languages );

            List<String> filtereds = getFilteredExtensions( archetypeFilteredExtentions, propertyFile );

            ArchetypeCreationRequest request = new ArchetypeCreationRequest()
                .setDefaultEncoding( defaultEncoding )
                .setProject( project )
                /* Used when in interactive mode */
                .setProperties( properties )
                .setLanguages( languages )
                /* Should be refactored to use some ant patterns */
                .setFiltereds( filtereds )
                /* This should be correctly handled */
                .setPreserveCData( preserveCData )
                .setKeepParent( keepParent )
                .setPartialArchetype( partialArchetype )
                /* This should be used before there and use only languages and filtereds */
                .setArchetypeRegistryFile( archetypeRegistryFile )
                .setLocalRepository( localRepository )
                /* this should be resolved and asked for user to verify */
                .setPackageName( packageName )
                .setPostPhase( archetypePostPhase )
                .setOutputDirectory( outputDirectory );

            ArchetypeCreationResult result = manager.createArchetypeFromProject( request );

            if ( result.getCause() != null )
            {
                throw new MojoFailureException( result.getCause(), result.getCause().getMessage(),
                                                result.getCause().getMessage() );
            }

            getLog().info( "Archetype created in " + outputDirectory );

            if ( testMode )
            {
                // Now here a properties file would be useful to write so that we could automate
                // some functional tests where we string together an:
                //
                // archetype create from project -> deploy it into a test repo
                // project create from archetype -> use the repository we deployed to archetype to
                // generate
                // test the output
                //
                // This of course would be strung together from the outside.
            }

        }
        catch ( MojoFailureException ex )
        {
            throw ex;
        }
        catch ( Exception ex )
        {
            throw new MojoFailureException( ex, ex.getMessage(), ex.getMessage() );
        }
    }

    private List<String> getFilteredExtensions( String archetypeFilteredExtentions, File propertyFile )
    {
        List<String> filteredExtensions = new ArrayList<String>();

        if ( StringUtils.isNotEmpty( archetypeFilteredExtentions ) )
        {
            filteredExtensions.addAll( Arrays.asList( StringUtils.split( archetypeFilteredExtentions, "," ) ) );

            getLog().debug( "Found in command line extensions = " + filteredExtensions );
        }

        if ( filteredExtensions.isEmpty() && propertyFile != null && propertyFile.exists() )
        {
            Properties properties = PropertyUtils.loadProperties( propertyFile );

            String extensions = properties.getProperty( Constants.ARCHETYPE_FILTERED_EXTENSIONS );
            if ( StringUtils.isNotEmpty( extensions ) )
            {
                filteredExtensions.addAll( Arrays.asList( StringUtils.split( extensions, "," ) ) );
            }

            getLog().debug( "Found in propertyFile " + propertyFile.getName() + " extensions = " + filteredExtensions );
        }

        if ( filteredExtensions.isEmpty() )
        {
            filteredExtensions.addAll( Constants.DEFAULT_FILTERED_EXTENSIONS );

            getLog().debug( "Using default extensions = " + filteredExtensions );
        }

        return filteredExtensions;
    }

    private List<String> getLanguages( String archetypeLanguages, File propertyFile )
    {
        List<String> resultingLanguages = new ArrayList<String>();

        if ( StringUtils.isNotEmpty( archetypeLanguages ) )
        {
            resultingLanguages.addAll( Arrays.asList( StringUtils.split( archetypeLanguages, "," ) ) );

            getLog().debug( "Found in command line languages = " + resultingLanguages );
        }

        if ( resultingLanguages.isEmpty() && propertyFile != null && propertyFile.exists() )
        {
            Properties properties = PropertyUtils.loadProperties( propertyFile );

            String languages = properties.getProperty( Constants.ARCHETYPE_LANGUAGES );
            if ( StringUtils.isNotEmpty( languages ) )
            {
                resultingLanguages.addAll( Arrays.asList( StringUtils.split( languages, "," ) ) );
            }

            getLog().debug( "Found in propertyFile " + propertyFile.getName() + " languages = " + resultingLanguages );
        }

        if ( resultingLanguages.isEmpty() )
        {
            resultingLanguages.addAll( Constants.DEFAULT_LANGUAGES );

            getLog().debug( "Using default languages = " + resultingLanguages );
        }

        return resultingLanguages;
    }
}
