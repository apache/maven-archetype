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
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.List;
import java.util.Map;

/**
 * User interaction component for archetype selection.
 * 
 * @todo this interface is bound to its implementation through the prompter exception
 */
public interface ArchetypeSelectionQueryer
{
    String ROLE = ArchetypeSelectionQueryer.class.getName();

    public Archetype selectArchetype( Map<String, List<Archetype>> map )
        throws PrompterException;

    boolean confirmSelection( ArchetypeDefinition archetypeDefinition )
        throws PrompterException;

    /**
     * Select an archetype from the given map.
     *
     * @param archetypes the archetypes to choose from
     * @param defaultDefinition the default archetype, if present in the map
     * @return the selected archetype
     * @throws org.codehaus.plexus.components.interactivity.PrompterException if there is a problem in making a
     *             selection
     */
    Archetype selectArchetype( Map<String, List<Archetype>> archetypes, ArchetypeDefinition defaultDefinition )
        throws PrompterException;
}
