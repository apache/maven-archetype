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
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.archetype.ui.ArchetypeGenerationConfigurator;
import org.apache.maven.archetype.ui.ArchetypeSelector;
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
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.Archetyper;

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
    /** @component */
    private Archetyper archetyper;

    /** @component */
    private ArchetypeSelector selector;

    /** @component */
    ArchetypeRegistryManager archetypeRegistryManager;

    /** @component */
    ArchetypeGenerationConfigurator configurator;

    /** @component */
    ArchetypeGenerator generator;

    /** @component */
    private Invoker invoker;

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
        throws MojoExecutionException, MojoFailureException
    {
        // This is what we need here:
        //
        // - determine what archetype to use
        // - configure it
        // - populate the request
        //
        // then:
        //
        // result = archetype.generateProjectFromArchteype( request );
        //
        // look at the result and respond accordingly.

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( archetypeGroupId );
        request.setArchetypeArtifactId( archetypeArtifactId );
        request.setArchetypeVersion( archetypeVersion );
        request.setOutputDirectory( basedir.getAbsolutePath() );
        request.setLocalRepository( localRepository );

        try
        {
            // This is not really necessary as we will use the central repository or the repository that
            // is specified with the archetype. There is no point in searching N repositories when we
            // know exactly where the archetype is.

            List repositories =
                archetypeRegistryManager.getRepositories(
                    pomRemoteRepositories,
                    remoteRepositories,
                    archetypeRegistryFile
                );

            // Only interactiveMode, repositories and registry file are needed outside the request.
            selector.selectArchetype(
                request,
                settings.getInteractiveMode(),
                archetypeRegistryFile,
                repositories
            );
//            Old way to call components
//            selector.selectArchetype(
//                archetypeGroupId,
//                archetypeArtifactId,
//                archetypeVersion,
//                settings.getInteractiveMode(),
//                propertyFile,
//                archetypeRegistryFile,
//                localRepository,
//                repositories
//            );

            // Create the request here for the archetyper and use that instead of the configurator directly here.

            // Only interactiveMode, system.properties (configuration properties) and repositories are needed outside the request.
            configurator.configureArchetype(
                request,
                settings.getInteractiveMode(),
                System.getProperties(),
                repositories
            );
//            Old way to call components
//            configurator.configureArchetype(
//                settings.getInteractiveMode(),
//                propertyFile,
//                System.getProperties(),
//                localRepository,
//                repositories
//            );

            // At this point, the archetype should have been downloaded by the configurator,
            // so the repositories are not needed in the request

            ArchetypeGenerationResult result = archetyper.generateProjectFromArchetype( request );

            // Old way to call the components
            //generator.generateArchetype( propertyFile, localRepository, repositories, basedir.getAbsolutePath() );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }

        // Configure Generation

        // At this point the project has been generated from the archetype and now we will
        // run some goals that the archetype creator has requested to be run once the project
        // has been created.

        Properties properties = PropertyUtils.loadProperties( propertyFile );

        String artifactId = request.getArtifactId();

        String postArchetypeGenerationGoals = request.getArchetypeGoals();

        if ( StringUtils.isEmpty( postArchetypeGenerationGoals ) )
        {
            postArchetypeGenerationGoals = goals;
        }

        if ( StringUtils.isNotEmpty( postArchetypeGenerationGoals ) )
        {
            invokePostArchetypeGenerationGoals( postArchetypeGenerationGoals, artifactId );
        }

//        FileUtils.fileDelete( propertyFile.getAbsolutePath() );
    }

    private void invokePostArchetypeGenerationGoals( String goals, String artifactId )
        throws
        MojoExecutionException,
        MojoFailureException
    {
        File projectBasedir = new File( basedir, artifactId );

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
