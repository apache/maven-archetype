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
package org.apache.maven.archetype.common.util;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** @author <a href="mailto:brianf@apache.org">Brian Fox</a> */
public class TestListScanner {

    @ParameterizedTest
    @CsvSource({"/", "\\"})
    public void testPaths(String separator) {
        List<String> archetypeResources = Arrays.asList(
                "pom.xml",
                "App.java",
                String.join(separator, "src", "main", "c", "App.c"),
                String.join(separator, "src", "main", "java", "App.java"),
                String.join(separator, "src", "main", "java", "inner", "package", "App2.java"),
                String.join(separator, "src", "main", "mdo", "App.mdo"),
                String.join(separator, "src", "main", "resources", "App.properties"),
                String.join(separator, "src", "main", "resources", "inner", "dir", "App2.properties"),
                String.join(separator, "src", "test", "c", "AppTest.c"),
                String.join(separator, "src", "test", "java", "AppTest.java"),
                String.join(separator, "src", "test", "mdo", "AppTest.mdo"),
                String.join(separator, "src", "test", "resources", "AppTest.properties"));

        ListScanner scanner = new ListScanner();
        scanner.setBasedir(String.join(separator, "src", "main", "java"));
        scanner.setIncludes("**" + separator + "*.java");
        scanner.setCaseSensitive(true);

        List<String> result = scanner.scan(archetypeResources);

        assertEquals(2, result.size());
        assertTrue(result.contains(String.join(separator, "src", "main", "java", "App.java")));
        assertTrue(result.contains(String.join(separator, "src", "main", "java", "inner", "package", "App2.java")));
    }
}
