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
package org.apache.maven.archetype.mojos;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.archetype.common.Constants;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateArchetypeFromProjectMojoTest {

    @TempDir
    File dir;

    private final CreateArchetypeFromProjectMojo mojo = new CreateArchetypeFromProjectMojo(null, null);

    @Test
    void documentedPropertyIsBoundToAParameter() throws Exception {
        assertEquals("${archetype.filteredExtensions}", defaultExpression("archetypeFilteredExtensions"));
    }

    @Test
    void misspelledPropertyIsStillBoundToAParameter() throws Exception {
        assertEquals("${archetype.filteredExtentions}", defaultExpression("archetypeFilteredExtentions"));
    }

    @Test
    void correctSpellingIsUsed() throws Exception {
        assertEquals(Arrays.asList("java", "xml"), mojo.getFilteredExtensions("java,xml", null, null));
    }

    @Test
    void misspelledSpellingIsUsed() throws Exception {
        assertEquals(Arrays.asList("java", "xml"), mojo.getFilteredExtensions(null, "java,xml", null));
    }

    @Test
    void correctSpellingWinsWhenBothAreSet() throws Exception {
        assertEquals(Arrays.asList("java"), mojo.getFilteredExtensions("java", "xml", null));
    }

    @Test
    void propertyFileIsUsedWhenNeitherIsSet() throws Exception {
        File propertyFile = new File(dir, "archetype.properties");
        Files.write(
                propertyFile.toPath(),
                (Constants.ARCHETYPE_FILTERED_EXTENSIONS + "=groovy").getBytes(StandardCharsets.UTF_8));

        assertEquals(Arrays.asList("groovy"), mojo.getFilteredExtensions(null, null, propertyFile));
    }

    @Test
    void defaultsAreUsedWhenNothingIsSet() throws Exception {
        List<String> extensions = mojo.getFilteredExtensions(null, "", new File(dir, "missing.properties"));

        assertEquals(Constants.DEFAULT_FILTERED_EXTENSIONS, extensions);
    }

    /** Reads the parameter's expression from the generated plugin descriptor, as Maven does at run time. */
    private static String defaultExpression(String parameter) throws Exception {
        try (InputStream in =
                CreateArchetypeFromProjectMojoTest.class.getResourceAsStream("/META-INF/maven/plugin.xml")) {
            Xpp3Dom plugin = Xpp3DomBuilder.build(in, "UTF-8");
            for (Xpp3Dom mojo : plugin.getChild("mojos").getChildren("mojo")) {
                if ("create-from-project".equals(mojo.getChild("goal").getValue())) {
                    return mojo.getChild("configuration").getChild(parameter).getValue();
                }
            }
        }
        throw new AssertionError("create-from-project not found in plugin.xml");
    }
}
