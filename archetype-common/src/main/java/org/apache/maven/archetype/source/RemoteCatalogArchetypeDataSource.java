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
package org.apache.maven.archetype.source;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.MetadataRequest;
import org.eclipse.aether.resolution.MetadataResult;

/**
 * @author Jason van Zyl
 */
@Named("remote-catalog")
@Singleton
public class RemoteCatalogArchetypeDataSource extends CatalogArchetypeDataSource implements ArchetypeDataSource {

    @Inject
    private RepositorySystem repositorySystem;

    /**
     * Id of the repository used to download catalog file. Proxy or authentication info can
     * be setup in settings.xml.
     */
    public static final String ARCHETYPE_REPOSITORY_ID = "archetype";

    public static final String CENTRAL_REPOSITORY_ID = "central";

    @Override
    public ArchetypeCatalog getArchetypeCatalog(
            RepositorySystemSession repositorySession, List<RemoteRepository> remoteRepositories)
            throws ArchetypeDataSourceException {

        MetadataRequest request = new MetadataRequest();
        request.setRepository(getRemoteRepo(remoteRepositories));
        request.setMetadata(new DefaultMetadata(ARCHETYPE_CATALOG_FILENAME, Metadata.Nature.RELEASE));

        MetadataResult metadataResult = repositorySystem
                .resolveMetadata(repositorySession, Collections.singletonList(request))
                .get(0);

        if (metadataResult.isResolved()) {
            try {
                return readCatalog(
                        new XmlStreamReader(metadataResult.getMetadata().getFile()));
            } catch (IOException e) {
                throw new ArchetypeDataSourceException(e);
            }
        } else {
            throw new ArchetypeDataSourceException(metadataResult.getException());
        }
    }

    private RemoteRepository getRemoteRepo(List<RemoteRepository> remoteRepositories) {

        if (remoteRepositories == null || remoteRepositories.isEmpty()) {
            return null;
        }

        for (RemoteRepository remoteRepository : remoteRepositories) {
            if (ARCHETYPE_REPOSITORY_ID.equals(remoteRepository.getId())) {
                return remoteRepository;
            }

            if (CENTRAL_REPOSITORY_ID.equals(remoteRepository.getId())) {
                return remoteRepository;
            }

            if (getRemoteRepo(remoteRepository.getMirroredRepositories()) != null) {
                return remoteRepository;
            }
        }

        return null;
    }

    @Override
    public File updateCatalog(RepositorySystemSession repositorySession, Archetype archetype)
            throws ArchetypeDataSourceException {
        throw new ArchetypeDataSourceException("Not supported yet.");
    }
}
