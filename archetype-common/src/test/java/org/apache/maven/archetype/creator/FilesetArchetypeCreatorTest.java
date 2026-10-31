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
package org.apache.maven.archetype.creator;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.codehaus.plexus.testing.PlexusExtension.getBasedir;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks what {@link FilesetArchetypeCreator} puts into the archetype it creates from the projects under
 * {@code src/test/resources/projects/fileset}. The archetype is not built afterwards (no post phase), so the tests do
 * not fork a Maven. Each project's {@code archetype.properties} holds the properties and the languages and filtered
 * extensions the create-from-project goal reads from the same file.
 */
@PlexusTest
public class FilesetArchetypeCreatorTest {

    @Inject
    private FilesetArchetypeCreator creator;

    @TempDir
    File outputDirectory;

    @Test
    public void testExcludePatternsSkipDirectoryByPrefixPattern() throws Exception {
        Archetype archetype = create("exclude-patterns");

        assertFalse(archetype.file(".toexclude/dummy.file").exists());
        assertTrue(archetype.file("nottoexclude/dummy.file").exists());
    }

    @Test
    public void testExcludePatternsSkipDirectoryAtAnyDepth() throws Exception {
        Archetype archetype = create("exclude-patterns-2");

        assertFalse(archetype.file(".sonar/file.txt").exists());
        assertFalse(archetype.file("folder/.sonar/file.txt").exists());
        assertTrue(archetype.file("folder/file.txt").exists());
    }

    @Test
    public void testSourceFileWithoutExtensionIsIncluded() throws Exception {
        Archetype archetype = create("include-file-with-no-extension");

        assertTrue(archetype.file("src/main/csharp/filewithnoextension").exists());
    }

    @Test
    public void testResourceFileWithoutExtensionIsIncluded() throws Exception {
        Archetype archetype = create("include-resource-file-with-no-extension");

        assertTrue(archetype.file("res/txt/filewithnoextension").exists());
    }

    @Test
    public void testSourcesAreMovedToThePackageAndFilteredWithProperties() throws Exception {
        Archetype archetype = create("create-1");

        String template = archetype.text("src/main/java/subfolder1/App.java");
        assertTrue(template.contains("// ${someProperty}"));
        assertTrue(template.contains("package ${package}.subfolder1;"));
        assertFalse(template.contains("${packageInPathFormat}"));
    }

    @Test
    public void testModuleAndNonStandardSourceDirectoryAreDescribed() throws Exception {
        Archetype archetype = create("create-2");

        String root = archetype.text("pom.xml");
        assertTrue(root.contains("<artifactId>${artifactId}</artifactId>"));
        assertTrue(root.contains("Maven archetype Test create-2"));
        assertTrue(root.contains("<packaging>pom</packaging>"));

        String subModule = archetype.text("subModule/pom.xml");
        assertTrue(subModule.contains("<artifactId>${rootArtifactId}</artifactId>"));
        assertTrue(subModule.contains("<parent>"));
        assertTrue(subModule.contains("Maven archetype Test create-2-subModule"));

        // the non-standard source directory src/it-test is part of the archetype
        String itApp = archetype.text("src/it-test/java/subfolder1/App.java");
        assertTrue(itApp.contains("// ${someProperty}"));

        String descriptor = archetype.descriptor();
        assertTrue(descriptor.contains("<module id=\"${rootArtifactId}-subModule\" dir=\"subModule\""));
        assertTrue(descriptor.contains("<directory>src/it-test/java</directory>"));
    }

    @Test
    public void testMultiModuleProjectTemplatesEveryKindOfFile() throws Exception {
        Archetype archetype = create("create-3");

        String template = archetype.text("pom.xml");
        assertTrue(template.contains("${groupId}"));
        assertTrue(template.contains("${artifactId}"));
        assertTrue(template.contains("${version}"));
        assertTrue(template.contains("Maven archetype Test create-3"));
        assertTrue(template.contains("<packaging>pom</packaging>"));
        assertFalse(template.contains("<parent>"));

        template = archetype.text("src/site/site.xml");
        assertTrue(template.contains("<!-- ${packageInPathFormat}/test"));
        assertTrue(template.contains("${someProperty} -->"));

        template = archetype.text("src/site/resources/site.png");
        assertFalse(template.contains("${someProperty}"));

        template = archetype.text("libs/pom.xml");
        assertTrue(template.contains("${groupId}"));
        assertTrue(template.contains("${artifactId}"));
        assertTrue(template.contains("${version}"));
        assertTrue(template.contains("Maven archetype Test create-3-libraries"));
        assertTrue(template.contains("<packaging>pom</packaging>"));
        assertTrue(template.contains("<parent>"));

        template = archetype.text("libs/prj-a/pom.xml");
        assertTrue(template.contains("${groupId}"));
        assertTrue(template.contains("${artifactId}"));
        assertTrue(template.contains("${version}"));
        assertTrue(template.contains("Maven archetype Test create-3-libraries-project-a"));
        assertFalse(template.contains("<packaging>pom</packaging>"));
        assertTrue(template.contains("<parent>"));

        template = archetype.text("libs/prj-a/src/main/mdo/descriptor.xml");
        assertTrue(template.contains("<!-- ${packageInPathFormat}/test"));
        assertTrue(template.contains("${someProperty} -->"));

        template = archetype.text("libs/prj-b/pom.xml");
        assertTrue(template.contains("${groupId}"));
        assertTrue(template.contains("${artifactId}"));
        assertTrue(template.contains("${version}"));
        assertTrue(template.contains("Maven archetype Test create-3-libraries-project-b"));
        assertFalse(template.contains("<packaging>pom</packaging>"));
        assertTrue(template.contains("<parent>"));

        template = archetype.text("libs/prj-b/src/main/java/test/com/Component.java");
        assertTrue(template.contains("${someProperty}"));
        assertTrue(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}"));

        template = archetype.text("libs/prj-b/src/main/java/test/com/package.html");
        assertTrue(template.contains("<!-- ${packageInPathFormat}/test"));
        assertTrue(template.contains("${someProperty} -->"));

        template = archetype.text("libs/prj-b/src/test/java/test/common/ComponentTest.java");
        assertTrue(template.contains("${someProperty}"));
        assertTrue(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}"));

        template = archetype.text("application/pom.xml");
        assertTrue(template.contains("${groupId}"));
        assertTrue(template.contains("${artifactId}"));
        assertTrue(template.contains("${version}"));
        assertTrue(template.contains("Maven archetype Test create-3-application"));
        assertFalse(template.contains("<packaging>pom</packaging>"));
        assertTrue(template.contains("<parent>"));

        template = archetype.text("application/src/main/java/Main.java");
        assertTrue(template.contains("${someProperty}"));
        assertFalse(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}/test"));

        template = archetype.text("application/src/main/java/test/application/Application.java");
        assertTrue(template.contains("${someProperty}"));
        assertTrue(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}"));

        template = archetype.text("application/src/main/java/test/application/audios/Application.ogg");
        assertFalse(template.contains("${someProperty}"));

        template = archetype.text("application/src/main/java/test/application/images/Application.png");
        assertFalse(template.contains("${someProperty}"));

        template = archetype.text("application/src/main/resources/log4j.properties");
        assertTrue(template.contains("${someProperty}"));
        assertFalse(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}/test"));

        template = archetype.text("application/src/main/resources/META-INF/MANIFEST.MF");
        assertTrue(template.contains("${someProperty}"));
        assertFalse(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}/test"));

        template = archetype.text("application/src/main/resources/test/application/some/Gro.groovy");
        assertTrue(template.contains("${someProperty}"));
        assertFalse(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}/test"));

        template = archetype.text("application/src/main/resources/splash.png");
        assertFalse(template.contains("${someProperty}"));

        template = archetype.text("application/src/test/java/TestAll.java");
        assertTrue(template.contains("${someProperty}"));
        assertFalse(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}/test"));

        template = archetype.text("application/src/test/java/test/application/ApplicationTest.java");
        assertTrue(template.contains("${someProperty}"));
        assertTrue(template.contains("package ${package}.test.application;"));
        assertTrue(template.contains("${packageInPathFormat}/test/application"));

        template = archetype.text("application/src/it-test/java/test/ItTest1.java");
        assertTrue(template.contains("${someProperty}"));
        assertTrue(template.contains("package ${package}.test;"));
        assertTrue(template.contains("${packageInPathFormat}/test"));

        template = archetype.text("application/src/it-test/java/ItTestAll.java");
        assertTrue(template.contains("${someProperty}"));
        assertFalse(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}/test"));

        template = archetype.text("application/src/it-test/resources/ItTest1Result.txt");
        assertTrue(template.contains("${someProperty}"));
        assertFalse(template.contains("${package}"));
        assertTrue(template.contains("${packageInPathFormat}/test"));
    }

    @Test
    public void testWarAndEarModulesKeepTheirPackaging() throws Exception {
        Archetype archetype = create("create-4");

        String template = archetype.text("pom.xml");
        assertTrue(template.contains("Maven archetype Test create-4 ${someProperty}"));
        assertTrue(template.contains("<packaging>pom</packaging>"));

        String earTemplate = archetype.text("subModuleEAR/pom.xml");
        assertTrue(earTemplate.contains("${groupId}"));
        assertTrue(earTemplate.contains("${artifactId}"));
        assertTrue(earTemplate.contains("${version}"));
        assertTrue(earTemplate.contains("Maven archetype Test create-4-subModuleEAR"));
        assertTrue(earTemplate.contains("<packaging>ear</packaging>"));
        assertTrue(earTemplate.contains("<parent>"));

        String warTemplate = archetype.text("subModuleWar/pom.xml");
        assertTrue(warTemplate.contains("${groupId}"));
        assertTrue(warTemplate.contains("${artifactId}"));
        assertTrue(warTemplate.contains("${version}"));
        assertTrue(warTemplate.contains("Maven archetype Test create-4-subModuleWar ${someProperty}"));
        assertTrue(warTemplate.contains("<packaging>war</packaging>"));
        assertTrue(warTemplate.contains("<parent>"));
    }

    @Test
    public void testFileWithoutKnownExtensionIsCopied() throws Exception {
        Archetype archetype = create("create-5");

        assertTrue(archetype.file("dummy.file").exists());
    }

    @Test
    public void testArtifactIdInDirectoryNameIsReplaced() throws Exception {
        Archetype archetype = create("create-6");

        assertTrue(
                archetype.file("src/main/webapp/resources/__artifactId__/a.jsp").exists());
        assertTrue(
                archetype.file("src/main/webapp/resources/__artifactId__/a.gif").exists());
    }

    /**
     * Creates the archetype of the project like the create-from-project goal does, without building it.
     */
    private Archetype create(String project) throws Exception {
        File projectDirectory = new File(getBasedir(), "target/test-classes/projects/fileset/" + project);
        File pomFile = new File(projectDirectory, "pom.xml");
        Model model;
        try (InputStream in = Files.newInputStream(pomFile.toPath())) {
            model = new MavenXpp3Reader().read(in);
        }
        MavenProject mavenProject = new MavenProject(model);
        mavenProject.setFile(pomFile);

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(new File(projectDirectory, "archetype.properties").toPath())) {
            properties.load(in);
        }

        // like the goal, take the languages and extensions out of the properties that are replaced in the files
        List<String> languages =
                list((String) properties.remove(Constants.ARCHETYPE_LANGUAGES), Constants.DEFAULT_LANGUAGES);
        List<String> filtereds = list(
                (String) properties.remove(Constants.ARCHETYPE_FILTERED_EXTENSIONS),
                Constants.DEFAULT_FILTERED_EXTENSIONS);

        ArchetypeCreationRequest request = new ArchetypeCreationRequest()
                .setDefaultEncoding("UTF-8")
                .setProject(mavenProject)
                .setProperties(properties)
                .setLanguages(languages)
                .setFiltereds(filtereds)
                .setKeepParent(true)
                .setOutputDirectory(outputDirectory);

        ArchetypeCreationResult result = new ArchetypeCreationResult();
        creator.createArchetype(request, result);
        assertNull(result.getCause(), () -> "Archetype wasn't created successfully: " + result.getCause());
        return new Archetype(outputDirectory);
    }

    private static List<String> list(String commaSeparated, List<String> defaults) {
        if (commaSeparated == null || commaSeparated.isEmpty()) {
            return defaults;
        }
        return new ArrayList<>(Arrays.asList(commaSeparated.split(",")));
    }

    /** The files of a created archetype. */
    private static final class Archetype {
        private final File resources;

        Archetype(File outputDirectory) {
            this.resources = new File(outputDirectory, "src/main/resources");
        }

        File file(String path) {
            return new File(resources, Constants.ARCHETYPE_RESOURCES + "/" + path);
        }

        /** The content of a file that must exist in the archetype's template. */
        String text(String path) throws IOException {
            File file = file(path);
            assertTrue(file.exists(), () -> file + " is missing from the archetype");
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }

        String descriptor() throws IOException {
            return new String(
                    Files.readAllBytes(new File(resources, Constants.ARCHETYPE_DESCRIPTOR).toPath()),
                    StandardCharsets.UTF_8);
        }
    }
}
