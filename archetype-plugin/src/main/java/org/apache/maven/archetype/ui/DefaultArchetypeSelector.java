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

package org.apache.maven.archetype.ui;

import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.ArchetypeSelectionFailure;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.exception.UnknownGroup;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** @plexus.component */
public class DefaultArchetypeSelector
    extends AbstractLogEnabled
    implements ArchetypeSelector
{
    static final String DEFAULT_ARCHETYPE_GROUPID = "org.apache.maven.archetypes";

    static final String DEFAULT_ARCHETYPE_VERSION = "1.0";

    static final String DEFAULT_ARCHETYPE_ARTIFACTID = "maven-archetype-quickstart";

    /** @plexus.requirement */
    private ArchetypeSelectionQueryer archetypeSelectionQueryer;
    /** @plexus.requirement */
    private org.apache.maven.archetype.Archetype archetype;

    public void selectArchetype( ArchetypeGenerationRequest request,
            Boolean interactiveMode, String catalogs )
        throws
        ArchetypeNotDefined,
        UnknownArchetype,
        UnknownGroup,
        IOException,
        PrompterException,
        ArchetypeSelectionFailure
    {
        //This should be an internal class
        ArchetypeDefinition definition = new ArchetypeDefinition();

        definition.setGroupId( request.getArchetypeGroupId() );
        definition.setArtifactId( request.getArchetypeArtifactId() );
        definition.setVersion( request.getArchetypeVersion() );

        Map archetypes = getArchetypesByCatalog ( catalogs );

        if ( definition.isDefined ()
            && StringUtils.isNotEmpty ( request.getArchetypeRepository () )
        )
        {
            getLogger ().info ( "Archetype defined by properties" );
        }
        else
        {
            if ( definition.isDefined ()
                && StringUtils.isEmpty ( request.getArchetypeRepository () )
            )
            {
                Iterator ca = new ArrayIterator ( StringUtils.split ( catalogs, "," ) );
                boolean found = false;
                while ( !found && ca.hasNext () )
                {
                    String catalogKey = (String) ca.next ();
                    String[] keySplitted = catalogKey.split("-", 2);
                    List catalog = (List) archetypes.get ( catalogKey );
                    Archetype example = new Archetype ();
                    example.setGroupId ( request.getArchetypeGroupId () );
                    example.setArtifactId ( request.getArchetypeArtifactId () );
                    if ( catalog.contains ( example ) )
                    {
                        found = true;

                        Archetype foundArchetype =
                            (Archetype) catalog.get ( catalog.indexOf ( example ) );
                        definition.setName ( foundArchetype.getArtifactId () );
                        if ( StringUtils.isNotEmpty( foundArchetype.getRepository () ) )
                        {
                            definition.setRepository ( foundArchetype.getRepository () );
                        }
                        else if ( keySplitted.length > 1 )
                        {
                            int lastIndex = catalogKey.lastIndexOf("/");
                            String catalogBase = catalogKey.substring(0,
                                    (lastIndex > 7 ? lastIndex : catalogKey.length()));
                            definition.setRepository ( catalogBase );
                        }

                        getLogger ().info (
                            "Archetype repository missing. Using the one from " + foundArchetype
                            + " found in catalog " + catalogKey
                        );
                    }
                }
                if ( !found )
                {
                    getLogger ().warn ( "No archetype repository found. Falling back to central repository (http://repo1.maven.org/maven2). " );
                    getLogger ().warn ( "Use -DarchetypeRepository=<your repository> if archetype's repository is elsewhere." );

                    definition.setRepository("http://repo1.maven.org/maven2");
                }
            }
            if ( !definition.isDefined () && definition.isPartiallyDefined () )
            {
                Iterator ca = new ArrayIterator ( StringUtils.split ( catalogs, "," ) );
                boolean found = false;
                while ( !found && ca.hasNext () )
                {
                    String catalogKey = (String) ca.next ();
                    List catalog = (List) archetypes.get ( catalogKey );
                    String[] keySplitted = catalogKey.split(":", 2);
                    Archetype example = new Archetype ();
                    example.setGroupId ( request.getArchetypeGroupId () );
                    example.setArtifactId ( request.getArchetypeArtifactId () );
                    if ( catalog.contains ( example ) )
                    {
                        found = true;

                        Archetype foundArchetype =
                            (Archetype) catalog.get ( catalog.indexOf ( example ) );
                        definition.setGroupId ( foundArchetype.getGroupId () );
                        definition.setArtifactId ( foundArchetype.getArtifactId () );
                        definition.setVersion ( foundArchetype.getVersion () );
                        definition.setName ( foundArchetype.getArtifactId () );
                        if ( StringUtils.isNotEmpty( foundArchetype.getRepository () ) )
                        {
                            definition.setRepository ( foundArchetype.getRepository () );
                        }
                        else if ( keySplitted.length > 1 )
                        {
                            int lastIndex = catalogKey.lastIndexOf("/");
                            String catalogBase = catalogKey.substring(0,
                                    (lastIndex > 7 ? lastIndex : catalogKey.length()));
                            definition.setRepository ( catalogBase );
                        }

                        String goals =
                            StringUtils.join ( foundArchetype.getGoals ().iterator (), "," );
                        definition.setGoals ( goals );

                        getLogger ().info (
                            "Archetype " + foundArchetype
                            + " found in catalog " + catalogKey
                        );
                    }
                }
                if ( !found )
                {
                    getLogger ().warn ( "Specified archetype not found." );
                    if ( interactiveMode.booleanValue () )
                    {
                        definition.setVersion ( null );
                        definition.setGroupId ( null );
                        definition.setArtifactId ( null );
                    }
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

        if ( !definition.isDefined() && !definition.isPartiallyDefined() )
        {
            // if artifact ID is set to it's default, we still prompt to confirm
            if ( definition.getArtifactId() == null )
            {
                getLogger().info( "No archetype defined. Using " + DEFAULT_ARCHETYPE_ARTIFACTID + " ("
                    + definition.getGroupId() + ":" + DEFAULT_ARCHETYPE_ARTIFACTID + ":" + definition.getVersion()
                    + ")" );
                definition.setArtifactId( DEFAULT_ARCHETYPE_ARTIFACTID );
            }

            if ( interactiveMode.booleanValue() )
            {
                if ( archetypes.size() > 0 )
                {
                    Archetype selectedArchetype = archetypeSelectionQueryer.selectArchetype( archetypes, definition );

                    definition.setGroupId( selectedArchetype.getGroupId() );
                    definition.setArtifactId( selectedArchetype.getArtifactId() );
                    definition.setVersion( selectedArchetype.getVersion() );
                    definition.setName( selectedArchetype.getArtifactId() );
                    String catalogKey = getCatalogKey ( archetypes, selectedArchetype );
                    String[] keySplitted = catalogKey.split(":", 2);
                    if ( StringUtils.isNotEmpty( selectedArchetype.getRepository () ) )
                    {
                        definition.setRepository ( selectedArchetype.getRepository () );
                    }
                    else if ( keySplitted.length > 1 )
                    {
                        int lastIndex = catalogKey.lastIndexOf("/");
                        String catalogBase = catalogKey.substring(0,
                                (lastIndex > 7 ? lastIndex : catalogKey.length()));
                        definition.setRepository ( catalogBase );
                    }
                    String goals = StringUtils.join( selectedArchetype.getGoals().iterator(), "," );
                    definition.setGoals( goals );
                }
            }
        }

        // Make sure the groupId and artifactId are valid, the version may just default to
        // the latest release.

        if ( !definition.isPartiallyDefined() )
        {
            throw new ArchetypeSelectionFailure( "No valid archetypes could be found to choose." );
        }

        request.setArchetypeGroupId( definition.getGroupId() );

        request.setArchetypeArtifactId( definition.getArtifactId() );

        request.setArchetypeVersion( definition.getVersion() );

        request.setArchetypeGoals( definition.getGoals() );

        request.setArchetypeName( definition.getName() );

        if (StringUtils.isNotEmpty(definition.getRepository())){
            request.setArchetypeRepository( definition.getRepository() );
        }
    }

    private Map getArchetypesByCatalog(String catalogs) {
        if ( catalogs == null )
        {
            throw new NullPointerException( "catalogs can not be null" );
        }

        Map archetypes = new HashMap();

        Iterator ca = new ArrayIterator(StringUtils.split(catalogs, ","));
        while (ca.hasNext()) {
            String catalog = (String) ca.next();

            if ("internal".equalsIgnoreCase(catalog)) {
                archetypes.put("internal", archetype.getInternalCatalog().getArchetypes());
            } else if ("local".equalsIgnoreCase(catalog)) {
                archetypes.put("local", archetype.getDefaultLocalCatalog().getArchetypes());
            } else if ("remote".equalsIgnoreCase(catalog)) {
                List archetypesFromRemote = archetype.getRemoteCatalog().getArchetypes();
                if(archetypesFromRemote.size() > 0) {
                    archetypes.put("remote", archetypesFromRemote);
                } else {
                    getLogger().warn("No archetype found in Remote catalog. Defaulting to internal Catalog");
                    archetypes.put("internal", archetype.getInternalCatalog().getArchetypes());
                }
            } else if (catalog.startsWith("file://")) {
                String path = catalog.substring(7);
                archetypes.put(catalog, archetype.getLocalCatalog(path).getArchetypes());
            } else if (catalog.startsWith("http://")) {
                archetypes.put(catalog, archetype.getRemoteCatalog(catalog).getArchetypes());
            }
        }

        if (archetypes.size() == 0) {
            getLogger().info("No catalog defined. Using internal catalog");

            archetypes.put("internal", archetype.getInternalCatalog().getArchetypes());
        }
        return archetypes;
    }

    private Properties getArchetypeDataSourceProperties( String sourceRoleHint,
                                                         Properties archetypeCatalogProperties )
    {
        Properties p = new Properties();

        for ( Iterator i = archetypeCatalogProperties.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();

            if ( key.startsWith( sourceRoleHint ) )
            {
                String k = key.substring( sourceRoleHint.length() + 1 );

                p.setProperty( k, archetypeCatalogProperties.getProperty( key ) );
            }
        }

        return p;
    }

    public void setArchetypeSelectionQueryer( ArchetypeSelectionQueryer archetypeSelectionQueryer )
    {
        this.archetypeSelectionQueryer = archetypeSelectionQueryer;
    }

    private String getCatalogKey(Map archetypes, Archetype selectedArchetype) {
        String key = "";
        Iterator keys = archetypes.keySet().iterator();
        boolean found = false;
        while ( keys.hasNext() && !found)
        {
            key = (String) keys.next();
            List catalog = (List) archetypes.get( key );
            if ( catalog.contains( selectedArchetype ) )
            {
                found = true;
            }
        }
        return key;
    }
}
