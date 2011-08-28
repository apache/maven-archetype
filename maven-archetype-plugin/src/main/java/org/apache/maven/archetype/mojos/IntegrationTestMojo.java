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
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * <p>Execute the archetype integration tests, consisting in generating projects from the current archetype and
 * optionally comparing generated projects with reference copy.</p>
 * 
 * <p>Each IT consists of a sub-directory in <code>src/test/resources/projects</code> containing:</p>
 * <ul>
 * <li>a <code>goal.txt</code> file, containing a list of goals to run against the generated project (can be empty,
 * content ignored before maven-archetype-plugin 2.1),</li>
 * <li>an <code>archetype.properties</code> file, containing properties for project generation,</li>
 * <li>an optional <code>reference/</code> directory containing a reference copy of the expected project created from the IT.</li>
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
    private ArchetypeGenerator archetypeGenerator;

    /** @component */
    private Invoker invoker;

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
            getLog().warn( "No Archetype IT projects: root 'projects' directory not found." );

            return;
        }

        try
        {
            @SuppressWarnings( "unchecked" )
            List<File> projectsGoalFiles = FileUtils.getFiles( projectsDirectory, "*/goal.txt", "" );

            if ( projectsGoalFiles.size() == 0 )
            {
                getLog().warn( "No Archetype IT projects: no directory with goal.txt found." );

                return;
            }

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
                    errorWriter.write( "\nArchetype IT '" + goalFile.getParentFile().getName() + "' failed: " );
                    errorWriter.write( ex.getMessage() );
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

    /**
     * Checks that actual directory content is the same as reference.
     *
     * @param reference the reference directory
     * @param actual the actual directory to compare with the reference
     * @throws IntegrationTestFailure if content differs
     */
    private void assertDirectoryEquals( File reference, File actual )
        throws IntegrationTestFailure, IOException
    {
        @SuppressWarnings( "unchecked" )
        List<String> referenceFiles = FileUtils.getFileAndDirectoryNames( reference, "**", null, false, true, true, true );
        getLog().debug( "reference content: " + referenceFiles );

        @SuppressWarnings( "unchecked" )
        List<String> actualFiles = FileUtils.getFileAndDirectoryNames( actual, "**", null, false, true, true, true );
        getLog().debug( "actual content: " + referenceFiles );

        boolean fileNamesEquals = CollectionUtils.isEqualCollection( referenceFiles, actualFiles );

        if ( !fileNamesEquals )
        {
            getLog().debug( "Actual list of files is not the same as reference:" );
            int missing = 0;
            for ( String ref : referenceFiles )
            {
                if ( actualFiles.contains( ref ) )
                {
                    actualFiles.remove( ref );
                    getLog().debug( "Contained " + ref );
                }
                else
                {
                    missing++;
                    getLog().error( "Not contained " + ref );
                }
            }
            getLog().error( "Remains " + actualFiles );

            throw new IntegrationTestFailure( "Reference and generated project differs (missing: " + missing
                + ", unexpected: " + actualFiles.size() + ")" );
        }

        boolean contentEquals = true;

        for ( String file : referenceFiles )
        {
            File referenceFile = new File( reference, file );
            File actualFile = new File( actual, file );

            if ( referenceFile.isDirectory() )
            {
                if ( actualFile.isFile() )
                {
                    getLog().warn( "File " + file + " is a directory in the reference but a file in actual" );
                    contentEquals = false;
                }
            }
            else if ( actualFile.isDirectory() )
            {
                if ( referenceFile.isFile() )
                {
                    getLog().warn( "File " + file + " is a file in the reference but a directory in actual" );
                    contentEquals = false;
                }
            }
            else if ( !FileUtils.contentEquals( referenceFile, actualFile ) )
            {
                getLog().warn( "Contents of file " + file + " are not equal" );
                contentEquals = false;
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

    private void processIntegrationTest( File goalFile, File archetypeFile )
        throws IntegrationTestFailure
    {
        getLog().info( "Processing Archetype IT project: " + goalFile.getParentFile().getName() );

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
                if ( result.getCause() instanceof ArchetypeNotConfigured )
                {
                    ArchetypeNotConfigured anc = (ArchetypeNotConfigured) result.getCause();

                    throw new IntegrationTestFailure( "Missing required properties in archetype.properties: "
                        + StringUtils.join( anc.getMissingProperties().iterator(), ", " ), anc );
                }

                throw new IntegrationTestFailure( result.getCause().getMessage(), result.getCause() );
            }

            File reference = new File( goalFile.getParentFile(), "reference" );

            if ( reference.exists() )
            {
                // compare generated project with reference
                getLog().info( "Comparing generated project with reference content: " + reference );

                assertDirectoryEquals( reference, new File( basedir, request.getArtifactId() ) );
            }

            String goals = FileUtils.fileRead( goalFile );

            invokePostArchetypeGenerationGoals( goals, new File( basedir, request.getArtifactId() ) );
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

    private void invokePostArchetypeGenerationGoals( String goals, File basedir )
        throws IntegrationTestFailure
    {
        if ( StringUtils.isBlank( goals ) )
        {
            getLog().info( "No post-archetype-generation goals to invoke." );

            return;
        }

        getLog().info( "Invoking post-archetype-generation goals: " + goals );

        InvocationRequest request = new DefaultInvocationRequest()
            .setBaseDirectory( basedir )
            .setGoals( Arrays.asList( StringUtils.split( goals, "," ) ) );

        try
        {
            InvocationResult result = invoker.execute( request );

            getLog().info( "Post-archetype-generation invoker exit code: " + result.getExitCode() );

            if ( result.getExitCode() != 0 )
            {
                throw new IntegrationTestFailure( "Execution failure: exit code = " + result.getExitCode(),
                                                  result.getExecutionException() );
            }
        }
        catch ( MavenInvocationException e )
        {
            throw new IntegrationTestFailure( "Cannot run additions goals.", e );
        }
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
