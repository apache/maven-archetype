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

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;

/** @author Jason van Zyl */
public class ArchetypeGenerationTest
    extends PlexusTestCase
{
    public void testProjectGenerationFromAnArchetype()
        throws Exception
    {
        ArchetypeManager archetype = (ArchetypeManager) lookup( ArchetypeManager.ROLE );

        // In the embedder the localRepository will be retrieved from the embedder itself and users won't
        // have to go through this muck.

        ArtifactRepository localRepository = createRepository(
            new File( getBasedir(), "target/test-classes/repositories/local" )
                .toURI().toURL().toExternalForm(), "local-repo" );
        
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        MavenRepositorySystemSession repositorySession = new MavenRepositorySystemSession();
        repositorySession.setLocalRepositoryManager( new SimpleLocalRepositoryManager( "target/test-classes/repositories/central" ) );
        buildingRequest.setRepositorySession( repositorySession );

        ArchetypeCatalog catalog = archetype.getLocalCatalog( buildingRequest );

        System.err.println( "archetypes => " + catalog.getArchetypes() );
        // Here I am just grabbing a OldArchetype but in a UI you would take the OldArchetype objects and present
        // them to the user.

        Archetype selection = catalog.getArchetypes().get( catalog.getArchetypes().size() - 1 );

        System.err.println( "Selected OldArchetype = " + selection );
        // Now you will present a dialog, or whatever, and grab the following values.

        String groupId = "com.mycompany";

        String artifactId = "app";

        String version = "1.0.0";

        String packageName = "org.mycompany.app";

        // With the selected OldArchetype and the parameters you can create a generation request as follows:
        File outputDirectory = new File( getBasedir(), "target/test-classes/projects/archetyper-generate-1" );
        FileUtils.forceDelete(outputDirectory);

        ArchetypeGenerationRequest agr = new ArchetypeGenerationRequest( selection )
            .setOutputDirectory( outputDirectory.getAbsolutePath() )
            .setLocalRepository( localRepository )
            .setGroupId( groupId )
            .setArtifactId( artifactId )
            .setVersion( version )
            .setPackage( packageName );

        Properties archetypeRequiredProperties = new Properties();
        archetypeRequiredProperties.setProperty( "property-with-default-1", "value-1" );
        archetypeRequiredProperties.setProperty( "property-with-default-2", "value-2" );
        archetypeRequiredProperties.setProperty( "property-with-default-3", "value-3" );
        archetypeRequiredProperties.setProperty( "property-with-default-4", "value-4" );
        archetypeRequiredProperties.setProperty( "property-without-default-1", "some-value-1" );
        archetypeRequiredProperties.setProperty( "property-without-default-2", "some-value-2" );
        archetypeRequiredProperties.setProperty( "property-without-default-3", "some-value-3" );
        archetypeRequiredProperties.setProperty( "property-without-default-4", "some-value-4" );
        archetypeRequiredProperties.setProperty( "property_underscored_1", "prop1" );
        archetypeRequiredProperties.setProperty( "property_underscored-2", "prop2" );
        agr.setProperties( archetypeRequiredProperties );
        agr.setProjectBuildingRequest( buildingRequest );
        
        // Then generate away!

        ArchetypeGenerationResult result = archetype.generateProjectFromArchetype( agr );

        if ( result.getCause() != null )
        {
            result.getCause().printStackTrace( System.err );
            fail( result.getCause().getMessage() );
        }
    }
    
    private ArtifactRepository createRepository( String url, String repositoryId )
    {
        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;

        ArtifactRepositoryPolicy snapshotsPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );

        ArtifactRepositoryPolicy releasesPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );
        
        return new MavenArtifactRepository( repositoryId, url, new DefaultRepositoryLayout() , snapshotsPolicy,
                                            releasesPolicy );
    }


}