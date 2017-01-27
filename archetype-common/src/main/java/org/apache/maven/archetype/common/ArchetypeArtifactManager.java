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
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.zip.ZipFile;

public interface ArchetypeArtifactManager
{
    String ROLE = ArchetypeArtifactManager.class.getName ();

    Model getArchetypePom ( File jar )
        throws XmlPullParserException, UnknownArchetype, IOException;

    /**
     * @param buildingRequest TODO
     */
    File getArchetypeFile( String groupId, String artifactId, String version,
                           ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                           List<ArtifactRepository> repositories, ProjectBuildingRequest buildingRequest )
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
     * @param buildingRequest TODO
     */
    boolean isFileSetArchetype( String groupId, String artifactId, String version,
                                ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                                List<ArtifactRepository> repositories, ProjectBuildingRequest buildingRequest );

    /**
     */
    boolean isOldArchetype( File archetypeFile );

    /**
     * @param buildingRequest TODO
     */
    boolean isOldArchetype( String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
                            ArtifactRepository localRepository, List<ArtifactRepository> repositories,
                            ProjectBuildingRequest buildingRequest );

    /**
     * @param buildingRequest TODO
     */
    boolean exists( String archetypeGroupId, String archetypeArtifactId, String archetypeVersion,
                    ArtifactRepository archetypeRepository, ArtifactRepository localRepository,
                    List<ArtifactRepository> repos, ProjectBuildingRequest buildingRequest );

    /**
     * Get the archetype file's post-generation script content, read as UTF-8 content.
     * 
     * @param archetypeFile the archetype file
     * @return the archetype file's post-generation script content or <code>null</code> if there is no script in the
     *   archetype
     * @throws UnknownArchetype
     */
    String getPostGenerationScript( File archetypeFile )
        throws UnknownArchetype;

    /**
     */
    ArchetypeDescriptor getFileSetArchetypeDescriptor( File archetypeFile )
        throws UnknownArchetype;

    /**
     * @param buildingRequest TODO
     */
    ArchetypeDescriptor getFileSetArchetypeDescriptor( String groupId, String artifactId, String version,
                                                       ArtifactRepository archetypeRepository,
                                                       ArtifactRepository localRepository,
                                                       List<ArtifactRepository> repositories,
                                                       ProjectBuildingRequest buildingRequest )
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
     * @param buildingRequest TODO
     */
    org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor( String groupId,
                                                                     String artifactId,
                                                                     String version,
                                                                     ArtifactRepository archetypeRepository,
                                                                     ArtifactRepository localRepository,
                                                                     List<ArtifactRepository> repositories,
                                                                     ProjectBuildingRequest buildingRequest )
        throws UnknownArchetype;
}
