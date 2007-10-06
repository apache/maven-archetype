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
import org.apache.maven.artifact.repository.ArtifactRepository;

import java.io.File;
import java.util.List;
import java.util.zip.ZipFile;

public interface ArchetypeArtifactManager
{
    String ROLE = ArchetypeArtifactManager.class.getName();

    /**
     */
    File getArchetypeFile(
        final String groupId,
        final String artifactId,
        final String version,
        final ArtifactRepository localRepository,
        final List repositories
    )
        throws
        UnknownArchetype;

    /**
     */
    ClassLoader getArchetypeJarLoader( File archetypeFile )
        throws
        UnknownArchetype;

    /**
     */
    ClassLoader getArchetypeJarLoader(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype;

    /**
     */
    List getArchetypes( String groupId,
                        ArtifactRepository localRepository,
                        List repositories )
        throws
        UnknownGroup;

    /**
     */
    ZipFile getArchetypeZipFile( File archetypeFile )
        throws
        UnknownArchetype;

    /**
     */
    ZipFile getArchetypeZipFile(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype;

    boolean exists( ArchetypeDefinition ad,
                    ArtifactRepository localRepository,
                    List remoteRepositories );

    boolean isFileSetArchetype(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    );

    /**
     */
    ArchetypeDescriptor getFileSetArchetypeDescriptor( File archetypeFile )
        throws
        UnknownArchetype;

    /**
     */
    org.apache.maven.archetype.metadata.ArchetypeDescriptor getFileSetArchetypeDescriptor(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype;

    /**
     */
    List getFilesetArchetypeResources( File archetypeFile )
        throws
        UnknownArchetype;

    /**
     */
    List getFilesetArchetypeResources(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype;

    /**
     */
    boolean isOldArchetype(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    );

    /**
     */
    org.apache.maven.archetype.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor(
        String groupId,
        String artifactId,
        String version,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype;

    /**
     */
    String getReleaseVersion(
        String groupId,
        String artifactId,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype;

    /**
     */
    List getVersions(
        String groupId,
        String artifactId,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        UnknownArchetype;
}
