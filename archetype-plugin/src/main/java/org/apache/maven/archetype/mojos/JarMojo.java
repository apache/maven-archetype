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

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;

/**
 * Create a jar archive from an archetype directory.
 *
 * @author rafale
 * @goal jar
 * @phase package
 * @requiresProject
 */
public class JarMojo
    extends AbstractMojo
{
    /**
     * Directory containing the classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File archetypeDirectory;

    /**
     * The maven archive configuration to use.
     * <p/>
     * <p>See <a
     * href="http://maven.apache.org/ref/current/maven-archiver/apidocs/org/apache/maven/archiver/MavenArchiveConfiguration.html">
     * the Javadocs for MavenArchiveConfiguration</a>.</p>
     *
     * @parameter
     */
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * Name of the generated JAR.
     *
     * @parameter alias="jarName" expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     */
    private JarArchiver jarArchiver;

    /**
     * Directory containing the generated JAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    protected static File getJarFile( File basedir,
                                      String finalName )
    {
        return new File( basedir, finalName + ".jar" );
    }

    public void execute()
        throws
        MojoExecutionException,
        MojoFailureException
    {
        File jarFile = createArchive();
        project.getArtifact().setFile( jarFile );
    }

    private File createArchive()
        throws
        MojoExecutionException
    {
        File jarFile = getJarFile( outputDirectory, finalName );

        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver( jarArchiver );

        archiver.setOutputFile( jarFile );

        archive.setForced( true );

        try
        {
            if ( !archetypeDirectory.exists() )
            {
                getLog().warn( "JAR will be empty - no content was marked for inclusion!" );
            }
            else
            {
                archiver.getArchiver().addDirectory( archetypeDirectory );
            }

            archiver.createArchive( project, archive );

            return jarFile;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error assembling JAR", e );
        }
    }
}
