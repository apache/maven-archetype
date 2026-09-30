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
package org.apache.maven.archetype.common;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPomManager {

    // ref: https://www.baeldung.com/java-pretty-print-xml
    // https://bugs.openjdk.java.net/browse/JDK-8262285?attachmentViewMode=list
    @Test
    public void testAddModulePomPackaging() throws Exception {
        PomManager pomManager = new DefaultPomManager();

        Path pomPath = Paths.get(
                getClass().getResource("/projects/pom-manager/pom-sample-1.xml").toURI());
        Path pomDestPath = pomPath.getParent().resolve("pom-sample-1-copied.xml");
        Files.copy(pomPath, pomDestPath, StandardCopyOption.REPLACE_EXISTING);

        final int moduleNumber = 4;
        for (int i = 0; i < moduleNumber; i++) {
            pomManager.addModule(pomDestPath.toFile(), "test" + i);
        }

        String fileText = new String(Files.readAllBytes(pomDestPath), StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile("(^[ ]+[\\r\\n]+){" + moduleNumber + "}", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileText);
        assertFalse(matcher.find());
    }

    @Test
    public void testAddModuleNonPomPackaging() throws Exception {
        PomManager pomManager = new DefaultPomManager();

        Path pomPath = Paths.get(
                getClass().getResource("/projects/pom-manager/pom-sample-2.xml").toURI());
        Path pomDestPath = pomPath.getParent().resolve("pom-sample-1-copied.xml");
        Files.copy(pomPath, pomDestPath, StandardCopyOption.REPLACE_EXISTING);

        Exception expectedException = null;
        try {
            pomManager.addModule(pomDestPath.toFile(), "test");
        } catch (InvalidPackaging e) {
            expectedException = e;
        }

        assertNotNull(expectedException);
    }

    @Test
    public void testMergePomsKeepsDeclaredDependencyOrder(@TempDir Path dir) throws Exception {
        String[] artifactIds = {"zulu", "alpha", "mike", "bravo", "yankee", "charlie", "xray", "delta", "whiskey"};

        StringBuilder generated = new StringBuilder();
        for (String artifactId : artifactIds) {
            generated
                    .append("<dependency><groupId>g</groupId><artifactId>")
                    .append(artifactId)
                    .append("</artifactId><version>1</version></dependency>");
        }
        Path existing = dir.resolve("pom.xml");
        Path temporary = dir.resolve("generated.xml");
        Files.write(existing, pom("<dependencies></dependencies>").getBytes(StandardCharsets.UTF_8));
        Files.write(
                temporary, pom("<dependencies>" + generated + "</dependencies>").getBytes(StandardCharsets.UTF_8));

        new DefaultPomManager().mergePoms(existing.toFile(), temporary.toFile());

        Model merged = new org.apache.maven.model.io.xpp3.MavenXpp3Reader()
                .read(Files.newBufferedReader(existing, StandardCharsets.UTF_8));
        List<String> actual = new ArrayList<>();
        for (Dependency dependency : merged.getDependencies()) {
            actual.add(dependency.getArtifactId());
        }
        assertEquals(java.util.Arrays.asList(artifactIds), actual);
    }

    private static String pom(String body) {
        return "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"><modelVersion>4.0.0</modelVersion>"
                + "<groupId>g</groupId><artifactId>a</artifactId><version>1</version>" + body + "</project>";
    }
}
