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

import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.exception.UnknownGroup;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.io.xpp3.ArchetypeDescriptorXpp3Reader;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptorBuilder;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.GroupRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.shared.downloader.DownloadException;
import org.apache.maven.shared.downloader.DownloadNotFoundException;
import org.apache.maven.shared.downloader.Downloader;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/** @plexus.component */
public class DefaultArchetypeArtifactManager
    extends AbstractLogEnabled
    implements ArchetypeArtifactManager
{
    /** @plexus.requirement */
    private Downloader downloader;

    /** @plexus.requirement */
    private RepositoryMetadataManager repositoryMetadataManager;

    public File getArchetypeFile(
        final String groupId,
        final String artifactId,
        final String version,
        final ArtifactRepository localRepository,
        final List repositories
    )
        throws
        UnknownArchetype
    {
        try
        {
            return downloader.download( groupId, artifactId, version, localRepository, repositories );
        }
        catch ( DownloadNotFoundException ex )
        {
            throw new UnknownArchetype( ex );
        }
        catch ( DownloadException ex )
        {
            throw new UnknownArchetype( ex );
        }
    }

    public ClassLoader getArchetypeJarLoader( File archetypeFile )
        throws
        UnknownArchetype
    {
        try
        {
            URL[] urls = new URL[1];

            urls[0] = archetypeFile.toURI().toURL();

            return new URLClassLoader( urls );
        }
        catch ( MalformedURLException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public ClassLoader getArchetypeJarLoader(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype
    {
        try
        {
            File archetypeFile =
                getArchetypeFile( groupId, artifactId, version, localRepository, repositories );
            URL[] urls = new URL[1];

            urls[0] = archetypeFile.toURI().toURL();

            return new URLClassLoader( urls );
        }
        catch ( MalformedURLException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public List getArchetypes(
        String groupId,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownGroup
    {
        try
        {
            List archetypes = new ArrayList();

            RepositoryMetadata metadata = new GroupRepositoryMetadata( groupId );

            repositoryMetadataManager.resolve( metadata, repositories, localRepository );

            for ( Iterator iter = metadata.getMetadata().getPlugins().iterator(); iter.hasNext(); )
            {
                Plugin plugin = (Plugin) iter.next();

                Archetype archetype = new Archetype();

                archetype.setGroupId( groupId );

                archetype.setArtifactId( plugin.getArtifactId() );

                archetype.setName( plugin.getName() );

                archetype.setPrefix( plugin.getPrefix() );

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "plugin=" + groupId + ":" + plugin.getArtifactId() );
                }

                if ( !archetypes.contains( archetype ) )
                {
                    archetypes.add( archetype );
                }
            } // end for

            return archetypes;
        }
        catch ( RepositoryMetadataResolutionException e )
        {
            throw new UnknownGroup( e );
        }
    }

    public ZipFile getArchetypeZipFile( File archetypeFile )
        throws
        UnknownArchetype
    {
        try
        {
            return new ZipFile( archetypeFile );
        }
        catch ( ZipException e )
        {
            throw new UnknownArchetype( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public ZipFile getArchetypeZipFile(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype
    {
        try
        {
            File archetypeFile =
                getArchetypeFile( groupId, artifactId, version, localRepository, repositories );

            return new ZipFile( archetypeFile );
        }
        catch ( ZipException e )
        {
            
            throw new UnknownArchetype( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public boolean exists( 
        String archetypeGroupId, 
        String archetypeArtifactId, 
        String archetypeVersion, 
        ArtifactRepository localRepository, 
        List remoteRepositories )
    {
        try
        {
            System.out.println( "archetypeGroupId = " + archetypeGroupId );
            System.out.println( "archetypeArtifactId = " + archetypeArtifactId );
            System.out.println( "archetypeVersion = " + archetypeVersion );
            System.out.println( "localRepository = " + localRepository );
            for ( Iterator i = remoteRepositories.iterator(); i.hasNext(); )
            {
                ArtifactRepository artifactRepository = (ArtifactRepository) i.next();

                System.out.println( "artifactRepository = " + artifactRepository );
            }

            File archetypeFile = downloader.download( archetypeGroupId, archetypeArtifactId, archetypeVersion, localRepository, remoteRepositories );

            System.out.println( "archetypeFile = " + archetypeFile );

            if ( archetypeVersion.equals( "RELEASE" ) || archetypeVersion.equals( "LATEST" ) )
            {
                // We do this so that we don't make another network call to get the version. The downloader
                // should tell us or just replace it with the new artifact code.                
                //TODO: Raphaël note: fix with find a way to return archetypeVersion
//                ad.setVersion( GavCalculator.calculate( archetypeFile.getAbsolutePath() ).getVersion() );
            }

            return archetypeFile.exists();
        }
        catch ( DownloadException e )
        {
            e.printStackTrace();
            getLogger().debug("OldArchetype don't exist", e);
            return false;
        }
        catch ( DownloadNotFoundException e )
        {
            e.printStackTrace( );
            getLogger().debug("OldArchetype don't exist", e);
            return false;
        }
    }

    public boolean isFileSetArchetype(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
    {
        try
        {
            ClassLoader archetypeJarLoader =
                getArchetypeJarLoader(
                    groupId,
                    artifactId,
                    version,
                    localRepository,
                    repositories
                );

            org.apache.maven.archetype.metadata.ArchetypeDescriptor descriptor =
                loadFileSetArchetypeDescriptor( archetypeJarLoader );

            return descriptor.getName() != null;
        }
        catch ( XmlPullParserException e )
        {
            return false;
        }
        catch ( IOException e )
        {
            return false;
        }
        catch ( UnknownArchetype e )
        {
            return false;
        }
    }

    public ArchetypeDescriptor getFileSetArchetypeDescriptor( File archetypeFile )
        throws
        UnknownArchetype
    {
        try
        {
            ClassLoader archetypeJarLoader = getArchetypeJarLoader( archetypeFile );

            return loadFileSetArchetypeDescriptor( archetypeJarLoader );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public org.apache.maven.archetype.metadata.ArchetypeDescriptor getFileSetArchetypeDescriptor(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype
    {
        try
        {
            ClassLoader archetypeJarLoader =
                getArchetypeJarLoader(
                    groupId,
                    artifactId,
                    version,
                    localRepository,
                    repositories
                );

            return loadFileSetArchetypeDescriptor( archetypeJarLoader );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public List getFilesetArchetypeResources( File archetypeFile )
        throws
        UnknownArchetype
    {
        List archetypeResources = new ArrayList();

        ZipFile zipFile = getArchetypeZipFile( archetypeFile );

        Enumeration enumeration = zipFile.entries();
        while ( enumeration.hasMoreElements() )
        {
            ZipEntry entry = (ZipEntry) enumeration.nextElement();

            if ( !entry.isDirectory()
                && entry.getName().startsWith( Constants.ARCHETYPE_RESOURCES )
                )
            {
                // not supposed to be file.seperator
                String resource =
                    StringUtils.replace(
                        entry.getName(),
                        Constants.ARCHETYPE_RESOURCES + "/",
                        ""
                    );
                getLogger().debug( "Found resource " + resource );
                // TODO:FIXME
                archetypeResources.add( resource );
            }
            else
            {
                getLogger().debug( "Not resource " + entry.getName() );
            }
        }
        return archetypeResources;
    }

    public List getFilesetArchetypeResources(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype
    {
        List archetypeResources = new ArrayList();

        ZipFile zipFile =
            getArchetypeZipFile( groupId, artifactId, version, localRepository, repositories );

        Enumeration enumeration = zipFile.entries();
        while ( enumeration.hasMoreElements() )
        {
            ZipEntry entry = (ZipEntry) enumeration.nextElement();

            if ( !entry.isDirectory()
                && entry.getName().startsWith( Constants.ARCHETYPE_RESOURCES )
                )
            {
                // not supposed to be file.seperator
                archetypeResources.add(
                    StringUtils.replace(
                        entry.getName(),
                        Constants.ARCHETYPE_RESOURCES + "/",
                        ""
                    )
                );
            }
        }
        return archetypeResources;
    }

    public boolean isOldArchetype(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
    {
        try
        {
            ClassLoader archetypeJarLoader =
                getArchetypeJarLoader(
                    groupId,
                    artifactId,
                    version,
                    localRepository,
                    repositories
                );

            org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor descriptor =
                loadOldArchetypeDescriptor( archetypeJarLoader );

            return descriptor.getId() != null;
        }
        catch ( XmlPullParserException e )
        {
            return false;
        }
        catch ( IOException e )
        {
            return false;
        }
        catch ( UnknownArchetype ex )
        {
            return false;
        }
    }

    public org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype
    {
        try
        {
            ClassLoader archetypeJarLoader =
                getArchetypeJarLoader(
                    groupId,
                    artifactId,
                    version,
                    localRepository,
                    repositories
                );

            return loadOldArchetypeDescriptor( archetypeJarLoader );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public String getReleaseVersion(
        String groupId,
        String artifactId,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype
    {
        try
        {
            RepositoryMetadata metadata =
                new GroupRepositoryMetadata( groupId + "." + artifactId );

            repositoryMetadataManager.resolve( metadata, repositories, localRepository );

            return metadata.getMetadata().getVersioning().getRelease();
        }
        catch ( RepositoryMetadataResolutionException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    public List getVersions(
        String groupId,
        String artifactId,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype
    {
        try
        {
            RepositoryMetadata metadata =
                new GroupRepositoryMetadata( groupId + "." + artifactId );

            repositoryMetadataManager.resolve( metadata, repositories, localRepository );

            return metadata.getMetadata().getVersioning().getVersions();
        }
        catch ( RepositoryMetadataResolutionException e )
        {
            throw new UnknownArchetype( e );
        }
    }

    private Reader getArchetypeDescriptorReader( ClassLoader archetypeJarLoader )
        throws
        IOException
    {
        InputStream is = getStream( Constants.ARCHETYPE_DESCRIPTOR, archetypeJarLoader );

        if ( is == null )
        {
            throw new IOException(
                "The " + Constants.ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        return new InputStreamReader( is );
    }

    private org.apache.maven.archetype.metadata.ArchetypeDescriptor loadFileSetArchetypeDescriptor(
        ClassLoader archetypeJarLoader
    )
        throws
        XmlPullParserException,
        IOException
    {
        Reader reader = getArchetypeDescriptorReader( archetypeJarLoader );

        ArchetypeDescriptorXpp3Reader archetypeReader = new ArchetypeDescriptorXpp3Reader();

        try
        {
            return archetypeReader.read( reader, true );
        }
        finally
        {
            reader.close();
        }
    }

    private org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor loadOldArchetypeDescriptor(
        ClassLoader archetypeJarLoader
    )
        throws
        IOException,
        XmlPullParserException
    {
        ArchetypeDescriptorBuilder builder = new ArchetypeDescriptorBuilder();

        org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor descriptor = null;

        Reader reader = null;
        try
        {
            reader = getOldArchetypeDescriptorReader( archetypeJarLoader );

            descriptor = builder.build( reader );
        }
        catch ( IOException ex )
        {
            getLogger().debug( "Can not load old archetype", ex );
        }
        catch ( XmlPullParserException ex )
        {
            getLogger().debug( "Can not load old archetype", ex );
        }
        finally
        {
            if ( reader != null )
            {
                reader.close();
            }
        }

        if ( descriptor == null )
        {
            try
            {
                reader = getOlderArchetypeDescriptorReader( archetypeJarLoader );

                descriptor = builder.build( reader );
            }
            finally
            {
                if ( reader != null )
                {
                    reader.close();
                }
            }
        }

        return descriptor;
    }

    private Reader getOldArchetypeDescriptorReader( ClassLoader archetypeJarLoader )
        throws
        IOException
    {
        InputStream is = getStream( Constants.OLD_ARCHETYPE_DESCRIPTOR, archetypeJarLoader );

        if ( is == null )
        {
            throw new IOException(
                "The " + Constants.OLD_ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        return new InputStreamReader( is );
    }

    private Reader getOlderArchetypeDescriptorReader( ClassLoader archetypeJarLoader )
        throws
        IOException
    {
        InputStream is = getStream( Constants.OLDER_ARCHETYPE_DESCRIPTOR, archetypeJarLoader );

        if ( is == null )
        {
            throw new IOException(
                "The " + Constants.OLDER_ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        return new InputStreamReader( is );
    }

    private InputStream getStream( String name,
                                   ClassLoader loader )
    {
        return
            ( loader == null )
                ? Thread.currentThread().getContextClassLoader().getResourceAsStream( name )
                : loader.getResourceAsStream( name );
    }
}
