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

import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.archetype.ui.ArchetypeGenerationConfigurator;
import org.apache.maven.archetype.ui.ArchetypeSelector;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Generates a new project from an archetype.
 *
 * @author rafale
 * @requiresProject false
 * @goal generate
 * @execute phase="generate-sources"
 */
public class CreateProjectFromArchetypeMojo
    extends AbstractMojo
    implements ContextEnabled
{
    /** @component */
    private ArchetypeManager archetype;

    /** @component */
    private ArchetypeSelector selector;

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
     * The archetype's repository.
     *
     * @parameter expression="${archetypeRepository}"
     */
    private String archetypeRepository;

    /**
     * The archetype's catalogs.
     * It is a comma separated list of catalogs.
     * Catalogs use scheme:
     * <ul>
     * <li>'<code>file://...</code>' with <code>archetype-catalog.xml</code> automatically appended when pointing to a directory</li>
     * <li>'<code>http://...</code>' with <code>archetype-catalog.xml</code> always appended</li>
     * <li>'<code>local</code>' which is the shortcut for '<code>file://~/.m2/archetype-catalog.xml</code>'</li>
     * <li>'<code>remote</code>' which is the shortcut for Maven Central repository, ie '<code>http://repo1.maven.org/maven2</code>'</li>
     * <li>'<code>internal</code>' which is an internal catalog</li>
     * </ul>
     *
     * Since 2.0-alpha-5, default value is no longer <code>internal,local</code> but <code>remote,local</code>.
     * If Maven Central repository catalog file is empty, <code>internal</code> catalog is used instead.
     *
     * @parameter expression="${archetypeCatalog}" default-value="remote,local"
     */
    private String archetypeCatalog;

    /**
     * Local maven repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * List of Remote Repositories used by the resolver.
     *
     * @parameter  expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List remoteArtifactRepositories;

    /**
     * User settings use to check the interactiveMode.
     *
     * @parameter expression="${interactiveMode}" default-value="${settings.interactiveMode}"
     * @required
     */
    private Boolean interactiveMode;

    /** @parameter expression="${basedir}" */
    private File basedir;

    /**
     *  @parameter expression="${session}"
     *  @readonly
     */
    private MavenSession session;

    /**
     * Additional goals that can be specified by the user during the creation of the archetype.
     *
     * @parameter expression="${goals}"
     */
    private String goals;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Properties executionProperties = session.getExecutionProperties();

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest()
            .setArchetypeGroupId( archetypeGroupId )
            .setArchetypeArtifactId( archetypeArtifactId )
            .setArchetypeVersion( archetypeVersion )
            .setOutputDirectory( basedir.getAbsolutePath() )
            .setLocalRepository( localRepository )
            .setArchetypeRepository( archetypeRepository )
            .setRemoteArtifactRepositories( remoteArtifactRepositories );

        try
        {
            if ( interactiveMode.booleanValue() )
            {
                getLog().info( "Generating project in Interactive mode" );
            }
            else
            {
                getLog().info( "Generating project in Batch mode" );
            }

            selector.selectArchetype( request, interactiveMode, archetypeCatalog );

            configurator.configureArchetype( request, interactiveMode, executionProperties );

            ArchetypeGenerationResult generationResult = archetype.generateProjectFromArchetype( request );

            if ( generationResult.getCause() != null )
            {
                throw new MojoFailureException( generationResult.getCause(), generationResult.getCause().getMessage(),
                                                generationResult.getCause().getMessage() );
            }
        }
        catch ( MojoFailureException ex )
        {
            throw ex;
        }
        catch ( Exception ex )
        {
            throw new MojoFailureException( ex.getMessage() );
        }

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
    }

    private void invokePostArchetypeGenerationGoals( String goals, String artifactId )
        throws MojoExecutionException, MojoFailureException
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
