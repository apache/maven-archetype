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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @plexus.component
 */
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
     * @plexus.requirement  roleHint="default"
     */
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;

    public List getArchetypeGroups ( File archetypeRegistryFile )
    {
        try
        {
            ArchetypeRegistry registry = readArchetypeRegistry ( archetypeRegistryFile );
            return registry.getArchetypeGroups ();
        }
        catch ( IOException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
            return
                Arrays.asList (
                    new String[] { "org.apache.maven.archetypes", "org.codehaus.mojo.archetypes" }
                );
        }
        catch ( XmlPullParserException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
            return
                Arrays.asList (
                    new String[] { "org.apache.maven.archetypes", "org.codehaus.mojo.archetypes" }
                );
        }
    }

    public List getFilteredExtensions (
        String archetypeFilteredExtentions,
        File archetypeRegistryFile
    )
    throws IOException
    {
        List filteredExtensions = new ArrayList ();

        if ( StringUtils.isNotEmpty ( archetypeFilteredExtentions ) )
        {
            filteredExtensions.addAll (
                Arrays.asList ( StringUtils.split ( archetypeFilteredExtentions, "," ) )
            );
        }

        try
        {
            ArchetypeRegistry registry = readArchetypeRegistry ( archetypeRegistryFile );

            filteredExtensions.addAll ( registry.getFilteredExtensions () );
        }
        catch ( IOException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
        }
        catch ( XmlPullParserException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
        }

        if ( filteredExtensions.isEmpty () )
        {
            filteredExtensions.addAll ( Constants.DEFAULT_FILTERED_EXTENSIONS );
        }

        return filteredExtensions;
    }

    public List getLanguages ( String archetypeLanguages, File archetypeRegistryFile )
    throws IOException
    {
        List languages = new ArrayList ();

        if ( StringUtils.isNotEmpty ( archetypeLanguages ) )
        {
            languages.addAll ( Arrays.asList ( StringUtils.split ( archetypeLanguages, "," ) ) );
        }

        try
        {
            ArchetypeRegistry registry = readArchetypeRegistry ( archetypeRegistryFile );

            languages.addAll ( registry.getLanguages () );
        }
        catch ( IOException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
        }
        catch ( XmlPullParserException e )
        {
            getLogger ().warn ( "Can not read ~/m2/archetype.xml" );
        }

        if ( languages.isEmpty () )
        {
            languages.addAll ( Constants.DEFAULT_LANGUAGES );
        }

        return languages;
    }

    public ArchetypeRegistry readArchetypeRegistry ( File archetypeRegistryFile )
    throws IOException, FileNotFoundException, XmlPullParserException
    {
        ArchetypeRegistryXpp3Reader reader = new ArchetypeRegistryXpp3Reader ();
        FileReader fileReader = new FileReader ( archetypeRegistryFile );

        try
        {
            return reader.read ( fileReader );
        }
        finally
        {
            IOUtil.close ( fileReader );
        }
    }

    public List getRepositories (
        List pomRemoteRepositories,
        String remoteRepositories,
        File archetypeRegistryFile
    )
    throws IOException, XmlPullParserException
    {
        List archetypeRemoteRepositories = new ArrayList ( pomRemoteRepositories );

        ArchetypeRegistry registry = readArchetypeRegistry ( archetypeRegistryFile );
        if ( !registry.getArchetypeRepositories ().isEmpty () )
        {
            archetypeRemoteRepositories = new ArrayList ();

            Iterator repositories = registry.getArchetypeRepositories ().iterator ();
            while ( repositories.hasNext () )
            {
                ArchetypeRepository repository = (ArchetypeRepository) repositories.next ();

                archetypeRemoteRepositories.add (
                    createRepository ( repository.getUrl (), repository.getId () )
                );
            }
        }

        if ( remoteRepositories != null )
        {
            archetypeRemoteRepositories = new ArrayList ();

            String[] s = StringUtils.split ( remoteRepositories, "," );

            for ( int i = 0; i < s.length; i++ )
            {
                archetypeRemoteRepositories.add ( createRepository ( s[i], "id" + i ) );
            }
        }

        return archetypeRemoteRepositories;
    }

    public void writeArchetypeRegistry (
        File archetypeRegistryFile,
        ArchetypeRegistry archetypeRegistry
    )
    throws IOException
    {
        ArchetypeRegistryXpp3Writer writer = new ArchetypeRegistryXpp3Writer ();
        FileWriter fileWriter = new FileWriter ( archetypeRegistryFile );

        try
        {
            writer.write ( fileWriter, archetypeRegistry );
        }
        finally
        {
            IOUtil.close ( fileWriter );
        }
    }

    /**
     * Code stealed from MavenArchetypeMojo
     * (org.apache.maven.plugins:maven-archetype-plugin:1.0-alpha4).
     */
    private ArtifactRepository createRepository ( String url, String repositoryId )
    {
        // snapshots vs releases
        // offline = to turning the update policy off

        // TODO: we'll need to allow finer grained creation of repositories but this will do for now

        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;

        ArtifactRepositoryPolicy snapshotsPolicy =
            new ArtifactRepositoryPolicy ( true, updatePolicyFlag, checksumPolicyFlag );

        ArtifactRepositoryPolicy releasesPolicy =
            new ArtifactRepositoryPolicy ( true, updatePolicyFlag, checksumPolicyFlag );

        return
            artifactRepositoryFactory.createArtifactRepository (
                repositoryId,
                url,
                defaultArtifactRepositoryLayout,
                snapshotsPolicy,
                releasesPolicy
            );
    }

    public void addGroup(String group, File archetypeRegistryFile)
    throws IOException, XmlPullParserException
    {
        ArchetypeRegistry registry;
        try
        {
            registry = readArchetypeRegistry(archetypeRegistryFile);
        }
        catch (FileNotFoundException ex)
        {
            registry = getDefaultArchetypeRegistry();
        }

        if (registry.getArchetypeGroups().contains(group))
        {
            getLogger().debug("Group " + group + " already exists");
        }
        else
        {
            registry.addArchetypeGroup(group.trim());
            getLogger().info("New group " + group + " added to registry");
        }

        writeArchetypeRegistry(archetypeRegistryFile, registry);
    }

    public ArchetypeRegistry getDefaultArchetypeRegistry()
    {
        ArchetypeRegistry registry = new ArchetypeRegistry ();

        registry.addArchetypeGroup ( "org.apache.maven.archetypes" );
        registry.addArchetypeGroup ( "org.codehaus.mojo.archetypes" );

        registry.getLanguages ().addAll ( Constants.DEFAULT_LANGUAGES );
        registry.getFilteredExtensions ().addAll ( Constants.DEFAULT_FILTERED_EXTENSIONS );

        ArchetypeRepository archetypeRepository = new ArchetypeRepository ();
        archetypeRepository.setId ( "central" );
        archetypeRepository.setUrl ( "http://repo1.maven.org/maven2" );

        registry.addArchetypeRepository(archetypeRepository);

        return registry;
    }
}
