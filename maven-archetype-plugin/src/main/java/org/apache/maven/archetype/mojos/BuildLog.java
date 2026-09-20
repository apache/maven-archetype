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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.invoker.InvocationOutputHandler;

/**
 * The <code>build.log</code> of one archetype integration test: takes the invoked build's output line by line, and
 * gives the verify script a stream to print to. Optionally mirrors every line to the Maven log.
 */
class BuildLog implements InvocationOutputHandler, AutoCloseable {

    private final PrintStream stream;

    private final Log mirror;

    BuildLog(File outputFile, Log mirror) throws IOException {
        File parent = outputFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("cannot create " + parent);
        }
        this.stream = new PrintStream(new FileOutputStream(outputFile), true, StandardCharsets.UTF_8.name());
        this.mirror = mirror;
    }

    @Override
    public void consumeLine(String line) {
        stream.println(line);
        if (mirror != null) {
            mirror.info(line);
        }
    }

    PrintStream getPrintStream() {
        return stream;
    }

    @Override
    public void close() {
        stream.close();
    }
}
