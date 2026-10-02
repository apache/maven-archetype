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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.maven.executor.ExecutorException;
import org.apache.maven.executor.ExecutorRequest;
import org.apache.maven.executor.ExecutorResult;
import org.apache.maven.executor.forked.ForkedMavenExecutor;

/**
 * Runs a Maven build in a forked JVM of the Maven installation this build runs with, the one {@code maven.home}
 * points to.
 */
public final class MavenBuilds {

    private MavenBuilds() {}

    /**
     * Runs the request and returns its result; the caller reads the exit code from it.
     *
     * @throws ExecutorException when Maven could not be started or ran into the request's timeout
     */
    public static ExecutorResult run(ExecutorRequest request) throws ExecutorException {
        try (ForkedMavenExecutor executor = new ForkedMavenExecutor(ExecutorRequest.discoverInstallationDirectory())) {
            return executor.execute(request);
        }
    }

    /**
     * Wraps a stream the executor is given as the build's output, which the executor closes when the build ends;
     * for a stream that has to outlive the build, such as {@code System.out} or a log the caller keeps writing to.
     */
    public static OutputStream keepOpen(OutputStream stream) {
        return new FilterOutputStream(stream) {
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                out.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                out.flush();
            }
        };
    }
}
