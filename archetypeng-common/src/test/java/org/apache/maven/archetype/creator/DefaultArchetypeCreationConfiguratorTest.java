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

package org.apache.maven.archetype.creator;

//import org.apache.maven.archetype.ui.DefaultArchetypeCreationConfigurator;
//import org.apache.maven.archetype.ui.ArchetypeCreationConfigurator;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DefaultArchetypeCreationConfiguratorTest
    extends AbstractMojoTestCase
{
    private List languages;
    private DefaultArtifactRepository localRepository;

    private List repositories;
    
    public void testNothing()
    {
        //TODO: All the tests were commented out Because the tested classes are beeing moved for now and will be removed soon
    }
    
// Commented out since no exception are thrown if using defaults to all values
//    public void testBatchModeArchetypeNotConfigured ()
//    throws Exception
//    {
//        System.out.println ( "testBatchModeArchetypeNotConfigured" );
//
//        String project = "configure-creation-3";
//        File projectFile = getProjectFile ( project );
//        File projectFileSample = getProjectSampleFile ( project );
//        copy ( projectFileSample, projectFile );
//        FileUtils.deleteDirectory ( new File ( projectFile.getParentFile (), "target" ) );
//
//        MavenProject mavenProject = loadProject ( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup ( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined ( instance );
//
//        Properties commandLineProperties = new Properties ();
//
//        File propertyFile = getPropertiesFile ( project );
//        File propertyFileSample = getPropertiesSampleFile ( project );
//        copy ( propertyFileSample, propertyFile );
//
//        try
//        {
//        languages = new ArrayList();
//        languages.add("java");
//        languages.add("aspectj");
//        languages.add("csharp");
//        languages.add("groovy");
//            instance.configureArchetypeCreation (
//                mavenProject,
//                Boolean.FALSE,
//                commandLineProperties,
//                propertyFile,
//                languages
//            );
//
//            fail ( "Exception must be thrown" );
//        }
//        catch ( ArchetypeNotConfigured e )
//        {
//            assertEquals (
//                "Exception not correct",
//                "The archetype is not configured",
//                e.getMessage ()
//            );
//        }
//    }
// Commented out since no exception are thrown if using defaults to all values
//    public void testBatchModeArchetypeNotDefined ()
//    throws Exception
//    {
//        System.out.println ( "testBatchModeArchetypeNotDefined" );
//
//        String project = "configure-creation-2";
//        File projectFile = getProjectFile ( project );
//        File projectFileSample = getProjectSampleFile ( project );
//        copy ( projectFileSample, projectFile );
//        FileUtils.deleteDirectory ( new File ( projectFile.getParentFile (), "target" ) );
//
//        MavenProject mavenProject = loadProject ( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup ( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined ( instance );
//
//        Properties commandLineProperties = new Properties ();
//
//        File propertyFile = getPropertiesFile ( project );
//        File propertyFileSample = getPropertiesSampleFile ( project );
//        copy ( propertyFileSample, propertyFile );
//
//        try
//        {
//        languages = new ArrayList();
//        languages.add("java");
//        languages.add("aspectj");
//        languages.add("csharp");
//        languages.add("groovy");
//            instance.configureArchetypeCreation (
//                mavenProject,
//                Boolean.FALSE,
//                commandLineProperties,
//                propertyFile,
//                languages
//            );
//
//            fail ( "Exception must be thrown" );
//        }
//        catch ( ArchetypeNotDefined e )
//        {
//            assertEquals (
//                "Exception not correct",
//                "The archetype is not defined",
//                e.getMessage ()
//            );
//        }
//    }
// /*Commented on 2007 09 25
//    public void testBatchModeDefinedAndConfigured()
//        throws
//        Exception
//    {
//        System.out.println( "testBatchModePackageDefault" );
//
//        String project = "configure-creation-6";
//        File projectFile = getProjectFile( project );
//        File projectFileSample = getProjectSampleFile( project );
//        copy( projectFileSample, projectFile );
//        FileUtils.deleteDirectory( new File( projectFile.getParentFile(), "target" ) );
//
//        MavenProject mavenProject = loadProject( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined( instance );
//
//        Properties commandLineProperties = new Properties();
//
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        languages = new ArrayList();
//        languages.add( "java" );
//        languages.add( "aspectj" );
//        languages.add( "csharp" );
//        languages.add( "groovy" );
//        instance.configureArchetypeCreation(
//            mavenProject,
//            Boolean.FALSE,
//            commandLineProperties,
//            propertyFile,
//            languages
//        );
//
//        Properties properties = loadProperties( propertyFile );
//
//        assertEquals(
//            "org.apache.maven.archetype",
//            properties.getProperty( Constants.ARCHETYPE_GROUP_ID )
//        );
//        assertEquals(
//            "maven-archetype-test",
//            properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID )
//        );
//        assertEquals( "1.0", properties.getProperty( Constants.ARCHETYPE_VERSION ) );
//        assertEquals( "some.group.id", properties.getProperty( Constants.GROUP_ID ) );
//        assertEquals( "some-artifact-id", properties.getProperty( Constants.ARTIFACT_ID ) );
//        assertEquals( "1.0", properties.getProperty( Constants.VERSION ) );
//        assertEquals( "org.codehaus.mojo", properties.getProperty( Constants.PACKAGE ) );
//    }
//
//    public void testBatchModePackageDefault()
//        throws
//        Exception
//    {
//        System.out.println( "testBatchModePackageDefault" );
//
//        String project = "configure-creation-5";
//        File projectFile = getProjectFile( project );
//        File projectFileSample = getProjectSampleFile( project );
//        copy( projectFileSample, projectFile );
//        FileUtils.deleteDirectory( new File( projectFile.getParentFile(), "target" ) );
//
//        MavenProject mavenProject = loadProject( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined( instance );
//
//        Properties commandLineProperties = new Properties();
//
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        languages = new ArrayList();
//        languages.add( "java" );
//        languages.add( "aspectj" );
//        languages.add( "csharp" );
//        languages.add( "groovy" );
//        instance.configureArchetypeCreation(
//            mavenProject,
//            Boolean.FALSE,
//            commandLineProperties,
//            propertyFile,
//            languages
//        );
//
//        Properties properties = loadProperties( propertyFile );
//
//        assertEquals(
//            "org.apache.maven.archetype",
//            properties.getProperty( Constants.ARCHETYPE_GROUP_ID )
//        );
//        assertEquals(
//            "maven-archetype-test",
//            properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID )
//        );
//        assertEquals( "1.0", properties.getProperty( Constants.ARCHETYPE_VERSION ) );
//        assertEquals( "some.group.id", properties.getProperty( Constants.GROUP_ID ) );
//        assertEquals( "some-artifact-id", properties.getProperty( Constants.ARTIFACT_ID ) );
//        assertEquals( "1.0", properties.getProperty( Constants.VERSION ) );
//        assertEquals( "org.apache.maven.archetype", properties.getProperty( Constants.PACKAGE ) );
//    }
// Commented on 2007 09 25*/
// Commented out since no exception are thrown if using defaults to all values
//    public void testBatchModePackageDefaultToEmpty ()
//    throws Exception
//    {
//        System.out.println ( "testBatchModePackageDefaultToEmpty" );
//
//        String project = "configure-creation-4";
//        File projectFile = getProjectFile ( project );
//        File projectFileSample = getProjectSampleFile ( project );
//        copy ( projectFileSample, projectFile );
//        FileUtils.deleteDirectory ( new File ( projectFile.getParentFile (), "target" ) );
//
//        MavenProject mavenProject = loadProject ( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup ( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined ( instance );
//
//        Properties commandLineProperties = new Properties ();
//
//        File propertyFile = getPropertiesFile ( project );
//        File propertyFileSample = getPropertiesSampleFile ( project );
//        copy ( propertyFileSample, propertyFile );
//
//        try
//        {
//        languages = new ArrayList();
//        languages.add("java");
//        languages.add("aspectj");
//        languages.add("csharp");
//        languages.add("groovy");
//            instance.configureArchetypeCreation (
//                mavenProject,
//                Boolean.FALSE,
//                commandLineProperties,
//                propertyFile,
//                languages
//            );
//
//            fail ( "Exception must be thrown" );
//        }
//        catch ( ArchetypeNotConfigured e )
//        {
//            assertEquals (
//                "Exception not correct",
//                "The archetype is not configured",
//                e.getMessage ()
//            );
//        }
//    }
// Commented out since no exception are thrown if using defaults to all values
//    public void testBatchModePropertyFileMissing ()
//    throws Exception
//    {
//        System.out.println ( "testBatchModePropertyFileMissing" );
//
//        String project = "configure-creation-1";
//        File projectFile = getProjectFile ( project );
//        File projectFileSample = getProjectSampleFile ( project );
//        copy ( projectFileSample, projectFile );
//        FileUtils.deleteDirectory ( new File ( projectFile.getParentFile (), "target" ) );
//
//        MavenProject mavenProject = loadProject ( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup ( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined ( instance );
//
//        Properties commandLineProperties = new Properties ();
//
//        File propertyFile = getPropertiesFile ( project );
//        assertTrue ( !propertyFile.exists () || propertyFile.delete () );
//
//        try
//        {
//        languages = new ArrayList();
//        languages.add("java");
//        languages.add("aspectj");
//        languages.add("csharp");
//        languages.add("groovy");
//            instance.configureArchetypeCreation (
//                mavenProject,
//                Boolean.FALSE,
//                commandLineProperties,
//                propertyFile,
//                languages
//            );
//
//            fail ( "Exception must be thrown" );
//        }
//        catch ( ArchetypeNotDefined e )
//        {
//            assertEquals (
//                "Exception not correct",
//                "The archetype is not defined",
//                e.getMessage ()
//            );
//        }
//    }
//
// /*Commented on 2007 09 25
//    public void testInteractiveModeNotConfirm()
//        throws
//        Exception
//    {
//        System.out.println( "testInteractiveModeNotConfirm" );
//
//        String project = "configure-creation-8";
//        File projectFile = getProjectFile( project );
//        File projectFileSample = getProjectSampleFile( project );
//        copy( projectFileSample, projectFile );
//        FileUtils.deleteDirectory( new File( projectFile.getParentFile(), "target" ) );
//
//        MavenProject mavenProject = loadProject( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined( instance );
//
//        Properties commandLineProperties = new Properties();
//
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        MockPrompter prompter = new MockPrompter();
//        prompter.addAnswer( "N" );
//        prompter.addAnswer( "N" );
//        prompter.addAnswer( "org.apache.maven.archetypes2" );
//        prompter.addAnswer( "maven-archetype-test2" );
//        prompter.addAnswer( "1.0.2" );
//        prompter.addAnswer( "some.group.id2" );
//        prompter.addAnswer( "some-artifact-id2" );
//        prompter.addAnswer( "1.0.2" );
//        prompter.addAnswer( "org.codehaus" );
//        prompter.addAnswer( "Y" );
//        prompter.addAnswer( "aProperty" );
//        prompter.addAnswer( "some.value" );
//        prompter.addAnswer( "N" );
//        prompter.addAnswer( "Y" );
//        setVariableValueToObject(
//            getVariableValueFromObject( instance, "archetypeCreationQueryer" ),
//            "prompter",
//            prompter
//        );
//
//        languages = new ArrayList();
//        languages.add( "java" );
//        languages.add( "aspectj" );
//        languages.add( "csharp" );
//        languages.add( "groovy" );
//        instance.configureArchetypeCreation(
//            mavenProject,
//            Boolean.TRUE,
//            commandLineProperties,
//            propertyFile,
//            languages
//        );
//
//        Properties properties = loadProperties( propertyFile );
//
//        assertEquals(
//            "org.apache.maven.archetypes2",
//            properties.getProperty( Constants.ARCHETYPE_GROUP_ID )
//        );
//        assertEquals(
//            "maven-archetype-test2",
//            properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID )
//        );
//        assertEquals( "1.0.2", properties.getProperty( Constants.ARCHETYPE_VERSION ) );
//        assertEquals( "some.group.id2", properties.getProperty( Constants.GROUP_ID ) );
//        assertEquals( "some-artifact-id2", properties.getProperty( Constants.ARTIFACT_ID ) );
//        assertEquals( "1.0.2", properties.getProperty( Constants.VERSION ) );
//        assertEquals( "org.codehaus", properties.getProperty( Constants.PACKAGE ) );
//        assertEquals( "some.value", properties.getProperty( "aProperty" ) );
//
//        assertEquals( 8, properties.size() );
//    }
// Commented on 2007 09 25*/
// Commented out if using defaults to all values => change the prompt question order
//    public void testInteractiveModePropertyFileMissing ()
//    throws Exception
//    {
//        System.out.println ( "testInteractiveModePropertyFileMissing" );
//
//        String project = "configure-creation-7";
//        File projectFile = getProjectFile ( project );
//        File projectFileSample = getProjectSampleFile ( project );
//        copy ( projectFileSample, projectFile );
//        FileUtils.deleteDirectory ( new File ( projectFile.getParentFile (), "target" ) );
//
//        MavenProject mavenProject = loadProject ( projectFile );
//
//        DefaultArchetypeCreationConfigurator instance =
//            (DefaultArchetypeCreationConfigurator) lookup ( ArchetypeCreationConfigurator.ROLE );
//        instanceDefined ( instance );
//
//        Properties commandLineProperties = new Properties ();
//
//        File propertyFile = getPropertiesFile ( project );
//        assertTrue ( !propertyFile.exists () || propertyFile.delete () );
//
//        MockPrompter prompter = new MockPrompter ();
//        prompter.addAnswer ( "org.apache.maven.archetypes" );
//        prompter.addAnswer ( "maven-archetype-test" );
//        prompter.addAnswer ( "1.0" );
//        prompter.addAnswer ( "some.group.id" );
//        prompter.addAnswer ( "some-artifact-id" );
//        prompter.addAnswer ( "1.0" );
//        prompter.addAnswer ( "N" );
//        prompter.addAnswer ( "Y" );
//        setVariableValueToObject (
//            getVariableValueFromObject ( instance, "archetypeCreationQueryer" ),
//            "prompter",
//            prompter
//        );
//
//        languages = new ArrayList();
//        languages.add("java");
//        languages.add("aspectj");
//        languages.add("csharp");
//        languages.add("groovy");
//        instance.configureArchetypeCreation (
//            mavenProject,
//            Boolean.TRUE,
//            commandLineProperties,
//            propertyFile,
//            languages
//        );
//
//        Properties properties = loadProperties ( propertyFile );
//
//        assertEquals (
//            "org.apache.maven.archetypes",
//            properties.getProperty ( Constants.ARCHETYPE_GROUP_ID )
//        );
//        assertEquals (
//            "maven-archetype-test",
//            properties.getProperty ( Constants.ARCHETYPE_ARTIFACT_ID )
//        );
//        assertEquals ( "1.0", properties.getProperty ( Constants.ARCHETYPE_VERSION ) );
//        assertEquals ( "some.group.id", properties.getProperty ( Constants.GROUP_ID ) );
//        assertEquals ( "some-artifact-id", properties.getProperty ( Constants.ARTIFACT_ID ) );
//        assertEquals ( "1.0", properties.getProperty ( Constants.VERSION ) );
//        assertEquals (
//            "org.apache.maven.archetype",
//            properties.getProperty ( Constants.PACKAGE )
//        );
//
//        assertEquals ( 7, properties.size () );
//    }

    protected void tearDown()
        throws
        Exception
    {
        super.tearDown();
    }

    protected void setUp()
        throws
        Exception
    {
        super.setUp();

        localRepository =
            new DefaultArtifactRepository(
                "local",
                new File( getBasedir(), "target/test-classes/repositories/local" ).toURI()
                    .toString(),
                new DefaultRepositoryLayout()
            );

        repositories =
            Arrays.asList(
                new ArtifactRepository[]
                    {
                        new DefaultArtifactRepository(
                            "central",
                            new File( getBasedir(), "target/test-classes/repositories/central" )
                                .toURI().toString(),
                            new DefaultRepositoryLayout()
                        )
                    }
            );
    }

    private void copy( final File in,
                       final File out )
        throws
        IOException
    {
        assertTrue( !out.exists() || out.delete() );
        assertFalse( out.exists() );
        IOUtil.copy( new FileReader( in ), new FileWriter( out ) );
        assertTrue( out.exists() );
        assertTrue( in.exists() );
    }

// /*Commented on 2007 09 25
//    private void instanceDefined( DefaultArchetypeCreationConfigurator instance )
//        throws
//        IllegalAccessException
//    {
//        assertNotNull( instance );
//        assertNotNull( getVariableValueFromObject( instance, "archetypeCreationQueryer" ) );
//        assertNotNull( getVariableValueFromObject( instance, "archetypeFactory" ) );
//        assertNotNull( getVariableValueFromObject( instance, "archetypePropertiesManager" ) );
//        assertNotNull( getVariableValueFromObject( instance, "archetypeFilesResolver" ) );
//    }
// Commented on 2007 09 25*/

    private MavenProject loadProject( final File projectFile )
        throws
        ArtifactNotFoundException,
        Exception,
        ArtifactResolutionException,
        ProjectBuildingException
    {
        MavenProjectBuilder builder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );
        return builder.buildWithDependencies( projectFile, localRepository, null );
    }

    private Properties loadProperties( File propertyFile )
        throws
        IOException,
        FileNotFoundException
    {
        Properties properties = new Properties();
        properties.load( new FileInputStream( propertyFile ) );
        return properties;
    }

    private File getProjectFile( String project )
    {
        return new File( getBasedir(), "target/test-classes/projects/" + project + "/pom.xml" );
    }

    private File getProjectSampleFile( String project )
    {
        return
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/pom.xml.sample"
            );
    }

    private File getPropertiesFile( String project )
    {
        return
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/archetype.properties"
            );
    }

    private File getPropertiesSampleFile( final String project )
    {
        File propertyFileSample =
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/archetype.properties.sample"
            );
        return propertyFileSample;
    }
}
