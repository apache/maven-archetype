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

import java.io.IOException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import java.io.File;
import org.apache.maven.archetype.Archetype;

/**
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
     * The maven project.
     *
     * @parameter  expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The archetype component.
     *
     * @component
     */
    private Archetype archetype;

    public void execute( )
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            File jarFile = archetype.archiveArchetype( archetypeDirectory, outputDirectory, finalName );

            project.getArtifact().setFile( jarFile );
        }
        catch ( DependencyResolutionRequiredException ex )
        {
            throw new MojoExecutionException( ex.getMessage(  ), ex );
        }
        catch ( IOException ex )
        {
            throw new MojoExecutionException( ex.getMessage(  ), ex );
        }
    }
}