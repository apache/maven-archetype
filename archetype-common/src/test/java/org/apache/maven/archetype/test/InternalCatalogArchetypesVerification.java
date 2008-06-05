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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.archetype.Archetype;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 * @author rafale
 */
public class InternalCatalogArchetypesVerification
        extends PlexusTestCase {

    public void testInternalCatalog()
            throws Exception {
        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup(ArchetypeRegistryManager.ROLE);

        ArtifactRepository localRepository = registryManager.createRepository(new File(getBasedir(),
                "target/test-classes/repositories/local").toURI().
                toURL().
                toExternalForm(),
                "local-repo");

        File outputDirectory = new File(getBasedir(), "target/internal-archetypes-projects");
        outputDirectory.mkdirs();

        Archetype archetype = (Archetype) lookup(Archetype.class.getName());

        ArchetypeCatalog result = archetype.getInternalCatalog();

        List archetypesUsed = new ArrayList();
        List archetypesRemoved = new ArrayList();
        Iterator archetypes = result.getArchetypes().iterator();
        int count = 1;
        while (archetypes.hasNext()) {
            org.apache.maven.archetype.catalog.Archetype a = (org.apache.maven.archetype.catalog.Archetype) archetypes.next();
            org.apache.maven.archetype.catalog.Archetype ar = new org.apache.maven.archetype.catalog.Archetype();

            ar.setGroupId(a.getGroupId());
            ar.setArtifactId(a.getArtifactId());
            ar.setVersion("RELEASE");
            ar.setDescription(a.getDescription());
            ar.setGoals(a.getGoals());
            ar.setProperties(a.getProperties());
            ar.setRepository(a.getRepository());
            if (ar.getRepository() == null) {
                ar.setRepository("http://repo1.maven.org/maven2");
            }
            System.err.println("\n\n\n\n\n\nTesting archetype " + ar);
            ArchetypeGenerationRequest request = new ArchetypeGenerationRequest(ar).setGroupId("groupId" + count).setArtifactId("artifactId" + count).setVersion("version" + count).setPackage("package" + count).setOutputDirectory(outputDirectory.getPath()).setLocalRepository(localRepository);
            ArchetypeGenerationResult generationResult = archetype.generateProjectFromArchetype(request);
            if (generationResult != null && generationResult.getCause() != null) {
                ar.setVersion(a.getVersion());
                request = new ArchetypeGenerationRequest(ar).setGroupId("groupId" + count).setArtifactId("artifactId" + count).setVersion("version" + count).setPackage("package" + count).setOutputDirectory(outputDirectory.getPath()).setLocalRepository(localRepository);
                generationResult = archetype.generateProjectFromArchetype(request);
                if (generationResult != null && generationResult.getCause() != null) {
                    archetypesRemoved.add(a);
                } else {
                    archetypesUsed.add(a);
                }
            } else {
                archetypesUsed.add(a);
            }
            count++;
            System.err.println("\n\n\n\n\n");
        }
        result.setArchetypes(archetypesUsed);

        StringWriter sw = new StringWriter();
        ArchetypeCatalogXpp3Writer acxw = new ArchetypeCatalogXpp3Writer();
        acxw.write(sw, result);

        System.err.println("Resulting catalog is\n" + sw.toString());
        System.err.println("Removed archetypes are \n" + archetypesRemoved);
    }
}
