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

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.ui.ArchetypeDefinition;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.AbstractMatcher;
import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultArchetypeSelectionQueryerTest
    extends PlexusTestCase
{
    private DefaultArchetypeSelectionQueryer queryer;

    public void setUp()
        throws Exception
    {
        super.setUp();

        queryer = (DefaultArchetypeSelectionQueryer) lookup( ArchetypeSelectionQueryer.ROLE );
    }

    public void testDefaultArchetypeInMapOtherSelection()
        throws PrompterException
    {
        Map<String, List<Archetype>> map = createDefaultArchetypeCatalog();

        MockControl control = MockControl.createControl( Prompter.class );
        Prompter prompter = (Prompter) control.getMock();
        prompter.prompt( "", "2" );
        control.setMatcher( createArgumentMatcher() );
        control.setReturnValue( "1" );
        queryer.setPrompter( prompter );

        control.replay();

        ArchetypeDefinition defaultDefinition = new ArchetypeDefinition();
        defaultDefinition.setGroupId( "default-groupId" );
        defaultDefinition.setArtifactId( "default-artifactId" );
        defaultDefinition.setVersion( "default-version" );
        Archetype archetype = queryer.selectArchetype( map, defaultDefinition );

        control.verify();

        assertEquals( "set-groupId", archetype.getGroupId() );
        assertEquals( "set-artifactId", archetype.getArtifactId() );
        assertEquals( "set-version", archetype.getVersion() );
    }

    public void testDefaultArchetypeInMapDefaultSelection()
        throws PrompterException
    {
        Map<String, List<Archetype>> map = createDefaultArchetypeCatalog();

        MockControl control = MockControl.createControl( Prompter.class );
        Prompter prompter = (Prompter) control.getMock();
        prompter.prompt( "", "2" );
        control.setMatcher( createArgumentMatcher() );
        control.setReturnValue( "2" );
        queryer.setPrompter( prompter );

        control.replay();

        ArchetypeDefinition defaultDefinition = new ArchetypeDefinition();
        defaultDefinition.setGroupId( "default-groupId" );
        defaultDefinition.setArtifactId( "default-artifactId" );
        defaultDefinition.setVersion( "default-version" );
        Archetype archetype = queryer.selectArchetype( map, defaultDefinition );

        control.verify();

        assertEquals( "default-groupId", archetype.getGroupId() );
        assertEquals( "default-artifactId", archetype.getArtifactId() );
        assertEquals( "default-version", archetype.getVersion() );
    }

    public void testDefaultArchetypeNotInMap()
        throws PrompterException
    {
        Map<String, List<Archetype>> map = createDefaultArchetypeCatalog();

        MockControl control = MockControl.createControl( Prompter.class );
        Prompter prompter = (Prompter) control.getMock();
        prompter.prompt( "" );
        control.setMatcher( createArgumentMatcher() );
        control.setReturnValue( "1" );
        queryer.setPrompter( prompter );

        control.replay();

        ArchetypeDefinition defaultDefinition = new ArchetypeDefinition();
        defaultDefinition.setGroupId( "invalid-groupId" );
        defaultDefinition.setArtifactId( "invalid-artifactId" );
        defaultDefinition.setVersion( "invalid-version" );
        Archetype archetype = queryer.selectArchetype( map, defaultDefinition );

        control.verify();

        assertEquals( "set-groupId", archetype.getGroupId() );
        assertEquals( "set-artifactId", archetype.getArtifactId() );
        assertEquals( "set-version", archetype.getVersion() );
    }

    public void testNoDefaultArchetype()
        throws PrompterException
    {
        Map<String, List<Archetype>> map = createDefaultArchetypeCatalog();

        MockControl control = MockControl.createControl( Prompter.class );
        Prompter prompter = (Prompter) control.getMock();
        prompter.prompt( "" );
        control.setMatcher( createArgumentMatcher() );
        control.setReturnValue( "1" );
        queryer.setPrompter( prompter );

        control.replay();

        Archetype archetype = queryer.selectArchetype( map );

        control.verify();

        assertEquals( "set-groupId", archetype.getGroupId() );
        assertEquals( "set-artifactId", archetype.getArtifactId() );
        assertEquals( "set-version", archetype.getVersion() );
    }

    public void testArchetypeFiltering()
        throws PrompterException
    {
        Map<String, List<Archetype>> map = createDefaultArchetypeCatalog();

        MockControl control = MockControl.createControl( Prompter.class );
        Prompter prompter = (Prompter) control.getMock();
        prompter.prompt( "" );
        control.setMatcher( createArgumentMatcher() );
        control.setReturnValue( "set-artifactId" );
        prompter.prompt( "" );
        control.setReturnValue( "1" );
        queryer.setPrompter( prompter );

        control.replay();

        Archetype archetype = queryer.selectArchetype( map );

        control.verify();

        assertEquals( "set-groupId", archetype.getGroupId() );
        assertEquals( "set-artifactId", archetype.getArtifactId() );
        assertEquals( "set-version", archetype.getVersion() );
    }

    private static Map<String, List<Archetype>> createDefaultArchetypeCatalog()
    {
        List<Archetype> list = new ArrayList<Archetype>();
        list.add( createArchetype( "set-groupId", "set-artifactId", "set-version" ) );
        list.add( createArchetype( "default-groupId", "default-artifactId", "default-version" ) );
        return Collections.singletonMap( "internal", list );
    }

    private static Archetype createArchetype( String groupId, String artifactId, String version )
    {
        Archetype a = new Archetype();
        a.setGroupId( groupId );
        a.setArtifactId( artifactId );
        a.setVersion( version );
        return a;
    }

    private static ArgumentsMatcher createArgumentMatcher()
    {
        return new AbstractMatcher()
        {
            private boolean isPromptString( Object o )
            {
                boolean value = false;
                if ( o instanceof String )
                {
                    String s = (String) o;
                    if ( s.startsWith( "Choose" ) )
                    {
                        // ignore the prompt
                        value = true;
                    }
                }
                return value;
            }

            protected boolean argumentMatches( Object o, Object o1 )
            {
                return isPromptString( o1 ) || super.argumentMatches( o, o1 );
            }

            protected String argumentToString( Object o )
            {
                return isPromptString( o ) ? "..." : super.argumentToString( o );
            }
        };
    }
}