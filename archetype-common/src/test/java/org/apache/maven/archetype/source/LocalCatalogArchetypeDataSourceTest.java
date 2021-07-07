package org.apache.maven.archetype.source;

/*
 *  Copyright 2007 rafale.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusTestCase;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.repository.LocalRepositoryManager;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class LocalCatalogArchetypeDataSourceTest extends PlexusTestCase
{

    @Override
    protected void setUp()
                    throws Exception
    {
        super.setUp();

        File catalogDirectory = getTestFile( "target/test-classes/repositories/test-catalog" );
        catalogDirectory.mkdirs();

        ArchetypeCatalog catalog = new ArchetypeCatalog();
        Archetype generatedArchetype = new Archetype();
        generatedArchetype.setGroupId( "groupId" );
        generatedArchetype.setArtifactId( "artifactId" );
        generatedArchetype.setVersion( "1" );
        generatedArchetype.setRepository( "http://localhost:0/repo/" );
        catalog.addArchetype( generatedArchetype );

        File catalogFile = new File( catalogDirectory, "archetype-catalog.xml" );
        ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();
        try ( Writer writer = new FileWriter( catalogFile ) )
        {
            catalogWriter.write( writer, catalog );
        }
    }
    
    public void testLocalCatalog()
                    throws Exception
    {
        ArchetypeManager archetype = lookup( ArchetypeManager.class );
        DefaultRepositorySystemSession drss = new DefaultRepositorySystemSession();
        LocalRepositoryManager localRepositoryManager = drss.getLocalRepositoryManager();
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
//        MavenRepositorySystemSession repositorySession = new MavenRepositorySystemSession();
//        drss.setLocalRepositoryManager((LocalRepositoryManager) new SimpleLocalRepositoryManager( getTestFile( "target/test-classes/repositories/test-catalog" ) ));
        buildingRequest.setRepositorySession( drss );
        
        
        ArchetypeCatalog result = archetype.getLocalCatalog( buildingRequest );

        assertEquals( 1, result.getArchetypes().size() );
        assertEquals( "groupId", result.getArchetypes().get( 0 ).getGroupId() );
        assertEquals( "artifactId", result.getArchetypes().get( 0 ).getArtifactId() );
        assertEquals( "1", result.getArchetypes().get( 0 ).getVersion() );
        assertEquals( "http://localhost:0/repo/", result.getArchetypes().get( 0 ).getRepository() );
    }

}
