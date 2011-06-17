package org.apache.maven.archetype.source;

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
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An archetype data source getting its content from a Confluence Wiki page.
 * By default, <a href="http://docs.codehaus.org/display/MAVENUSER/Archetypes+List">MAVENUSER/Archetypes List</a>
 * is used.
 *
 * @author            Jason van Zyl
 * @plexus.component  role-hint="wiki"
 */
public class WikiArchetypeDataSource
    implements ArchetypeDataSource
{
    private static String DEFAULT_ARCHETYPE_INVENTORY_PAGE =
        "http://docs.codehaus.org/pages/viewpagesrc.action?pageId=48400";

    static String cleanup( String val )
    {
        val = val.replaceAll( "\\r|\\n|\\s{2,}|\\[|\\|[^\\]]+]|\\]", "" );
        return val;
    }

    static String cleanupUrl( String val )
    {
        return val.replaceAll( "\\r|\\n|\\s{2,}|\\[|\\]|\\&nbsp;", "" );
    }

    public ArchetypeCatalog getArchetypeCatalog( Properties properties )
        throws ArchetypeDataSourceException
    {
        ArchetypeCatalog ac = new ArchetypeCatalog();
        ac.setArchetypes( getArchetypes( properties ) );
        return ac;
    }

    public Set<Archetype> getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        String url = properties.getProperty( "url" );

        if ( url == null )
        {
            url = DEFAULT_ARCHETYPE_INVENTORY_PAGE;
        }

        Set<Archetype> archetypes = new HashSet<Archetype>();

        String pageSource = "";
        InputStream in = null;
        try
        {
            in = new URL( cleanupUrl( url ) ).openStream();

            pageSource = IOUtil.toString( in );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error retrieving list of archetypes from " + url );
        }
        finally
        {
            IOUtil.close( in );
        }

        // | ArtifactId | GroupId | Version | Repository | Description |
        Pattern ptn =
            Pattern.compile(
                "<br>\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_.:/ \\[\\],]+)\\|(.*) \\|"
            );

        Matcher m = ptn.matcher( pageSource );

        while ( m.find() )
        {
            Archetype archetype = new Archetype();

            archetype.setArtifactId( m.group( 1 ).trim() );

            archetype.setGroupId( m.group( 2 ).trim() );

            String version = m.group( 3 ).trim();
            if ( version.equals( "" ) )
            {
                version = "RELEASE";
            }
            archetype.setVersion( version );

            archetype.setRepository( cleanupUrl( m.group( 4 ).trim() ) );

            archetype.setDescription( cleanup( m.group( 5 ).trim() ) );

            archetypes.add( archetype );
        }
        return archetypes;
    }

    public void updateCatalog( Properties properties, Archetype archetype )
        throws ArchetypeDataSourceException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
