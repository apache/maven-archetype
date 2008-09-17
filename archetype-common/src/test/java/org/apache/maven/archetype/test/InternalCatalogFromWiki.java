/*
 *  Copyright 2008 rafale.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.maven.archetype.test;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 * @author rafale
 */
public class InternalCatalogFromWiki
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

        org.apache.maven.archetype.Archetype plexusarchetype = (org.apache.maven.archetype.Archetype) lookup(org.apache.maven.archetype.Archetype.class.getName());
        ArchetypeArtifactManager aam = (ArchetypeArtifactManager) lookup(ArchetypeArtifactManager.class.getName());
        ArchetypeRegistryManager arm = (ArchetypeRegistryManager) lookup(ArchetypeRegistryManager.class.getName());


//        ArchetypeDataSource ads =  (ArchetypeDataSource) lookup( ArchetypeDataSource.ROLE, "wiki" );
        ArchetypeDataSource ads = new WikiArchetypeDataSource();

        ArchetypeCatalog ac = ads.getArchetypeCatalog(new Properties());
        List modifiedArchetypes = new ArrayList();
        Iterator archetypes = ac.getArchetypes().iterator();
        while (archetypes.hasNext()) {
            Archetype archetype = (Archetype) archetypes.next();
            if (archetype.getRepository() != null &&
                    archetype.getRepository().indexOf("repo1.apache.org/maven2") >= 0) {
                archetype.setRepository(null);
            }
            modifiedArchetypes.add(archetype);
        }
        ac.setArchetypes(modifiedArchetypes);

        System.out.println("AR=" + ac.getArchetypes());

        StringWriter sw = new StringWriter();

        ArchetypeCatalogXpp3Writer acxw = new ArchetypeCatalogXpp3Writer();
        acxw.write(sw, ac);

        System.out.println("AC=" + sw.toString());



        List archetypesUsed = new ArrayList();
        List archetypesRemoved = new ArrayList();
        archetypes = ac.getArchetypes().iterator();
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

            Properties properties = new Properties();
            if (aam.isFileSetArchetype(a.getGroupId(), a.getArtifactId(), "RELEASE", arm.createRepository(
                    a.getRepository(),
                    a.getRepository() + "-repo"), localRepository, new ArrayList( /*repositories*/))) {
                ArchetypeDescriptor descriptor = aam.getFileSetArchetypeDescriptor(a.getGroupId(), a.getArtifactId(), "RELEASE", arm.createRepository(
                        a.getRepository(),
                        a.getRepository() + "-repo"), localRepository, new ArrayList( /*repositories*/));

                Iterator required = descriptor.getRequiredProperties().iterator();
                while (required.hasNext()) {
                    RequiredProperty prop = (RequiredProperty) required.next();

                    properties.setProperty(prop.getKey(), prop.getDefaultValue() != null && !"".equals(prop.getDefaultValue()) ? prop.getDefaultValue() : "test-value");
                }

            }
            request.setProperties(properties);
            ArchetypeGenerationResult generationResult = plexusarchetype.generateProjectFromArchetype(request);
            if (generationResult != null && generationResult.getCause() != null) {
                ar.setVersion(a.getVersion());
                request = new ArchetypeGenerationRequest(ar).setGroupId("groupId" + count).setArtifactId("artifactId" + count).setVersion("version" + count).setPackage("package" + count).setOutputDirectory(outputDirectory.getPath()).setLocalRepository(localRepository);
                properties = new Properties();
                if (aam.isFileSetArchetype(a.getGroupId(), a.getArtifactId(), a.getVersion(), arm.createRepository(
                        a.getRepository(),
                        a.getRepository() + "-repo"), localRepository, new ArrayList( /*repositories*/))) {
                    ArchetypeDescriptor descriptor = aam.getFileSetArchetypeDescriptor(a.getGroupId(), a.getArtifactId(), a.getVersion(), arm.createRepository(
                            a.getRepository(),
                            a.getRepository() + "-repo"), localRepository, new ArrayList( /*repositories*/));

                    Iterator required = descriptor.getRequiredProperties().iterator();
                    while (required.hasNext()) {
                        RequiredProperty prop = (RequiredProperty) required.next();

                        properties.setProperty(prop.getKey(), prop.getDefaultValue() != null && !"".equals(prop.getDefaultValue()) ? prop.getDefaultValue() : "test-value");
                    }

                }
                request.setProperties(properties);
                generationResult = plexusarchetype.generateProjectFromArchetype(request);
                if (generationResult != null && generationResult.getCause() != null) {
                    if ("http://repo1.maven.org/maven2".equals(ar.getRepository())) {
                        ar.setRepository(null);
                    }
//                    archetypesRemoved.add(ar);
                } else {
                    if ("http://repo1.maven.org/maven2".equals(ar.getRepository())) {
                        ar.setRepository(null);
                    }
                    if( !(ar.getVersion().indexOf("SNAPSHOT") > 0) && !(ar.getVersion().indexOf("snapshot") > 0) )
                    {
                        archetypesUsed.add(ar);
                    }
                }
            } else {
                if ("http://repo1.maven.org/maven2".equals(ar.getRepository())) {
                    ar.setRepository(null);
                }
                archetypesUsed.add(ar);
            }
            count++;
            System.err.println("\n\n\n\n\n");
        }
        ac = new ArchetypeCatalog();
        ac.setArchetypes(archetypesUsed);

        sw = new StringWriter();
        acxw = new ArchetypeCatalogXpp3Writer();
        acxw.write(sw, ac);

        System.err.println("Resulting catalog is\n" + sw.toString());
        System.err.println("Removed archetypes are \n" + archetypesRemoved);
    }
}
