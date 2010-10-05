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

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

/**
 * Execute the archetype integration tests, consisting of a creation of a project from the current archetype with
 * defined properties and optional comparison with reference copy. An IT consists of a directory in
 * <code>src/test/resources/projects</code> containing:
 * <ul>
 * <li><code>goal.txt</code> (content actually not used, but future version should interpret it as a goal to run against
 * the generated project: see <a href="http://jira.codehaus.org/browse/ARCHETYPE-334/">ARCHETYPE-334</a>),</li>
 * <li><code>archetype.properties</code> with properties for project generation,</li>
 * <li>optional <code>reference/</code> directory containing a reference copy of the expected project created from the IT.</li>
 * </ul>
 *
 * @author rafale
 * @requiresProject true
 * @goal integration-test
 */
public class IntegrationTestMojo
    extends AbstractMojo
{
    /** @component */
    ArchetypeGenerator archetypeGenerator;

    /**
     * The archetype project to execute the integration tests on.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Skip the integration test.
     *
     * @parameter expression="${archetype.test.skip}"
     * @readonly
     */
    private boolean skip = false;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            return;
        }

        File projectsDirectory = new File( project.getBasedir(), "target/test-classes/projects" );

        if ( !projectsDirectory.exists() )
        {
            return;
        }

        try
        {
            List<File> projectsGoalFiles = FileUtils.getFiles( projectsDirectory, "*/goal.txt", "" );

            File archetypeFile = project.getArtifact().getFile();

            StringWriter errorWriter = new StringWriter();
            for ( File goalFile : projectsGoalFiles )
            {
                try
                {
                    processIntegrationTest( goalFile, archetypeFile );
                }
                catch ( IntegrationTestFailure ex )
                {
                    errorWriter.write( "Test " + goalFile.getParentFile().getName() + " failed\n" );
                    errorWriter.write( ex.getStackTrace() + "\n" );
                    errorWriter.write( ex.getMessage() + "\n" );
                    errorWriter.write( "\n" );
                }
            }

            String errors = errorWriter.toString();
            if ( !StringUtils.isEmpty( errors ) )
            {
                throw new MojoExecutionException( errors );
            }
        }
        catch ( IOException ex )
        {
            throw new MojoFailureException( ex, ex.getMessage(), ex.getMessage() );
        }
    }

    private void assertTest( File reference, File basedir )
        throws IntegrationTestFailure, IOException
    {
        List<String> referenceFiles = FileUtils.getFileNames( reference, "**", null, false );
        List<String> projectFiles = FileUtils.getFileNames( basedir, "**", null, false );

        boolean fileNamesEquals = CollectionUtils.isEqualCollection( referenceFiles, projectFiles );

        if ( !fileNamesEquals )
        {
            for ( String ref : referenceFiles )
            {
                if ( projectFiles.contains( ref ) )
                {
                    projectFiles.remove( ref );
                    getLog().debug( "Contained " + ref );
                }
                else
                {
                    getLog().error( "Not contained " + ref );
                }
            }
            getLog().error( "Remains " + projectFiles );

            throw new IntegrationTestFailure( "Reference and generated project differs" );
        }

        boolean contentEquals = true;

        for ( String file : referenceFiles )
        {
            if ( file.endsWith( "pom.xml" ) )
            {
                if ( !modelEquals( new File( reference, file ), new File( basedir, file ) ) )
                {
                    getLog().warn( "Contents of file " + file + " are not equal" );
                    contentEquals = false;
                }
            }
            else
            {
                if ( !FileUtils.contentEquals( new File( reference, file ), new File( basedir, file ) ) )
                {
                    getLog().warn( "Contents of file " + file + " are not equal" );
                    contentEquals = false;
                }
            }
        }
        if ( !contentEquals )
        {
            throw new IntegrationTestFailure( "Some content are not equals" );
        }
    }

    private Properties loadProperties( final File propertiesFile )
        throws IOException, FileNotFoundException
    {
        Properties properties = new Properties();

        InputStream in = null;
        try
        {
            in = new FileInputStream( propertiesFile );

            properties.load( in );
        }
        finally
        {
            IOUtil.close( in );
        }

        return properties;
    }

    private boolean modelEquals( File referencePom, File generatedPom )
        throws IOException
    {
        return FileUtils.contentEquals( referencePom, generatedPom );
    }

    private void processIntegrationTest( File goalFile, File archetypeFile )
        throws IntegrationTestFailure
    {
        try
        {
            Properties properties = getProperties( goalFile );

            String basedir = goalFile.getParentFile().getPath() + "/project";

            FileUtils.deleteDirectory( basedir );

            FileUtils.mkdir( basedir );

            ArchetypeGenerationRequest request = new ArchetypeGenerationRequest()
                .setArchetypeGroupId( project.getGroupId() )
                .setArchetypeArtifactId( project.getArtifactId() )
                .setArchetypeVersion( project.getVersion() )
                .setGroupId( properties.getProperty( Constants.GROUP_ID ) )
                .setArtifactId( properties.getProperty( Constants.ARTIFACT_ID ) )
                .setVersion( properties.getProperty( Constants.VERSION ) )
                .setPackage( properties.getProperty( Constants.PACKAGE ) )
                .setOutputDirectory( basedir )
                .setProperties( properties );

            ArchetypeGenerationResult result = new ArchetypeGenerationResult();

            archetypeGenerator.generateArchetype( request, archetypeFile, result );

            if ( result.getCause() != null )
            {
                throw new IntegrationTestFailure( result.getCause() );
            }

            File reference = new File( goalFile.getParentFile(), "reference" );

            if ( reference.exists() )
            {
                // compare generated project with reference
                assertTest( reference, new File( basedir, request.getArtifactId() ) );
            }
        }
        catch ( IOException ioe )
        {
            throw new IntegrationTestFailure( ioe );
        }
    }

    private Properties getProperties( File goalFile )
        throws IOException
    {
        File propertiesFile = new File( goalFile.getParentFile(), "archetype.properties" );

        return loadProperties( propertiesFile );
    }

    class IntegrationTestFailure
        extends Exception
    {
        IntegrationTestFailure()
        {
            super();
        }

        IntegrationTestFailure( String message )
        {
            super( message );
        }

        IntegrationTestFailure( Throwable cause )
        {
            super( cause );
        }

        IntegrationTestFailure( String message, Throwable cause )
        {
            super( message, cause );
        }
    }
}
