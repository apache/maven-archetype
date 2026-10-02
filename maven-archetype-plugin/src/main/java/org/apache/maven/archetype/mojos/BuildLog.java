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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.maven.plugin.logging.Log;

/**
 * The <code>build.log</code> of one archetype integration test: the invoked build writes to it, and the verify
 * script gets a stream to print to afterwards. Optionally mirrors every line of the build's output to the Maven log.
 */
class BuildLog implements AutoCloseable {

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

    /**
     * The stream to hand the build as its output: it writes to the log file and, when mirroring, logs each
     * completed line. Closing it flushes; the log file stays open for the verify script.
     */
    OutputStream getBuildOutput() {
        if (mirror == null) {
            return new OutputStream() {
                @Override
                public void write(int b) {
                    stream.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    stream.write(b, off, len);
                }

                @Override
                public void flush() {
                    stream.flush();
                }
            };
        }
        return new OutputStream() {
            private final ByteArrayOutputStream line = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                stream.write(b);
                if (b == '\n') {
                    mirrorLine();
                } else if (b != '\r') {
                    line.write(b);
                }
            }

            @Override
            public void write(byte[] b, int off, int len) {
                for (int i = off; i < off + len; i++) {
                    write(b[i]);
                }
            }

            @Override
            public void flush() {
                stream.flush();
            }

            @Override
            public void close() {
                if (line.size() > 0) {
                    mirrorLine();
                }
                stream.flush();
            }

            private void mirrorLine() {
                mirror.info(new String(line.toByteArray(), StandardCharsets.UTF_8));
                line.reset();
            }
        };
    }

    /** Writes a line to the log file and, when mirroring, to the Maven log. */
    void consumeLine(String line) {
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
