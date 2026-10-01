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

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;

/**
 * Holds the single {@link VelocityEngine} used to process archetype templates.
 * <p>
 * Replaces {@code org.codehaus.plexus.velocity.VelocityComponent}; the engine is configured with the same
 * properties that component sets, followed by the settings that preserve compatibility of Velocity 2.x with
 * Velocity 1.x
 * (<a href="https://velocity.apache.org/engine/2.3/upgrading.html">Velocity Upgrading</a>).
 */
@Named
@Singleton
public class ArchetypeVelocityEngine {

    private final VelocityEngine engine;

    public ArchetypeVelocityEngine() {
        Properties properties = new Properties();

        // Defaults formerly applied by plexus-velocity's DefaultVelocityComponent
        properties.setProperty("resource.loaders", "classpath,file");
        properties.setProperty(
                "resource.loader.classpath.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty(
                "resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        properties.setProperty("resource.loader.file.path", "");
        properties.setProperty("runtime.log.log_invalid_references", "false");
        properties.setProperty("resource.manager.log_when_found", "false");
        properties.setProperty(
                "event_handler.include.class", "org.apache.velocity.app.event.implement.IncludeRelativePath");
        properties.setProperty("velocimacro.inline.replace_global", "true");
        properties.setProperty("parser.space_gobbling", "bc");

        // Archetype settings, formerly in VelocityConfigurator

        // # No automatic conversion of methods arguments
        properties.put("introspector.conversion_handler.class", "none");

        // # Have #if($foo) only returns false if $foo is false or null
        properties.put("directive.if.empty_check", false);

        // # Allow '-' in identifiers (since 2.1)
        properties.put("parser.allow_hyphen_in_identifiers", true);

        // # Enable backward compatibility mode for Velocimacros
        properties.put("velocimacro.enable_bc_mode", true);

        // # When using an invalid reference handler, also include quiet references (since 2.2)
        properties.put("event_handler.invalid_references.quiet", "true");

        // # When using an invalid reference handler, also include null references (since 2.2)
        properties.put("event_handler.invalid_references.null", true);

        // # When using an invalid reference handler, also include tested references (since 2.2)
        properties.put("event_handler.invalid_references.tested", true);

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperties(properties);
        velocityEngine.init();
        this.engine = velocityEngine;
    }

    public VelocityEngine getEngine() {
        return engine;
    }
}
