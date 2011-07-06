package org.apache.maven.archetype;

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

import org.apache.commons.io.IOUtils;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.codehaus.plexus.PlexusTestCase;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * @author rafale
 */
public class ArchetypeCatalogsTest
    extends PlexusTestCase
{
    public void testRemoteCatalog()
        throws Exception
    {

        ArchetypeManager archetype = (ArchetypeManager) lookup( ArchetypeManager.class.getName() );

        ArchetypeCatalog result = archetype.getRemoteCatalog( "http://localhost:" + port + "/repo/" );

        assertEquals( 1, result.getArchetypes().size() );
        assertEquals( "groupId", ( (Archetype) result.getArchetypes().get( 0 ) ).getGroupId() );
        assertEquals( "artifactId", ( (Archetype) result.getArchetypes().get( 0 ) ).getArtifactId() );
        assertEquals( "1", ( (Archetype) result.getArchetypes().get( 0 ) ).getVersion() );
        assertEquals( "http://localhost:" + port + "/repo/",
                      ( (Archetype) result.getArchetypes().get( 0 ) ).getRepository() );
    }

    public void testLocalCatalog()
        throws Exception
    {

        ArchetypeManager archetype = (ArchetypeManager) lookup( ArchetypeManager.class.getName() );

        ArchetypeCatalog result =
            archetype.getLocalCatalog( getTestFile( "target/test-classes/repositories/test-catalog" ).
                getAbsolutePath() );

        assertEquals( 1, result.getArchetypes().size() );
        assertEquals( "groupId", ( (Archetype) result.getArchetypes().get( 0 ) ).getGroupId() );
        assertEquals( "artifactId", ( (Archetype) result.getArchetypes().get( 0 ) ).getArtifactId() );
        assertEquals( "1", ( (Archetype) result.getArchetypes().get( 0 ) ).getVersion() );
        assertEquals( "http://localhost:" + port + "/repo/",
                      ( (Archetype) result.getArchetypes().get( 0 ) ).getRepository() );
    }


    private Server server;

    int port;

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
        Writer writer = new FileWriter( catalogFile );
        catalogWriter.write( writer, catalog );
        IOUtils.closeQuietly( writer );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        // Stop Jetty
        server.stop();
    }
}
