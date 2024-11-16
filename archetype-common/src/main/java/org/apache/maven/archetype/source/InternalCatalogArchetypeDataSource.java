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

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason van Zyl
 */
@Named("internal-catalog")
@Singleton
public class InternalCatalogArchetypeDataSource extends CatalogArchetypeDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalCatalogArchetypeDataSource.class);

    @Override
    public ArchetypeCatalog getArchetypeCatalog(
            RepositorySystemSession repositorySession, List<RemoteRepository> remoteRepositories)
            throws ArchetypeDataSourceException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(ARCHETYPE_CATALOG_FILENAME);
                Reader reader = new XmlStreamReader(in)) {
            return readCatalog(reader);
        } catch (IOException e) {
            throw new ArchetypeDataSourceException("Error reading archetype catalog.", e);
        }
    }

    @Override
    public File updateCatalog(RepositorySystemSession repositorySession, Archetype archetype)
            throws ArchetypeDataSourceException {
        throw new ArchetypeDataSourceException("Not supported yet.");
    }
}
