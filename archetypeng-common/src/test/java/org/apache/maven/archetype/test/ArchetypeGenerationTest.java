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

package org.apache.maven.archetype.test;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.Archetyper;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.List;
import java.util.Properties;

/** @author Jason van Zyl */
public class ArchetypeGenerationTest
    extends PlexusTestCase
{
    public void testProjectGenerationFromAnArchetype()
        throws Exception
    {
        Archetyper archetype = (Archetyper) lookup( Archetyper.ROLE );

        // In the embedder the localRepository will be retrieved from the embedder itself and users won't
        // have to go through this muck.

        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.ROLE );

        ArtifactRepository localRepository = registryManager.createRepository(
            new File( getBasedir(), "target/test-classes/repositories/local" )
            .toURI().toURL().toExternalForm(), "local-repo" );

        Properties catalogProperties = new Properties();
        catalogProperties.setProperty("sources", "remote-catalog");
        catalogProperties.setProperty("remote-catalog.repository",
            new File( getBasedir(), "target/test-classes/repositories/central" )
            .toURI().toURL().toExternalForm());
        List archetypes = archetype.getAvailableArchetypes( catalogProperties );
System.err.println("archetypes => "+archetypes);
        // Here I am just grabbing a Archetype but in a UI you would take the Archetype objects and present
        // them to the user.

        Archetype selection = (Archetype) archetypes.get( archetypes.size() -1 );
System.err.println("Selected Archetype = "+selection);
        // Now you will present a dialog, or whatever, and grab the following values.

        String groupId = "com.mycompany";

        String artifactId = "app";

        String version = "1.0.0";

        String packageName = "org.mycompany.app";

        // With the selected Archetype and the parameters you can create a generation request as follows:

        ArchetypeGenerationRequest agr = new ArchetypeGenerationRequest( selection )
            .setOutputDirectory( new File( getBasedir(),
                "target/test-classes/projects/archetyper-generate-1" ).getAbsolutePath() )
            .setLocalRepository( localRepository )
            .setGroupId( groupId )
            .setArtifactId( artifactId )
            .setVersion( version )
            .setPackage( packageName );

        Properties archetypeRequiredProperties = new Properties();
        archetypeRequiredProperties.setProperty("property-with-default-1", "value-1");
        archetypeRequiredProperties.setProperty("property-with-default-2", "value-2");
        archetypeRequiredProperties.setProperty("property-with-default-3", "value-3");
        archetypeRequiredProperties.setProperty("property-with-default-4", "value-4");
        archetypeRequiredProperties.setProperty("property-without-default-1", "some-value-1");
        archetypeRequiredProperties.setProperty("property-without-default-2", "some-value-2");
        archetypeRequiredProperties.setProperty("property-without-default-3", "some-value-3");
        archetypeRequiredProperties.setProperty("property-without-default-4", "some-value-4");
        agr.setProperties(archetypeRequiredProperties);

        // Then generate away!

        ArchetypeGenerationResult result = archetype.generateProjectFromArchetype( agr );

        if ( result.getCause() != null )
        {result.getCause().printStackTrace(System.err);
            fail( result.getCause().getMessage() );
        }
    }
}