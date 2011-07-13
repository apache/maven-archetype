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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.archetype.catalog.Archetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Olivier Lamy
 * @since 2.1
 */
public class ArchetypeSelectorUtils
{
    private ArchetypeSelectorUtils()
    {
        // no constructor for utility class
    }

    private static String extractGroupIdFromFilter( String filter )
    {
        return StringUtils.contains( filter, ':' ) ? StringUtils.substringBefore( filter, ":" ) : null;
    }

    private static String extractArtifactIdFromFilter( String filter )
    {
        // if no : the full text is considered as artifactId content
        return StringUtils.contains( filter, ':' ) ? StringUtils.substringAfter( filter, ":" ) : filter;
    }

    /**
     * apply some filtering on archetypes.
     * currently only on artifactId contains filter
     *
     * @param archetypesPerCatalog
     * @return
     */
    public static Map<String, List<Archetype>> getFilteredArchetypesByCatalog(
        Map<String, List<Archetype>> archetypesPerCatalog, String filter )
    {
        if ( archetypesPerCatalog == null || archetypesPerCatalog.isEmpty() )
        {
            return Collections.emptyMap();
        }
        Map<String, List<Archetype>> filtered =
            new LinkedHashMap<String, List<Archetype>>( archetypesPerCatalog.size() );

        for ( Map.Entry<String, List<Archetype>> entry : archetypesPerCatalog.entrySet() )
        {
            List<Archetype> archetypes = new ArrayList<Archetype>();

            for ( Archetype archetype : entry.getValue() )
            {
                String groupId = ArchetypeSelectorUtils.extractGroupIdFromFilter( filter );
                String artifactId = ArchetypeSelectorUtils.extractArtifactIdFromFilter( filter );

                if ( ( ( groupId == null ) || StringUtils.contains( archetype.getGroupId(), groupId ) )
                    && StringUtils.contains( archetype.getArtifactId(), artifactId ) )
                {
                    archetypes.add( archetype );
                }
            }

            if ( !archetypes.isEmpty() )
            {
                filtered.put( entry.getKey(), archetypes );
            }
        }

        return filtered;
    }

}
