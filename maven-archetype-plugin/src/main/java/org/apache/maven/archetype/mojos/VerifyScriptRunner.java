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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.maven.archetype.mojos.IntegrationTestMojo.IntegrationTestFailure;

/**
 * Runs the Groovy verify script of an archetype integration test. The script sees <code>basedir</code>,
 * <code>scriptdir</code> and <code>context</code>; it passes when it returns <code>null</code> or <code>true</code>
 * and fails on any other value or an exception.
 */
final class VerifyScriptRunner {

    private final String encoding;

    VerifyScriptRunner(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Resolves and runs the script, if there is one.
     *
     * @param scriptDir the test project directory holding the script, which the script sees as <code>basedir</code>
     * @param scriptName the script file name, with or without the <code>.groovy</code> extension
     * @param context the <code>context</code> map the script sees
     * @param log where the script's output goes, may be <code>null</code>
     * @throws IntegrationTestFailure if the script fails or is a BeanShell script
     * @throws IOException if the script cannot be read
     */
    void run(File scriptDir, String scriptName, Map<String, ?> context, BuildLog log)
            throws IntegrationTestFailure, IOException {
        if (scriptName == null || scriptName.isEmpty()) {
            return;
        }
        File script = resolve(scriptDir, scriptName);
        if (script == null) {
            return;
        }
        PrintStream out = log != null ? log.getPrintStream() : System.out;
        if (log != null) {
            log.consumeLine("Running post-build script: " + script);
        }

        Binding binding = new Binding();
        binding.setVariable("basedir", scriptDir);
        binding.setVariable("scriptdir", script.getParentFile());
        binding.setVariable("context", context != null ? context : new LinkedHashMap<String, Object>());
        binding.setVariable("out", out);

        Charset charset;
        try {
            charset = encoding == null || encoding.isEmpty() ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        } catch (IllegalArgumentException e) {
            throw new IntegrationTestFailure("unsupported encoding for the post-build script: " + encoding, e);
        }
        String source = new String(Files.readAllBytes(script.toPath()), charset);
        Object result;
        // scripts written for the previous runner print with System.out/err and expect that in build.log
        PrintStream systemOut = System.out;
        PrintStream systemErr = System.err;
        try {
            if (log != null) {
                System.setOut(out);
                System.setErr(out);
            }
            result = new GroovyShell(Thread.currentThread().getContextClassLoader(), binding).evaluate(source);
        } catch (Throwable e) {
            if (log != null) {
                e.printStackTrace(out);
            }
            throw new IntegrationTestFailure("post-build script " + script + " failed: " + e.getMessage(), e);
        } finally {
            System.setOut(systemOut);
            System.setErr(systemErr);
        }
        if (log != null) {
            log.consumeLine("Finished post-build script: " + script);
        }
        if (result != null && !Boolean.parseBoolean(String.valueOf(result))) {
            throw new IntegrationTestFailure("post-build script " + script + " returned " + result + ".");
        }
    }

    private static File resolve(File scriptDir, String scriptName) throws IntegrationTestFailure {
        File script = new File(scriptDir, scriptName);
        if (script.isFile()) {
            if (script.getName().endsWith(".bsh")) {
                throw beanShell(script);
            }
            return script;
        }
        File groovy = new File(scriptDir, scriptName + ".groovy");
        if (groovy.isFile()) {
            return groovy;
        }
        File bsh = new File(scriptDir, scriptName + ".bsh");
        if (bsh.isFile()) {
            throw beanShell(bsh);
        }
        return null;
    }

    private static IntegrationTestFailure beanShell(File script) {
        return new IntegrationTestFailure("BeanShell verify scripts are no longer supported, port " + script
                + " to Groovy (same variables: basedir, scriptdir, context)");
    }
}
