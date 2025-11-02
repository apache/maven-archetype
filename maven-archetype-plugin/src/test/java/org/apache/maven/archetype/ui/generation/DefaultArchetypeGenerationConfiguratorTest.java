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
package org.apache.maven.archetype.ui.generation;

import java.io.File;
import java.util.Properties;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.ArchetypeGenerationConfigurationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TODO probably testing a little deep, could just test ArchetypeConfiguration
 */
public class DefaultArchetypeGenerationConfiguratorTest extends PlexusTestCase {
    private DefaultArchetypeGenerationConfigurator configurator;

    @Override
    protected void customizeContainerConfiguration(ContainerConfiguration configuration) {
        configuration.setClassPathScanning("index");
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        configurator = (DefaultArchetypeGenerationConfigurator) lookup(ArchetypeGenerationConfigurator.ROLE);

        ArchetypeArtifactManager manager = EasyMock.createMock(ArchetypeArtifactManager.class);

        File archetype = new File("archetype.jar");
        EasyMock.expect(manager.exists(
                        eq("archetypeGroupId"),
                        eq("archetypeArtifactId"),
                        eq("archetypeVersion"),
                        anyObject(),
                        anyObject()))
                .andReturn(true);
        EasyMock.expect(manager.getArchetypeFile(
                        eq("archetypeGroupId"),
                        eq("archetypeArtifactId"),
                        eq("archetypeVersion"),
                        anyObject(),
                        anyObject()))
                .andReturn(archetype);
        EasyMock.expect(manager.isFileSetArchetype(archetype)).andReturn(false);
        EasyMock.expect(manager.isOldArchetype(archetype)).andReturn(true);

        EasyMock.expect(manager.getOldArchetypeDescriptor(archetype)).andReturn(new ArchetypeDescriptor());

        EasyMock.replay(manager);
        configurator.setArchetypeArtifactManager(manager);
    }

    @Test
    public void testOldArchetypeGeneratedFieldsInRequestBatchMode()
            throws PrompterException, ArchetypeGenerationConfigurationFailure, ArchetypeNotConfigured, UnknownArchetype,
                    ArchetypeNotDefined {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId("archetypeGroupId");
        request.setArchetypeArtifactId("archetypeArtifactId");
        request.setArchetypeVersion("archetypeVersion");
        Properties properties = new Properties();
        properties.setProperty("groupId", "preset-groupId");
        properties.setProperty("artifactId", "preset-artifactId");
        properties.setProperty("version", "preset-gen-version");
        properties.setProperty("package", "preset-gen-package");

        configurator.configureArchetype(request, Boolean.FALSE, properties);

        assertEquals("preset-groupId", request.getGroupId());
        assertEquals("preset-artifactId", request.getArtifactId());
        assertEquals("preset-gen-version", request.getVersion());
        assertEquals("preset-gen-package", request.getPackage());
    }

    @Test
    public void testOldArchetypeGeneratedFieldsDefaultsBatchMode()
            throws PrompterException, UnknownArchetype, ArchetypeNotDefined, ArchetypeGenerationConfigurationFailure,
                    ArchetypeNotConfigured {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId("archetypeGroupId");
        request.setArchetypeArtifactId("archetypeArtifactId");
        request.setArchetypeVersion("archetypeVersion");
        Properties properties = new Properties();
        properties.setProperty("groupId", "preset-groupId");
        properties.setProperty("artifactId", "preset-artifactId");

        configurator.configureArchetype(request, Boolean.FALSE, properties);

        assertEquals("preset-groupId", request.getGroupId());
        assertEquals("preset-artifactId", request.getArtifactId());
        assertEquals("1.0-SNAPSHOT", request.getVersion());
        assertEquals("preset-groupId", request.getPackage());
    }

    // TODO: should test this in interactive mode to check for prompting
    @Test
    public void testOldArchetypeGeneratedFieldsDefaultsMissingGroupId()
            throws PrompterException, UnknownArchetype, ArchetypeNotDefined, ArchetypeGenerationConfigurationFailure {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId("archetypeGroupId");
        request.setArchetypeArtifactId("archetypeArtifactId");
        request.setArchetypeVersion("archetypeVersion");
        Properties properties = new Properties();
        properties.setProperty("artifactId", "preset-artifactId");

        try {
            configurator.configureArchetype(request, Boolean.FALSE, properties);
            fail("An exception must be thrown");
        } catch (ArchetypeNotConfigured e) {

        }
    }
}
