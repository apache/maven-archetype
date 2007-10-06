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

package org.apache.maven.archetype.generator;

//import org.apache.maven.archetype.ui.ArchetypeGenerationConfigurator;
//import org.apache.maven.archetype.ui.DefaultArchetypeGenerationConfigurator;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
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

public class DefaultArchetypeGenerationConfiguratorTest
    extends AbstractMojoTestCase
{
    ArtifactRepository localRepository;
    List repositories;

    public void testNothing()
    {
        //TODO: All the tests were commented out Because the tested classes are beeing moved for now and will be removed soon
    }
    
// /*Commented on 2007 09 25
//    public void testBatchModeArchetypeNotDefined()
//        throws
//        Exception
//    {
//        System.out.println( "testBatchModeArchetypeNotDefined" );
//
//        String archetypeGroupId = null;
//        String archetypeArtifactId = null;
//        String archetypeVersion = null;
//
//        Boolean interactiveMode = Boolean.FALSE;
//
//        String project = "configure-1";
//        File propertyFile = getPropertiesFile( project );
//        assertTrue( !propertyFile.exists() || propertyFile.delete() );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        assertFalse( propertyFile.exists() );
//
//        try
//        {
//            instance.configureArchetype(
//                interactiveMode,
//                propertyFile,
//                System.getProperties(),
//                localRepository,
//                repositories
//            );
//
//            fail( "Exception must be thrown" );
//        }
//        catch ( FileNotFoundException e )
//        {
//            assertFalse( propertyFile.exists() );
//        }
//    }
//
//    public void testBatchModeOldArchetype()
//        throws
//        Exception
//    {
//        System.out.println( "testBatchModeOldArchetype" );
//
//        String archetypeGroupId = null;
//        String archetypeArtifactId = null;
//        String archetypeVersion = null;
//
//        Boolean interactiveMode = Boolean.FALSE;
//
//        String project = "configure-7";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        Properties systemProperties = new Properties();
//        systemProperties.setProperty( "groupId", "system-value" );
//        systemProperties.setProperty( "artifactId", "system-value" );
//        systemProperties.setProperty( "version", "system-value" );
//        systemProperties.setProperty( "package", "system-value" );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        assertTrue( propertyFile.exists() );
//
//        instance.configureArchetype(
//            interactiveMode,
//            propertyFile,
//            systemProperties,
//            localRepository,
//            repositories
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        Properties properties = loadProperties( propertyFile );
//        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
//        assertEquals( "old", properties.getProperty( "archetype.artifactId" ) );
//        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
//
//        assertEquals( "system-value", properties.getProperty( "groupId" ) );
//        assertEquals( "system-value", properties.getProperty( "artifactId" ) );
//        assertEquals( "system-value", properties.getProperty( "version" ) );
//        assertEquals( "system-value", properties.getProperty( "package" ) );
//    }
//
//    public void testBatchModePropertiesDefinedInFile()
//        throws
//        Exception
//    {
//        System.out.println( "testBatchModePropertiesDefinedInSytem" );
//
//        String archetypeGroupId = null;
//        String archetypeArtifactId = null;
//        String archetypeVersion = null;
//
//        Boolean interactiveMode = Boolean.FALSE;
//
//        String project = "configure-4";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        assertTrue( propertyFile.exists() );
//
//        instance.configureArchetype(
//            interactiveMode,
//            propertyFile,
//            System.getProperties(),
//            localRepository,
//            repositories
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        Properties properties = loadProperties( propertyFile );
//        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
//        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
//        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
//
//        assertEquals( "file-value", properties.getProperty( "groupId" ) );
//        assertEquals( "file-value", properties.getProperty( "artifactId" ) );
//        assertEquals( "file-value", properties.getProperty( "version" ) );
//        assertEquals( "file-value", properties.getProperty( "package" ) );
//        assertEquals( "file-value", properties.getProperty( "property-with-default-1" ) );
//        assertEquals( "file-value", properties.getProperty( "property-with-default-2" ) );
//        assertEquals( "file-value", properties.getProperty( "property-with-default-3" ) );
//        assertEquals( "file-value", properties.getProperty( "property-with-default-4" ) );
//        assertEquals( "file-value", properties.getProperty( "property-without-default-1" ) );
//        assertEquals( "file-value", properties.getProperty( "property-without-default-2" ) );
//        assertEquals( "file-value", properties.getProperty( "property-without-default-3" ) );
//        assertEquals( "file-value", properties.getProperty( "property-without-default-4" ) );
//    }
//
//    public void testBatchModePropertiesDefinedInSystem()
//        throws
//        Exception
//    {
//        System.out.println( "testBatchModePropertiesDefinedInSystem" );
//
//        String archetypeGroupId = null;
//        String archetypeArtifactId = null;
//        String archetypeVersion = null;
//
//        Boolean interactiveMode = Boolean.FALSE;
//
//        String project = "configure-3";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        Properties systemProperties = new Properties();
//        systemProperties.setProperty( "groupId", "system-value" );
//        systemProperties.setProperty( "artifactId", "system-value" );
//        systemProperties.setProperty( "version", "system-value" );
//        systemProperties.setProperty( "package", "system-value" );
//        systemProperties.setProperty( "property-with-default-1", "system-value" );
//        systemProperties.setProperty( "property-with-default-2", "system-value" );
//        systemProperties.setProperty( "property-with-default-3", "system-value" );
//        systemProperties.setProperty( "property-with-default-4", "system-value" );
//        systemProperties.setProperty( "property-without-default-1", "system-value" );
//        systemProperties.setProperty( "property-without-default-2", "system-value" );
//        systemProperties.setProperty( "property-without-default-3", "system-value" );
//        systemProperties.setProperty( "property-without-default-4", "system-value" );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        assertTrue( propertyFile.exists() );
//
//        instance.configureArchetype(
//            interactiveMode,
//            propertyFile,
//            systemProperties,
//            localRepository,
//            repositories
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        Properties properties = loadProperties( propertyFile );
//        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
//        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
//        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
//
//        assertEquals( "system-value", properties.getProperty( "groupId" ) );
//        assertEquals( "system-value", properties.getProperty( "artifactId" ) );
//        assertEquals( "system-value", properties.getProperty( "version" ) );
//        assertEquals( "system-value", properties.getProperty( "package" ) );
//        assertEquals( "system-value", properties.getProperty( "property-with-default-1" ) );
//        assertEquals( "system-value", properties.getProperty( "property-with-default-2" ) );
//        assertEquals( "system-value", properties.getProperty( "property-with-default-3" ) );
//        assertEquals( "system-value", properties.getProperty( "property-with-default-4" ) );
//        assertEquals( "system-value", properties.getProperty( "property-without-default-1" ) );
//        assertEquals( "system-value", properties.getProperty( "property-without-default-2" ) );
//        assertEquals( "system-value", properties.getProperty( "property-without-default-3" ) );
//        assertEquals( "system-value", properties.getProperty( "property-without-default-4" ) );
//    }
//
//    public void testBatchModePropertiesNotDefined()
//        throws
//        Exception
//    {
//        System.out.println( "testBatchModePropertiesNotDefined" );
//
//        String archetypeGroupId = "archetypes";
//        String archetypeArtifactId = "basic";
//        String archetypeVersion = "1.0";
//
//        Boolean interactiveMode = Boolean.FALSE;
//
//        String project = "configure-2";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        assertTrue( propertyFile.exists() );
//
//        try
//        {
//            instance.configureArchetype(
//                interactiveMode,
//                propertyFile,
//                System.getProperties(),
//                localRepository,
//                repositories
//            );
//
//            fail( "Exception must be thrown" );
//        }
//        catch ( ArchetypeNotConfigured e )
//        {
//            assertEquals(
//                "Exception not correct",
//                "The archetype is not configurated",
//                e.getMessage()
//            );
//        }
//    }
//
//    public void testInteractiveModeFileSetArchetype()
//        throws
//        Exception
//    {
//        System.out.println( "testInteractiveModeFileSetArchetype" );
//
//        String archetypeGroupId = "archetypes";
//        String archetypeArtifactId = "fileset";
//        String archetypeVersion = "1.0";
//
//        Boolean interactiveMode = Boolean.TRUE;
//
//        String project = "configure-9";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        MockPrompter prompter = new MockPrompter();
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "N" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "Y" );
//        setVariableValueToObject(
//            getVariableValueFromObject( instance, "archetypeGenerationQueryer" ),
//            "prompter",
//            prompter
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        instance.configureArchetype(
//            interactiveMode,
//            propertyFile,
//            System.getProperties(),
//            localRepository,
//            repositories
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        Properties properties = loadProperties( propertyFile );
//        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
//        assertEquals( "fileset", properties.getProperty( "archetype.artifactId" ) );
//        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
//
//        assertEquals( "user-value2", properties.getProperty( "groupId" ) );
//        assertEquals( "user-value2", properties.getProperty( "artifactId" ) );
//        assertEquals( "user-value2", properties.getProperty( "version" ) );
//        assertEquals( "user-value2", properties.getProperty( "package" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-1" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-2" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-3" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-4" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-1" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-2" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-3" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-4" ) );
//    }
//
//    public void testInteractiveModeOldArchetype()
//        throws
//        Exception
//    {
//        System.out.println( "testInteractiveModePropertiesNotDefined" );
//
//        String archetypeGroupId = "archetypes";
//        String archetypeArtifactId = "old";
//        String archetypeVersion = "1.0";
//
//        Boolean interactiveMode = Boolean.TRUE;
//
//        String project = "configure-8";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        MockPrompter prompter = new MockPrompter();
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "Y" );
//        setVariableValueToObject(
//            getVariableValueFromObject( instance, "archetypeGenerationQueryer" ),
//            "prompter",
//            prompter
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        instance.configureArchetype(
//            interactiveMode,
//            propertyFile,
//            System.getProperties(),
//            localRepository,
//            repositories
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        Properties properties = loadProperties( propertyFile );
//        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
//        assertEquals( "old", properties.getProperty( "archetype.artifactId" ) );
//        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
//
//        assertEquals( "user-value", properties.getProperty( "groupId" ) );
//        assertEquals( "user-value", properties.getProperty( "artifactId" ) );
//        assertEquals( "user-value", properties.getProperty( "version" ) );
//        assertEquals( "user-value", properties.getProperty( "package" ) );
//    }
//
//    public void testInteractiveModePropertiesNotDefined()
//        throws
//        Exception
//    {
//        System.out.println( "testInteractiveModePropertiesNotDefined" );
//
//        String archetypeGroupId = "archetypes";
//        String archetypeArtifactId = "basic";
//        String archetypeVersion = "1.0";
//
//        Boolean interactiveMode = Boolean.TRUE;
//
//        String project = "configure-5";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        MockPrompter prompter = new MockPrompter();
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "N" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "user-value2" );
//        prompter.addAnswer( "Y" );
//        setVariableValueToObject(
//            getVariableValueFromObject( instance, "archetypeGenerationQueryer" ),
//            "prompter",
//            prompter
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        instance.configureArchetype(
//            interactiveMode,
//            propertyFile,
//            System.getProperties(),
//            localRepository,
//            repositories
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        Properties properties = loadProperties( propertyFile );
//        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
//        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
//        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
//
//        assertEquals( "user-value2", properties.getProperty( "groupId" ) );
//        assertEquals( "user-value2", properties.getProperty( "artifactId" ) );
//        assertEquals( "user-value2", properties.getProperty( "version" ) );
//        assertEquals( "user-value2", properties.getProperty( "package" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-1" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-2" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-3" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-with-default-4" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-1" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-2" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-3" ) );
//        assertEquals( "user-value2", properties.getProperty( "property-without-default-4" ) );
//    }
//
//    public void testInteractiveModePropertiesOverrided()
//        throws
//        Exception
//    {
//        System.out.println( "testInteractiveModePropertiesOverrided" );
//
//        String archetypeGroupId = "archetypes";
//        String archetypeArtifactId = "basic";
//        String archetypeVersion = "1.0";
//
//        Boolean interactiveMode = Boolean.TRUE;
//
//        String project = "configure-6";
//        File propertyFile = getPropertiesFile( project );
//        File propertyFileSample = getPropertiesSampleFile( project );
//        copy( propertyFileSample, propertyFile );
//
//        DefaultArchetypeGenerationConfigurator instance =
//            (DefaultArchetypeGenerationConfigurator) lookup(
//                ArchetypeGenerationConfigurator.ROLE
//            );
//        instanceDefined( instance );
//
//        MockPrompter prompter = new MockPrompter();
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "user-value" );
//        prompter.addAnswer( "Y" );
//        setVariableValueToObject(
//            getVariableValueFromObject( instance, "archetypeGenerationQueryer" ),
//            "prompter",
//            prompter
//        );
//
//        Properties systemProperties = new Properties();
//        systemProperties.setProperty( "groupId", "system-value" );
//        systemProperties.setProperty( "version", "system-value" );
//        systemProperties.setProperty( "property-with-default-1", "system-value" );
//        systemProperties.setProperty( "property-with-default-3", "system-value" );
//        systemProperties.setProperty( "property-without-default-1", "system-value" );
//        systemProperties.setProperty( "property-without-default-3", "system-value" );
//
//        assertTrue( propertyFile.exists() );
//
//        instance.configureArchetype(
//            interactiveMode,
//            propertyFile,
//            systemProperties,
//            localRepository,
//            repositories
//        );
//
//        assertTrue( propertyFile.exists() );
//
//        Properties properties = loadProperties( propertyFile );
//        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
//        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
//        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
//
//        assertEquals( "system-value", properties.getProperty( "groupId" ) );
//        assertEquals( "file-value", properties.getProperty( "artifactId" ) );
//        assertEquals( "system-value", properties.getProperty( "version" ) );
//        assertEquals( "user-value", properties.getProperty( "package" ) );
//
//        assertEquals( "system-value", properties.getProperty( "property-with-default-1" ) );
//        assertEquals( "file-value", properties.getProperty( "property-with-default-2" ) );
//        assertEquals( "system-value", properties.getProperty( "property-with-default-3" ) );
//        assertEquals( "default-value", properties.getProperty( "property-with-default-4" ) );
//
//        assertEquals( "system-value", properties.getProperty( "property-without-default-1" ) );
//        assertEquals( "file-value", properties.getProperty( "property-without-default-2" ) );
//        assertEquals( "system-value", properties.getProperty( "property-without-default-3" ) );
//        assertEquals( "user-value", properties.getProperty( "property-without-default-4" ) );
//    }
// Commented on 2007 09 25*/

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
        IOException,
        FileNotFoundException
    {
        assertTrue( !out.exists() || out.delete() );
        assertFalse( out.exists() );
        IOUtil.copy( new FileReader( in ), new FileWriter( out ) );
        assertTrue( out.exists() );
        assertTrue( in.exists() );
    }

// /*Commented on 2007 09 25
//    private void instanceDefined( DefaultArchetypeGenerationConfigurator instance )
//        throws
//        IllegalAccessException
//    {
//        assertNotNull( instance );
//        assertNotNull( getVariableValueFromObject( instance, "archetypeArtifactManager" ) );
//        assertNotNull( getVariableValueFromObject( instance, "archetypeFactory" ) );
//        assertNotNull( getVariableValueFromObject( instance, "archetypeGenerationQueryer" ) );
//        assertNotNull( getVariableValueFromObject( instance, "archetypePropertiesManager" ) );
//    }
// Commented on 2007 09 25*/

    private Properties loadProperties( File propertyFile )
        throws
        IOException,
        FileNotFoundException
    {
        Properties properties = new Properties();
        properties.load( new FileInputStream( propertyFile ) );
        return properties;
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
