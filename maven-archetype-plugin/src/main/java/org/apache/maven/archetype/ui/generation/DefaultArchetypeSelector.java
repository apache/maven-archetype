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
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.ArchetypeSelectionFailure;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.exception.UnknownGroup;
import org.apache.maven.archetype.ui.ArchetypeDefinition;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component( role = ArchetypeSelector.class )
public class DefaultArchetypeSelector
    extends AbstractLogEnabled
    implements ArchetypeSelector
{
    static final String DEFAULT_ARCHETYPE_GROUPID = "org.apache.maven.archetypes";

    static final String DEFAULT_ARCHETYPE_VERSION = "1.0";

    static final String DEFAULT_ARCHETYPE_ARTIFACTID = "maven-archetype-quickstart";

    @Requirement
    private ArchetypeSelectionQueryer archetypeSelectionQueryer;

    @Requirement
    private ArchetypeManager archetypeManager;

    public void selectArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode, String catalogs )
        throws ArchetypeNotDefined, UnknownArchetype, UnknownGroup, IOException, PrompterException,
        ArchetypeSelectionFailure
    {
        ArchetypeDefinition definition = new ArchetypeDefinition( request );

        if ( definition.isDefined() && StringUtils.isNotEmpty( request.getArchetypeRepository() ) )
        {
            getLogger().info( "Archetype defined by properties" );
            return;
        }

        Map<String, List<Archetype>> archetypes = getArchetypesByCatalog( catalogs );

        if ( StringUtils.isNotBlank( request.getFilter() ) )
        {
            // applying some filtering depending on filter parameter
            archetypes = ArchetypeSelectorUtils.getFilteredArchetypesByCatalog( archetypes, request.getFilter() );
            if ( archetypes.isEmpty() )
            {
                getLogger().info( "Your filter doesn't match any archetype, so try again with another value." );
                return;
            }
        }

        if ( definition.isDefined() && StringUtils.isEmpty( request.getArchetypeRepository() ) )
        {
            Map.Entry<String, Archetype> found =
                findArchetype( archetypes, request.getArchetypeGroupId(), request.getArchetypeArtifactId() );

            if ( found != null )
            {
                String catalogKey = found.getKey();
                Archetype archetype = found.getValue();

                updateRepository( definition, archetype, catalogKey );

                getLogger().info( "Archetype repository missing. Using the one from " + archetype + " found in catalog "
                                      + catalogKey );
            }
            else
            {
                getLogger().warn(
                    "Archetype not found in any catalog. Falling back to central repository (http://repo1.maven.org/maven2)." );
                getLogger().warn(
                    "Use -DarchetypeRepository=<your repository> if archetype's repository is elsewhere." );

                definition.setRepository( "http://repo1.maven.org/maven2" );
            }
        }

        if ( !definition.isDefined() && definition.isPartiallyDefined() )
        {
            Map.Entry<String, Archetype> found =
                findArchetype( archetypes, request.getArchetypeGroupId(), request.getArchetypeArtifactId() );

            if ( found != null )
            {
                String catalogKey = found.getKey();
                Archetype archetype = found.getValue();

                updateDefinition( definition, archetype, catalogKey );

                getLogger().info( "Archetype " + archetype + " found in catalog " + catalogKey );
            }
            else
            {
                getLogger().warn( "Specified archetype not found." );
                if ( interactiveMode.booleanValue() )
                {
                    definition.setVersion( null );
                    definition.setGroupId( null );
                    definition.setArtifactId( null );
                }
            }
        }

        // set the defaults - only group and version can be auto-defaulted
        if ( definition.getGroupId() == null )
        {
            definition.setGroupId( DEFAULT_ARCHETYPE_GROUPID );
        }
        if ( definition.getVersion() == null )
        {
            definition.setVersion( DEFAULT_ARCHETYPE_VERSION );
        }

        if ( !definition.isPartiallyDefined() )
        {
            // if artifact ID is set to its default, we still prompt to confirm
            if ( definition.getArtifactId() == null )
            {
                getLogger().info(
                    "No archetype defined. Using " + DEFAULT_ARCHETYPE_ARTIFACTID + " (" + definition.getGroupId() + ":"
                        + DEFAULT_ARCHETYPE_ARTIFACTID + ":" + definition.getVersion() + ")" );
                definition.setArtifactId( DEFAULT_ARCHETYPE_ARTIFACTID );
            }

            if ( interactiveMode.booleanValue() && ( archetypes.size() > 0 ) )
            {
                Archetype selectedArchetype = archetypeSelectionQueryer.selectArchetype( archetypes, definition );

                String catalogKey = getCatalogKey( archetypes, selectedArchetype );

                updateDefinition( definition, selectedArchetype, catalogKey );
            }

            // Make sure the groupId and artifactId are valid, the version may just default to
            // the latest release.
            if ( !definition.isPartiallyDefined() )
            {
                throw new ArchetypeSelectionFailure( "No valid archetypes could be found to choose." );
            }
        }

        // finally update the request with gathered information
        definition.updateRequest( request );
    }


    private Map<String, List<Archetype>> getArchetypesByCatalog( String catalogs )
    {
        if ( catalogs == null )
        {
            throw new NullPointerException( "catalogs cannot be null" );
        }

        Map<String, List<Archetype>> archetypes = new LinkedHashMap<String, List<Archetype>>();

        for ( String catalog : StringUtils.split( catalogs, "," ) )
        {
            if ( "internal".equalsIgnoreCase( catalog ) )
            {
                archetypes.put( "internal", archetypeManager.getInternalCatalog().getArchetypes() );
            }
            else if ( "local".equalsIgnoreCase( catalog ) )
            {
                archetypes.put( "local", archetypeManager.getDefaultLocalCatalog().getArchetypes() );
            }
            else if ( "remote".equalsIgnoreCase( catalog ) )
            {
                List<Archetype> archetypesFromRemote = archetypeManager.getRemoteCatalog().getArchetypes();
                if ( archetypesFromRemote.size() > 0 )
                {
                    archetypes.put( "remote", archetypesFromRemote );
                }
                else
                {
                    getLogger().warn( "No archetype found in remote catalog. Defaulting to internal catalog" );
                    archetypes.put( "internal", archetypeManager.getInternalCatalog().getArchetypes() );
                }
            }
            else if ( catalog.startsWith( "file://" ) )
            {
                String path = catalog.substring( 7 );
                archetypes.put( catalog, archetypeManager.getLocalCatalog( path ).getArchetypes() );
            }
            else if ( catalog.startsWith( "http://" ) || catalog.startsWith( "https://" ) )
            {
                archetypes.put( catalog, archetypeManager.getRemoteCatalog( catalog ).getArchetypes() );
            }
        }

        if ( archetypes.size() == 0 )
        {
            getLogger().info( "No catalog defined. Using internal catalog" );

            archetypes.put( "internal", archetypeManager.getInternalCatalog().getArchetypes() );
        }
        return archetypes;
    }

    private void updateRepository( ArchetypeDefinition definition, Archetype archetype, String catalogKey )
    {
        String repository = archetype.getRepository();
        if ( StringUtils.isNotEmpty( repository ) )
        {
            definition.setRepository( repository );
        }
        else if ( catalogKey.indexOf( ':' ) > 1 )
        {
            // file: or http:
            int lastIndex = catalogKey.lastIndexOf( '/' );
            String catalogBase = catalogKey.substring( 0, ( lastIndex > 7 ? lastIndex : catalogKey.length() ) );
            definition.setRepository( catalogBase );
        }
    }

    private void updateDefinition( ArchetypeDefinition definition, Archetype archetype, String catalogKey )
    {
        definition.setGroupId( archetype.getGroupId() );
        definition.setArtifactId( archetype.getArtifactId() );
        definition.setVersion( archetype.getVersion() );
        definition.setName( archetype.getArtifactId() );
        updateRepository( definition, archetype, catalogKey );
        definition.setGoals( StringUtils.join( archetype.getGoals().iterator(), "," ) );
    }

    public void setArchetypeSelectionQueryer( ArchetypeSelectionQueryer archetypeSelectionQueryer )
    {
        this.archetypeSelectionQueryer = archetypeSelectionQueryer;
    }

    private String getCatalogKey( Map<String, List<Archetype>> archetypes, Archetype selectedArchetype )
    {
        for ( Map.Entry<String, List<Archetype>> entry : archetypes.entrySet() )
        {
            List<Archetype> catalog = entry.getValue();

            if ( catalog.contains( selectedArchetype ) )
            {
                return entry.getKey();
            }
        }
        return "";
    }

    private Map.Entry<String, Archetype> findArchetype( Map<String, List<Archetype>> archetypes, String groupId,
                                                        String artifactId )
    {
        Archetype example = new Archetype();
        example.setGroupId( groupId );
        example.setArtifactId( artifactId );

        for ( Map.Entry<String, List<Archetype>> entry : archetypes.entrySet() )
        {
            List<Archetype> catalog = entry.getValue();

            if ( catalog.contains( example ) )
            {
                Archetype archetype = catalog.get( catalog.indexOf( example ) );

                return newMapEntry( entry.getKey(), archetype );
            }
        }

        return null;
    }

    private static <K, V> Map.Entry<K, V> newMapEntry( K key, V value )
    {
        Map<K, V> map = new HashMap<K, V>( 1 );
        map.put( key, value );

        return map.entrySet().iterator().next();
    }
}
