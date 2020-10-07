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
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;
import java.util.Map;

/**
 * Build a JAR from the current Archetype project.
 *
 * @author rafale
 */
@Mojo( name = "jar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true )
public class JarMojo
    extends AbstractMojo
{
    /**
     * Directory containing the classes.
     */
    @Parameter( defaultValue = "${project.build.outputDirectory}", required = true )
    private File archetypeDirectory;

    /**
     * Name of the generated JAR.
     */
    @Parameter( defaultValue = "${project.build.finalName}", alias = "jarName", required = true )
    private String finalName;

    /**
     * Directory containing the generated JAR.
     */
    @Parameter( defaultValue = "${project.build.directory}", required = true )
    private File outputDirectory;

    /**
     * The Maven project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    /**
     * The {@link MavenSession}.
     */
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    /**
     * The Jar archiver.
     */
    @Component
    private Map<String, Archiver> archivers;

    /**
     * The archive configuration to use. See <a href="https://maven.apache.org/shared/maven-archiver/index.html">Maven
     * Archiver Reference</a>.
     *
     * @since 3.2.0
     */
    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Timestamp for reproducible output archive entries, either formatted as ISO 8601
     * <code>yyyy-MM-dd'T'HH:mm:ssXXX</code> or as an int representing seconds since the epoch (like
     * <a href="https://reproducible-builds.org/docs/source-date-epoch/">SOURCE_DATE_EPOCH</a>).
     *
     * @since 3.2.0
     */
    @Parameter( defaultValue = "${project.build.outputTimestamp}" )
    private String outputTimestamp;

    /**
     * The archetype manager component.
     */
    @Component
    private ArchetypeManager manager;

    /**
     * The archetype artifact manager component.
     */
    @Component
    private ArchetypeArtifactManager archetypeArtifactManager;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        File jarFile = new File( outputDirectory, finalName + ".jar" );
        getLog().info( "Building archetype jar: " + jarFile );

        MavenArchiver archiver = new MavenArchiver();
        archiver.setCreatedBy( "Maven Archetype Plugin", "org.apache.maven.plugins", "maven-archetype-plugin" );

        archiver.setOutputFile( jarFile );

        archiver.setArchiver( (JarArchiver) archivers.get( "jar" ) );

        // configure for Reproducible Builds based on outputTimestamp value
        archiver.configureReproducible( outputTimestamp );

        try
        {
            archiver.getArchiver().addDirectory( archetypeDirectory );

            archiver.createArchive( session, project, archive );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error assembling archetype jar " + jarFile, e );
        }

        checkArchetypeFile( jarFile );

        project.getArtifact().setFile( jarFile );
    }

    private void checkArchetypeFile( File jarFile )
        throws MojoExecutionException
    {
        try
        {
            if ( archetypeArtifactManager.isFileSetArchetype( jarFile ) )
            {
                checkFileSetArchetypeFile( jarFile );
            }
            else if ( archetypeArtifactManager.isOldArchetype( jarFile ) )
            {
                getLog().warn( "Building an Old (1.x) Archetype: consider migrating it to current 2.x Archetype." );
            }
            else
            {
                throw new MojoExecutionException( "The current project does not build an archetype" );
            }
        }
        catch ( UnknownArchetype ua )
        {
            throw new MojoExecutionException( ua.getMessage(), ua );
        }
    }

    private void checkFileSetArchetypeFile( File jarFile )
        throws UnknownArchetype
    {
        ArchetypeDescriptor archetypeDescriptor = archetypeArtifactManager.getFileSetArchetypeDescriptor( jarFile );

        for ( RequiredProperty rp : archetypeDescriptor.getRequiredProperties() )
        {
            if ( rp.getKey().contains( "." ) )
            {
                getLog().warn( "Invalid required property name '" + rp.getKey()
                                   + "': dot character makes is unusable in Velocity template" );
            }
        }
    }
}
