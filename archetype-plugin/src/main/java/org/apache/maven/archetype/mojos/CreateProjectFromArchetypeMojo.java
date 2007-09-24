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

package org.apache.maven.archetype.mojos;

import org.apache.maven.archetype.common.ArchetypePropertiesManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.generator.ArchetypeGenerationConfigurator;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.archetype.generator.ArchetypeSelector;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Generates sample project from archetype.
 *
 * @author rafale
 * @requiresProject false
 * @goal create
 * @execute phase="generate-sources"
 */
public class CreateProjectFromArchetypeMojo
    extends AbstractMojo
    implements ContextEnabled
{
    // Select

    /**
     * The archetype's artifactId.
     *
     * @parameter expression="${archetypeArtifactId}"
     */
    private String archetypeArtifactId;

    /**
     * The archetype's groupId.
     *
     * @parameter expression="${archetypeGroupId}"
     */
    private String archetypeGroupId;

    /**
     * The archetype's version.
     *
     * @parameter expression="${archetypeVersion}"
     */
    private String archetypeVersion;


    /** @component */
    private ArchetypeSelector selector;

    //! Select

    /** @component */
    ArchetypeRegistryManager archetypeRegistryManager;

    /** @component */
    ArchetypeGenerationConfigurator configurator;

    /**
     * The location of the registry file.
     *
     * @parameter expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * Local maven repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Remote repositories defined in the project's pom (used only when called from an existing
     * project).
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List pomRemoteRepositories;

    /**
     * The property file that holds the plugin configuration.
     *
     * @parameter default-value="archetype.properties" expression="${archetype.properties}"
     */
    private File propertyFile = null;

    /**
     * Other remote repositories available for discovering dependencies and extensions.
     *
     * @parameter expression="${remoteRepositories}"
     */
    private String remoteRepositories;

    /**
     * User settings use to check the interactiveMode.
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /** @component */
    ArchetypeGenerator generator;

    /**
     * Maven invoker used to execution additional goals after the archetype has been created.
     *
     * @component
     */
    private Invoker invoker;

    /** @parameter expression="${basedir}" */
    private File basedir;

    /**
     * Additional goals that can be specified by the user during the creation of the archetype.
     *
     * @parameter expression="${goals}"
     */
    private String goals;

    /** @component */
    private ArchetypePropertiesManager propertiesManager;

    public void execute()
        throws
        MojoExecutionException,
        MojoFailureException
    {
        // Select Archetype

        try
        {
            List repositories =
                archetypeRegistryManager.getRepositories(
                    pomRemoteRepositories,
                    remoteRepositories,
                    archetypeRegistryFile
                );

            selector.selectArchetype(
                archetypeGroupId,
                archetypeArtifactId,
                archetypeVersion,
                settings.getInteractiveMode(),
                propertyFile,
                archetypeRegistryFile,
                localRepository,
                repositories
            );

            configurator.configureArchetype(
                settings.getInteractiveMode(),
                propertyFile,
                System.getProperties(),
                localRepository,
                repositories
            );

            generator.generateArchetype( propertyFile, localRepository, repositories, basedir.getAbsolutePath() );

            Properties archetypeProperties = PropertyUtils.loadProperties( propertyFile );

            if ( archetypeProperties != null )
            {
                getPluginContext().put( "artifactId", archetypeProperties.getProperty( "artifactId" ) );
            }
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }

        // Configure Generation


        // At this point the archetype has been generated from the archetype and now we will
        // run some goals that the archetype creator has requested to be run once the project
        // has been created.

        String postArchetypeGenerationGoals;

        Properties p = new Properties();

        try
        {
            propertiesManager.readProperties( p, new File( basedir, "archetype.properties" ) );

            postArchetypeGenerationGoals = p.getProperty( Constants.ARCHETYPE_POST_GENERATION_GOALS );
        }
        catch( Exception e )
        {
            postArchetypeGenerationGoals = goals;
        }

        if ( StringUtils.isNotEmpty( postArchetypeGenerationGoals ) )
        {
            invokePostArchetypeGenerationGoals( postArchetypeGenerationGoals );
        }        
    }


    private void invokePostArchetypeGenerationGoals( String goals )
        throws
        MojoExecutionException,
        MojoFailureException
    {
        //TODO update the archetype descriptor to save goals and properties
        //TODO probably write out the properties to a file for now
        //TODO remove the properties files when the execution is complete

        File projectBasedir = new File( basedir, (String) getPluginContext().get( "artifactId" ) );

        if ( projectBasedir.exists() )
        {
            InvocationRequest request = new DefaultInvocationRequest()
                .setBaseDirectory( projectBasedir )
                .setGoals( Arrays.asList( StringUtils.split( goals, "," ) ) );

            try
            {
                invoker.execute( request );
            }
            catch ( MavenInvocationException e )
            {
                throw new MojoExecutionException( "Cannot run additions goals." );
            }
        }
    }
}
