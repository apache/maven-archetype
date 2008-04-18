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

import org.apache.maven.archetype.downloader.DownloadException;
import org.apache.maven.archetype.downloader.DownloadNotFoundException;
import org.apache.maven.archetype.downloader.Downloader;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.io.xpp3.ArchetypeDescriptorXpp3Reader;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptorBuilder;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.model.Model;

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
import java.util.List;
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

    /**
     * @plexus.requirement
     */
    private RepositoryMetadataManager repositoryMetadataManager;

    public File getArchetypeFile (
        final String groupId,
        final String artifactId,
        final String version,
        ArtifactRepository archetypeRepository,
        final ArtifactRepository localRepository,
        final List repositories
    )
    throws UnknownArchetype
    {
        try
        {
            return
                downloader.download (
                    groupId,
                    artifactId,
                    version,
                    archetypeRepository,
                    localRepository,
                    repositories
                );
        }
        catch ( DownloadNotFoundException ex )
        {
            throw new UnknownArchetype ( ex );
        }
        catch ( DownloadException ex )
        {
            throw new UnknownArchetype ( ex );
        }
    }

    public ClassLoader getArchetypeJarLoader ( File archetypeFile )
    throws UnknownArchetype
    {
        try
        {
            URL[] urls = new URL[1];

            urls[0] = archetypeFile.toURI ().toURL ();

            return new URLClassLoader ( urls );
        }
        catch ( MalformedURLException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public Model getArchetypePom ( File jar )
    throws XmlPullParserException, UnknownArchetype, IOException
    {
        String pomFileName = null;
        ZipFile zipFile = getArchetypeZipFile ( jar );
        Enumeration enumeration = zipFile.entries ();
        while ( enumeration.hasMoreElements () )
        {
            ZipEntry el = (ZipEntry) enumeration.nextElement ();

            String entry = el.getName ();
            if ( entry.startsWith ( "META-INF" ) && entry.endsWith ( "pom.xml" ) )
            {
                pomFileName = entry;
            }
        }

        if ( pomFileName == null )
        {
            return null;
        }

        ZipEntry pom =
            zipFile.getEntry ( StringUtils.replace ( pomFileName, File.separator, "/" ) );
        if ( pom == null )
        {
            pom = zipFile.getEntry ( StringUtils.replace ( pomFileName, "/", File.separator ) );
        }
        if ( pom == null )
        {
            return null;
        }
        return pomManager.readPom ( zipFile.getInputStream ( pom ) );
    }

    public ZipFile getArchetypeZipFile ( File archetypeFile )
    throws UnknownArchetype
    {
        try
        {
            return new ZipFile ( archetypeFile );
        }
        catch ( ZipException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public boolean isFileSetArchetype ( File archetypeFile )
    {
        try
        {
            ZipFile zipFile = getArchetypeZipFile ( archetypeFile );

            return isFileSetArchetype ( zipFile );
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

    public boolean isFileSetArchetype (
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository archetypeRepository,
        ArtifactRepository localRepository,
        List repositories
    )
    {
        try
        {
            ZipFile zipFile =
                getArchetypeZipFile (
                    getArchetypeFile (
                        groupId,
                        artifactId,
                        version,
                        archetypeRepository,
                        localRepository,
                        repositories
                    )
                );

            return isFileSetArchetype ( zipFile );
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

    public boolean isOldArchetype ( File archetypeFile )
    {
        try
        {
            ZipFile zipFile = getArchetypeZipFile ( archetypeFile );

            return isOldArchetype ( zipFile );
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

    public boolean isOldArchetype (
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository archetypeRepository,
        ArtifactRepository localRepository,
        List repositories
    )
    {
        try
        {
            ZipFile zipFile =
                getArchetypeZipFile (
                    getArchetypeFile (
                        groupId,
                        artifactId,
                        version,
                        archetypeRepository,
                        localRepository,
                        repositories
                    )
                );

            return isOldArchetype ( zipFile );
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

    public boolean exists (
        String archetypeGroupId,
        String archetypeArtifactId,
        String archetypeVersion,
        ArtifactRepository archetypeRepository,
        ArtifactRepository localRepository,
        List remoteRepositories
    )
    {
        try
        {
            File archetypeFile =
                downloader.download (
                    archetypeGroupId,
                    archetypeArtifactId,
                    archetypeVersion,
                    archetypeRepository,
                    localRepository,
                    remoteRepositories
                );

            return archetypeFile.exists ();
        }
        catch ( DownloadException e )
        {
            getLogger ().debug ( "Archetype don't exist", e );
            return false;
        }
        catch ( DownloadNotFoundException e )
        {
            getLogger ().debug ( "Archetype don't exist", e );
            return false;
        }
    }

    public ArchetypeDescriptor getFileSetArchetypeDescriptor ( File archetypeFile )
    throws UnknownArchetype
    {
        try
        {
            ZipFile zipFile = getArchetypeZipFile ( archetypeFile );

            return loadFileSetArchetypeDescriptor ( zipFile );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public org.apache.maven.archetype.metadata.ArchetypeDescriptor getFileSetArchetypeDescriptor (
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository archetypeRepository,
        ArtifactRepository localRepository,
        List repositories
    )
    throws UnknownArchetype
    {
        try
        {
            ZipFile zipFile =
                getArchetypeZipFile (
                    getArchetypeFile (
                        groupId,
                        artifactId,
                        version,
                        archetypeRepository,
                        localRepository,
                        repositories
                    )
                );

            return loadFileSetArchetypeDescriptor ( zipFile );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public List getFilesetArchetypeResources ( File archetypeFile )
    throws UnknownArchetype
    {
        List archetypeResources = new ArrayList ();

        ZipFile zipFile = getArchetypeZipFile ( archetypeFile );

        Enumeration enumeration = zipFile.entries ();
        while ( enumeration.hasMoreElements () )
        {
            ZipEntry entry = (ZipEntry) enumeration.nextElement ();

            if ( !entry.isDirectory ()
                && entry.getName ().startsWith ( Constants.ARCHETYPE_RESOURCES )
            )
            {
                // not supposed to be file.seperator
                String resource =
                    StringUtils.replace (
                        entry.getName (),
                        Constants.ARCHETYPE_RESOURCES + "/",
                        ""
                    );
                getLogger ().debug ( "Found resource " + resource );
                // TODO:FIXME
                archetypeResources.add ( resource );
            }
            else
            {
                getLogger ().debug ( "Not resource " + entry.getName () );
            }
        }
        return archetypeResources;
    }

    public org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor (
        File archetypeFile
    )
    throws UnknownArchetype
    {
        try
        {
            ZipFile zipFile = getArchetypeZipFile ( archetypeFile );

            return loadOldArchetypeDescriptor ( zipFile );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    public org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor (
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository archetypeRepository,
        ArtifactRepository localRepository,
        List repositories
    )
    throws UnknownArchetype
    {
        try
        {
            ZipFile zipFile =
                getArchetypeZipFile (
                    getArchetypeFile (
                        groupId,
                        artifactId,
                        version,
                        archetypeRepository,
                        localRepository,
                        repositories
                    )
                );

            return loadOldArchetypeDescriptor ( zipFile );
        }
        catch ( XmlPullParserException e )
        {
            throw new UnknownArchetype ( e );
        }
        catch ( IOException e )
        {
            throw new UnknownArchetype ( e );
        }
    }

    private Reader getArchetypeDescriptorReader ( ZipFile zipFile )
    throws IOException
    {
        ZipEntry entry =
            zipFile.getEntry (
                StringUtils.replace ( Constants.ARCHETYPE_DESCRIPTOR, File.separator, "/" )
            );

        if ( entry == null )
        {
            getLogger ().debug (
                "No found " + Constants.ARCHETYPE_DESCRIPTOR + " retrying with windows path"
            );
            entry =
                zipFile.getEntry (
                    StringUtils.replace ( Constants.ARCHETYPE_DESCRIPTOR, "/", File.separator )
                );
        }
        if ( entry == null )
        {
            throw new IOException (
                "The " + Constants.ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        InputStream is = zipFile.getInputStream ( entry );

        if ( is == null )
        {
            throw new IOException (
                "The " + Constants.ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }
        return new InputStreamReader ( is );
    }

    private boolean isFileSetArchetype ( ZipFile zipFile )
    throws IOException, XmlPullParserException
    {
        org.apache.maven.archetype.metadata.ArchetypeDescriptor descriptor =
            loadFileSetArchetypeDescriptor ( zipFile );

        return descriptor.getName () != null;
    }

    private boolean isOldArchetype ( ZipFile zipFile )
    throws IOException, XmlPullParserException
    {
        org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor descriptor =
            loadOldArchetypeDescriptor ( zipFile );

        return descriptor.getId () != null;
    }

    private org.apache.maven.archetype.metadata.ArchetypeDescriptor loadFileSetArchetypeDescriptor (
        ZipFile zipFile
    )
    throws IOException, XmlPullParserException
    {
        Reader reader = getArchetypeDescriptorReader ( zipFile );

        ArchetypeDescriptorXpp3Reader archetypeReader = new ArchetypeDescriptorXpp3Reader ();

        try
        {
            return archetypeReader.read ( reader, true );
        }
        finally
        {
            reader.close ();
        }
    }

    private org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor
    loadOldArchetypeDescriptor ( ZipFile zipFile )
    throws IOException, XmlPullParserException
    {
        ArchetypeDescriptorBuilder builder = new ArchetypeDescriptorBuilder ();

        org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor descriptor = null;

        Reader reader = null;
        try
        {
            reader = getOldArchetypeDescriptorReader ( zipFile );

            descriptor = builder.build ( reader );
        }
        catch ( IOException ex )
        {
            getLogger ().debug ( "Can not load old archetype", ex );
        }
        catch ( XmlPullParserException ex )
        {
            getLogger ().debug ( "Can not load old archetype", ex );
        }
        finally
        {
            if ( reader != null )
            {
                reader.close ();
            }
        }

        if ( descriptor == null )
        {
            try
            {
                reader = getOlderArchetypeDescriptorReader ( zipFile );

                descriptor = builder.build ( reader );
            }
            finally
            {
                if ( reader != null )
                {
                    reader.close ();
                }
            }
        }

        return descriptor;
    }

    private Reader getOldArchetypeDescriptorReader ( ZipFile zipFile )
    throws IOException
    {
        ZipEntry entry =
            zipFile.getEntry (
                StringUtils.replace ( Constants.OLD_ARCHETYPE_DESCRIPTOR, File.separator, "/" )
            );

        if ( entry == null )
        {
            getLogger ().debug (
                "No found " + Constants.OLD_ARCHETYPE_DESCRIPTOR + " retrying with windows path"
            );
            entry =
                zipFile.getEntry (
                    StringUtils.replace ( Constants.OLD_ARCHETYPE_DESCRIPTOR, "/", File.separator )
                );
        }

        if ( entry == null )
        {
            throw new IOException (
                "The " + Constants.OLD_ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        InputStream is = zipFile.getInputStream ( entry );

        if ( is == null )
        {
            throw new IOException (
                "The " + Constants.OLD_ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }
        return new InputStreamReader ( is );
    }

    private Reader getOlderArchetypeDescriptorReader ( ZipFile zipFile )
    throws IOException
    {
        ZipEntry entry =
            zipFile.getEntry (
                StringUtils.replace ( Constants.OLDER_ARCHETYPE_DESCRIPTOR, File.separator, "/" )
            );

        if ( entry == null )
        {
            getLogger ().debug (
                "No found " + Constants.OLDER_ARCHETYPE_DESCRIPTOR + " retrying with windows path"
            );
            entry =
                zipFile.getEntry (
                    StringUtils.replace (
                        Constants.OLDER_ARCHETYPE_DESCRIPTOR,
                        "/",
                        File.separator
                    )
                );
        }
        if ( entry == null )
        {
            throw new IOException (
                "The " + Constants.OLDER_ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        InputStream is = zipFile.getInputStream ( entry );

        if ( is == null )
        {
            throw new IOException (
                "The " + Constants.OLDER_ARCHETYPE_DESCRIPTOR + " descriptor cannot be found."
            );
        }

        return new InputStreamReader ( is );
    }
}
