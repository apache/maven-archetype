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
package org.apache.maven.archetype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public abstract class LoggingSupport {
    private final Logger logger = getLogger(getClass());

    private static final String GUICE_ENHANCED = "$$EnhancerByGuice$$";

    private static Logger getLogger(final Class<?> klazz) {
        requireNonNull(klazz);
        if (klazz.getName().contains(GUICE_ENHANCED)) {
            return LoggerFactory.getLogger(klazz.getSuperclass());
        }
        return LoggerFactory.getLogger(klazz);
    }

    protected Logger getLogger() {
        return logger;
    }
}
