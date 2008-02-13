package org.apache.maven.archetype.ui;

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
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.MockControl;

import java.io.IOException;
import java.util.Collections;

public class DefaultArchetypeSelectorTest
    extends PlexusTestCase
{
    private DefaultArchetypeSelector selector;

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

        MockControl control = MockControl.createControl( ArchetypeSelectionQueryer.class );

        ArchetypeSelectionQueryer queryer = (ArchetypeSelectionQueryer) control.getMock();
        // expect it to not be called

        control.replay();

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.TRUE, "" );

        control.verify();

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

        MockControl control = MockControl.createControl( ArchetypeSelectionQueryer.class );

        ArchetypeSelectionQueryer queryer = (ArchetypeSelectionQueryer) control.getMock();
        // expect it to not be called

        control.replay();

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.TRUE, "" );

        control.verify();

        assertEquals( "org.apache.maven.archetypes", request.getArchetypeGroupId() );
        assertEquals( "preset-artifactId", request.getArchetypeArtifactId() );
        assertEquals( "RELEASE", request.getArchetypeVersion() );
    }

    public void testArchetypeArtifactIdNotInRequest()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

        MockControl control = MockControl.createControl( ArchetypeSelectionQueryer.class );

        ArchetypeSelectionQueryer queryer = (ArchetypeSelectionQueryer) control.getMock();
        queryer.selectArchetype( Collections.EMPTY_MAP );
        control.setMatcher( MockControl.ALWAYS_MATCHER );
        Archetype archetype = new Archetype();
        archetype.setArtifactId( "set-artifactId" );
        archetype.setGroupId( "set-groupId" );
        archetype.setVersion( "set-version" );
        control.setReturnValue( archetype );

        control.replay();

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.TRUE, "" );

        control.verify();

        assertEquals( "set-groupId", request.getArchetypeGroupId() );
        assertEquals( "set-artifactId", request.getArchetypeArtifactId() );
        assertEquals( "set-version", request.getArchetypeVersion() );
    }
}
