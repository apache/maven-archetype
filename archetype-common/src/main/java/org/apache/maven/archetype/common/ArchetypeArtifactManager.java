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

import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import java.util.List;
import java.util.zip.ZipFile;

public interface ArchetypeArtifactManager
{
    String ROLE = ArchetypeArtifactManager.class.getName ();

    Model getArchetypePom ( File jar )
        throws XmlPullParserException, UnknownArchetype, IOException;

    /**
     */
    File getArchetypeFile( String groupId, String artifactId, String version,
                           ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                           List<ArtifactRepository> repositories )
        throws UnknownArchetype;

    /**
     */
    ClassLoader getArchetypeJarLoader( File archetypeFile )
        throws UnknownArchetype;

    /**
     */
    ZipFile getArchetypeZipFile( File archetypeFile )
        throws UnknownArchetype;

    /**
     */
    boolean isFileSetArchetype( File archetypeFile );

    /**
     */
    boolean isFileSetArchetype( String groupId, String artifactId, String version,
                                ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                                List<ArtifactRepository> repositories );

    /**
     */
    boolean isOldArchetype( File archetypeFile );

    /**
     */
    boolean isOldArchetype( String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
                            ArtifactRepository localRepository, List<ArtifactRepository> repositories );

    /**
     */
    boolean exists( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion,
                    ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                    List<ArtifactRepository> repos );

    /**
     */
    ArchetypeDescriptor getFileSetArchetypeDescriptor( File archetypeFile )
        throws UnknownArchetype;

    /**
     */
    ArchetypeDescriptor getFileSetArchetypeDescriptor( String groupId, String artifactId, String version,
                                                       ArtifactRepository archetypeRepository,
                                                       ArtifactRepository localRepository,
                                                       List<ArtifactRepository> repositories )
        throws UnknownArchetype;

    /**
     */
    List<String> getFilesetArchetypeResources( File archetypeFile )
        throws UnknownArchetype;

    /**
     */
    org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor( File archetypeFile )
        throws UnknownArchetype;

    /**
     */
    org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor(
            String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
            ArtifactRepository localRepository, List<ArtifactRepository> repositories )
        throws UnknownArchetype;

    /**
     */
    Reader getScriptFileReader( File archetypeFile, String scriptFile )
        throws UnknownArchetype;
}
