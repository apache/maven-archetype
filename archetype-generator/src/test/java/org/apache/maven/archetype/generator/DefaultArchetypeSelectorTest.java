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

public class DefaultArchetypeSelectorTest
    extends AbstractMojoTestCase
{
    ArtifactRepository localRepository;
    List repositories;

    public void testBatchModeNoPropertyDefined()
        throws
        Exception
    {
        System.out.println( "testBatchModeNoPropertyDefined" );

        String archetypeGroupId = null;
        String archetypeArtifactId = null;
        String archetypeVersion = null;

        Boolean interactiveMode = Boolean.FALSE;
        String project = "select-1";
        File propertyFile = getPropertiesFile( project );
        assertTrue( !propertyFile.exists() || propertyFile.delete() );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );
        instanceDefined( instance );

        assertFalse( propertyFile.exists() );

        try
        {
            instance.selectArchetype(
                archetypeGroupId,
                archetypeArtifactId,
                archetypeVersion,
                interactiveMode,
                propertyFile,
                archetypeRegistryFile,
                localRepository,
                repositories
            );

            fail( "Exception must be thrown" );
        }
        catch ( Exception e )
        {
            assertEquals(
                "Exception not correct",
                "The archetype is not defined",
                e.getMessage()
            );
            assertFalse( propertyFile.exists() );
        }
    }

    public void testBatchModePropertiesDefinedInFile()
        throws
        Exception
    {
        System.out.println( "testBatchModePropertiesDefinedInFile" );

        String archetypeGroupId = null;
        String archetypeArtifactId = null;
        String archetypeVersion = null;

        Boolean interactiveMode = Boolean.FALSE;

        String project = "select-2";
        File propertyFile = getPropertiesFile( project );
        File propertyFileSample = getPropertiesSampleFile( project );
        copy( propertyFileSample, propertyFile );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertTrue( propertyFile.exists() );

        instance.selectArchetype(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            interactiveMode,
            propertyFile,
            archetypeRegistryFile,
            localRepository,
            repositories
        );

        assertTrue( propertyFile.exists() );

        Properties properties = loadProperties( propertyFile );
        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
    }

    public void testBatchModePropertiesDefinedInSystem()
        throws
        Exception
    {
        System.out.println( "testBatchModePropertiesDefinedInSystem" );

        String archetypeGroupId = "archetypes";
        String archetypeArtifactId = "basic";
        String archetypeVersion = "1.0";

        Boolean interactiveMode = Boolean.FALSE;

        String project = "select-3";
        File propertyFile = getPropertiesFile( project );
        assertTrue( !propertyFile.exists() || propertyFile.delete() );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertFalse( propertyFile.exists() );

        instance.selectArchetype(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            interactiveMode,
            propertyFile,
            archetypeRegistryFile,
            localRepository,
            repositories
        );

        assertTrue( propertyFile.exists() );

        Properties properties = loadProperties( propertyFile );
        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
    }

    public void testBatchModePropertiesOverrided()
        throws
        Exception
    {
        System.out.println( "testBatchModePropertiesOverrided" );

        String archetypeGroupId = "archetypes";
        String archetypeArtifactId = "dont-exist";
        String archetypeVersion = "1.0";

        Boolean interactiveMode = Boolean.FALSE;

        String project = "select-4";
        File propertyFile = getPropertiesFile( project );
        File propertyFileSample = getPropertiesSampleFile( project );
        copy( propertyFileSample, propertyFile );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertTrue( propertyFile.exists() );

        try
        {
            instance.selectArchetype(
                archetypeGroupId,
                archetypeArtifactId,
                archetypeVersion,
                interactiveMode,
                propertyFile,
                archetypeRegistryFile,
                localRepository,
                repositories
            );

            fail( "Exception must be thrown" );
        }
        catch ( Exception e )
        {
            assertEquals(
                "Exception not correct",
                "The desired archetype does not exist (" + archetypeGroupId + ":"
                    + archetypeArtifactId + ":" + archetypeVersion + ")",
                e.getMessage()
            );
            assertTrue( propertyFile.exists() );

            Properties properties = loadProperties( propertyFile );
            assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
            assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
            assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
        }
    }

    public void testInteractiveModePropertiesDefinedInFileAndAskedWithoutConfirmation()
        throws
        Exception
    {
        System.out.println(
            "testInteractiveModePropertiesDefinedInFileAndAskedWithoutConfirmation"
        );

        String archetypeGroupId = null;
        String archetypeArtifactId = null;
        String archetypeVersion = null;

        Boolean interactiveMode = Boolean.TRUE;

        String project = "select-8";
        File propertyFile = getPropertiesFile( project );
        File propertyFileSample = getPropertiesSampleFile( project );
        copy( propertyFileSample, propertyFile );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertTrue( propertyFile.exists() );

        MockPrompter prompter = new MockPrompter();
        prompter.addAnswer( "1" );
        prompter.addAnswer( "N" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "Y" );
        setVariableValueToObject(
            getVariableValueFromObject( instance, "archetypeSelectionQueryer" ),
            "prompter",
            prompter
        );

        instance.selectArchetype(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            interactiveMode,
            propertyFile,
            archetypeRegistryFile,
            localRepository,
            repositories
        );

        assertTrue( propertyFile.exists() );

        Properties properties = loadProperties( propertyFile );
        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
    }

    public void testInteractiveModePropertiesDefinedInFileWithoutConfirmation()
        throws
        Exception
    {
        System.out.println( "testInteractiveModePropertiesDefinedInFileWithoutConfirmation" );

        String archetypeGroupId = "archetypes";
        String archetypeArtifactId = "basic";
        String archetypeVersion = "1.0";

        Boolean interactiveMode = Boolean.TRUE;

        String project = "select-7";
        File propertyFile = getPropertiesFile( project );
        File propertyFileSample = getPropertiesSampleFile( project );
        copy( propertyFileSample, propertyFile );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertTrue( propertyFile.exists() );

        MockPrompter prompter = new MockPrompter();
        prompter.addAnswer( "N" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "Y" );
        setVariableValueToObject(
            getVariableValueFromObject( instance, "archetypeSelectionQueryer" ),
            "prompter",
            prompter
        );

        instance.selectArchetype(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            interactiveMode,
            propertyFile,
            archetypeRegistryFile,
            localRepository,
            repositories
        );

        assertTrue( propertyFile.exists() );

        Properties properties = loadProperties( propertyFile );
        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
    }

    public void testInteractiveModePropertiesDefinedInSystemAndAskedMissing()
        throws
        Exception
    {
        System.out.println( "testInteractiveModePropertiesDefinedInSystemAndAskedMissing" );

        String archetypeGroupId = "archetypes";
        String archetypeArtifactId = "basic";
        String archetypeVersion = null;

        Boolean interactiveMode = Boolean.TRUE;

        String project = "select-6";
        File propertyFile = getPropertiesFile( project );
        assertTrue( !propertyFile.exists() || propertyFile.delete() );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertFalse( propertyFile.exists() );

        MockPrompter prompter = new MockPrompter();
        prompter.addAnswer( "1" );
        prompter.addAnswer( "Y" );
        setVariableValueToObject(
            getVariableValueFromObject( instance, "archetypeSelectionQueryer" ),
            "prompter",
            prompter
        );

        instance.selectArchetype(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            interactiveMode,
            propertyFile,
            archetypeRegistryFile,
            localRepository,
            repositories
        );

        assertTrue( propertyFile.exists() );

        Properties properties = loadProperties( propertyFile );
        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
    }

    public void testInteractiveModePropertiesNotDefined()
        throws
        Exception
    {
        System.out.println( "testInteractiveModePropertiesNotDefined" );

        String archetypeGroupId = null;
        String archetypeArtifactId = null;
        String archetypeVersion = null;

        Boolean interactiveMode = Boolean.TRUE;

        String project = "select-5";
        File propertyFile = getPropertiesFile( project );
        assertTrue( !propertyFile.exists() || propertyFile.delete() );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertFalse( propertyFile.exists() );

        MockPrompter prompter = new MockPrompter();
        prompter.addAnswer( "2" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "Y" );
        setVariableValueToObject(
            getVariableValueFromObject( instance, "archetypeSelectionQueryer" ),
            "prompter",
            prompter
        );

        instance.selectArchetype(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            interactiveMode,
            propertyFile,
            archetypeRegistryFile,
            localRepository,
            repositories
        );

        assertTrue( propertyFile.exists() );

        Properties properties = loadProperties( propertyFile );
        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
    }


    public void testInteractiveModeUnknownGroup()
        throws
        Exception
    {
        System.out.println( "testInteractiveModeUnknownGroup" );

        String archetypeGroupId = null;
        String archetypeArtifactId = null;
        String archetypeVersion = null;

        Boolean interactiveMode = Boolean.TRUE;

        String project = "select-9";
        File propertyFile = getPropertiesFile( project );
        assertTrue( !propertyFile.exists() || propertyFile.delete() );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertFalse( propertyFile.exists() );

        MockPrompter prompter = new MockPrompter();
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "Y" );
        setVariableValueToObject(
            getVariableValueFromObject( instance, "archetypeSelectionQueryer" ),
            "prompter",
            prompter
        );

        instance.selectArchetype(
            archetypeGroupId,
            archetypeArtifactId,
            archetypeVersion,
            interactiveMode,
            propertyFile,
            archetypeRegistryFile,
            localRepository,
            repositories
        );

        assertTrue( propertyFile.exists() );

        Properties properties = loadProperties( propertyFile );
        assertEquals( "archetypes", properties.getProperty( "archetype.groupId" ) );
        assertEquals( "basic", properties.getProperty( "archetype.artifactId" ) );
        assertEquals( "1.0", properties.getProperty( "archetype.version" ) );
    }

    public void testInteractiveModeUnknownGroups()
        throws
        Exception
    {
        System.out.println( "testInteractiveModeUnknownGroups" );

        String archetypeGroupId = null;
        String archetypeArtifactId = null;
        String archetypeVersion = null;

        Boolean interactiveMode = Boolean.TRUE;

        String project = "select-10";
        File propertyFile = getPropertiesFile( project );
        assertTrue( !propertyFile.exists() || propertyFile.delete() );

        File archetypeRegistryFile = getRegistryFile( project );

        DefaultArchetypeSelector instance =
            (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );

        instanceDefined( instance );

        assertFalse( propertyFile.exists() );

        MockPrompter prompter = new MockPrompter();
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        prompter.addAnswer( "1" );
        setVariableValueToObject(
            getVariableValueFromObject( instance, "archetypeSelectionQueryer" ),
            "prompter",
            prompter
        );

        try
        {
            instance.selectArchetype(
                archetypeGroupId,
                archetypeArtifactId,
                archetypeVersion,
                interactiveMode,
                propertyFile,
                archetypeRegistryFile,
                localRepository,
                repositories
            );

            fail( "Exception must be thrown" );
        }
        catch ( Exception e )
        {
            assertEquals(
                "Exception not correct",
                "No registered group contain an archetype",
                e.getMessage()
            );
            assertFalse( propertyFile.exists() );
        }
    }

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

    private void instanceDefined( final DefaultArchetypeSelector instance )
        throws
        IllegalAccessException
    {
        assertNotNull( instance );
        assertNotNull( getVariableValueFromObject( instance, "archetypeArtifactManager" ) );
        assertNotNull( getVariableValueFromObject( instance, "archetypeFactory" ) );
        assertNotNull( getVariableValueFromObject( instance, "archetypePropertiesManager" ) );
        assertNotNull( getVariableValueFromObject( instance, "archetypeRegistryManager" ) );
        assertNotNull( getVariableValueFromObject( instance, "archetypeSelectionQueryer" ) );
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

    private File getRegistryFile( final String project )
    {
        File archetypeRegistryFile =
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/archetype.xml"
            );
        return archetypeRegistryFile;
    }
}
