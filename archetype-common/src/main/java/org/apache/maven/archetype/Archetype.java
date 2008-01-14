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


import org.apache.maven.artifact.DependencyResolutionRequiredException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;

/** @author Jason van Zyl */
public interface Archetype
{
    String ROLE = Archetype.class.getName();

    /**
     * A command to create an OldArchetype from an existing Maven project given the suppled
     * creation request.
     *
     * @param request
     * @return The result of creating the archetype from the existing project. It contains any errors that might have occured.
     */
    ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request );

    /**
     * A command to generate a Maven project from an OldArchetype given the suppled
     * generation request.
     *
     * @param request
     * @return The result of creating the proejct from the existing archetype. It contains any errors that might have occured.
     */
    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request );

    /**
     * Gives the catalog of archetypes internal to the plugin.
     * @return the catalog.
     */
    ArchetypeCatalog getInternalCatalog();

    /**
     * Gives the catalog of archetypes located in $user.home/.m2/repository/archetype-catalog.xml.
     * @return the catalog.
     */
    ArchetypeCatalog getDefaultLocalCatalog();

    /**
     * Gives the catalog of archetypes located in the given path.
     * if path is a file, it used as is.
     * if path is a directory, archetype-catalog.xml is appended to it.
     * @param path the catalog file path or directory containing the catalog file.
     * @return the catalog.
     */
    ArchetypeCatalog getLocalCatalog( String path );

    /**
     * Gives the catalog of archetypes located at http://repo1.maven.org/maven2/archetype-catalog.xml.
     * @return the catalog.
     */
    ArchetypeCatalog getRemoteCatalog();

    /**
     * Gives the catalog of archetypes located at the given url.
     * if the url doesn't define a catalog, then 'archetype-catalog.xml' is appended to it for search.
     * @param url the catalog url or base url containing the catalog file.
     * @return the catalog.
     */
    ArchetypeCatalog getRemoteCatalog( String url );

    /**
     * Creates a jar file for an archetype.
     *
     * @param archetypeDirectory
     * @param outputDirectory
     * @param finalName
     * @return The File to the generated jar
     * @throws org.apache.maven.artifact.DependencyResolutionRequiredException
     *
     * @throws java.io.IOException
     */
    File archiveArchetype(
        File archetypeDirectory,
        File outputDirectory,
        String finalName )
        throws DependencyResolutionRequiredException, IOException;
    void updateLocalCatalog(org.apache.maven.archetype.catalog.Archetype archetype, String path);
    void updateLocalCatalog(org.apache.maven.archetype.catalog.Archetype archetype);
}
