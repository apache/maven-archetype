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
package org.apache.maven.archetype.old;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ArchetypeTest extends PlexusTestCase {
    private OldArchetype archetype;

    @Override
    protected void customizeContainerConfiguration(ContainerConfiguration configuration) {
        configuration.setClassPathScanning("index");
    }

    public void testArchetype() throws Exception {
        FileUtils.deleteDirectory(getTestFile("target/quickstart"));

        // ----------------------------------------------------------------------
        // This needs to be encapsulated in a maven test case.
        // ----------------------------------------------------------------------

        String mavenRepoLocal =
                getTestFile("target/local-repository").toURI().toURL().getFile();

        String mavenRepoRemote =
                getTestFile("src/test/repository").toURI().toURL().toString();

        RemoteRepository remoteRepository = new RemoteRepository.Builder("remote", "default", mavenRepoRemote)
                .setReleasePolicy(new RepositoryPolicy())
                .setSnapshotPolicy(new RepositoryPolicy())
                .build();

        List<RemoteRepository> remoteRepositories = new ArrayList<>();
        remoteRepositories.add(remoteRepository);

        DefaultRepositorySystemSession repositorySession = new DefaultRepositorySystemSession();
        RepositorySystem repositorySystem = lookup(RepositorySystem.class);
        LocalRepositoryManager localRepositoryManager =
                repositorySystem.newLocalRepositoryManager(repositorySession, new LocalRepository(mavenRepoLocal));
        repositorySession.setLocalRepositoryManager(localRepositoryManager);

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest()
                .setPackage("org.apache.maven.quickstart")
                .setGroupId("maven")
                .setArtifactId("quickstart")
                .setVersion("1.0-alpha-1-SNAPSHOT")
                .setArchetypeGroupId("org.apache.maven.archetypes")
                .setArchetypeArtifactId("maven-archetype-quickstart")
                .setArchetypeVersion("1.0-alpha-1-SNAPSHOT")
                .setRemoteRepositories(remoteRepositories)
                .setRepositorySession(repositorySession)
                .setOutputDirectory(getTestFile("target").getAbsolutePath());

        archetype.createArchetype(request);

        // ----------------------------------------------------------------------
        // Set up the Velocity context
        // ----------------------------------------------------------------------

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("basedir", request.getOutputDirectory());
        parameters.put("package", request.getPackage());
        parameters.put("packageName", request.getPackage());
        parameters.put("groupId", request.getGroupId());
        parameters.put("artifactId", request.getArtifactId());
        parameters.put("version", request.getVersion());

        Context context = new VelocityContext();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }

        // ----------------------------------------------------------------------
        // Validate POM generation
        // ----------------------------------------------------------------------

        Artifact archetypeArtifact = new DefaultArtifact(
                request.getArchetypeGroupId(), request.getArchetypeArtifactId(), "jar", request.getArchetypeVersion());

        StringWriter writer = new StringWriter();

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        Thread.currentThread()
                .setContextClassLoader(getContextClassloader(archetypeArtifact, repositorySession, remoteRepositories));

        try {
            VelocityComponent velocity = (VelocityComponent) lookup(VelocityComponent.class.getName());

            velocity.getEngine()
                    .mergeTemplate(
                            OldArchetype.ARCHETYPE_RESOURCES + "/" + OldArchetype.ARCHETYPE_POM,
                            "utf-8",
                            context,
                            writer);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        Model generatedModel, templateModel;
        try {
            StringReader strReader = new StringReader(writer.toString());

            MavenXpp3Reader reader = new MavenXpp3Reader();

            templateModel = reader.read(strReader);
        } catch (IOException e) {
            throw new ArchetypeTemplateProcessingException("Error reading template POM", e);
        }

        File artifactDir = getTestFile("target", (String) parameters.get("artifactId"));
        File pomFile = getTestFile(artifactDir.getAbsolutePath(), OldArchetype.ARCHETYPE_POM);

        try (FileReader pomReader = new FileReader(pomFile)) {
            MavenXpp3Reader reader = new MavenXpp3Reader();

            generatedModel = reader.read(pomReader);
        } catch (IOException | XmlPullParserException e) {
            throw new ArchetypeTemplateProcessingException("Error reading generated POM", e);
        }
        assertEquals(
                "Generated POM ArtifactId is not equivalent to expected result.",
                generatedModel.getArtifactId(),
                templateModel.getArtifactId());
        assertEquals(
                "Generated POM GroupId is not equivalent to expected result.",
                generatedModel.getGroupId(),
                templateModel.getGroupId());
        assertEquals(
                "Generated POM Id is not equivalent to expected result.",
                generatedModel.getId(),
                templateModel.getId());
        assertEquals(
                "Generated POM Version is not equivalent to expected result.",
                generatedModel.getVersion(),
                templateModel.getVersion());
        assertEquals(
                "Generated POM Packaging is not equivalent to expected result.",
                generatedModel.getPackaging(),
                templateModel.getPackaging());
        assertEquals(
                "Generated POM Developers is not equivalent to expected result.",
                generatedModel.getDevelopers(),
                templateModel.getDevelopers());
        assertEquals(
                "Generated POM Scm is not equivalent to expected result.",
                generatedModel.getScm(),
                templateModel.getScm());
    }

    // Gets the classloader for this artifact's file.
    private ClassLoader getContextClassloader(
            Artifact archetypeArtifact,
            RepositorySystemSession repositorySystemSession,
            List<RemoteRepository> remoteRepositories)
            throws Exception {
        RepositorySystem repositorySystem = lookup(RepositorySystem.class);
        ArtifactRequest request = new ArtifactRequest(archetypeArtifact, remoteRepositories, null);
        ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, request);
        URL[] urls = new URL[] {artifactResult.getArtifact().getFile().toURI().toURL()};
        return new URLClassLoader(urls);
    }

    public void testAddModuleToParentPOM() throws Exception {
        String pom = "<project>\n" + "  <packaging>pom</packaging>\n" + "</project>";

        StringWriter out = new StringWriter();
        assertTrue(DefaultOldArchetype.addModuleToParentPom("myArtifactId1", new StringReader(pom), out));

        assertThat(
                out.toString(),
                isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                + "<project>\n"
                                + "  <packaging>pom</packaging>\n"
                                + "  <modules>\n"
                                + "    <module>myArtifactId1</module>\n"
                                + "  </modules>\n"
                                + "</project>")
                        .normalizeWhitespace());

        pom = "<project>\n"
                + "  <modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "</project>";

        out = new StringWriter();
        assertTrue(DefaultOldArchetype.addModuleToParentPom("myArtifactId2", new StringReader(pom), out));

        assertThat(
                out.toString(),
                isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                + "<project>\n"
                                + "  <modelVersion>4.0.0</modelVersion>\n"
                                + "  <packaging>pom</packaging>\n"
                                + "  <modules>\n"
                                + "    <module>myArtifactId2</module>\n"
                                + "  </modules>\n"
                                + "</project>")
                        .normalizeWhitespace());

        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "  </modules>\n"
                + "</project>";

        out = new StringWriter();
        assertTrue(DefaultOldArchetype.addModuleToParentPom("myArtifactId3", new StringReader(pom), out));

        assertThat(
                out.toString(),
                isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                + "<project><modelVersion>4.0.0</modelVersion>\n"
                                + "  <packaging>pom</packaging>\n"
                                + "  <modules>\n"
                                + "    <module>myArtifactId3</module>\n"
                                + "  </modules>\n"
                                + "</project>")
                        .normalizeWhitespace());

        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId3</module>\n"
                + "  </modules>\n"
                + "</project>";

        out = new StringWriter();
        assertTrue(DefaultOldArchetype.addModuleToParentPom("myArtifactId4", new StringReader(pom), out));

        assertThat(
                out.toString(),
                isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                + "<project><modelVersion>4.0.0</modelVersion>\n"
                                + "  <packaging>pom</packaging>\n"
                                + "  <modules>\n"
                                + "    <module>myArtifactId3</module>\n"
                                + "    <module>myArtifactId4</module>\n"
                                + "  </modules>\n"
                                + "</project>")
                        .normalizeWhitespace());

        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId3</module>\n"
                + "  </modules>\n"
                + "</project>";

        out = new StringWriter();
        assertFalse(DefaultOldArchetype.addModuleToParentPom("myArtifactId3", new StringReader(pom), out));

        // empty means unchanged
        assertEquals("", out.toString().trim());

        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId1</module>\n"
                + "    <module>myArtifactId2</module>\n"
                + "    <module>myArtifactId3</module>\n"
                + "  </modules>\n"
                + "  <profiles>\n"
                + "    <profile>\n"
                + "      <id>profile1</id>\n"
                + "      <modules>\n"
                + "        <module>module1</module>\n"
                + "      </modules>\n"
                + "    </profile>\n"
                + "    <profile>\n"
                + "      <id>profile2</id>\n"
                + "      <modules>\n"
                + "        <module>module2</module>\n"
                + "      </modules>\n"
                + "    </profile>\n"
                + "  </profiles>\n"
                + "</project>";

        out = new StringWriter();
        assertTrue(DefaultOldArchetype.addModuleToParentPom("module1", new StringReader(pom), out));

        assertThat(
                out.toString(),
                isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                                + "<project><modelVersion>4.0.0</modelVersion>\n"
                                + "  <packaging>pom</packaging>\n"
                                + "  <modules>\n"
                                + "    <module>myArtifactId1</module>\n"
                                + "    <module>myArtifactId2</module>\n"
                                + "    <module>myArtifactId3</module>\n"
                                + "    <module>module1</module>\n"
                                + "  </modules>\n"
                                + "  <profiles>\n"
                                + "    <profile>\n"
                                + "      <id>profile1</id>\n"
                                + "      <modules>\n"
                                + "        <module>module1</module>\n"
                                + "      </modules>\n"
                                + "    </profile>\n"
                                + "    <profile>\n"
                                + "      <id>profile2</id>\n"
                                + "      <modules>\n"
                                + "        <module>module2</module>\n"
                                + "      </modules>\n"
                                + "    </profile>\n"
                                + "  </profiles>\n"
                                + "</project>")
                        .normalizeWhitespace());
    }

    public void testAddModuleToParentPOMNoPackaging() throws Exception {
        try {
            String pom = "<project>\n</project>";
            DefaultOldArchetype.addModuleToParentPom("myArtifactId1", new StringReader(pom), new StringWriter());
            fail("Should fail to add a module to a JAR packaged project");
        } catch (InvalidPackaging e) {
            // great!
            assertEquals(
                    "Unable to add module to the current project as it is not of packaging type 'pom'",
                    e.getLocalizedMessage());
        }
    }

    public void testAddModuleToParentPOMJarPackaging() throws Exception {
        try {
            String pom = "<project>\n  <packaging>jar</packaging>\n</project>";
            DefaultOldArchetype.addModuleToParentPom("myArtifactId1", new StringReader(pom), new StringWriter());
            fail("Should fail to add a module to a JAR packaged project");
        } catch (InvalidPackaging e) {
            // great!
            assertEquals(
                    "Unable to add module to the current project as it is not of packaging type 'pom'",
                    e.getLocalizedMessage());
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        archetype = (OldArchetype) lookup(OldArchetype.ROLE);
    }
}
