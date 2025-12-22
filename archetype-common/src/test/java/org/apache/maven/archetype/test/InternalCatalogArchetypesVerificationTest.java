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
package org.apache.maven.archetype.test;

import javax.inject.Inject;

import java.io.File;
import java.util.Properties;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.codehaus.plexus.testing.PlexusTest;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.testing.PlexusExtension.getBasedir;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author rafale
 */
@PlexusTest
public class InternalCatalogArchetypesVerificationTest {
    private static final String CENTRAL = "https://repo.maven.apache.org/maven2";

    @Inject
    private ArchetypeManager archetypeManager;

    @Inject
    private RepositorySystem repositorySystem;

    @Test
    public void testInternalCatalog() throws Exception {

        File outputDirectory = new File(getBasedir(), "target/internal-archetypes-projects");
        outputDirectory.mkdirs();
        FileUtils.cleanDirectory(outputDirectory);

        ArchetypeCatalog catalog = archetypeManager.getInternalCatalog();

        // quickstart has a parameters with defaults ... so it should not be needed
        // can be connected with ARCHETYPE-574
        Properties props = new Properties();
        props.put("javaCompilerVersion", "11");
        props.put("junitVersion", "5.11.0");

        int count = 1;
        for (Archetype archetype : catalog.getArchetypes()) {
            // this should be also default ...
            archetype.setRepository(CENTRAL);

            DefaultRepositorySystemSession repositorySession = new DefaultRepositorySystemSession();
            LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(
                    repositorySession, new LocalRepository("target/test-classes/repositories/local"));
            repositorySession.setLocalRepositoryManager(localRepositoryManager);

            ArchetypeGenerationRequest request = new ArchetypeGenerationRequest(archetype)
                    .setGroupId("org.apache.maven.archetype.test")
                    .setArtifactId("archetype" + count)
                    .setVersion("1.0-SNAPSHOT")
                    .setPackage("com.acme")
                    .setProperties(props)
                    .setOutputDirectory(outputDirectory.getPath())
                    .setRepositorySession(repositorySession);

            ArchetypeGenerationResult generationResult = archetypeManager.generateProjectFromArchetype(request);

            assertNull(
                    generationResult.getCause(),
                    "Archetype wasn't generated successfully: " + generationResult.getCause());

            count++;
        }
    }
}
