package org.apache.maven.archetype;

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

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.ProjectBuildingRequest;

import java.io.File;
import java.io.IOException;

/** @author Jason van Zyl */
public interface ArchetypeManager
{
    String ROLE = ArchetypeManager.class.getName();

    /**
     * A command to create an archetype from an existing Maven project given the supplied creation request.
     *
     * @param request
     * @return The result of creating the archetype from the existing project. It contains any errors that might have
     *         occurred.
     */
    ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request );

    /**
     * A command to generate a Maven project from an archetype given the supplied generation request.
     *
     * @param request
     * @return The result of creating the project from the existing archetype. It contains any errors that might have
     *         occurred.
     */
    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request );

    /**
     * Gives the catalog of archetypes internal to the plugin.
     * 
     * @return the catalog.
     */
    ArchetypeCatalog getInternalCatalog();

    /**
     * Gives the catalog of archetypes located in the given path.
     * if path is a file, it used as is.
     * if path is a directory, archetype-catalog.xml is appended to it.
     * 
     * @param buildingRequest the catalog file path or directory containing the catalog file.
     * @return the catalog.
     */
    ArchetypeCatalog getLocalCatalog( ProjectBuildingRequest buildingRequest );

    /**
     * Gives the catalog of archetypes located at
     * <code>https://repo.maven.apache.org/maven2/archetype-catalog.xml</code>.
     * @param buildingRequest TODO
     * 
     * @return the catalog.
     */
    ArchetypeCatalog getRemoteCatalog( ProjectBuildingRequest buildingRequest );

    /**
     * Creates a jar file for an archetype.
     *
     * @param archetypeDirectory
     * @param outputDirectory
     * @param finalName
     * @return The File to the generated jar
     * @throws org.apache.maven.artifact.DependencyResolutionRequiredException
     * @throws java.io.IOException
     * @deprecated replaced by archetype plugin's JarMojo using maven-archiver component for Reproducible Builds
     */
    @Deprecated
    File archiveArchetype( File archetypeDirectory, File outputDirectory, String finalName )
        throws DependencyResolutionRequiredException, IOException;

    void updateLocalCatalog( ProjectBuildingRequest buildingRequest, Archetype archetype );
}
