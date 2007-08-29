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

import org.apache.maven.archetype.common.Archetype;
import org.apache.maven.archetype.common.ArchetypeDefinition;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @plexus.component
 */
public class DefaultArchetypeSelectionQueryer
extends AbstractLogEnabled
implements ArchetypeSelectionQueryer
{
    /**
     * @plexus.requirement
     */
    private Prompter prompter;

    public boolean confirmSelection ( ArchetypeDefinition archetypeDefinition )
    throws PrompterException
    {
        String query =
            "Confirm archetype selection: \n" + archetypeDefinition.getGroupId () + "/"
            + archetypeDefinition.getName () + "\n";

        String answer = prompter.prompt ( query, Arrays.asList ( new String[] { "Y", "N" } ), "Y" );

        return "Y".equalsIgnoreCase ( answer );
    }

    public Archetype selectArtifact ( List archetypes )
    throws PrompterException
    {
        String query = "Choose archetype:\n";
        Map answerMap = new HashMap ();
        List answers = new ArrayList ();
        Iterator archetypeIterator = archetypes.iterator ();
        int counter = 1;
        while ( archetypeIterator.hasNext () )
        {
            Archetype archetype = (Archetype) archetypeIterator.next ();

            answerMap.put ( "" + counter, archetype );
            query +=
                "" + counter + ": " + archetype.getName () + " (" + archetype.getGroupId () + ":"
                + archetype.getArtifactId () + ")\n";
            answers.add ( "" + counter );

            counter++;
        }
        query += "Choose a number: ";

        String answer = prompter.prompt ( query, answers );

        return (Archetype) answerMap.get ( answer );
    }

    public String selectGroup ( List groups )
    throws PrompterException
    {
        String query = "Choose group:\n";
        Map answerMap = new HashMap ();
        List answers = new ArrayList ();
        Iterator groupIterator = groups.iterator ();
        int counter = 1;
        while ( groupIterator.hasNext () )
        {
            String group = (String) groupIterator.next ();

            answerMap.put ( "" + counter, group );
            query += "" + counter + ": " + group + "\n";
            answers.add ( "" + counter );

            counter++;
        }
        query += "Choose a number: ";

        String answer = prompter.prompt ( query, answers );

        return (String) answerMap.get ( answer );
    }

    public String selectVersion ( List archetypeVersions )
    throws PrompterException
    {
        String query = "Choose version: \n";
        Map answerMap = new HashMap ();
        List answers = new ArrayList ();

        Iterator archetypeVersionsKeys = archetypeVersions.iterator ();
        int counter = 1;
        while ( archetypeVersionsKeys.hasNext () )
        {
            String archetypeVersion = (String) archetypeVersionsKeys.next ();

            answerMap.put ( "" + counter, archetypeVersion );
            query += "" + counter + ": " + archetypeVersion + "\n";
            answers.add ( "" + counter );

            counter++;
        }
        query += "Choose a number: ";

        String answer = prompter.prompt ( query, answers );

        return (String) answerMap.get ( answer );
    }
}
