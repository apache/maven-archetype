package org.apache.maven.archetype.ui.generation;

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

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.exception.ArchetypeGenerationConfigurationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.io.IOException;
import java.util.Properties;

/**
 * Configurator component to organize steps for project generation from an archetype.
 */
public interface ArchetypeGenerationConfigurator
{
    String ROLE = ArchetypeGenerationConfigurator.class.getName();

    void configureArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode,
                             Properties commandLineProperties )
        throws ArchetypeNotDefined, UnknownArchetype, ArchetypeNotConfigured, IOException, PrompterException,
        ArchetypeGenerationConfigurationFailure;
}
