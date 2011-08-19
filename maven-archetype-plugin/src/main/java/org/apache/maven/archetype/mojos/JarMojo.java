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
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * Build a JAR from the current Archetype project.
 *
 * @author           rafale
 * @goal             jar
 * @phase            package
 * @requiresProject
 */
public class JarMojo
    extends AbstractMojo
{
    /**
     * Directory containing the classes.
     *
     * @parameter  expression="${project.build.outputDirectory}"
     * @required
     */
    private File archetypeDirectory;

    /**
     * Name of the generated JAR.
     *
     * @parameter  alias="jarName" expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * Directory containing the generated JAR.
     *
     * @parameter  expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The Maven project.
     *
     * @parameter  expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The archetype manager component.
     *
     * @component
     */
    private ArchetypeManager manager;

    /**
     * The archetype artifact manager component.
     *
     * @component
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            getLog().info( "Building archetype jar: " + new File( outputDirectory, finalName ) );

            File jarFile = manager.archiveArchetype( archetypeDirectory, outputDirectory, finalName );

            checkArchetypeFile( jarFile );

            project.getArtifact().setFile( jarFile );
        }
        catch ( DependencyResolutionRequiredException ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
        catch ( IOException ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
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
            else if ( !archetypeArtifactManager.isOldArchetype( jarFile ) )
            {
                getLog().warn( "Building an Old (1.x) Archetype: consider migrating it to current 2.x Archetype." );
            }
            else
            {
                throw new MojoExecutionException( "The current project does not built an archetype" );
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