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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.maven.shared.scriptinterpreter.RunFailureException;
import org.apache.maven.shared.scriptinterpreter.ScriptRunner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.introspection.ReflectionValueExtractor;

/**
 * <p>Execute the archetype integration tests, consisting in generating projects from the current archetype and optionally
 * comparing generated projects with reference copy.</p>
 * 
 * <p>Each IT consists of a sub-directory in <code>src/test/resources/projects</code> containing:</p>
 * 
 * <ul>
 * <li>a <code>goal.txt</code> file, containing a list of goals to run against the generated project (can be empty,
 * content ignored before maven-archetype-plugin 2.1),</li>
 * <li>an <code>archetype.properties</code> file, containing properties for project generation,</li>
 * <li>an optional <code>reference/</code> directory containing a reference copy of the expected project created from
 * the IT.</li>
 * </ul>
 * <p>To let the IT create a Maven module below some other Maven project (being generated from another archetype)
 * one can additionally specify an optional <code>archetype.pom.properties</code> file in the parent directory,
 * specifying the archetype's <code>groupId</code>, <code>artifactId</code> and <code>version</code> along with its 
 * <code>archetype.properties</code> file, containing properties for project generation. Both files are leveraged
 * to create the parent project for this IT. Parent projects can be nested.</p>
 * 
 * <p>An example structure for such an integration test looks like this</p>
 * <table summary="integration test folder structure">
 * <tr>
 * <th>File/Directory</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>src/test/resources/projects/it1</code></td>
 * <td>Directory for integration test 1</td>
 * </tr>
 * <tr>
 * <td><code>src/test/resources/projects/it1/archetype.pom.properties</code></td>
 * <td>GAV for the archetype from which to generate the parent</td>
 * </tr>
 * <tr>
 * <td><code>src/test/resources/projects/it1/archetype.properties</code></td>
 * <td>All required properties for the archetype being specified by <code>archetype.pom.properties</code> on this level</td>
 * </tr>
 * <tr>
 * <td><code>src/test/resources/projects/it1/child</code></td>
 * <td>Directory for maven module within integration test 1 (this folder's name is not relevant)</td>
 * </tr>
 * <tr>
 * <td><code>src/test/resources/projects/it1/child/goal.txt</code></td>
 * <td>The file containing the list of goals to be executed against the generated project</td>
 * </tr>
 * <tr>
 * <td><code>src/test/resources/projects/it1/child/archetype.properties</code></td>
 * <td>All required properties for this project's archetype</td>
 * </tr>
 * </table>
 *  
 * <p>Notice that it is expected to be run as part as of a build after the <code>package</code> phase and not directly as a
 * goal from CLI.</p>
 *
 * @author rafale
 */
@Mojo( name = "integration-test", requiresProject = true )
public class IntegrationTestMojo
    extends AbstractMojo
{

    @Component
    private ArchetypeGenerator archetypeGenerator;

    @Component
    private Invoker invoker;
    
    @Component
    private ArtifactResolver artifactResolver;
    
    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true )
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter( defaultValue = "${localRepository}", readonly = true, required = true )
    protected ArtifactRepository localRepository;
    
    /**
     * The archetype project to execute the integration tests on.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;
    
    /**
     * Skip the integration test.
     */
    @Parameter( property = "archetype.test.skip" )
    private boolean skip = false;

    /**
     * Directory of test projects
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.projectsDirectory", defaultValue = "${project.build.testOutputDirectory}/projects", required = true )
    private File testProjectsDirectory;

    /**
     * Relative path of a cleanup/verification hook script to run after executing the build. This script may be written
     * with either BeanShell or Groovy. If the file extension is omitted (e.g. <code>verify</code>), the
     * plugin searches for the file by trying out the well-known extensions <code>.bsh</code> and <code>.groovy</code>.
     * If this script exists for a particular project but returns any non-null value different from <code>true</code> or
     * throws an exception, the corresponding build is flagged as a failure.
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.verifyScript", defaultValue = "verify" )
    private String postBuildHookScript;

    /**
     * Suppress logging to the <code>build.log</code> file.
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.noLog", defaultValue = "false" )
    private boolean noLog;

    /**
     * Flag used to determine whether the build logs should be output to the normal mojo log.
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.streamLogs", defaultValue = "true" )
    private boolean streamLogs;

    /**
     * The file encoding for the post-build script.
     *
     * @since 2.2
     */
    @Parameter( property = "encoding", defaultValue = "${project.build.sourceEncoding}" )
    private String encoding;

    /**
     * The local repository to run maven instance.
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.localRepositoryPath", defaultValue = "${settings.localRepository}", required = true )
    private File localRepositoryPath;

    /**
     * flag to enable show mvn version used for running its (cli option : -V,--show-version )
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.showVersion", defaultValue = "false" )
    private boolean showVersion;

    /**
     * Ignores the EOL encoding for comparing files (default and original behaviour is false).
     *
     * @since 2.3
     */
    @Parameter( property = "archetype.test.ignoreEOLStyle", defaultValue = "false" )
    private boolean ignoreEOLStyle;

    /**
     * Whether to show debug statements in the build output.
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.debug", defaultValue = "false" )
    private boolean debug;

    /**
     * A list of additional properties which will be used to filter tokens in settings.xml
     *
     * @since 2.2
     */
    @Parameter
    private Map<String, String> filterProperties;

    /**
     * The current user system settings for use in Maven.
     *
     * @since 2.2
     */
    @Parameter( defaultValue = "${settings}", required = true, readonly = true )
    private Settings settings;

    /**
     * Path to an alternate <code>settings.xml</code> to use for Maven invocation with all ITs. Note that the
     * <code>&lt;localRepository&gt;</code> element of this settings file is always ignored, i.e. the path given by the
     * parameter {@link #localRepositoryPath} is dominant.
     *
     * @since 2.2
     */
    @Parameter( property = "archetype.test.settingsFile" )
    private File settingsFile;

    /**
     * Common set of properties to pass in on each project's command line, via -D parameters.
     *
     * @since 3.0.2
     */
    @Parameter
    private Map<String, String> properties = new HashMap<>();

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            return;
        }

        if ( !testProjectsDirectory.exists() )
        {
            getLog().warn( "No Archetype IT projects: root 'projects' directory not found." );

            return;
        }

        File archetypeFile = project.getArtifact().getFile();

        if ( archetypeFile == null )
        {
            throw new MojoFailureException( "Unable to get the archetypes' artifact which should have just been built:"
                                                + " you probably launched 'mvn archetype:integration-test' instead of"
                                                + " 'mvn integration-test'." );
        }

        try
        {
            List<File> projectsGoalFiles = FileUtils.getFiles( testProjectsDirectory, "**/goal.txt", "" );

            if ( projectsGoalFiles.size() == 0 )
            {
                getLog().warn( "No Archetype IT projects: no directory with goal.txt found." );

                return;
            }

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
     * @param actual    the actual directory to compare with the reference
     * @throws IntegrationTestFailure if content differs
     */
    private void assertDirectoryEquals( File reference, File actual )
        throws IntegrationTestFailure, IOException
    {
        List<String> referenceFiles =
            FileUtils.getFileAndDirectoryNames( reference, "**", null, false, true, true, true );
        getLog().debug( "reference content: " + referenceFiles );

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

            throw new IntegrationTestFailure(
                "Reference and generated project differs (missing: " + missing + ", unexpected: " + actualFiles.size()
                    + ")" );
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
            else if ( !contentEquals( referenceFile, actualFile ) )
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

    /**
     * Uses the {@link #ignoreEOLStyle} attribute to compare the two files. If {@link #ignoreEOLStyle} is true,
     * then the comparison does not take care about the EOL (aka newline) character.
     */
    private boolean contentEquals( File referenceFile, File actualFile )
        throws IOException
    {
        // Original behaviour
        if ( !ignoreEOLStyle )
        {
            getLog().warn( "Property ignoreEOLStyle was not set - files will be compared considering their EOL style!" );
            return FileUtils.contentEquals( referenceFile, actualFile );
        }

        getLog().debug( "Comparing files with EOL style ignored." );
        
        try ( BufferedReader referenceFileReader = new BufferedReader( new FileReader( referenceFile ) ); 
              BufferedReader actualFileReader = new BufferedReader( new FileReader( actualFile ) ) )
        {
            String refLine = null;
            String actualLine = null;

            do
            {
                refLine = referenceFileReader.readLine();
                actualLine = actualFileReader.readLine();
                if ( !Objects.equals( refLine, actualLine ) )
                {
                    return false;
                }
            }
            while ( refLine != null || actualLine != null );

            return true;
        }
    }

    private Properties loadProperties( final File propertiesFile )
        throws IOException
    {
        Properties properties = new Properties();

        try ( InputStream in = new FileInputStream( propertiesFile ) )
        {
            properties.load( in );
        }

        return properties;
    }

    private void processIntegrationTest( File goalFile, File archetypeFile )
        throws IntegrationTestFailure, MojoExecutionException
    {
        getLog().info( "Processing Archetype IT project: " + goalFile.getParentFile().getName() );

        try
        {
            Properties properties = getProperties( goalFile );

            File basedir = new File( goalFile.getParentFile(), "project" );

            FileUtils.deleteDirectory( basedir );

            FileUtils.mkdir( basedir.toString() );
            
            basedir = setupParentProjects( goalFile.getParentFile().getParentFile(), basedir );

            ArchetypeGenerationRequest request = generate( project.getGroupId(), project.getArtifactId(), project.getVersion(), archetypeFile, properties, basedir.toString() );

            File reference = new File( goalFile.getParentFile(), "reference" );

            if ( reference.exists() )
            {
                // compare generated project with reference
                getLog().info( "Comparing generated project with reference content: " + reference );

                assertDirectoryEquals( reference, new File( basedir, request.getArtifactId() ) );
            }

            String goals = FileUtils.fileRead( goalFile );

            if ( StringUtils.isNotEmpty( goals ) )
            {
                invokePostArchetypeGenerationGoals( goals.trim(), new File( basedir, request.getArtifactId() ),
                                                    goalFile );
            }
        }
        catch ( IOException ioe )
        {
            throw new IntegrationTestFailure( ioe );
        }
    }

    private ArchetypeGenerationRequest generate( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion, File archetypeFile, Properties properties, String basedir ) throws IntegrationTestFailure
    {
        //@formatter:off
        ArchetypeGenerationRequest request =
            new ArchetypeGenerationRequest().setArchetypeGroupId( archetypeGroupId ).setArchetypeArtifactId(
                archetypeArtifactId ).setArchetypeVersion( archetypeVersion ).setGroupId(
                properties.getProperty( Constants.GROUP_ID ) ).setArtifactId(
                properties.getProperty( Constants.ARTIFACT_ID ) ).setVersion(
                properties.getProperty( Constants.VERSION ) ).setPackage(
                properties.getProperty( Constants.PACKAGE ) ).setOutputDirectory( basedir ).setProperties(
                properties );
        //@formatter:on

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        archetypeGenerator.generateArchetype( request, archetypeFile, result );

        if ( result.getCause() != null )
        {
            if ( result.getCause() instanceof ArchetypeNotConfigured )
            {
                ArchetypeNotConfigured anc = (ArchetypeNotConfigured) result.getCause();

                throw new IntegrationTestFailure(
                    "Missing required properties in archetype.properties: " + StringUtils.join(
                        anc.getMissingProperties().iterator(), ", " ), anc );
            }

            throw new IntegrationTestFailure( result.getCause().getMessage(), result.getCause() );
        }
        return request;
    }

    private File setupParentProjects( File configFolder, File buildFolder )
        throws IOException, MojoExecutionException, IntegrationTestFailure
    {
        // look for 'archetype.pom.properties'
        File archetypePomPropertiesFile = new File( configFolder, "archetype.pom.properties" );
        if ( !archetypePomPropertiesFile.exists() ) 
        {
            getLog().debug( "No 'archetype.pom.properties' file found in " + configFolder );
            return buildFolder;
        }
        
        // go up to the parent configuration folder
        buildFolder = setupParentProjects( configFolder.getParentFile(), buildFolder );
        
        Properties archetypePomProperties = loadProperties( archetypePomPropertiesFile );
        String groupId = archetypePomProperties.getProperty( Constants.GROUP_ID );
        if ( StringUtils.isEmpty( groupId ) )
        {
            throw new MojoExecutionException( "Property " + Constants.GROUP_ID + " not set in " + archetypePomPropertiesFile );
        }
        String artifactId = archetypePomProperties.getProperty( Constants.ARTIFACT_ID );
        if ( StringUtils.isEmpty( artifactId ) )
        {
            throw new MojoExecutionException( "Property " + Constants.ARTIFACT_ID + " not set in " + archetypePomPropertiesFile );
        }
        String version = archetypePomProperties.getProperty( Constants.VERSION );
        if ( StringUtils.isEmpty( version ) )
        {
            throw new MojoExecutionException( "Property " + Constants.VERSION + " not set in " + archetypePomPropertiesFile );
        }
        
        File archetypeFile;
        try
        {
            archetypeFile = getArchetypeFile( groupId, artifactId, version );
        }
        catch ( ArtifactResolverException e )
        {
            throw new MojoExecutionException( "Could not resolve archetype artifact " , e );
        }
        Properties archetypeProperties = getProperties( archetypePomPropertiesFile );
        getLog().info( "Setting up parent project in " + buildFolder );
        ArchetypeGenerationRequest request = generate( groupId, artifactId, version, archetypeFile, archetypeProperties, buildFolder.toString() );
        return new File( buildFolder, request.getArtifactId() );
    }

    private File getArchetypeFile( String groupId, String artifactId, String version ) 
        throws ArtifactResolverException
    {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest( session.getProjectBuildingRequest() );
        if ( localRepository != null )
        {
            buildingRequest = buildingRequest.setLocalRepository( localRepository );
        }
        if ( remoteRepositories != null && !remoteRepositories.isEmpty()  )
        {
            buildingRequest = buildingRequest.setRemoteRepositories( remoteRepositories );
        }

        DefaultArtifactCoordinate coordinate = new DefaultArtifactCoordinate();
        coordinate.setGroupId( groupId );
        coordinate.setArtifactId( artifactId );
        coordinate.setVersion( version );
        
        return artifactResolver.resolveArtifact( buildingRequest, coordinate ).getArtifact().getFile();
    }

    private Properties getProperties( File goalFile )
        throws IOException
    {
        File propertiesFile = new File( goalFile.getParentFile(), "archetype.properties" );

        return loadProperties( propertiesFile );
    }

    private void invokePostArchetypeGenerationGoals( String goals, File basedir, File goalFile )
        throws IntegrationTestFailure, IOException, MojoExecutionException
    {
        FileLogger logger = setupLogger( basedir );

        if ( !StringUtils.isBlank( goals ) )
        {

            getLog().info( "Invoking post-archetype-generation goals: " + goals );

            if ( !localRepositoryPath.exists() )
            {
                localRepositoryPath.mkdirs();
            }

            //@formatter:off
            InvocationRequest request = new DefaultInvocationRequest().setBaseDirectory( basedir ).setGoals(
                Arrays.asList( StringUtils.split( goals, "," ) ) ).setLocalRepositoryDirectory(
                localRepositoryPath ).setBatchMode( true ).setShowErrors( true );
            //@formatter:on

            request.setDebug( debug );

            request.setShowVersion( showVersion );

            if ( logger != null )
            {
                request.setErrorHandler( logger );

                request.setOutputHandler( logger );
            }
            
            if ( !properties.isEmpty() )
            {
                Properties props = new Properties();
                for ( Map.Entry<String, String> entry : properties.entrySet() )
                {
                    if ( entry.getValue() != null )
                    {
                        props.setProperty( entry.getKey(), entry.getValue() );
                    }
                }
                request.setProperties( props );
            }

            File interpolatedSettingsFile = null;
            if ( settingsFile != null )
            {
                File interpolatedSettingsDirectory =
                    new File( project.getBuild().getOutputDirectory(), "archetype-it" );
                if ( interpolatedSettingsDirectory.exists() )
                {
                    FileUtils.deleteDirectory( interpolatedSettingsDirectory );
                }
                interpolatedSettingsDirectory.mkdir();
                interpolatedSettingsFile =
                    new File( interpolatedSettingsDirectory, "interpolated-" + settingsFile.getName() );

                buildInterpolatedFile( settingsFile, interpolatedSettingsFile );

                request.setUserSettingsFile( interpolatedSettingsFile );
            }

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
        else
        {
            getLog().info( "No post-archetype-generation goals to invoke." );
        }
        // verify result
        ScriptRunner scriptRunner = new ScriptRunner( getLog() );
        scriptRunner.setScriptEncoding( encoding );

        Map<String, Object> context = new LinkedHashMap<>();
        context.put( "projectDir", basedir );

        try
        {
            scriptRunner.run( "post-build script", goalFile.getParentFile(), postBuildHookScript, context, logger,
                              "failure post script", true );
        }
        catch ( RunFailureException e )
        {
            throw new IntegrationTestFailure( "post build script failure failure: " + e.getMessage(), e );
        }
    }

    private FileLogger setupLogger( File basedir )
        throws IOException
    {
        FileLogger logger = null;

        if ( !noLog )
        {
            File outputLog = new File( basedir, "build.log" );

            if ( streamLogs )
            {
                logger = new FileLogger( outputLog, getLog() );
            }
            else
            {
                logger = new FileLogger( outputLog );
            }

            getLog().debug( "build log initialized in: " + outputLog );

        }

        return logger;
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

    /**
     * Returns the map-based value source used to interpolate settings and other stuff.
     *
     * @return The map-based value source for interpolation, never <code>null</code>.
     */
    private Map<String, Object> getInterpolationValueSource()
    {
        Map<String, Object> props = new HashMap<>();
        if ( filterProperties != null )
        {
            props.putAll( filterProperties );
        }
        if ( filterProperties != null )
        {
            props.putAll( filterProperties );
        }
        props.put( "basedir", this.project.getBasedir().getAbsolutePath() );
        props.put( "baseurl", toUrl( this.project.getBasedir().getAbsolutePath() ) );
        if ( settings.getLocalRepository() != null )
        {
            props.put( "localRepository", settings.getLocalRepository() );
            props.put( "localRepositoryUrl", toUrl( settings.getLocalRepository() ) );
        }
        return new CompositeMap( this.project, props );
    }

    protected void buildInterpolatedFile( File originalFile, File interpolatedFile )
        throws MojoExecutionException
    {
        getLog().debug( "Interpolate " + originalFile.getPath() + " to " + interpolatedFile.getPath() );

        try
        {
            String xml;

            // interpolation with token @...@
            Map<String, Object> composite = getInterpolationValueSource();

            try ( Reader xmlStreamReader = ReaderFactory.newXmlReader( originalFile );
                  Reader reader = new InterpolationFilterReader( xmlStreamReader, composite, "@", "@" ) )
            {
                xml = IOUtil.toString( reader );
            }

            
            try ( Writer writer = WriterFactory.newXmlWriter( interpolatedFile ) )
            {
                interpolatedFile.getParentFile().mkdirs();
                
                writer.write( xml );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to interpolate file " + originalFile.getPath(), e );
        }
    }

    private static class CompositeMap
        implements Map<String, Object>
    {

        /**
         * The Maven project from which to extract interpolated values, never <code>null</code>.
         */
        private MavenProject mavenProject;

        /**
         * The set of additional properties from which to extract interpolated values, never <code>null</code>.
         */
        private Map<String, Object> properties;

        /**
         * Creates a new interpolation source backed by the specified Maven project and some user-specified properties.
         *
         * @param mavenProject The Maven project from which to extract interpolated values, must not be
         *                     <code>null</code>.
         * @param properties   The set of additional properties from which to extract interpolated values, may be
         *                     <code>null</code>.
         */
        protected CompositeMap( MavenProject mavenProject, Map<String, Object> properties )
        {
            if ( mavenProject == null )
            {
                throw new IllegalArgumentException( "no project specified" );
            }
            this.mavenProject = mavenProject;
            this.properties = properties == null ? (Map) new Properties() : properties;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#clear()
         */
        @Override
        public void clear()
        {
            // nothing here
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        @Override
        public boolean containsKey( Object key )
        {
            if ( !( key instanceof String ) )
            {
                return false;
            }

            String expression = (String) key;
            if ( expression.startsWith( "project." ) || expression.startsWith( "pom." ) )
            {
                try
                {
                    Object evaluated = ReflectionValueExtractor.evaluate( expression, this.mavenProject );
                    if ( evaluated != null )
                    {
                        return true;
                    }
                }
                catch ( Exception e )
                {
                    // uhm do we have to throw a RuntimeException here ?
                }
            }

            return properties.containsKey( key ) || mavenProject.getProperties().containsKey( key );
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        @Override
        public boolean containsValue( Object value )
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#entrySet()
         */
        @Override
        public Set<Entry<String, Object>> entrySet()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#get(java.lang.Object)
         */
        @Override
        public Object get( Object key )
        {
            if ( !( key instanceof String ) )
            {
                return null;
            }

            String expression = (String) key;
            if ( expression.startsWith( "project." ) || expression.startsWith( "pom." ) )
            {
                try
                {
                    Object evaluated = ReflectionValueExtractor.evaluate( expression, this.mavenProject );
                    if ( evaluated != null )
                    {
                        return evaluated;
                    }
                }
                catch ( Exception e )
                {
                    // uhm do we have to throw a RuntimeException here ?
                }
            }

            Object value = properties.get( key );

            return ( value != null ? value : this.mavenProject.getProperties().get( key ) );

        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#isEmpty()
         */
        @Override
        public boolean isEmpty()
        {
            return this.mavenProject == null && this.mavenProject.getProperties().isEmpty()
                && this.properties.isEmpty();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#keySet()
         */
        @Override
        public Set<String> keySet()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public Object put( String key, Object value )
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#putAll(java.util.Map)
         */
        @Override
        public void putAll( Map<? extends String, ? extends Object> t )
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#remove(java.lang.Object)
         */
        @Override
        public Object remove( Object key )
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#size()
         */
        @Override
        public int size()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.Map#values()
         */
        @Override
        public Collection<Object> values()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Converts the specified filesystem path to a URL. The resulting URL has no trailing slash regardless whether the
     * path denotes a file or a directory.
     *
     * @param filename The filesystem path to convert, must not be <code>null</code>.
     * @return The <code>file:</code> URL for the specified path, never <code>null</code>.
     */
    private static String toUrl( String filename )
    {
        /*
         * NOTE: Maven fails to properly handle percent-encoded "file:" URLs (WAGON-111) so don't use File.toURI() here
         * as-is but use the decoded path component in the URL.
         */
        String url = "file://" + new File( filename ).toURI().getPath();
        if ( url.endsWith( "/" ) )
        {
            url = url.substring( 0, url.length() - 1 );
        }
        return url;
    }
}
