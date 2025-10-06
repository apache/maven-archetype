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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

public interface ArchetypeArtifactManager {

    Model getArchetypePom(File jar) throws XmlPullParserException, UnknownArchetype, IOException;

    File getArchetypeFile(
            String groupId,
            String artifactId,
            String version,
            List<RemoteRepository> repositories,
            RepositorySystemSession repositorySystemSession)
            throws UnknownArchetype;

    ClassLoader getArchetypeJarLoader(File archetypeFile) throws UnknownArchetype;

    ZipFile getArchetypeZipFile(File archetypeFile) throws UnknownArchetype;

    boolean isFileSetArchetype(File archetypeFile);

    boolean isOldArchetype(File archetypeFile);

    boolean exists(
            String archetypeGroupId,
            String archetypeArtifactId,
            String archetypeVersion,
            List<RemoteRepository> repos,
            RepositorySystemSession repositorySystemSession);

    /**
     * Get the archetype file's post-generation script content, read as UTF-8 content.
     *
     * @param archetypeFile the archetype file
     * @return the archetype file's post-generation script content or <code>null</code> if there is no script in the
     *   archetype
     * @throws UnknownArchetype
     */
    String getPostGenerationScript(File archetypeFile) throws UnknownArchetype;

    ArchetypeDescriptor getFileSetArchetypeDescriptor(File archetypeFile) throws UnknownArchetype;

    List<String> getFilesetArchetypeResources(File archetypeFile) throws UnknownArchetype;

    org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor getOldArchetypeDescriptor(File archetypeFile)
            throws UnknownArchetype;
}
