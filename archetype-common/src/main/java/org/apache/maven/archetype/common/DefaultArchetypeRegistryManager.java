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

package org.apache.maven.archetype.common;

import org.apache.maven.archetype.registry.Archetype;
import org.apache.maven.archetype.registry.ArchetypeRegistry;
import org.apache.maven.archetype.registry.ArchetypeRepository;
import org.apache.maven.archetype.registry.io.xpp3.ArchetypeRegistryXpp3Reader;
import org.apache.maven.archetype.registry.io.xpp3.ArchetypeRegistryXpp3Writer;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @plexus.component */
public class DefaultArchetypeRegistryManager
    extends AbstractLogEnabled
    implements ArchetypeRegistryManager
{
    /**
     * Used to create ArtifactRepository objects given the urls of the remote repositories.
     *
     * @plexus.requirement
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * Determines whether the layout is legacy or not.
     *
     * @plexus.requirement roleHint="default"
     */
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;

    public List getArchetypeGroups( File archetypeRegistryFile )
    {
        try
        {
            ArchetypeRegistry registry = readArchetypeRegistry( archetypeRegistryFile );
            return registry.getArchetypeGroups();
        }
        catch ( IOException e )
        {
            getLogger().warn( "Can not read ~/m2/archetype.xml" );
            return
                Arrays.asList(
                    new String[]{"org.apache.maven.archetypes", "org.codehaus.mojo.archetypes"}
                );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().warn( "Can not read ~/m2/archetype.xml" );
            return
                Arrays.asList(
                    new String[]{"org.apache.maven.archetypes", "org.codehaus.mojo.archetypes"}
                );
        }
    }

    public List getFilteredExtensions(
        String archetypeFilteredExtentions,
        File archetypeRegistryFile
    )
        throws
        IOException
    {
        List filteredExtensions = new ArrayList();

        if ( StringUtils.isNotEmpty( archetypeFilteredExtentions ) )
        {
            filteredExtensions.addAll(
                Arrays.asList( StringUtils.split( archetypeFilteredExtentions, "," ) )
            );
        }

        try
        {
            ArchetypeRegistry registry = readArchetypeRegistry( archetypeRegistryFile );

            filteredExtensions.addAll( registry.getFilteredExtensions() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Can not read ~/m2/archetype.xml" );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().warn( "Can not read ~/m2/archetype.xml" );
        }

        if ( filteredExtensions.isEmpty() )
        {
            filteredExtensions.addAll( Constants.DEFAULT_FILTERED_EXTENSIONS );
        }

        return filteredExtensions;
    }

    public List getLanguages( String archetypeLanguages,
                              File archetypeRegistryFile )
        throws
        IOException
    {
        List languages = new ArrayList();

        if ( StringUtils.isNotEmpty( archetypeLanguages ) )
        {
            languages.addAll( Arrays.asList( StringUtils.split( archetypeLanguages, "," ) ) );
        }

        try
        {
            ArchetypeRegistry registry = readArchetypeRegistry( archetypeRegistryFile );

            languages.addAll( registry.getLanguages() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Can not read ~/m2/archetype.xml" );
        }
        catch ( XmlPullParserException e )
        {
            getLogger().warn( "Can not read ~/m2/archetype.xml" );
        }

        if ( languages.isEmpty() )
        {
            languages.addAll( Constants.DEFAULT_LANGUAGES );
        }

        return languages;
    }

    public ArchetypeRegistry readArchetypeRegistry( File archetypeRegistryFile )
        throws
        IOException,
        XmlPullParserException
    {
        if ( !archetypeRegistryFile.exists() )
        {
            ArchetypeRegistry registry = getDefaultArchetypeRegistry();

            writeArchetypeRegistry( archetypeRegistryFile, registry );            
        }

        ArchetypeRegistryXpp3Reader reader = new ArchetypeRegistryXpp3Reader();
        
        FileReader fileReader = new FileReader( archetypeRegistryFile );

        try
        {
            return reader.read( fileReader );
        }
        finally
        {
            IOUtil.close( fileReader );
        }
    }

    public List getRepositories(
        List pomRemoteRepositories,
        String remoteRepositories,
        File archetypeRegistryFile
    )
        throws
        IOException,
        XmlPullParserException
    {
        List archetypeRemoteRepositories = new ArrayList( pomRemoteRepositories );

        ArchetypeRegistry registry = readArchetypeRegistry( archetypeRegistryFile );
        if ( !registry.getArchetypeRepositories().isEmpty() )
        {
            archetypeRemoteRepositories = new ArrayList();

            Iterator repositories = registry.getArchetypeRepositories().iterator();
            while ( repositories.hasNext() )
            {
                ArchetypeRepository repository = (ArchetypeRepository) repositories.next();

                archetypeRemoteRepositories.add(
                    createRepository( repository.getUrl(), repository.getId() )
                );
            }
        }

        if ( remoteRepositories != null )
        {
            archetypeRemoteRepositories = new ArrayList();

            String[] s = StringUtils.split( remoteRepositories, "," );

            for ( int i = 0; i < s.length; i++ )
            {
                archetypeRemoteRepositories.add( createRepository( s[i], "id" + i ) );
            }
        }

        return archetypeRemoteRepositories;
    }

    public void writeArchetypeRegistry(
        File archetypeRegistryFile,
        ArchetypeRegistry archetypeRegistry
    )
        throws
        IOException
    {
        ArchetypeRegistryXpp3Writer writer = new ArchetypeRegistryXpp3Writer();
        FileWriter fileWriter = new FileWriter( archetypeRegistryFile );

        try
        {
            writer.write( fileWriter, archetypeRegistry );
        }
        finally
        {
            IOUtil.close( fileWriter );
        }
    }

    /**
     * Code stealed from MavenArchetypeMojo
     * (org.apache.maven.plugins:maven-archetype-plugin:1.0-alpha4).
     */
    private ArtifactRepository createRepository( String url,
                                                 String repositoryId )
    {
        // snapshots vs releases
        // offline = to turning the update policy off

        // TODO: we'll need to allow finer grained creation of repositories but this will do for now

        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;

        ArtifactRepositoryPolicy snapshotsPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );

        ArtifactRepositoryPolicy releasesPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );

        return
            artifactRepositoryFactory.createArtifactRepository(
                repositoryId,
                url,
                defaultArtifactRepositoryLayout,
                snapshotsPolicy,
                releasesPolicy
            );
    }

    public void addGroup( String group,
                          File archetypeRegistryFile )
        throws
        IOException,
        XmlPullParserException
    {
        ArchetypeRegistry registry;
        try
        {
            registry = readArchetypeRegistry( archetypeRegistryFile );
        }
        catch ( FileNotFoundException ex )
        {
            registry = getDefaultArchetypeRegistry();
        }

        if ( registry.getArchetypeGroups().contains( group ) )
        {
            getLogger().debug( "Group " + group + " already exists" );
        }
        else
        {
            registry.addArchetypeGroup( group.trim() );
            getLogger().info( "New group " + group + " added to registry" );
        }

        writeArchetypeRegistry( archetypeRegistryFile, registry );
    }

    public ArchetypeRegistry getDefaultArchetypeRegistry()
    {
        ArchetypeRegistry registry = new ArchetypeRegistry();

        registry.addArchetypeGroup( "org.apache.maven.archetypes" );

        registry.addArchetypeGroup( "org.codehaus.mojo.archetypes" );

        registry.getLanguages().addAll( Constants.DEFAULT_LANGUAGES );

        registry.getFilteredExtensions().addAll( Constants.DEFAULT_FILTERED_EXTENSIONS );

        registry.addArchetypeRepository( addRepository( "central", "http://repo1.maven.org/maven2") );

        try
        {
            Map archetypes = loadArchetypesFromWiki( DEFAULT_ARCHETYPE_INVENTORY_PAGE );

            for ( Iterator i = archetypes.values().iterator(); i.hasNext(); )
            {
                Archetype archetype = (Archetype) i.next();

                registry.addArchetype( archetype );

                registry.addArchetypeRepository( addRepository( archetype.getRepository(), archetype.getRepository() ) );                
            }
        }
        catch ( Exception e )
        {
            getLogger().warn( "Could not load archetypes listed at " + DEFAULT_ARCHETYPE_INVENTORY_PAGE );
        }

        return registry;
    }

    private ArchetypeRepository addRepository( String id, String url )
    {
        ArchetypeRepository archetypeRepository = new ArchetypeRepository();

        archetypeRepository.setId( id );

        archetypeRepository.setUrl( url );

        return archetypeRepository;
    }

    private String DEFAULT_ARCHETYPE_INVENTORY_PAGE="http://docs.codehaus.org/pages/viewpagesrc.action?pageId=48400";

    static Map loadArchetypesFromWiki( String url )
        throws Exception
    {
        Map archetypes = new LinkedHashMap();

        StringBuffer sb = new StringBuffer();

        InputStream in = new URL( cleanupUrl( url ) ).openStream();

        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

        char[] buffer = new char[1024];

        int len = 0;

        while ( ( len = reader.read( buffer ) ) > -1 )
        {
            sb.append( buffer, 0, len );
        }

        Pattern ptn = Pattern.compile( "<br>\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_.:/ \\[\\],]+)\\|([^|]+)\\|" );

        Matcher m = ptn.matcher( sb.toString() );

        while ( m.find() )
        {
            org.apache.maven.archetype.registry.Archetype arch = new Archetype();

            arch.setArtifactId( m.group( 1 ).trim() );

            arch.setGroupId( m.group( 2 ).trim() );

            String version = m.group( 3 ).trim();

            if ( version.equals( "" ) )
            {
                version = "LATEST";
            }

            arch.setVersion( version );

            arch.setRepository(cleanupUrl(m.group(4).trim()));

            arch.setDescription( cleanup( m.group( 5 ).trim() ) );

            archetypes.put( arch.getArtifactId(), arch );
        }
        return archetypes;
    }

    static String cleanup( String val )
    {
        val = val.replaceAll( "\\r|\\n|\\s{2,}", "" );
        return val;
    }

    static String cleanupUrl( String val )
    {
        return val.replaceAll( "\\r|\\n|\\s{2,}|\\[|\\]|\\&nbsp;", "" );
    }
}
