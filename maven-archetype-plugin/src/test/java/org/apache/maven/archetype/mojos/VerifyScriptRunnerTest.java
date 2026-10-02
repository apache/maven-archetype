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
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.archetype.mojos.IntegrationTestMojo.IntegrationTestFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifyScriptRunnerTest {

    @TempDir
    File dir;

    private final VerifyScriptRunner runner = new VerifyScriptRunner("UTF-8");

    @Test
    void scriptSeesItsVariablesAndItsOutputLandsInTheLog() throws Exception {
        write(
                "verify.groovy",
                "println 'groovy ' + basedir.name\n"
                        + "System.out.println 'system ' + context.projectDir\n"
                        + "System.err.println 'error ' + scriptdir.name\n"
                        + "return true\n");
        Map<String, Object> context = new HashMap<>();
        context.put("projectDir", "generated");
        PrintStream systemOut = System.out;

        try (BuildLog log = new BuildLog(new File(dir, "build.log"), null)) {
            runner.run(dir, "verify", context, log);
        }

        String logged = new String(Files.readAllBytes(new File(dir, "build.log").toPath()), StandardCharsets.UTF_8);
        assertTrue(logged.contains("groovy " + dir.getName()), logged);
        assertTrue(logged.contains("system generated"), logged);
        assertTrue(logged.contains("error " + dir.getName()), logged);
        assertSame(systemOut, System.out, "System.out is restored");
    }

    @Test
    void nullAndTruePassAnythingElseFails() throws Exception {
        write("verify.groovy", "null");
        runner.run(dir, "verify", Collections.emptyMap(), null);
        write("verify.groovy", "'yes'");
        IntegrationTestFailure failure = assertThrows(
                IntegrationTestFailure.class, () -> runner.run(dir, "verify", Collections.emptyMap(), null));
        assertTrue(failure.getMessage().endsWith("returned yes."), failure.getMessage());
    }

    @Test
    void failingScriptIsReportedNotThrown() throws Exception {
        write("verify.groovy", "assert 1 == 2");
        assertThrows(IntegrationTestFailure.class, () -> runner.run(dir, "verify", Collections.emptyMap(), null));
        write("verify.groovy", "throw new NoClassDefFoundError('gone')");
        assertThrows(IntegrationTestFailure.class, () -> runner.run(dir, "verify", Collections.emptyMap(), null));
    }

    @Test
    void beanShellScriptIsRefusedWithAPointer() throws Exception {
        write("verify.bsh", "return true;");
        IntegrationTestFailure failure = assertThrows(
                IntegrationTestFailure.class, () -> runner.run(dir, "verify", Collections.emptyMap(), null));
        assertTrue(
                failure.getMessage().contains("port " + new File(dir, "verify.bsh") + " to Groovy"),
                failure.getMessage());
    }

    @Test
    void missingOrUnsetScriptIsSkipped() throws Exception {
        runner.run(dir, "verify", Collections.emptyMap(), null);
        runner.run(dir, null, Collections.emptyMap(), null);
        runner.run(dir, "", Collections.emptyMap(), null);
    }

    @Test
    void unsupportedEncodingIsAFailureNotACrash() throws Exception {
        write("verify.groovy", "true");
        IntegrationTestFailure failure = assertThrows(
                IntegrationTestFailure.class,
                () -> new VerifyScriptRunner("no-such-charset").run(dir, "verify", Collections.emptyMap(), null));
        assertEquals("unsupported encoding for the post-build script: no-such-charset", failure.getMessage());
    }

    private void write(String name, String content) throws Exception {
        Files.write(new File(dir, name).toPath(), content.getBytes(StandardCharsets.UTF_8));
    }
}
