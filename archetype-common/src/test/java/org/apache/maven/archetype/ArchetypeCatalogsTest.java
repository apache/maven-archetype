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
package org.apache.maven.archetype;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalContainer;
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalDeployer;
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 * @author rafale
 */
public class ArchetypeCatalogsTest
    extends PlexusTestCase
{
    public void testRemoteCatalog()
        throws Exception
    {

        Archetype archetype = (Archetype) lookup( Archetype.class );

        ArchetypeCatalog result = archetype.getRemoteCatalog( "http://localhost:18881/repo/" );

        assertEquals( 1, result.getArchetypes().size() );
        assertEquals( "groupId", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getGroupId() );
        assertEquals( "artifactId", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getArtifactId() );
        assertEquals( "1", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getVersion() );
        assertEquals( "http://localhost:18881/repo/", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getRepository() );
    }

    public void testLocalCatalog()
        throws Exception
    {

        Archetype archetype = (Archetype) lookup( Archetype.class );

        ArchetypeCatalog result = archetype.getLocalCatalog( getTestFile( "target/test-classes/repositories/test-catalog" ).
            getAbsolutePath() );

        assertEquals( 1, result.getArchetypes().size() );
        assertEquals( "groupId", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getGroupId() );
        assertEquals( "artifactId", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getArtifactId() );
        assertEquals( "1", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getVersion() );
        assertEquals( "http://localhost:18881/repo/", ((org.apache.maven.archetype.catalog.Archetype) result.getArchetypes().
            get( 0 )).getRepository() );
    }

    private Jetty6xEmbeddedLocalContainer cargo;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        //        Start Cargo

        File catalogDirectory = getTestFile( "target/test-classes/repositories/test-catalog" );
        catalogDirectory.mkdirs();

        getTestFile( "target/test-classes/repositories/test-catalog/dummy" ).createNewFile();

        ArchetypeCatalog catalog = new ArchetypeCatalog();
        org.apache.maven.archetype.catalog.Archetype generatedArchetype = new org.apache.maven.archetype.catalog.Archetype();
        generatedArchetype.setGroupId( "groupId" );
        generatedArchetype.setArtifactId( "artifactId" );
        generatedArchetype.setVersion( "1" );
        generatedArchetype.setRepository( "http://localhost:18881/repo/" );
        catalog.addArchetype( generatedArchetype );

        File catalogFile = new File( catalogDirectory, "archetype-catalog.xml" );
        ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();
        Writer writer = new FileWriter( catalogFile );
        catalogWriter.write( writer, catalog );
        IOUtils.closeQuietly( writer );


        Jetty6xEmbeddedStandaloneLocalConfiguration configuration =
            new Jetty6xEmbeddedStandaloneLocalConfiguration( "target/repository-webapp" );
        configuration.setProperty( ServletPropertySet.PORT, "18881" );

        System.setProperty( "org.apache.maven.archetype.reporitory.directory",
            getTestPath( "target/test-classes/repositories/test-catalog" ) );
        cargo = new Jetty6xEmbeddedLocalContainer( configuration );
        cargo.setTimeout( 180000L );
        cargo.start();

        DeployableFactory factory = new DefaultDeployableFactory();
        WAR war = (WAR) factory.createDeployable( cargo.getId(),
            "target/wars/archetype-repository.war",
            DeployableType.WAR );

        war.setContext( "/repo" );

        Deployer deployer = new Jetty6xEmbeddedLocalDeployer( cargo );
        deployer.deploy( war,
            new URLDeployableMonitor( new URL( "http://localhost:18881/repo/dummy" ) ) );
        deployer.start( war );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        //        Stop Cargo

        cargo.stop();
    }
}
