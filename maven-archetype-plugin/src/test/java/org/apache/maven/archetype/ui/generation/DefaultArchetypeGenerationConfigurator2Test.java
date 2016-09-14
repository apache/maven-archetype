package org.apache.maven.archetype.ui.generation;

import java.io.IOException;
import java.util.Properties;

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
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.MockControl;

/**
 * Tests the ability to use variables in default fields in batch mode
 */
public class DefaultArchetypeGenerationConfigurator2Test
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
        control.setReturnValue( true );
        manager.isOldArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null );
        control.setReturnValue( false );
        manager.getFileSetArchetypeDescriptor( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null,
                                               null, null );
        ArchetypeDescriptor descriptor = new ArchetypeDescriptor();
        RequiredProperty groupId = new RequiredProperty();
        groupId.setKey( "groupId" );
        groupId.setDefaultValue( "com.example.${groupName}" );
        RequiredProperty artifactId = new RequiredProperty();
        artifactId.setKey( "artifactId" );
        artifactId.setDefaultValue( "${serviceName}" );
        RequiredProperty thePackage = new RequiredProperty();
        thePackage.setKey( "package" );
        thePackage.setDefaultValue( "com.example.${groupName}" );
        RequiredProperty groupName = new RequiredProperty();
        groupName.setKey( "groupName" );
        groupName.setDefaultValue( null );
        RequiredProperty serviceName = new RequiredProperty();
        serviceName.setKey( "serviceName" );
        serviceName.setDefaultValue( null );
        descriptor.addRequiredProperty( groupId );
        descriptor.addRequiredProperty( artifactId );
        descriptor.addRequiredProperty( thePackage );
        descriptor.addRequiredProperty( groupName );
        descriptor.addRequiredProperty( serviceName );
        control.setReturnValue( descriptor );
        control.replay();
        configurator.setArchetypeArtifactManager( manager );
    }
    
    public void testJIRA_509_FileSetArchetypeDefaultsWithVariables()
            throws PrompterException, ArchetypeGenerationConfigurationFailure, IOException, ArchetypeNotConfigured,
            UnknownArchetype, ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();
        properties.setProperty( "groupName", "myGroupName" );
        properties.setProperty( "serviceName", "myServiceName" );
        
        configurator.configureArchetype( request, Boolean.FALSE, properties );
        
        assertEquals( "com.example.myGroupName", request.getGroupId() );
        assertEquals( "myServiceName", request.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", request.getVersion() );
        assertEquals( "com.example.myGroupName", request.getPackage() );
    }
          
}