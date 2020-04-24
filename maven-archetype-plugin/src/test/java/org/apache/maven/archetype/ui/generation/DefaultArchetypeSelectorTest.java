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
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.ArchetypeSelectionFailure;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.exception.UnknownGroup;
import org.apache.maven.archetype.ui.ArchetypeDefinition;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.EasyMock;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DefaultArchetypeSelectorTest
    extends PlexusTestCase
{
    private DefaultArchetypeSelector selector;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        selector = (DefaultArchetypeSelector) lookup( ArchetypeSelector.ROLE );
    }

    public void testArchetypeCoordinatesInRequest()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeArtifactId( "preset-artifactId" );
        request.setArchetypeGroupId( "preset-groupId" );
        request.setArchetypeVersion( "preset-version" );

        ArchetypeSelectionQueryer queryer = EasyMock.createMock( ArchetypeSelectionQueryer.class );
        // expect it to not be called

        EasyMock.replay( queryer );

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.TRUE, "" );

        EasyMock.verify( queryer );

        assertEquals( "preset-groupId", request.getArchetypeGroupId() );
        assertEquals( "preset-artifactId", request.getArchetypeArtifactId() );
        assertEquals( "preset-version", request.getArchetypeVersion() );
    }

    public void testArchetypeArtifactIdInRequest()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeArtifactId( "preset-artifactId" );

        ArchetypeSelectionQueryer queryer = EasyMock.createMock( ArchetypeSelectionQueryer.class );
        // expect it to not be called

        EasyMock.replay( queryer );

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.TRUE, "" );

        EasyMock.verify( queryer );

        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_GROUPID, request.getArchetypeGroupId() );
        assertEquals( "preset-artifactId", request.getArchetypeArtifactId() );
        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_VERSION, request.getArchetypeVersion() );
    }

    public void testArchetypeArtifactIdNotInRequest()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

        ArchetypeSelectionQueryer queryer = EasyMock.createMock( ArchetypeSelectionQueryer.class );
                
        Archetype archetype = new Archetype();
        archetype.setArtifactId( "set-artifactId" );
        archetype.setGroupId( "set-groupId" );
        archetype.setVersion( "set-version" );
        ArchetypeDefinition y = EasyMock.anyObject();
        Map<String, List<Archetype>> x = EasyMock.anyObject();
        EasyMock.expect( queryer.selectArchetype( x , y ) ).andReturn( archetype );

        EasyMock.replay( queryer );

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.TRUE, "" );

        EasyMock.verify( queryer );

        assertEquals( "set-groupId", request.getArchetypeGroupId() );
        assertEquals( "set-artifactId", request.getArchetypeArtifactId() );
        assertEquals( "set-version", request.getArchetypeVersion() );
    }

    public void testArchetypeNotInRequestDefaultsInBatchMode()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

        ArchetypeSelectionQueryer queryer = EasyMock.createMock( ArchetypeSelectionQueryer.class );
        // expect it to not be called

        EasyMock.replay( queryer );

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.FALSE, "" );

        EasyMock.verify( queryer );

        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_GROUPID, request.getArchetypeGroupId() );
        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_ARTIFACTID, request.getArchetypeArtifactId() );
        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_VERSION, request.getArchetypeVersion() );
    }

    public void testArchetypeNotInRequestDefaults()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

        ArchetypeSelectionQueryer queryer = EasyMock.createMock( ArchetypeSelectionQueryer.class );
        Archetype archetype = new Archetype();
        archetype.setArtifactId( "set-artifactId" );
        archetype.setGroupId( "set-groupId" );
        archetype.setVersion( "set-version" );
        ArchetypeDefinition y = EasyMock.anyObject();
        Map<String, List<Archetype>> x = EasyMock.anyObject();
        EasyMock.expect( queryer.selectArchetype( x , y ) ).andReturn( archetype );

        EasyMock.replay( queryer );

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.TRUE, "" );

        EasyMock.verify( queryer );

        assertEquals( "set-groupId", request.getArchetypeGroupId() );
        assertEquals( "set-artifactId", request.getArchetypeArtifactId() );
        assertEquals( "set-version", request.getArchetypeVersion() );
    }

}
