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

import java.util.List;
import java.util.Properties;

/** @author Jason van Zyl */
public interface Archetyper
{
    String ROLE = Archetyper.class.getName();

    /**
     * A command to create an Archetype from an existing Maven project given the suppled
     * creation request.
     *
     * @param request
     * @return The result of creating the archetype from the existing project. It contains any errors that might have occured.
     */
    ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request );

    /**
     * A command to generate a Maven project from an Archetype given the suppled
     * generation request.
     *  
     * @param request
     * @return The result of creating the proejct from the existing archetype. It contains any errors that might have occured.
     */
    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request );

    /**
     * Get all available archetypes using the standard ~/.m2/archetype-catalog.properties as the
     * definition for the sources to be used and the configuration for each
     * {@org.apache.maven.archetype.source.ArchetypeDataSource} listed.
     *
     * @return A Map of available archetypes collected from all available source.
     */
    List getAvailableArchetypes();

    /**
     * Get all available archetypes using a specified catalog properties as the
     * definition for the sources to be used and the configuration for each
     * {@org.apache.maven.archetype.source.ArchetypeDataSource} listed.
     *
     * @return A Map of available archetypes collected from all available source.
     */
    List getAvailableArchetypes( Properties properties );
}
