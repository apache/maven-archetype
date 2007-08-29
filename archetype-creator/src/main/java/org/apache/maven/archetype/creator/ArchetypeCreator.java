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

package org.apache.maven.archetype.creator;

import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.TemplateCreationException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.IOException;

import java.util.List;

public interface ArchetypeCreator
{
    String ROLE = ArchetypeCreator.class.getName ();

    void createArchetype (
        MavenProject project,
        File propertyFile,
        List languages,
        List filtereds,
        String defaultEncoding,
        boolean ignoreReplica,
        File archetypeRegistryFile
    )
    throws IOException,
        ArchetypeNotDefined,
        ArchetypeNotConfigured,
        TemplateCreationException,
        XmlPullParserException;
}
