package org.apache.maven.archetype.ui;

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

import org.apache.maven.project.MavenProject;

import java.util.Properties;

public interface ArchetypeFactory
{
    String ROLE = ArchetypeFactory.class.getName();

    ArchetypeDefinition createArchetypeDefinition( Properties properties );

    @SuppressWarnings( "checkstyle:linelength" )
    ArchetypeConfiguration createArchetypeConfiguration( org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor archetypeDescriptor,
                                                         Properties properties );

    @SuppressWarnings( "checkstyle:linelength" )
    ArchetypeConfiguration createArchetypeConfiguration( org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor,
                                                         Properties properties );

    @SuppressWarnings( "checkstyle:linelength" )
    ArchetypeConfiguration createArchetypeConfiguration( MavenProject project, ArchetypeDefinition archetypeDefinition,
                                                         Properties properties );

    void updateArchetypeConfiguration( ArchetypeConfiguration archetypeConfiguration,
                                       ArchetypeDefinition archetypeDefinition );
}
