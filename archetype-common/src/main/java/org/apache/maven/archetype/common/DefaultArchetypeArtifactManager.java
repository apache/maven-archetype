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

    private Map<String, File> archetypeCache = new TreeMap<String, File>();

    public File getArchetypeFile( final String groupId, final String artifactId, final String version,
                                  ArtifactRepository archetypeRepository, final ArtifactRepository localRepository,
                                  final List<ArtifactRepository> repositories )
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

            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
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

            ZipEntry pom = zipFile.getEntry( pomFileName );

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
            getLogger().debug( "checking fileset archetype status on " + archetypeFile );

            zipFile = getArchetypeZipFile( archetypeFile );

            return isFileSetArchetype( zipFile );
        }
        catch ( IOException e )
        {
            getLogger().debug( e.toString() );
            return false;
        }
        catch ( UnknownArchetype e )
        {
            getLogger().debug( e.toString() );
            return false;
        }
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public boolean isFileSetArchetype( String groupId, String artifactId, String version,
                                       ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                                       List<ArtifactRepository> repositories )
    {
        try
        {
            File archetypeFile = getArchetypeFile( groupId, artifactId, version, archetypeRepository,
                                                   localRepository, repositories );

            return isFileSetArchetype( archetypeFile );
        }
        catch ( UnknownArchetype e )
        {
            getLogger().debug( e.toString() );
            return false;
        }
    }

    public boolean isOldArchetype( File archetypeFile )
    {
        ZipFile zipFile = null;
        try
        {
            getLogger().debug( "checking old archetype status on " + archetypeFile );

            zipFile = getArchetypeZipFile( archetypeFile );

            return isOldArchetype( zipFile );
        }
        catch ( IOException e )
        {
            getLogger().debug( e.toString() );
            return false;
        }
        catch ( UnknownArchetype e )
        {
            getLogger().debug( e.toString() );
            return false;
        }
        finally
        {
            closeZipFile( zipFile );
        }
    }

    public boolean isOldArchetype( String groupId, String artifactId, String version,
                                   ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                                   List<ArtifactRepository> repositories )
    {
        try
        {
            File archetypeFile = getArchetypeFile( groupId, artifactId, version, archetypeRepository,
                                                   localRepository, repositories );

            return isOldArchetype( archetypeFile );
        }
        catch ( UnknownArchetype e )
        {
            getLogger().debug( e.toString() );
            return false;
        }
    }

    public boolean exists( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion,
                           ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                           List<ArtifactRepository> remoteRepositories )
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
            String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
            ArtifactRepository localRepository, List<ArtifactRepository> repositories )
        throws UnknownArchetype
    {
        File archetypeFile =
            getArchetypeFile( groupId, artifactId, version, archetypeRepository, localRepository, repositories );

        return getFileSetArchetypeDescriptor( archetypeFile );
    }

    public List<String> getFilesetArchetypeResources( File archetypeFile )
        throws UnknownArchetype
    {
        getLogger().debug( "getFilesetArchetypeResources( \"" + archetypeFile.getAbsolutePath() + "\" )" );
        List<String> archetypeResources = new ArrayList<String>();

        ZipFile zipFile = null;
        try
        {
            zipFile = getArchetypeZipFile( archetypeFile );

            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
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
            String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
            ArtifactRepository localRepository, List<ArtifactRepository> repositories )
        throws UnknownArchetype
    {
        File archetypeFile =
            getArchetypeFile( groupId, artifactId, version, archetypeRepository, localRepository, repositories );

        return getOldArchetypeDescriptor( archetypeFile );
    }
    
    public Reader getScriptFileReader( File archetypeFile, String scriptFile )
        throws UnknownArchetype
    {
        ZipFile zipFile = null;
        try
        {
            zipFile = getArchetypeZipFile( archetypeFile );

            return getDescriptorReader( zipFile, scriptFile );
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

    private File getArchetype( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion )
    {
        String key = archetypeGroupId + ":" + archetypeArtifactId + ":" + archetypeVersion;

        if ( archetypeCache.containsKey( key ) )
        {
            getLogger().debug( "Found archetype " + key + " in cache: " + archetypeCache.get( key ) );

            return archetypeCache.get( key );
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
        throws IOException
    {
        Reader reader = null;
        try
        {
            reader = getArchetypeDescriptorReader( zipFile );

            return ( reader != null );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    private boolean isOldArchetype( ZipFile zipFile )
        throws IOException
    {
        Reader reader = null;
        try
        {
            reader = getOldArchetypeDescriptorReader( zipFile );

            return ( reader != null );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    private org.apache.maven.archetype.metadata.ArchetypeDescriptor loadFileSetArchetypeDescriptor( ZipFile zipFile )
        throws IOException, XmlPullParserException
    {
        Reader reader = null;
        try
        {
            reader = getArchetypeDescriptorReader( zipFile );

            if ( reader == null )
            {
                return null;
            }

            ArchetypeDescriptorXpp3Reader archetypeReader = new ArchetypeDescriptorXpp3Reader();
            return archetypeReader.read( reader, false );
        }
        catch ( IOException e )
        {
            throw e;
        }
        catch ( XmlPullParserException e )
        {
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
        Reader reader = null;
        try
        {
            reader = getOldArchetypeDescriptorReader( zipFile );

            if ( reader == null )
            {
                return null;
            }

            ArchetypeDescriptorBuilder builder = new ArchetypeDescriptorBuilder();
            return builder.build( reader );
        }
        catch ( IOException ex )
        {
            throw ex;
        }
        catch ( XmlPullParserException ex )
        {
            throw ex;
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    private Reader getArchetypeDescriptorReader( ZipFile zipFile )
        throws IOException
    {
        return getDescriptorReader( zipFile, Constants.ARCHETYPE_DESCRIPTOR );
    }

    private Reader getOldArchetypeDescriptorReader( ZipFile zipFile )
        throws IOException
    {
        Reader reader = getDescriptorReader( zipFile, Constants.OLD_ARCHETYPE_DESCRIPTOR );

        if ( reader == null )
        {
            reader = getDescriptorReader( zipFile, Constants.OLDER_ARCHETYPE_DESCRIPTOR );
        }

        return reader;
    }

    private Reader getDescriptorReader( ZipFile zipFile, String descriptor )
        throws IOException
    {
        ZipEntry entry = searchEntry( zipFile, descriptor );

        if ( entry == null )
        {
            return null;
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
        if( searchString == null )
        {
            return null;
        }
    
        getLogger().debug( "Searching for " + searchString + " inside " + zipFile.getName() );

        Enumeration<? extends ZipEntry> enu = zipFile.entries();
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
            getLogger().error( "Failed to close " + zipFile.getName() + " zipFile." );
        }
    }
}
