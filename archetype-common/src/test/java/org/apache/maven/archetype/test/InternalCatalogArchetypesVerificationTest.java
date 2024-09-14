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

import java.io.File;
import java.util.Properties;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;

/**
 *
 * @author rafale
 */
public class InternalCatalogArchetypesVerificationTest extends PlexusTestCase {
    private static final String CENTRAL = "https://repo.maven.apache.org/maven2";

    @Override
    protected void customizeContainerConfiguration(ContainerConfiguration configuration) {
        configuration.setClassPathScanning("index");
    }

    public void testInternalCatalog() throws Exception {
        ArtifactRepository localRepository = createRepository(
                new File(getBasedir(), "target/test-classes/repositories/local")
                        .toURI()
                        .toURL()
                        .toExternalForm(),
                "local-repo");

        File outputDirectory = new File(getBasedir(), "target/internal-archetypes-projects");
        outputDirectory.mkdirs();
        FileUtils.cleanDirectory(outputDirectory);

        ArchetypeManager archetypeManager = (ArchetypeManager) lookup(ArchetypeManager.class.getName());
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

            ArchetypeGenerationRequest request = new ArchetypeGenerationRequest(archetype)
                    .setGroupId("org.apache.maven.archetypeManager.test")
                    .setArtifactId("archetypeManager" + count)
                    .setVersion("1.0-SNAPSHOT")
                    .setPackage("com.acme")
                    .setProperties(props)
                    .setOutputDirectory(outputDirectory.getPath());

            ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
            DefaultRepositorySystemSession repositorySession = new DefaultRepositorySystemSession();
            RepositorySystem repositorySystem = lookup(RepositorySystem.class);
            LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(
                    repositorySession, new LocalRepository(localRepository.getBasedir()));
            repositorySession.setLocalRepositoryManager(localRepositoryManager);
            buildingRequest.setRepositorySession(repositorySession);
            request.setProjectBuildingRequest(buildingRequest);

            ArchetypeGenerationResult generationResult = archetypeManager.generateProjectFromArchetype(request);

            assertNull(
                    "Archetype wasn't generated successfully: " + generationResult.getCause(),
                    generationResult.getCause());

            count++;
        }
    }

    private ArtifactRepository createRepository(String url, String repositoryId) {
        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;

        ArtifactRepositoryPolicy snapshotsPolicy =
                new ArtifactRepositoryPolicy(true, updatePolicyFlag, checksumPolicyFlag);

        ArtifactRepositoryPolicy releasesPolicy =
                new ArtifactRepositoryPolicy(true, updatePolicyFlag, checksumPolicyFlag);

        return new MavenArtifactRepository(
                repositoryId, url, new DefaultRepositoryLayout(), snapshotsPolicy, releasesPolicy);
    }
}
