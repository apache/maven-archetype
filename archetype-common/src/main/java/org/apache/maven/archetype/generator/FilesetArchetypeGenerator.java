package org.apache.maven.archetype.generator;

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

import org.apache.maven.archetype.exception.ArchetypeGenerationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.archetype.exception.OutputFileExists;
import org.apache.maven.archetype.exception.PomFileExists;
import org.apache.maven.archetype.exception.ProjectDirectoryExists;
import org.apache.maven.archetype.exception.UnknownArchetype;

import java.io.File;
import org.apache.maven.archetype.ArchetypeGenerationRequest;

/**
 * Generate a Maven project from an archetype.
 *
 * @author rafale
 * @version $Id: OldArchetype.java 953452 2010-06-10 20:56:32Z hboutemy $
 */
public interface FilesetArchetypeGenerator
{
    String ROLE = FilesetArchetypeGenerator.class.getName();

    /**
     * Generate a project from an archetype file.
     */
    void generateArchetype( ArchetypeGenerationRequest request, File archetypeFile )
            throws UnknownArchetype, ArchetypeNotConfigured, ProjectDirectoryExists, PomFileExists, OutputFileExists,
            ArchetypeGenerationFailure, InvalidPackaging;
}
