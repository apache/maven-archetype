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

import org.apache.commons.collections.CollectionUtils;

import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeGenerationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.OutputFileExists;
import org.apache.maven.archetype.exception.PomFileExists;
import org.apache.maven.archetype.exception.ProjectDirectoryExists;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.generator.FilesetArchetypeGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author           rafale
 * @description
 * @requiresProject  true
 * @goal             integration-test
 */
public class IntegrationTestMojo
extends AbstractMojo
{
    /**
     * @component
     */
    FilesetArchetypeGenerator filesetGenerator;

    /**
     * @parameter  expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter  expression="${archetype.test.skip}"
     * @readonly
     */
    private boolean skip = false;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        if ( !skip )
        {
            try
            {
                File projectsDirectory =
                    new File ( project.getBasedir (), "target/test-classes/projects" );

                if ( projectsDirectory.exists () )
                {
                    File archetypeFile = project.getArtifact ().getFile ();

                    List projectsGoalFiles =
                        FileUtils.getFiles ( projectsDirectory, "*/goal.txt", "" );

                    Iterator goalFiles = projectsGoalFiles.iterator ();

                    StringWriter errorWriter = new StringWriter ();
                    while ( goalFiles.hasNext () )
                    {
                        File goalFile = (File) goalFiles.next ();

                        try
                        {
                            processIntegrationTest ( goalFile, archetypeFile );
                        }
                        catch ( IntegrationTestFailure ex )
                        {
                            errorWriter.write (
                                "Test " + goalFile.getParentFile ().getName () + " failed\n"
                            );

                            errorWriter.write ( ex.getStackTrace () + "\n" );
                            errorWriter.write ( ex.getMessage () + "\n" );
                            errorWriter.write ( "\n" );
                        }
                    }

                    String errors = errorWriter.toString ();
                    if ( !StringUtils.isEmpty ( errors ) )
                    {
                        throw new MojoExecutionException ( errors );
                    }
                }
            }
            catch ( IOException ex )
            {
                throw new MojoFailureException ( ex, ex.getMessage (), ex.getMessage () );
            }
        }
    }

    private void assertTest ( File reference, File basedir )
    throws IntegrationTestFailure, IOException
    {
        List referenceFiles = FileUtils.getFileNames ( reference, "**", null, false );
        List projectFiles = FileUtils.getFileNames ( basedir, "**", null, false );

        boolean fileNamesEquals =
            CollectionUtils.isEqualCollection ( referenceFiles, projectFiles );

        {
            Iterator refs = referenceFiles.iterator ();
            while ( refs.hasNext () )
            {
                String ref = (String) refs.next ();

                if ( projectFiles.contains ( ref ) )
                {
                    projectFiles.remove ( ref );
                    getLog ().debug ( "Contained " + ref );
                }
                else
                {
                    getLog ().debug ( "Not contained " + ref );
                }
            }
            getLog ().debug ( "Remains " + projectFiles );
        }

        if ( !fileNamesEquals )
        {
            throw new IntegrationTestFailure ( "Reference and generated project differs" );
        }

        boolean contentEquals = true;
        Iterator files = referenceFiles.iterator ();
        while ( files.hasNext () )
        {
            String file = (String) files.next ();

            if ( file.endsWith ( "pom.xml" ) )
            {
                if ( !modelEquals ( new File ( reference, file ), new File ( basedir, file ) ) )
                {
                    getLog ().warn ( "Contents of file " + file + " are not equal" );
                    contentEquals = false;
                }
            }
            else
            {
                if ( !FileUtils.contentEquals (
                        new File ( reference, file ),
                        new File ( basedir, file )
                    )
                )
                {
                    getLog ().warn ( "Contents of file " + file + " are not equal" );
                    contentEquals = false;
                }
            }
        }
        if ( !contentEquals )
        {
            throw new IntegrationTestFailure ( "Some content are not equals" );
        }
    }

    private Properties loadProperties ( final File propertiesFile )
    throws IOException, FileNotFoundException
    {
        Properties properties = new Properties ();

        properties.load ( new FileInputStream ( propertiesFile ) );

        return properties;
    }

    private boolean modelEquals ( File referencePom, File generatedPom )
    throws IOException
    {
        return FileUtils.contentEquals ( referencePom, generatedPom );
    }

    private void processIntegrationTest ( File goalFile, File archetypeFile )
    throws IntegrationTestFailure
    {
        try
        {
            Properties testProperties = getTestProperties ( goalFile );

            Properties properties = getProperties ( goalFile );

            String basedir = goalFile.getParentFile ().getPath () + "/project";

            FileUtils.mkdir ( basedir );

            filesetGenerator.generateArchetype ( properties, archetypeFile, basedir );

            File reference = new File ( goalFile.getParentFile (), "reference" );

            assertTest (
                reference,
                new File ( basedir, properties.getProperty ( Constants.ARTIFACT_ID ) )
            );
        }
        catch ( ArchetypeNotConfigured ex )
        {
            throw new IntegrationTestFailure ( ex );
        }
        catch ( UnknownArchetype ex )
        {
            throw new IntegrationTestFailure ( ex );
        }
        catch ( PomFileExists ex )
        {
            throw new IntegrationTestFailure ( ex );
        }
        catch ( ProjectDirectoryExists ex )
        {
            throw new IntegrationTestFailure ( ex );
        }
        catch ( ArchetypeGenerationFailure ex )
        {
            throw new IntegrationTestFailure ( ex );
        }
        catch ( IOException ex )
        {
            throw new IntegrationTestFailure ( ex );
        }
        catch ( OutputFileExists ex )
        {
            throw new IntegrationTestFailure ( ex );
        }
    }

    private Properties getProperties ( File goalFile )
    throws IOException
    {
        File propertiesFile = new File ( goalFile.getParentFile (), "archetype.properties" );

        return loadProperties ( propertiesFile );
    }

    private Properties getTestProperties ( File goalFile )
    throws IOException
    {
        return loadProperties ( goalFile );
    }

    class IntegrationTestFailure
    extends Exception
    {
        IntegrationTestFailure ()
        {
            super ();
        }

        IntegrationTestFailure ( String message )
        {
            super ( message );
        }

        IntegrationTestFailure ( Throwable cause )
        {
            super ( cause );
        }

        IntegrationTestFailure ( String message, Throwable cause )
        {
            super ( message, cause );
        }
    }
}
