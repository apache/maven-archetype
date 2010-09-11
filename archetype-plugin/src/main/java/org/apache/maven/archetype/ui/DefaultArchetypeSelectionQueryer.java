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

import org.apache.maven.archetype.catalog.Archetype;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @plexus.component */
public class DefaultArchetypeSelectionQueryer
    extends AbstractLogEnabled
    implements ArchetypeSelectionQueryer
{
    /** @plexus.requirement role-hint="archetype" */
    private Prompter prompter;

    public boolean confirmSelection( ArchetypeDefinition archetypeDefinition )
        throws PrompterException
    {
        String query =
            "Confirm archetype selection: \n" + archetypeDefinition.getGroupId() + "/" + archetypeDefinition.getName()
                + "\n";

        String answer = prompter.prompt( query, Arrays.asList( new String[] { "Y", "N" } ), "Y" );

        return "Y".equalsIgnoreCase( answer );
    }

    public Archetype selectArchetype( Map<String, List<Archetype>> catalogs )
        throws PrompterException
    {
        return selectArchetype( catalogs, null );
    }

    public Archetype selectArchetype( Map<String, List<Archetype>> catalogs, ArchetypeDefinition defaultDefinition )
        throws PrompterException
    {
        StringBuilder query = new StringBuilder( "Choose archetype:\n" );

        Map<String, List<Archetype>> archetypeAnswerMap = new HashMap<String, List<Archetype>>();
        Map<String, String> reversedArchetypeAnswerMap = new HashMap<String, String>();
        List<String> answers = new ArrayList<String>();
        List<Archetype> archetypeVersions;
        int counter = 1;
        int defaultSelection = 0;

        for ( Map.Entry<String, List<Archetype>> entry : catalogs.entrySet() )
        {
            String catalog = entry.getKey();

            for ( Archetype archetype : entry.getValue() )
            {
                String mapKey = String.valueOf( counter );

                String archetypeKey = archetype.getGroupId() + ":" + archetype.getArtifactId();

                if ( reversedArchetypeAnswerMap.containsKey( archetypeKey ) )
                {
                    mapKey = reversedArchetypeAnswerMap.get( archetypeKey );
                    archetypeVersions = archetypeAnswerMap.get( mapKey );
                }
                else
                {
                    archetypeVersions = new ArrayList<Archetype>();
                    archetypeAnswerMap.put( mapKey, archetypeVersions );
                    reversedArchetypeAnswerMap.put( archetypeKey, mapKey );

                    String description = archetype.getDescription();
                    if ( description == null )
                    {
                        description = "-";
                    }

                    query.append( mapKey + ": " + catalog + " -> " + archetype.getArtifactId() + " ("
                        + description + ")\n" );

                    answers.add( mapKey );

                    // the version is not tested. This is intentional.
                    if ( defaultDefinition != null && archetype.getGroupId().equals( defaultDefinition.getGroupId() )
                        && archetype.getArtifactId().equals( defaultDefinition.getArtifactId() ) )
                    {
                        defaultSelection = counter;
                    }

                    counter++;
                }

                archetypeVersions.add( archetype );
            }

        }

        query.append( "Choose a number: " );

        String answer;
        if ( defaultSelection == 0 )
        {
            answer = prompter.prompt( query.toString(), answers );
        }
        else
        {
            answer = prompter.prompt( query.toString(), answers, Integer.toString( defaultSelection ) );
        }

        archetypeVersions = archetypeAnswerMap.get( answer );

        if ( archetypeVersions.size() == 1 )
        {
            return archetypeVersions.get( 0 );
        }
        else
        {
            return selectVersion( archetypeVersions );
        }
    }

    private Archetype selectVersion( List<Archetype> archetypes )
        throws PrompterException
    {
        StringBuilder query = new StringBuilder( "Choose version: \n" );

        Map<String, Archetype> answerMap = new HashMap<String, Archetype>();
        List<String> answers = new ArrayList<String>();

        Collections.sort( archetypes, new Comparator<Archetype>()
        {
            public int compare( Archetype a1, Archetype a2 )
            {
                return a1.getVersion().compareTo( a2.getVersion() );
            }
        } );

        int counter = 1;
        for ( Archetype archetype : archetypes )
        {
            String mapKey = String.valueOf( counter );
            String archetypeVersion = archetype.getVersion();

            answerMap.put( mapKey, archetype );

            query.append( mapKey + ": " + archetypeVersion + "\n" );

            answers.add( mapKey );

            counter++;
        }

        query.append( "Choose a number: " );

        String answer = prompter.prompt( query.toString(), answers );

        return answerMap.get( answer );
    }

    public void setPrompter( Prompter prompter )
    {
        this.prompter = prompter;
    }
}
