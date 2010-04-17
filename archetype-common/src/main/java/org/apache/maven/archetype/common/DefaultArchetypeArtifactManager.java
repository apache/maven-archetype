package org.apache.maven.archetype.common;

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

import org.apache.maven.archetype.downloader.DownloadException;
import org.apache.maven.archetype.downloader.DownloadNotFoundException;
import org.apache.maven.archetype.downloader.Downloader;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.io.xpp3.ArchetypeDescriptorXpp3Reader;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptorBuilder;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @plexus.component
 */
public class DefaultArchetypeArtifactManager
    extends AbstractLogEnabled
    implements ArchetypeArtifactManager
{
    /**
     * @plexus.requirement
     */
    private Downloader downloader;

    /**
     * @plexus.requirement
     */
    private PomManager pomManager;

    private Map archetypeCache = new TreeMap();

    public File getArchetypeFile( final String groupId, final String artifactId, final String version,
                                  ArtifactRepository archetypeRepository, final ArtifactRepository localRepository,
                                  final List repositories )
        throws UnknownArchetype
    {
        try
        {
            File archetype = getArchetype( groupId, artifactId, version );

            if ( archetype == null )
            {
                archetype =
                    downloader.download( groupId, artifactId, version, archetypeRepository, localRepository,
                                         repositories );

                setArchetype( groupId, artifactId, version, archetype );
            }
            return archetype;
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
        throws UnknownArchetype
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

    public Model getArchetypePom( File jar )
        throws XmlPullParserException, UnknownArchetype, IOException
    {
        ZipFile zipFile = null;
        try
        {
            String pomFileName = null;
            zipFile = getArchetypeZipFile( jar );
            Enumeration enumeration = zipFile.entries();
            while ( enumeration.hasMoreElements() )
            {
                ZipEntry el = (ZipEntry) enumeration.nextElement();

                String entry = el.getName();
                if ( entry.startsWith( "META-INF" ) && entry.endsWith( "pom.xml" ) )
                {
                    pomFileName = entry;
                }
            }

            if ( pomFileName == null )
            {
                return null;
            }

            ZipEntry pom = zipFile.getEntry( StringUtils.replace( pomFileName, File.separator, "/" ) );
            if ( pom == null )
            {
                pom = zipFile.getEntry( StringUtils.replace( pomFileName, "/", File.separator ) );
            }
            if ( pom == null )
            {
                return null;
            }
            return pomManager.readPom( zipFile.getInputStream( pom ) );
        }
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public ZipFile getArchetypeZipFile( File archetypeFile )
        throws UnknownArchetype
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

    public boolean isFileSetArchetype( File archetypeFile )
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = getArchetypeZipFile( archetypeFile );

            return isFileSetArchetype( zipFile );
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
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public boolean isFileSetArchetype( String groupId, String artifactId, String version,
                                       ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                                       List repositories )
    {
        try
        {
            File archetypeFile = getArchetypeFile( groupId, artifactId, version, archetypeRepository,
                                                   localRepository, repositories );
            return isFileSetArchetype( archetypeFile );
        }
        catch ( UnknownArchetype e )
        {
            return false;
        }
    }

    public boolean isOldArchetype( File archetypeFile )
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = getArchetypeZipFile( archetypeFile );

            return isOldArchetype( zipFile );
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
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public boolean isOldArchetype( String groupId, String artifactId, String version,
                                   ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                                   List repositories )
    {
        ZipFile zipFile = null;
        try
        {
            zipFile =
                getArchetypeZipFile( getArchetypeFile( groupId, artifactId, version, archetypeRepository,
                                                       localRepository, repositories ) );

            return isOldArchetype( zipFile );
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
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public boolean exists( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion,
                           ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                           List remoteRepositories )
    {
        try
        {
            File archetype = getArchetype( archetypeGroupId, archetypeArtifactId, archetypeVersion );
            if ( archetype == null )
            {
                archetype =
                    downloader.download( archetypeGroupId, archetypeArtifactId, archetypeVersion, archetypeRepository,
                                         localRepository, remoteRepositories );
                setArchetype( archetypeGroupId, archetypeArtifactId, archetypeVersion, archetype );
            }

            return archetype.exists();
        }
        catch ( DownloadException e )
        {
            getLogger().debug(
                               "Archetype " + archetypeGroupId + ":" + archetypeArtifactId + ":" + archetypeVersion
                                   + " doesn't exist", e );
            return false;
        }
        catch ( DownloadNotFoundException e )
        {
            getLogger().debug(
                              "Archetype " + archetypeGroupId + ":" + archetypeArtifactId + ":" + archetypeVersion
                                  + " doesn't exist", e );
            return false;
        }
    }

    public ArchetypeDescriptor getFileSetArchetypeDescriptor( File archetypeFile )
        throws UnknownArchetype
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = getArchetypeZipFile( archetypeFile );

            return loadFileSetArchetypeDescriptor( zipFile );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype( e );
        }
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public org.apache.maven.archetype.metadata.ArchetypeDescriptor getFileSetArchetypeDescriptor(
                                                                                                  String groupId,
                                                                                                  String artifactId,
                                                                                                  String version,
                                                                                                  ArtifactRepository archetypeRepository,
                                                                                                  ArtifactRepository localRepository,
                                                                                                  List repositories )
        throws UnknownArchetype
    {
        File archetypeFile =
            getArchetypeFile( groupId, artifactId, version, archetypeRepository, localRepository, repositories );

        return getFileSetArchetypeDescriptor( archetypeFile );
    }

    public List getFilesetArchetypeResources( File archetypeFile )
        throws UnknownArchetype
    {
        getLogger().debug( "getFilesetArchetypeResources( \"" + archetypeFile.getAbsolutePath() + "\" )" );
        List archetypeResources = new ArrayList();

        ZipFile zipFile = null;
        try
        {
            zipFile = getArchetypeZipFile( archetypeFile );

            Enumeration enumeration = zipFile.entries();
            while ( enumeration.hasMoreElements() )
            {
                ZipEntry entry = (ZipEntry) enumeration.nextElement();

                if ( !entry.isDirectory() && entry.getName().startsWith( Constants.ARCHETYPE_RESOURCES ) )
                {
                    // not supposed to be file.seperator
                    String resource = entry.getName().substring( Constants.ARCHETYPE_RESOURCES.length() + 1 );
                    getLogger().debug( "  - found resource (" + Constants.ARCHETYPE_RESOURCES + "/)" + resource );
                    // TODO:FIXME
                    archetypeResources.add( resource );
                }
                else
                {
                    getLogger().debug( "  - ignored resource " + entry.getName() );
                }
            }
            return archetypeResources;
        }
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor( File archetypeFile )
        throws UnknownArchetype
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = getArchetypeZipFile( archetypeFile );

            return loadOldArchetypeDescriptor( zipFile );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype( e );
        }
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor(
                                                                                                    String groupId,
                                                                                                    String artifactId,
                                                                                                    String version,
                                                                                                    ArtifactRepository archetypeRepository,
                                                                                                    ArtifactRepository localRepository,
                                                                                                    List repositories )
        throws UnknownArchetype
    {
        File archetypeFile =
            getArchetypeFile( groupId, artifactId, version, archetypeRepository, localRepository, repositories );

        return getOldArchetypeDescriptor( archetypeFile );
    }

    private File getArchetype( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion )
    {
        String key = archetypeGroupId + ":" + archetypeArtifactId + ":" + archetypeVersion;

        if ( archetypeCache.containsKey( key ) )
        {
            getLogger().debug( "Found archetype " + key + " in cache: " + archetypeCache.get( key ) );

            return (File) archetypeCache.get( key );
        }

        getLogger().debug( "Not found archetype " + key + " in cache" );
        return null;
    }

    private void setArchetype( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion,
                               File archetype )
    {
        String key = archetypeGroupId + ":" + archetypeArtifactId + ":" + archetypeVersion;

        archetypeCache.put( key, archetype );
    }

    private boolean isFileSetArchetype( ZipFile zipFile )
        throws IOException, XmlPullParserException
    {
        org.apache.maven.archetype.metadata.ArchetypeDescriptor descriptor = loadFileSetArchetypeDescriptor( zipFile );

        return descriptor.getName() != null;
    }

    private boolean isOldArchetype( ZipFile zipFile )
        throws IOException, XmlPullParserException
    {
        org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor descriptor = loadOldArchetypeDescriptor( zipFile );

        return descriptor.getId() != null;
    }

    private org.apache.maven.archetype.metadata.ArchetypeDescriptor loadFileSetArchetypeDescriptor( ZipFile zipFile )
        throws IOException, XmlPullParserException
    {
        Reader reader = getArchetypeDescriptorReader( zipFile );

        ArchetypeDescriptorXpp3Reader archetypeReader = new ArchetypeDescriptorXpp3Reader();

        try
        {
            return archetypeReader.read( reader, true );
        }
        catch ( IOException e )
        {
            getLogger().debug( "Cannot read archetype descriptor", e );
            throw e;
        }
        catch ( XmlPullParserException e )
        {
            getLogger().error( "Cannot parse archetype descriptor", e );
            throw e;
        }
        finally
        {
            reader.close();
        }
    }

    private org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor loadOldArchetypeDescriptor( ZipFile zipFile )
        throws IOException, XmlPullParserException
    {
        ArchetypeDescriptorBuilder builder = new ArchetypeDescriptorBuilder();

        org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor descriptor = null;

        Reader reader = null;
        try
        {
            reader = getOldArchetypeDescriptorReader( zipFile );

            descriptor = builder.build( reader );
        }
        catch ( IOException ex )
        {
            getLogger().debug( "Cannot load old archetype", ex );
        }
        catch ( XmlPullParserException ex )
        {
            getLogger().error( "Cannot parse old archetype", ex );
        }
        finally
        {
            IOUtil.close( reader );
        }

        if ( descriptor == null )
        {
            try
            {
                reader = getOlderArchetypeDescriptorReader( zipFile );

                descriptor = builder.build( reader );
            }
            finally
            {
                IOUtil.close( reader );
            }
        }

        return descriptor;
    }

    private Reader getArchetypeDescriptorReader( ZipFile zipFile )
        throws IOException
    {
        return getDescriptorReader( zipFile, Constants.ARCHETYPE_DESCRIPTOR );
    }

    private Reader getOldArchetypeDescriptorReader( ZipFile zipFile )
        throws IOException
    {
        return getDescriptorReader( zipFile, Constants.OLD_ARCHETYPE_DESCRIPTOR );
    }

    private Reader getOlderArchetypeDescriptorReader( ZipFile zipFile )
        throws IOException
    {
        return getDescriptorReader( zipFile, Constants.OLDER_ARCHETYPE_DESCRIPTOR );
    }

    private Reader getDescriptorReader( ZipFile zipFile, String descriptor )
        throws IOException
    {
        ZipEntry entry = searchEntry( zipFile, StringUtils.replace( descriptor, File.separator, "/" ) );

        if ( entry == null )
        {
            getLogger().debug( "No found " + descriptor + " retrying with windows path separator." );

            entry = searchEntry( zipFile, StringUtils.replace( descriptor, "/", File.separator ) );
        }

        if ( entry == null )
        {
            throw new IOException( "The " + descriptor + " descriptor cannot be found in " + zipFile.getName() + "." );
        }

        InputStream is = zipFile.getInputStream( entry );

        if ( is == null )
        {
            throw new IOException( "The " + descriptor + " descriptor cannot be read in " + zipFile.getName() + "." );
        }

        return ReaderFactory.newXmlReader( is );
    }

    private ZipEntry searchEntry( ZipFile zipFile, String searchString )
    {
        getLogger().debug( "Searching for " + searchString + " inside " + zipFile.getName() );

        Enumeration enu = zipFile.entries();
        while ( enu.hasMoreElements() )
        {
            ZipEntry entryfound = (ZipEntry) enu.nextElement();
            getLogger().debug( "  - " + entryfound.getName() );

            if ( searchString.equals( entryfound.getName() ) )
            {
                getLogger().debug( "Entry found" );
                return entryfound;
            }
        }
        return null;
    }

    private void closeZipFile( ZipFile zipFile )
    {
        try
        {
            zipFile.close();
        }
        catch ( Exception e )
        {
            getLogger().error( "Failed to close zipFile" );
        }
    }
}
