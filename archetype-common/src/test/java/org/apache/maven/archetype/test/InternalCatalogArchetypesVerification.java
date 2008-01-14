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

import java.io.File;
import java.util.Iterator;
import org.apache.maven.archetype.Archetype;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 * @author rafale
 */
public class InternalCatalogArchetypesVerification 
    extends PlexusTestCase{

    

    public void testInternalCatalog ()
    throws Exception
    {
        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.ROLE );

        ArtifactRepository localRepository = registryManager.createRepository( new File( getBasedir(),
            "target/test-classes/repositories/local" ).toURI().
            toURL().
            toExternalForm(),
            "local-repo" );
        
        File outputDirectory = new File(getBasedir(), "target/internal-archetypes-projects");
        outputDirectory.mkdirs();
        
        Archetype archetype = (Archetype) lookup ( Archetype.class );

        ArchetypeCatalog result = archetype.getInternalCatalog ();

        Iterator archetypes = result.getArchetypes ().iterator ();
        int count = 1;
        while ( archetypes.hasNext () )
        {
            org.apache.maven.archetype.catalog.Archetype a =
                (org.apache.maven.archetype.catalog.Archetype) archetypes.next ();

            System.err.println("\n\n\n\n\n\nTesting archetype "+a);
            ArchetypeGenerationRequest request = new ArchetypeGenerationRequest ( a )
            .setGroupId ( "groupId" + count )
            .setArtifactId ( "artifactId" + count )
            .setVersion ( "version" + count )
            .setPackage ( "package" + count )
            .setOutputDirectory(outputDirectory.getPath())
            .setLocalRepository( localRepository );
            archetype.generateProjectFromArchetype ( request );
            count++;
            System.err.println("\n\n\n\n\n");
        }
    }
}
