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
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;

/** @author Jason van Zyl */
public class ArchetypeGenerationTest extends PlexusTestCase {

    @Override
    protected void customizeContainerConfiguration(ContainerConfiguration configuration) {
        configuration.setClassPathScanning("index");
    }

    public void testProjectGenerationFromAnArchetype() throws Exception {
        ArchetypeManager archetype = (ArchetypeManager) lookup(ArchetypeManager.ROLE);

        DefaultRepositorySystemSession repositorySession = new DefaultRepositorySystemSession();
        RepositorySystem repositorySystem = lookup(RepositorySystem.class);
        LocalRepositoryManager localRepositoryManager = repositorySystem.newLocalRepositoryManager(
                repositorySession, new LocalRepository("target/test-classes/repositories/central"));
        repositorySession.setLocalRepositoryManager(localRepositoryManager);

        ArchetypeCatalog catalog = archetype.getLocalCatalog(repositorySession);

        System.err.println("archetypes => " + catalog.getArchetypes());
        // Here I am just grabbing a OldArchetype but in a UI you would take the OldArchetype objects and present
        // them to the user.

        Archetype selection =
                catalog.getArchetypes().get(catalog.getArchetypes().size() - 1);

        System.err.println("Selected OldArchetype = " + selection);
        // Now you will present a dialog, or whatever, and grab the following values.

        String groupId = "com.mycompany";

        String artifactId = "app";

        String version = "1.0.0";

        String packageName = "org.mycompany.app";

        // With the selected OldArchetype and the parameters you can create a generation request as follows:
        File outputDirectory = new File(getBasedir(), "target/test-classes/projects/archetyper-generate-1");
        FileUtils.forceDelete(outputDirectory);

        ArchetypeGenerationRequest agr = new ArchetypeGenerationRequest(selection)
                .setOutputDirectory(outputDirectory.getAbsolutePath())
                .setGroupId(groupId)
                .setArtifactId(artifactId)
                .setVersion(version)
                .setPackage(packageName)
                .setRepositorySession(repositorySession);

        Properties archetypeRequiredProperties = new Properties();
        archetypeRequiredProperties.setProperty("property-with-default-1", "value-1");
        archetypeRequiredProperties.setProperty("property-with-default-2", "value-2");
        archetypeRequiredProperties.setProperty("property-with-default-3", "value-3");
        archetypeRequiredProperties.setProperty("property-with-default-4", "value-4");
        archetypeRequiredProperties.setProperty("property-without-default-1", "some-value-1");
        archetypeRequiredProperties.setProperty("property-without-default-2", "some-value-2");
        archetypeRequiredProperties.setProperty("property-without-default-3", "some-value-3");
        archetypeRequiredProperties.setProperty("property-without-default-4", "some-value-4");
        archetypeRequiredProperties.setProperty("property_underscored_1", "prop1");
        archetypeRequiredProperties.setProperty("property_underscored-2", "prop2");
        agr.setProperties(archetypeRequiredProperties);

        // Then generate away!

        ArchetypeGenerationResult result = archetype.generateProjectFromArchetype(agr);

        if (result.getCause() != null) {
            result.getCause().printStackTrace(System.err);
            fail(result.getCause().getMessage());
        }
    }
}
