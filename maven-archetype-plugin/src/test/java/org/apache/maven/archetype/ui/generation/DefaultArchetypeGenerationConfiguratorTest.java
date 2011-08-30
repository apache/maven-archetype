package org.apache.maven.archetype.ui.generation;

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

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.ArchetypeGenerationConfigurationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.ArchetypeSelectionFailure;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.exception.UnknownGroup;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.MockControl;

import java.io.IOException;
import java.util.Properties;

/**
 * @todo probably testing a little deep, could just test ArchetypeConfiguration
 */
public class DefaultArchetypeGenerationConfiguratorTest
    extends PlexusTestCase
{
    private DefaultArchetypeGenerationConfigurator configurator;

    public void setUp()
        throws Exception
    {
        super.setUp();

        configurator = (DefaultArchetypeGenerationConfigurator) lookup( ArchetypeGenerationConfigurator.ROLE );

        MockControl control = MockControl.createControl( ArchetypeArtifactManager.class );
        control.setDefaultMatcher( MockControl.ALWAYS_MATCHER );

        ArchetypeArtifactManager manager = (ArchetypeArtifactManager) control.getMock();
        manager.exists( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null );
        control.setReturnValue( true );
        manager.isFileSetArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null );
        control.setReturnValue( false );
        manager.isOldArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null );
        control.setReturnValue( true );
        manager.getOldArchetypeDescriptor( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null,
                                               null, null );
        control.setReturnValue( new ArchetypeDescriptor() );
        control.replay();
        configurator.setArchetypeArtifactManager( manager );
    }

    public void testOldArchetypeGeneratedFieldsInRequestBatchMode()
        throws PrompterException, ArchetypeGenerationConfigurationFailure, IOException, ArchetypeNotConfigured,
        UnknownArchetype, ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();
        properties.setProperty( "groupId", "preset-groupId" );
        properties.setProperty( "artifactId", "preset-artifactId" );
        properties.setProperty( "version", "preset-gen-version" );
        properties.setProperty( "package", "preset-gen-package" );

        configurator.configureArchetype( request, Boolean.FALSE, properties );

        assertEquals( "preset-groupId", request.getGroupId() );
        assertEquals( "preset-artifactId", request.getArtifactId() );
        assertEquals( "preset-gen-version", request.getVersion() );
        assertEquals( "preset-gen-package", request.getPackage() );
    }

    public void testOldArchetypeGeneratedFieldsDefaultsBatchMode()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined, ArchetypeGenerationConfigurationFailure, ArchetypeNotConfigured
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();
        properties.setProperty( "groupId", "preset-groupId" );
        properties.setProperty( "artifactId", "preset-artifactId" );

        configurator.configureArchetype( request, Boolean.FALSE, properties );

        assertEquals( "preset-groupId", request.getGroupId() );
        assertEquals( "preset-artifactId", request.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", request.getVersion() );
        assertEquals( "preset-groupId", request.getPackage() );
    }

    // TODO: should test this in interactive mode to check for prompting
    public void testOldArchetypeGeneratedFieldsDefaultsMissingGroupId()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined, ArchetypeGenerationConfigurationFailure, ArchetypeNotConfigured
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();
        properties.setProperty( "artifactId", "preset-artifactId" );

        try
        {
            configurator.configureArchetype( request, Boolean.FALSE, properties );
            fail( "An exception must be thrown" );
        }
        catch ( ArchetypeNotConfigured e )
        {

        }
    }
}