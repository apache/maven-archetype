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
package org.apache.maven.archetype.downloader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.util.List;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * @author Jason van Zyl
 */
@Named
@Singleton
public class DefaultDownloader implements Downloader {

    @Inject
    private RepositorySystem repositorySystem;

    @Override
    public File download(
            String groupId,
            String artifactId,
            String version,
            List<RemoteRepository> remoteRepositories,
            RepositorySystemSession repositorySystemSession)
            throws DownloadException, DownloadNotFoundException {

        DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, "", "jar", version);

        ArtifactRequest request = new ArtifactRequest(artifact, remoteRepositories, "archetype");
        try {
            ArtifactResult result = repositorySystem.resolveArtifact(repositorySystemSession, request);
            return result.getArtifact().getFile();
        } catch (ArtifactResolutionException e) {
            throw new DownloadException("Could not resolve artifact " + artifactId, e);
        }
    }
}
