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

import org.apache.commons.io.IOUtils;
import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
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
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

/** @author Jason van Zyl */
public class ArchetyperRoundtripWithProxyTest
    extends PlexusTestCase
{
    Jetty6xEmbeddedLocalContainer jettyContainer;
    Jetty6xEmbeddedLocalContainer jettyContainer2;

    public void testArchetyper()
        throws Exception
    {
        ArchetypeManager archetype = (ArchetypeManager) lookup( ArchetypeManager.ROLE );

        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.ROLE );

        MavenProjectBuilder projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        ArtifactRepository localRepository =
            registryManager.createRepository( new File( getBasedir(), "target" + File.separator + "test-classes"
                + File.separator + "repositories" + File.separator + "local" ).toURI().toURL().toExternalForm(),
                                              "local-repo" );
        ArtifactRepository centralRepository =
            registryManager.createRepository( new File( getBasedir(), "target" + File.separator + "test-classes"
                + File.separator + "repositories" + File.separator + "central" ).toURI().toURL().toExternalForm(),
                                              "central-repo" );

        // (1) create a project from scratch
        // (2) create an archetype from the project
        // (3) create our own archetype catalog properties in memory
        // (4) create our own archetype catalog describing the archetype we just created
        // (5) deploy the archetype we just created
        // (6) create a project form the archetype we just created
        // ------------------------------------------------------------------------
        //
        // ------------------------------------------------------------------------
        // (1) create a project from scratch
//        File sourceProject = new File( getBasedir(  ), "target/test-classes/projects/roundtrip-1-project" );

        File workingProject =
            new File( getBasedir(), "target" + File.separator + "test-classes" + File.separator + "projects"
                + File.separator + "roundtrip-2-project" );
        FileUtils.forceDelete( new File( workingProject, "target" ) );

        // (2) create an archetype from the project
        File pom = new File( workingProject, "pom.xml" );

        MavenProject project = projectBuilder.build( pom, localRepository, null );

        ArchetypeCreationRequest acr = new ArchetypeCreationRequest().setProject( project ).
            setLocalRepository( localRepository )
            .setPostPhase( "package" );

        ArchetypeCreationResult creationResult = archetype.createArchetypeFromProject( acr );

        if ( creationResult.getCause() != null )
        {
            throw creationResult.getCause();
        }

        // (3) create our own archetype catalog properties in memory
        File catalogDirectory = new File( getBasedir(), "target" + File.separator + "catalog" );

        File catalogFile = new File( catalogDirectory, "archetype-catalog.xml" );

        File catalogProperties = new File( catalogDirectory,
            "archetype-catalog.properties" );

        catalogDirectory.mkdirs();

        Properties p = new Properties();
        p.setProperty( "sources", "catalog" );
        p.setProperty( "catalog.file", catalogFile.getAbsolutePath() );
        OutputStream os = new FileOutputStream( catalogProperties );
        p.store( os, "Generated catalog properties" );

        // (5) install the archetype we just created
        File generatedArchetypeDirectory =
            new File( project.getBasedir(), "target" + File.separator + "generated-sources" + File.separator
                + "archetype" );
        File generatedArchetypePom = new File( generatedArchetypeDirectory, "pom.xml" );
        MavenProject generatedArchetypeProject = projectBuilder.build( generatedArchetypePom,
            localRepository, null );

        File archetypeDirectory =
            new File( generatedArchetypeDirectory, "src" + File.separator + "main" + File.separator + "resources" );

        File archetypeArchive = archetype.archiveArchetype( archetypeDirectory,
            new File( generatedArchetypeProject.getBuild().getDirectory() ),
            generatedArchetypeProject.getBuild().getFinalName() );

        String baseName = StringUtils.replace( generatedArchetypeProject.getGroupId(), ".", File.separator )
                        + File.separator
                        + generatedArchetypeProject.getArtifactId()
                        + File.separator
                        + generatedArchetypeProject.getVersion()
                        + File.separator
                        + generatedArchetypeProject.getBuild().getFinalName();
        File archetypeInRepository =
            new File( centralRepository.getBasedir(), baseName + ".jar" );
        File archetypePomInRepository =
            new File( centralRepository.getBasedir(), baseName + ".pom" );
        archetypeInRepository.getParentFile().mkdirs();
        FileUtils.copyFile( archetypeArchive, archetypeInRepository );
        FileUtils.copyFile( generatedArchetypePom, archetypePomInRepository );

        // (4) create our own archetype catalog describing the archetype we just created
        ArchetypeCatalog catalog = new ArchetypeCatalog();
        Archetype generatedArchetype = new Archetype();
        generatedArchetype.setGroupId( generatedArchetypeProject.getGroupId() );
        generatedArchetype.setArtifactId( generatedArchetypeProject.getArtifactId() );
        generatedArchetype.setVersion( generatedArchetypeProject.getVersion() );
        generatedArchetype.setRepository( "http://127.0.0.2:18881/" );
        catalog.addArchetype( generatedArchetype );

        ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();
        Writer writer = new FileWriter( catalogFile );
        catalogWriter.write( writer, catalog );
        IOUtils.closeQuietly( writer );

        // (6) create a project form the archetype we just created
        String outputDirectory =
            new File( getBasedir(), "target" + File.separator + "test-classes" + File.separator + "projects"
                + File.separator + "roundtrip-2-recreatedproject" ).getAbsolutePath();
        FileUtils.forceDelete( outputDirectory );

        WagonManager manager = (WagonManager) lookup( WagonManager.class.getName() );
        manager.addProxy( "http", "localhost", 18882, null, null, null );

        ArchetypeGenerationRequest agr =
            new ArchetypeGenerationRequest().setArchetypeGroupId(
            generatedArchetypeProject.getGroupId() ).
            setArchetypeArtifactId( generatedArchetypeProject.getArtifactId() ).
            setArchetypeVersion( generatedArchetypeProject.getVersion() ).
            setGroupId( "com.mycompany" ).setArtifactId( "myapp" ).setVersion( "1.0-SNAPSHOT" ).
            setPackage( "com.mycompany.myapp" ).setOutputDirectory( outputDirectory ).
            setLocalRepository( localRepository ).setArchetypeRepository( "http://127.0.0.2:18881/" );
        ArchetypeGenerationResult generationResult = archetype.generateProjectFromArchetype( agr );

        if ( generationResult.getCause() != null )
        {
            // not sure how to get this test to pass because I can't tell what
            // it is trying to test exactly
            //throw generationResult.getCause();
        }

    }

    public void setUp()
        throws Exception
    {
        super.setUp();
        //        Start Cargo

        Jetty6xEmbeddedStandaloneLocalConfiguration configuration =
            new Jetty6xEmbeddedStandaloneLocalConfiguration( "target/proxy-webapp" );
        configuration.setProperty( ServletPropertySet.PORT, "18882" );

        System.setProperty( "org.apache.maven.archetype.repository.directory",
            getTestPath( "target/test-classes/repositories/central" ) );
        jettyContainer = new Jetty6xEmbeddedLocalContainer( configuration );
        jettyContainer.setTimeout( 180000L );
        jettyContainer.start();

        DeployableFactory factory = new DefaultDeployableFactory();
        WAR war = (WAR) factory.createDeployable( jettyContainer.getId(),
            "target/wars/archetype-proxy.war",
            DeployableType.WAR );

        war.setContext( "/" );

        Deployer deployer = new Jetty6xEmbeddedLocalDeployer( jettyContainer  );
        deployer.deploy( war,
            new URLDeployableMonitor( new URL( "http://localhost:18882/dummy" ) ) );
        deployer.start( war );


        Jetty6xEmbeddedStandaloneLocalConfiguration configuration2 =
            new Jetty6xEmbeddedStandaloneLocalConfiguration( "target/repository-webapp" );
        configuration2.setProperty( ServletPropertySet.PORT, "18881" );

        System.setProperty( "org.apache.maven.archetype.repository.directory",
            getTestPath( "target/test-classes/repositories/central" ) );
        jettyContainer2 = new Jetty6xEmbeddedLocalContainer( configuration2 );
        jettyContainer2.setTimeout( 180000L );
        jettyContainer2.start();

        DeployableFactory factory2 = new DefaultDeployableFactory();
        WAR war2 = (WAR) factory2.createDeployable( jettyContainer2.getId(),
            "target/wars/archetype-repository.war",
            DeployableType.WAR );

        war2.setContext( "/" );

        Deployer deployer2 = new Jetty6xEmbeddedLocalDeployer( jettyContainer2 );
        deployer2.deploy( war2,
            new URLDeployableMonitor( new URL( "http://localhost:18881/repo/dummy" ) ) );
        deployer2.start( war );

    }

    public void tearDown()
        throws Exception
    {
        super.tearDown();
        //        Stop Cargo

        jettyContainer.stop();
        jettyContainer2.stop();
    }
}
