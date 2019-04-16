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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.ui.generation.ArchetypeGenerationConfigurator;
import org.apache.maven.archetype.ui.generation.ArchetypeSelector;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Generates a new project from an archetype, or updates the actual project if using a partial archetype.
 * If the project is fully generated, it is generated in a directory corresponding to its artifactId.
 * If the project is updated with a partial archetype, it is done in the current directory.
 *
 * @author rafale
 */
@Mojo( name = "generate", requiresProject = false )
@Execute( phase = LifecyclePhase.GENERATE_SOURCES )
public class CreateProjectFromArchetypeMojo
    extends AbstractMojo
    implements ContextEnabled
{
    @Component
    private ArchetypeManager manager;

    @Component
    private ArchetypeSelector selector;

    @Component
    private ArchetypeGenerationConfigurator configurator;

    @Component
    private Invoker invoker;

    /**
     * The archetype's artifactId.
     */
    @Parameter( property = "archetypeArtifactId" )
    private String archetypeArtifactId;

    /**
     * The archetype's groupId.
     */
    @Parameter( property = "archetypeGroupId" )
    private String archetypeGroupId;

    /**
     * The archetype's version.
     */
    @Parameter( property = "archetypeVersion" )
    private String archetypeVersion;

    /**
     * The archetype catalogs to use to build a list and let the user choose from.
     * It is a comma separated list of catalogs.
     * Catalogs use the following schemes:
     * <ul>
     * <li>'<code>local</code>' which is the shortcut to the local repository</li>
     * <li>'<code>remote</code>' which is the shortcut for Maven Central repository or its mirror</li>
     * <li>'<code>internal</code>' which is an internal catalog</li>
     * </ul>
     * <p/>
     * If you want the catalogs to come from a different repository, please add the following to your 
     * {@code settings.xml}
     * <pre>
     *   &lt;repository&gt;
     *     &lt;id&gt;archetype&lt;/id&gt;
     *     &lt;url&gt;https://repository.domain.com/path/to/repo/&lt;/url&gt;
     *   &lt;/repository&gt;
     *   
     *   &lt;!-- in case of a repository with authentication --&gt;
     *   &lt;server&gt;
     *     &lt;id&gt;archetype&lt;/id&gt;
     *     &lt;username&gt;user.name&lt;/username&gt;
     *     &lt;password&gt;s3cr3t&lt;/password&gt;
     *   &lt;/server&gt;
     * </pre>
     * If Maven Central repository catalog file is empty, <code>internal</code> catalog is used instead.
     */
    @Parameter( property = "archetypeCatalog", defaultValue = "remote,local" )
    private String archetypeCatalog;

    /**
     * Local Maven repository.
     */
    @Parameter( defaultValue = "${localRepository}", readonly = true, required = true )
    private ArtifactRepository localRepository;

    /**
     * List of remote repositories used by the resolver.
     */
    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true )
    private List<ArtifactRepository> remoteArtifactRepositories;

    /**
     * User settings used to check the interactiveMode.
     */
    @Parameter( property = "interactiveMode", defaultValue = "${settings.interactiveMode}", required = true )
    private Boolean interactiveMode;

    @Parameter( defaultValue = "${basedir}", property = "outputDirectory" )
    private File outputDirectory;

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession session;

    /**
     * Goals to immediately run on the project created from the archetype.
     */
    @Parameter( property = "goals" )
    private String goals;

    /**
     * Applying some filter on displayed archetypes list: format is <code>artifactId</code> or <code>groupId:artifactId</code>.
     * <ul>
     * <li><code>org.apache:</code> -> displays all archetypes which contain org.apache in groupId</li>
     * <li><code>:jee</code> or <code>jee</code> -> displays all archetypes which contain jee in artifactId</li>
     * <li><code>org.apache:jee</code> -> displays all archetypes which contain org.apache in groupId AND jee in artifactId</li>
     * </ul>
     *
     * @since 2.1
     */
    @Parameter( property = "filter" )
    private String filter;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Properties executionProperties = session.getUserProperties();

        ArchetypeGenerationRequest request =
            new ArchetypeGenerationRequest().setArchetypeGroupId( archetypeGroupId )
            .setArchetypeArtifactId( archetypeArtifactId )
            .setArchetypeVersion( archetypeVersion )
            .setOutputDirectory( outputDirectory.getAbsolutePath() )
            .setLocalRepository( localRepository )
            .setRemoteArtifactRepositories( remoteArtifactRepositories )
            .setFilter( filter )
            .setProjectBuildingRequest( session.getProjectBuildingRequest() );

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

            if ( StringUtils.isBlank( request.getArchetypeArtifactId() ) )
            {
                // no archetype found: stopping
                return;
            }

            configurator.configureArchetype( request, interactiveMode, executionProperties );

            ArchetypeGenerationResult generationResult = manager.generateProjectFromArchetype( request );

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
            throw (MojoFailureException) new MojoFailureException( ex.getMessage() ).initCause( ex );
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
        getLog().info( "Invoking post-archetype-generation goals: " + goals );

        File projectBasedir = new File( outputDirectory, artifactId );

        if ( projectBasedir.exists() )
        {
            InvocationRequest request = new DefaultInvocationRequest().setBaseDirectory( projectBasedir ).setGoals(
                Arrays.asList( StringUtils.split( goals, "," ) ) );

            try
            {
                InvocationResult result = invoker.execute( request );
                
                if ( result.getExitCode() != 0 )
                {
                    throw new MojoExecutionException( "Failed to invoke goals",
                                                      result.getExecutionException() );
                }
            }
            catch ( MavenInvocationException e )
            {
                throw new MojoExecutionException( "Cannot run additions goals.", e );
            }
        }
        else
        {
            getLog().info( "Post-archetype-generation goals aborted: unavailable basedir " + projectBasedir );
        }
    }
}
