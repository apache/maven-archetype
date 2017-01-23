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
import org.apache.maven.model.Model;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.interpolation.ModelInterpolator;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

/**
 * @author Jason van Zyl
 */
public class ArchetyperRoundtripWithProxyIT
    extends PlexusTestCase
{
    Server proxyServer;

    int proxyPort;

    Server server;

    int port;

    public void testArchetyper()
        throws Exception
    {
        ArchetypeManager archetype = (ArchetypeManager) lookup( ArchetypeManager.ROLE );

        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.ROLE );

        MavenProjectBuilder projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        ArtifactRepository localRepository = registryManager.createRepository( new File( getBasedir(),
                                                                                         "target" + File.separator
                                                                                             + "test-classes"
                                                                                             + File.separator
                                                                                             + "repositories"
                                                                                             + File.separator
                                                                                             + "local" ).toURI().toURL().toExternalForm(),
                                                                               "local-repo" );
        ArtifactRepository centralRepository = registryManager.createRepository( new File( getBasedir(),
                                                                                           "target" + File.separator
                                                                                               + "test-classes"
                                                                                               + File.separator
                                                                                               + "repositories"
                                                                                               + File.separator
                                                                                               + "central" ).toURI().toURL().toExternalForm(),
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

        File workingProject = new File( getBasedir(),
                                        "target" + File.separator + "test-classes" + File.separator + "projects"
                                            + File.separator + "roundtrip-2-project" );
        FileUtils.forceDelete( new File( workingProject, "target" ) );

        // (2) create an archetype from the project
        File pom = new File( workingProject, "pom.xml" );

        MavenProject project = projectBuilder.build( pom, localRepository, null );

        ArchetypeCreationRequest acr = new ArchetypeCreationRequest().setProject( project ).
            setLocalRepository( localRepository ).setPostPhase( "package" );

        ArchetypeCreationResult creationResult = archetype.createArchetypeFromProject( acr );

        if ( creationResult.getCause() != null )
        {
            throw creationResult.getCause();
        }

        // (3) create our own archetype catalog properties in memory
        File catalogDirectory = new File( getBasedir(), "target" + File.separator + "catalog" );

        File catalogFile = new File( catalogDirectory, "archetype-catalog.xml" );

        File catalogProperties = new File( catalogDirectory, "archetype-catalog.properties" );

        catalogDirectory.mkdirs();

        Properties p = new Properties();
        p.setProperty( "sources", "catalog" );
        p.setProperty( "catalog.file", catalogFile.getAbsolutePath() );
        OutputStream os = new FileOutputStream( catalogProperties );
        p.store( os, "Generated catalog properties" );

        // (5) install the archetype we just created
        File generatedArchetypeDirectory = new File( project.getBasedir(),
                                                     "target" + File.separator + "generated-sources" + File.separator
                                                         + "archetype" );
        File generatedArchetypePom = new File( generatedArchetypeDirectory, "pom.xml" );
        MavenProject generatedArchetypeProject = projectBuilder.build( generatedArchetypePom, localRepository, null );
        ModelInterpolator modelInterpolator = (ModelInterpolator)lookup( ModelInterpolator.ROLE );
        Model generatedArchetypeModel = modelInterpolator.interpolate( generatedArchetypeProject.getModel(), generatedArchetypePom.getParentFile(), new DefaultProjectBuilderConfiguration(), true );

        File archetypeDirectory =
            new File( generatedArchetypeDirectory, "src" + File.separator + "main" + File.separator + "resources" );

        File archetypeArchive = archetype.archiveArchetype( archetypeDirectory, new File(
            generatedArchetypeModel.getBuild().getDirectory() ),
                                                            generatedArchetypeModel.getBuild().getFinalName() );

        String baseName =
            StringUtils.replace( generatedArchetypeProject.getGroupId(), ".", File.separator ) + File.separator
                + generatedArchetypeProject.getArtifactId() + File.separator + generatedArchetypeProject.getVersion()
                + File.separator + generatedArchetypeModel.getBuild().getFinalName();
        File archetypeInRepository = new File( centralRepository.getBasedir(), baseName + ".jar" );
        File archetypePomInRepository = new File( centralRepository.getBasedir(), baseName + ".pom" );
        archetypeInRepository.getParentFile().mkdirs();
        FileUtils.copyFile( archetypeArchive, archetypeInRepository );
        FileUtils.copyFile( generatedArchetypePom, archetypePomInRepository );

        // (4) create our own archetype catalog describing the archetype we just created
        ArchetypeCatalog catalog = new ArchetypeCatalog();
        Archetype generatedArchetype = new Archetype();
        generatedArchetype.setGroupId( generatedArchetypeProject.getGroupId() );
        generatedArchetype.setArtifactId( generatedArchetypeProject.getArtifactId() );
        generatedArchetype.setVersion( generatedArchetypeProject.getVersion() );
        generatedArchetype.setRepository( "http://localhost:" + port + "/repo" );
        catalog.addArchetype( generatedArchetype );

        ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();
        Writer writer = new FileWriter( catalogFile );
        catalogWriter.write( writer, catalog );
        IOUtils.closeQuietly( writer );

        // (6) create a project form the archetype we just created
        String outputDirectory = new File( getBasedir(),
                                           "target" + File.separator + "test-classes" + File.separator + "projects"
                                               + File.separator + "roundtrip-2-recreatedproject" ).getAbsolutePath();
        FileUtils.forceDelete( outputDirectory );

        WagonManager manager = (WagonManager) lookup( WagonManager.class.getName() );
        manager.addProxy( "http", "localhost", proxyPort, null, null, null );

        ArchetypeGenerationRequest agr =
            new ArchetypeGenerationRequest().setArchetypeGroupId( generatedArchetypeProject.getGroupId() ).
                setArchetypeArtifactId( generatedArchetypeProject.getArtifactId() ).
                setArchetypeVersion( generatedArchetypeProject.getVersion() ).
                setGroupId( "com.mycompany" ).setArtifactId( "myapp" ).setVersion( "1.0-SNAPSHOT" ).
                setPackage( "com.mycompany.myapp" ).setOutputDirectory( outputDirectory ).
                setLocalRepository( localRepository ).setArchetypeRepository( "http://localhost:" + port + "/repo" );
        ArchetypeGenerationResult generationResult = archetype.generateProjectFromArchetype( agr );

        if ( generationResult.getCause() != null )
        {
            throw generationResult.getCause();
        }

    }

    public void setUp()
        throws Exception
    {
        super.setUp();
        // Start Proxy Jetty

        System.setProperty( "org.apache.maven.archetype.repository.directory",
                            getTestPath( "target/test-classes/repositories/central" ) );

        proxyServer = new Server( 0 );

        WebAppContext webappProxy = new WebAppContext();
        webappProxy.setContextPath( "/" );
        webappProxy.setWar( "target/wars/archetype-proxy.war" );
        proxyServer.setHandler( webappProxy );

        proxyServer.start();

        proxyPort = proxyServer.getConnectors()[0].getLocalPort();

        server = new Server( 0 );

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath( "/repo" );
        webapp.setWar( "target/wars/archetype-repository.war" );
        server.setHandler( webapp );

        server.start();

        port = server.getConnectors()[0].getLocalPort();

    }

    public void tearDown()
        throws Exception
    {
        super.tearDown();
        // Stop Jettys

        proxyServer.stop();
        server.stop();
    }
}
