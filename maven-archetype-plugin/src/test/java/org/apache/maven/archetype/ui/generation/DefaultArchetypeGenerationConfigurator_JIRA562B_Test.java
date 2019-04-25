package org.apache.maven.archetype.ui.generation;

import java.lang.reflect.Field;

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

import java.util.Properties;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.MockControl;
import junit.framework.Assert;

/**
 * Tests the ability to use variables in default fields in batch mode
 */
public class DefaultArchetypeGenerationConfigurator_JIRA562B_Test
    extends PlexusTestCase
{
    private DefaultArchetypeGenerationConfigurator configurator;
    private ArchetypeGenerationQueryer archetypeGenerationQueryer;
    private Prompter keyboardMock;
    private MockControl keyboardControl;

    public void setUp()
        throws Exception
    {
        super.setUp();

        configurator = (DefaultArchetypeGenerationConfigurator) lookup( ArchetypeGenerationConfigurator.ROLE );
        archetypeGenerationQueryer = (DefaultArchetypeGenerationQueryer) lookup ( ArchetypeGenerationQueryer.class );

        ProjectBuildingRequest buildingRequest = null;
        
        MockControl control = MockControl.createControl( ArchetypeArtifactManager.class );
        control.setDefaultMatcher( MockControl.ALWAYS_MATCHER );

        ArchetypeArtifactManager manager = (ArchetypeArtifactManager) control.getMock();
        manager.exists( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null, buildingRequest );
        control.setReturnValue( true );
        manager.isFileSetArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null, buildingRequest );
        control.setReturnValue( true );
        manager.isOldArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, null, buildingRequest );
        control.setReturnValue( false );
        manager.getFileSetArchetypeDescriptor( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null,
                                               null, null, buildingRequest );
        ArchetypeDescriptor descriptor = new ArchetypeDescriptor();

        RequiredProperty cycled1 = new RequiredProperty();
        cycled1.setKey("cycled1");
        cycled1.setDefaultValue("${cycled2}");
        RequiredProperty cycled2 = new RequiredProperty();
        cycled2.setKey("cycled2");
        cycled2.setDefaultValue("${cycled1}");       
        
        descriptor.addRequiredProperty( cycled1 );
        descriptor.addRequiredProperty( cycled2 );
        control.setReturnValue( descriptor );
        control.replay();
        configurator.setArchetypeArtifactManager( manager );   
        
        keyboardControl = MockControl.createControl( Prompter.class );   
        keyboardControl.setDefaultMatcher( MockControl.EQUALS_MATCHER );
        keyboardMock = (Prompter) keyboardControl.getMock();
        
        Field archetypeGenerationQueryerField = DefaultArchetypeGenerationConfigurator.class.getDeclaredField("archetypeGenerationQueryer");
        archetypeGenerationQueryerField.setAccessible(true);
        archetypeGenerationQueryerField.set(configurator, archetypeGenerationQueryer);
        
        Field prompterField = DefaultArchetypeGenerationQueryer.class.getDeclaredField("prompter");
        prompterField.setAccessible(true);
        prompterField.set(archetypeGenerationQueryer, keyboardMock);
        
    }   

    public void testJIRA_562_PropertiesReferringEachOtherIndirectlyWithLoops() throws Exception
    {
                
        // Reproduce maven archetype generation (interactive mode)
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "gid" );
        request.setArchetypeArtifactId( "aid" );
        request.setArchetypeVersion( "ver" );
        
        Properties properties = new Properties();
        
                        
        // Simulate interactive mode to force param ordering
        try
        {
            configurator.configureArchetype( request, Boolean.TRUE, properties );
        }
        catch ( ArchetypeNotConfigured e)
        {
            assertEquals("Archetype gid:aid:ver evaluated transitive dependencies :(" 
                        + DefaultArchetypeGenerationConfigurator.MAX_TRANSITIVE_CYCLES + ") times. "
                        + "Did you may define loop cycles?", e.getMessage());
        }
    }      
}