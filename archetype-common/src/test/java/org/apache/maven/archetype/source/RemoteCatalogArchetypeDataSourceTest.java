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

import java.io.FileWriter;
import java.io.File;
import java.io.Writer;

import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class RemoteCatalogArchetypeDataSourceTest extends AbstractMojoTestCase
{
    private Server server;

    int port;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        System.setProperty( "org.apache.maven.archetype.repository.directory",
                            getTestPath( "target/test-classes/repositories/test-catalog" ) );

        // Start Jetty
        server = new Server( 0 );

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath( "/repo" );
        webapp.setWar( "target/wars/archetype-repository.war" );
        server.setHandler( webapp );

        server.start();

        port = server.getConnectors()[0].getLocalPort();

        File catalogDirectory = getTestFile( "target/test-classes/repositories/test-catalog" );
        catalogDirectory.mkdirs();

        getTestFile( "target/test-classes/repositories/test-catalog/dummy" ).createNewFile();

        ArchetypeCatalog catalog = new ArchetypeCatalog();
        Archetype generatedArchetype = new Archetype();
        generatedArchetype.setGroupId( "groupId" );
        generatedArchetype.setArtifactId( "artifactId" );
        generatedArchetype.setVersion( "1" );
        generatedArchetype.setRepository( "http://localhost:" + port + "/repo/" );
        catalog.addArchetype( generatedArchetype );

        File catalogFile = new File( catalogDirectory, "archetype-catalog.xml" );
        ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();
        try ( Writer writer = new FileWriter( catalogFile ) )
        {
            catalogWriter.write( writer, catalog );
        }
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        // Stop Jetty
        server.stop();
    }
    
    public void testRemoteCatalog()
        throws Exception
    {
        ArchetypeManager archetype = lookup( ArchetypeManager.class );
        
        LegacySupport legacySupport = lookup( LegacySupport.class );
        legacySupport.setSession( newMavenSession( new MavenProjectStub() ) );

        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        buildingRequest.getRemoteRepositories().add( new MavenArtifactRepository( "central",
                                                                                  "http://localhost:" + port + "/repo/",
                                                                                  new DefaultRepositoryLayout(), null,
                                                                                  null ) );

        ArchetypeCatalog result = archetype.getRemoteCatalog( buildingRequest );

        assertEquals( 1, result.getArchetypes().size() );
        assertEquals( "groupId", result.getArchetypes().get( 0 ).getGroupId() );
        assertEquals( "artifactId", result.getArchetypes().get( 0 ).getArtifactId() );
        assertEquals( "1", result.getArchetypes().get( 0 ).getVersion() );
        assertEquals( "http://localhost:" + port + "/repo/", result.getArchetypes().get( 0 ).getRepository() );
    }

}
