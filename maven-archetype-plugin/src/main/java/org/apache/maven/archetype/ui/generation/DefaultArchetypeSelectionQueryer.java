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

import org.apache.commons.lang.math.NumberUtils;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.ui.ArchetypeDefinition;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

@Component( role = ArchetypeSelectionQueryer.class )
public class DefaultArchetypeSelectionQueryer
    extends AbstractLogEnabled
    implements ArchetypeSelectionQueryer
{
    @Requirement( hint = "archetype" )
    private Prompter prompter;

    public boolean confirmSelection( ArchetypeDefinition archetypeDefinition )
        throws PrompterException
    {
        String query =
            "Confirm archetype selection: \n" + archetypeDefinition.getGroupId() + "/" + archetypeDefinition.getName()
                + "\n";

        String answer = prompter.prompt( query, Arrays.asList( new String[]{ "Y", "N" } ), "Y" );

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
        Archetype selection = null;
        Map<String, List<Archetype>> filteredCatalogs = catalogs;

        do
        {
            StringBuilder query = new StringBuilder( "Choose archetype:\n" );

            Set<String> archetypeKeys = new HashSet<String>();
            List<String> answers = new ArrayList<String>();
            Map<String, Archetype> archetypeAnswerMap = new HashMap<String, Archetype>();

            int counter = 0;
            int defaultSelection = 0;

            for ( Map.Entry<String, List<Archetype>> entry : filteredCatalogs.entrySet() )
            {
                String catalog = entry.getKey();

                for ( Archetype archetype : entry.getValue() )
                {
                    String archetypeKey = archetype.getGroupId() + ":" + archetype.getArtifactId();

                    if ( !archetypeKeys.add( archetypeKey ) )
                    {
                        continue;
                    }

                    counter++;

                    String description = archetype.getDescription();
                    if ( description == null )
                    {
                        description = "-";
                    }

                    String answer = String.valueOf( counter );

                    query.append( answer + ": " + catalog + " -> " + archetype.getGroupId() + ":"
                        + archetype.getArtifactId() + " (" + description + ")\n" );

                    answers.add( answer );

                    archetypeAnswerMap.put( answer, archetype );

                    // the version is not tested. This is intentional.
                    if ( defaultDefinition != null && archetype.getGroupId().equals( defaultDefinition.getGroupId() )
                        && archetype.getArtifactId().equals( defaultDefinition.getArtifactId() ) )
                    {
                        defaultSelection = counter;
                    }
                }
            }

            if ( counter == 0 )
            {
                query.append( "   Your filter doesn't match any archetype (hint: enter to return to initial list)\n" );
            }

            query.append( "Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): " );

            String answer;
            if ( defaultSelection == 0 )
            {
                answer = prompter.prompt( query.toString() );
            }
            else
            {
                answer = prompter.prompt( query.toString(), Integer.toString( defaultSelection ) );
            }

            if ( NumberUtils.isNumber( answer ) )
            {
                selection = archetypeAnswerMap.get( answer );
            }
            else
            {
                // not a number so apply filter and ask again
                filteredCatalogs = ArchetypeSelectorUtils.getFilteredArchetypesByCatalog( catalogs, answer );
            }
        }
        while ( selection == null );

        return selectVersion( catalogs, selection.getGroupId(), selection.getArtifactId() );
    }

    private Archetype selectVersion( Map<String, List<Archetype>> catalogs, String groupId, String artifactId )
        throws PrompterException
    {
        SortedMap<ArtifactVersion, Archetype> archetypeVersionsMap = new TreeMap<ArtifactVersion, Archetype>();

        for ( Map.Entry<String, List<Archetype>> entry : catalogs.entrySet() )
        {
            for ( Archetype archetype : entry.getValue() )
            {
                if ( !groupId.equals( archetype.getGroupId() ) || !artifactId.equals( archetype.getArtifactId() ) )
                {
                    continue;
                }

                ArtifactVersion version = new DefaultArtifactVersion( archetype.getVersion() );

                // don't override the first catalog containing a defined version of the artifact
                if ( !archetypeVersionsMap.containsKey( version ) )
                {
                    archetypeVersionsMap.put( version, archetype );
                }
            }
        }

        if ( archetypeVersionsMap.size() == 1 )
        {
            return archetypeVersionsMap.values().iterator().next();
        }

        // let the user choose between available versions
        StringBuilder query = new StringBuilder( "Choose " + groupId + ":" + artifactId + " version: \n" );

        List<String> answers = new ArrayList<String>();
        Map<String, Archetype> answerMap = new HashMap<String, Archetype>();

        int counter = 1;
        String mapKey = null;

        for ( Map.Entry<ArtifactVersion, Archetype> entry : archetypeVersionsMap.entrySet() )
        {
            ArtifactVersion version = entry.getKey();
            Archetype archetype = entry.getValue();

            mapKey = String.valueOf( counter );

            query.append( mapKey + ": " + version + "\n" );

            answers.add( mapKey );

            answerMap.put( mapKey, archetype );

            counter++;
        }

        query.append( "Choose a number: " );

        Archetype archetype = null;

        do
        {
            String answer = prompter.prompt( query.toString(), answers, mapKey );

            archetype = answerMap.get( answer );
        }
        while ( archetype == null );

        return archetype;
    }

    public void setPrompter( Prompter prompter )
    {
        this.prompter = prompter;
    }
}
