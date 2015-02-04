package org.apache.maven.archetype.test;

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

import java.io.File;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author rafale
 */
public class InternalCatalogArchetypesVerificationTest
    extends PlexusTestCase
{
    private static final String CENTRAL = "http://repo.maven.apache.org/maven2";

    public void testInternalCatalog()
        throws Exception
    {
        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.ROLE );

        ArtifactRepository localRepository = registryManager.createRepository( new File( getBasedir(),
                "target/test-classes/repositories/local" ).toURI().toURL().toExternalForm(),
                "local-repo");

        File outputDirectory = new File( getBasedir(), "target/internal-archetypes-projects" );
        outputDirectory.mkdirs();
        FileUtils.cleanDirectory( outputDirectory );

        ArchetypeManager archetype = (ArchetypeManager) lookup( ArchetypeManager.class.getName() );

        ArchetypeCatalog catalog = archetype.getInternalCatalog();

        int count = 1;
        for ( Archetype a : catalog.getArchetypes() )
        {
            Archetype ar = new Archetype();
            ar.setGroupId( a.getGroupId() );
            ar.setArtifactId( a.getArtifactId() );
            ar.setVersion( a.getVersion() );
            ar.setDescription( a.getDescription() );
            ar.setGoals( a.getGoals() );
            ar.setProperties( a.getProperties() );
            ar.setRepository( a.getRepository() );
            if ( ar.getRepository() == null )
            {
                ar.setRepository( CENTRAL );
            }

            ArchetypeGenerationRequest request =
                new ArchetypeGenerationRequest( ar )
                .setGroupId( "org.apache.maven.archetype.test" )
                .setArtifactId( "archetype" + count )
                .setVersion( "1.0-SNAPSHOT" )
                .setPackage( "com.acme" )
                .setOutputDirectory( outputDirectory.getPath() )
                .setLocalRepository( localRepository );
            ArchetypeGenerationResult generationResult = archetype.generateProjectFromArchetype( request );
            
            assertTrue ( "Archetype wasn't generated successfully", generationResult.getCause() == null );
            
            count++;
        }
    }
}
