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
import org.easymock.AbstractMatcher;
import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_GROUPID, request.getArchetypeGroupId() );
        assertEquals( "preset-artifactId", request.getArchetypeArtifactId() );
        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_VERSION, request.getArchetypeVersion() );
    }

    public void testArchetypeArtifactIdNotInRequest()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

        MockControl control = MockControl.createControl( ArchetypeSelectionQueryer.class );

        ArchetypeSelectionQueryer queryer = (ArchetypeSelectionQueryer) control.getMock();
        queryer.selectArchetype( Collections.<String, List<Archetype>> emptyMap(), new ArchetypeDefinition() );
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

    public void testArchetypeNotInRequestDefaultsInBatchMode()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

        MockControl control = MockControl.createControl( ArchetypeSelectionQueryer.class );

        ArchetypeSelectionQueryer queryer = (ArchetypeSelectionQueryer) control.getMock();
        // expect it to not be called

        control.replay();

        selector.setArchetypeSelectionQueryer( queryer );

        selector.selectArchetype( request, Boolean.FALSE, "" );

        control.verify();

        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_GROUPID, request.getArchetypeGroupId() );
        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_ARTIFACTID, request.getArchetypeArtifactId() );
        assertEquals( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_VERSION, request.getArchetypeVersion() );
    }

    public void testArchetypeNotInRequestDefaults()
        throws PrompterException, IOException, UnknownGroup, ArchetypeSelectionFailure, UnknownArchetype,
        ArchetypeNotDefined
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();

        MockControl control = MockControl.createControl( ArchetypeSelectionQueryer.class );

        ArchetypeSelectionQueryer queryer = (ArchetypeSelectionQueryer) control.getMock();
        queryer.selectArchetype( Collections.<String, List<Archetype>> emptyMap(), createDefaultArchetypeDefinition() );
        control.setMatcher( createArgumentMatcher() );
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

    private ArchetypeDefinition createDefaultArchetypeDefinition()
    {
        ArchetypeDefinition definition = new ArchetypeDefinition();
        definition.setGroupId( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_GROUPID );
        definition.setArtifactId( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_ARTIFACTID );
        definition.setVersion( DefaultArchetypeSelector.DEFAULT_ARCHETYPE_VERSION );
        return definition;
    }

    private static ArgumentsMatcher createArgumentMatcher()
    {
        return new AbstractMatcher()
        {
            protected boolean argumentMatches( Object o, Object o1 )
            {
                return !( o instanceof ArchetypeDefinition ) ||
                    compareArchetypeDefinition( (ArchetypeDefinition) o, (ArchetypeDefinition) o1 );
            }

            private boolean compareArchetypeDefinition( ArchetypeDefinition o, ArchetypeDefinition o1 )
            {
                if ( o1 == o )
                {
                    return true;
                }
                if ( o == null )
                {
                    return false;
                }

                if ( o1.getArtifactId() != null
                    ? !o1.getArtifactId().equals( o.getArtifactId() )
                    : o.getArtifactId() != null )
                {
                    return false;
                }
                if ( o1.getGroupId() != null ? !o1.getGroupId().equals( o.getGroupId() ) : o.getGroupId() != null )
                {
                    return false;
                }
                if ( o1.getName() != null ? !o1.getName().equals( o.getName() ) : o.getName() != null )
                {
                    return false;
                }
                if ( o1.getVersion() != null ? !o1.getVersion().equals( o.getVersion() ) : o.getVersion() != null )
                {
                    return false;
                }
                if ( o1.getGoals() != null ? !o1.getGoals().equals( o.getGoals() ) : o.getGoals() != null )
                {
                    return false;
                }
                if ( o1.getRepository() != null
                    ? !o1.getRepository().equals( o.getRepository() )
                    : o.getRepository() != null )
                {
                    return false;
                }

                return true;
            }

            protected String argumentToString( Object o )
            {
                return o instanceof Map ? "..." : toString( (ArchetypeDefinition) o );
            }

            private String toString( ArchetypeDefinition archetypeDefinition )
            {
                return "groupId = " + archetypeDefinition.getGroupId() + ", " + "artifactId = " +
                    archetypeDefinition.getArtifactId() + ", " + "version = " + archetypeDefinition.getVersion();
            }
        };
    }
}
