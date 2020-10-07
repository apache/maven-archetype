package org.apache.maven.archetype.old;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class ArchetypeTest
    extends PlexusTestCase
{
    private OldArchetype archetype;

    public void testArchetype()
        throws Exception
    {
        FileUtils.deleteDirectory( getTestFile( "target/quickstart" ) );

        // ----------------------------------------------------------------------
        // This needs to be encapsulated in a maven test case.
        // ----------------------------------------------------------------------

        ArtifactRepositoryLayout layout =
            (ArtifactRepositoryLayout) getContainer().lookup( ArtifactRepositoryLayout.ROLE );

        String mavenRepoLocal = getTestFile( "target/local-repository" ).toURI().toURL().toString();

        ArtifactRepository localRepository = new DefaultArtifactRepository( "local", mavenRepoLocal, layout );

        List<ArtifactRepository> remoteRepositories = new ArrayList<>();

        String mavenRepoRemote = getTestFile( "src/test/repository" ).toURI().toURL().toString();

        ArtifactRepository remoteRepository = new DefaultArtifactRepository( "remote", mavenRepoRemote, layout );

        remoteRepositories.add( remoteRepository );
        
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        buildingRequest.setRemoteRepositories( remoteRepositories );
        MavenRepositorySystemSession repositorySession = new MavenRepositorySystemSession();
        repositorySession.setLocalRepositoryManager( new SimpleLocalRepositoryManager( localRepository.getBasedir() ) );
        buildingRequest.setRepositorySession( repositorySession );

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest()
            .setProjectBuildingRequest( buildingRequest )
            .setPackage( "org.apache.maven.quickstart" )
            .setGroupId( "maven" )
            .setArtifactId( "quickstart" )
            .setVersion( "1.0-alpha-1-SNAPSHOT" )
            .setArchetypeGroupId( "org.apache.maven.archetypes" )
            .setArchetypeArtifactId( "maven-archetype-quickstart" )
            .setArchetypeVersion( "1.0-alpha-1-SNAPSHOT" )
            .setLocalRepository( localRepository )
            .setRemoteArtifactRepositories( remoteRepositories )
            .setOutputDirectory( getTestFile( "target" ).getAbsolutePath() );
        //parameters.put( "name", "jason" );

        archetype.createArchetype( request, remoteRepository );

        // ----------------------------------------------------------------------
        // Set up the Velocity context
        // ----------------------------------------------------------------------

        Map<String, Object> parameters = new HashMap<>();
        parameters.put( "basedir", request.getOutputDirectory() );
        parameters.put( "package", request.getPackage() );
        parameters.put( "packageName", request.getPackage() );
        parameters.put( "groupId", request.getGroupId() );
        parameters.put( "artifactId", request.getArtifactId() );
        parameters.put( "version", request.getVersion() );

        Context context = new VelocityContext();

        for ( Map.Entry<String, Object> entry : parameters.entrySet() )
        {
            context.put( entry.getKey(), entry.getValue() );
        }

        // ----------------------------------------------------------------------
        // Validate POM generation
        // ----------------------------------------------------------------------

        ArtifactFactory artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.class.getName() );
        Artifact archetypeArtifact = artifactFactory.createArtifact( request.getArchetypeGroupId(), request.getArchetypeArtifactId(),
                                                                     request.getArchetypeVersion(), Artifact.SCOPE_RUNTIME, "jar" );

        StringWriter writer = new StringWriter();

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(
            getContextClassloader( archetypeArtifact, localRepository, remoteRepositories ) );

        try
        {
            VelocityComponent velocity = (VelocityComponent) lookup( VelocityComponent.class.getName() );

            velocity.getEngine().mergeTemplate( OldArchetype.ARCHETYPE_RESOURCES + "/" + OldArchetype.ARCHETYPE_POM, context,
                                                writer );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( old );
        }

        Model generatedModel, templateModel;
        try
        {
            StringReader strReader = new StringReader( writer.toString() );

            MavenXpp3Reader reader = new MavenXpp3Reader();

            templateModel = reader.read( strReader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeTemplateProcessingException( "Error reading template POM", e );
        }

        File artifactDir = getTestFile( "target", (String) parameters.get( "artifactId" ) );
        File pomFile = getTestFile( artifactDir.getAbsolutePath(), OldArchetype.ARCHETYPE_POM );

        try ( FileReader pomReader = new FileReader( pomFile ) )
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();

            generatedModel = reader.read( pomReader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeTemplateProcessingException( "Error reading generated POM", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeTemplateProcessingException( "Error reading generated POM", e );
        }
        assertEquals( "Generated POM ArtifactId is not equivalent to expected result.", generatedModel.getArtifactId(),
                      templateModel.getArtifactId() );
        assertEquals( "Generated POM GroupId is not equivalent to expected result.", generatedModel.getGroupId(),
                      templateModel.getGroupId() );
        assertEquals( "Generated POM Id is not equivalent to expected result.", generatedModel.getId(),
                      templateModel.getId() );
        assertEquals( "Generated POM Version is not equivalent to expected result.", generatedModel.getVersion(),
                      templateModel.getVersion() );
        assertEquals( "Generated POM Packaging is not equivalent to expected result.", generatedModel.getPackaging(),
                      templateModel.getPackaging() );
        assertEquals( "Generated POM Developers is not equivalent to expected result.", generatedModel.getDevelopers(),
                      templateModel.getDevelopers() );
        assertEquals( "Generated POM Scm is not equivalent to expected result.", generatedModel.getScm(),
                      templateModel.getScm() );
    }

    // Gets the classloader for this artifact's file.
    private ClassLoader getContextClassloader( Artifact archetypeArtifact, ArtifactRepository localRepository,
                                               List<ArtifactRepository> remoteRepositories )
        throws Exception
    {
        ArtifactResolver artifactResolver = (ArtifactResolver) lookup( ArtifactResolver.class.getName() );
        try
        {
            artifactResolver.resolve( archetypeArtifact, remoteRepositories, localRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new ArchetypeDescriptorException( "Error attempting to download archetype: " + e.getMessage(), e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new ArchetypeNotFoundException( "OldArchetype does not exist: " + e.getMessage(), e );
        }

        URLClassLoader archetypeJarLoader;
        try
        {
            URL[] urls = new URL[1];

            urls[0] = archetypeArtifact.getFile().toURI().toURL();

            archetypeJarLoader = new URLClassLoader( urls );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDescriptorException(
                "Error reading the " + OldArchetype.ARCHETYPE_DESCRIPTOR + " descriptor.", e );
        }

        return archetypeJarLoader;
    }

    public void testAddModuleToParentPOM()
        throws Exception
    {
        String pom = "<project>\n"
            + "  <packaging>pom</packaging>\n"
            + "</project>";

        StringWriter out = new StringWriter();
        assertTrue( DefaultOldArchetype.addModuleToParentPom( "myArtifactId1", new StringReader( pom ), out ) );

        assertThat( out.toString(), isIdenticalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<project>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId1</module>\n"
                + "  </modules>\n"
                + "</project>" ).normalizeWhitespace() );

        pom = "<project>\n"
            + "  <modelVersion>4.0.0</modelVersion>\n"
            + "  <packaging>pom</packaging>\n"
            + "</project>";

        out = new StringWriter();
        assertTrue( DefaultOldArchetype.addModuleToParentPom( "myArtifactId2", new StringReader( pom ), out ) );

        assertThat( out.toString(), isIdenticalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<project>\n"
                + "  <modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId2</module>\n"
                + "  </modules>\n"
                + "</project>" ).normalizeWhitespace() );

        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
            + "  <packaging>pom</packaging>\n"
            + "  <modules>\n"
            + "  </modules>\n"
            + "</project>";

        out = new StringWriter();
        assertTrue( DefaultOldArchetype.addModuleToParentPom( "myArtifactId3", new StringReader( pom ), out ) );

        assertThat( out.toString(), isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId3</module>\n"
                + "  </modules>\n"
                + "</project>" ).normalizeWhitespace() );

        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
            + "  <packaging>pom</packaging>\n"
            + "  <modules>\n"
            + "    <module>myArtifactId3</module>\n"
            + "  </modules>\n"
            + "</project>";

        out = new StringWriter();
        assertTrue( DefaultOldArchetype.addModuleToParentPom( "myArtifactId4", new StringReader( pom ), out ) );

        assertThat( out.toString(), isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId3</module>\n"
                + "    <module>myArtifactId4</module>\n"
                + "  </modules>\n"
                + "</project>" ).normalizeWhitespace() );

        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
            + "  <packaging>pom</packaging>\n"
            + "  <modules>\n"
            + "    <module>myArtifactId3</module>\n"
            + "  </modules>\n"
            + "</project>";

        out = new StringWriter();
        assertFalse( DefaultOldArchetype.addModuleToParentPom( "myArtifactId3", new StringReader( pom ), out ) );

        // empty means unchanged
        assertEquals( "", out.toString().trim() );


        pom = "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId1</module>\n"
                + "    <module>myArtifactId2</module>\n"
                + "    <module>myArtifactId3</module>\n"
                + "  </modules>\n"
                + "  <profiles>\n"
                + "    <profile>\n"
                + "      <id>profile1</id>\n"
                + "      <modules>\n"
                + "        <module>module1</module>\n"
                + "      </modules>\n"
                + "    </profile>\n"
                + "    <profile>\n"
                + "      <id>profile2</id>\n"
                + "      <modules>\n"
                + "        <module>module2</module>\n"
                + "      </modules>\n"
                + "    </profile>\n"
                + "  </profiles>\n"
                + "</project>";

        out = new StringWriter();
        assertTrue( DefaultOldArchetype.addModuleToParentPom( "module1", new StringReader( pom ), out ) );

        assertThat( out.toString(), isIdenticalTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<project><modelVersion>4.0.0</modelVersion>\n"
                + "  <packaging>pom</packaging>\n"
                + "  <modules>\n"
                + "    <module>myArtifactId1</module>\n"
                + "    <module>myArtifactId2</module>\n"
                + "    <module>myArtifactId3</module>\n"
                + "    <module>module1</module>\n"
                + "  </modules>\n"
                + "  <profiles>\n"
                + "    <profile>\n"
                + "      <id>profile1</id>\n"
                + "      <modules>\n"
                + "        <module>module1</module>\n"
                + "      </modules>\n"
                + "    </profile>\n"
                + "    <profile>\n"
                + "      <id>profile2</id>\n"
                + "      <modules>\n"
                + "        <module>module2</module>\n"
                + "      </modules>\n"
                + "    </profile>\n"
                + "  </profiles>\n"
                + "</project>" ).normalizeWhitespace() );
    }

    public void testAddModuleToParentPOMNoPackaging()
        throws Exception
    {
        try
        {
            String pom = "<project>\n</project>";
            DefaultOldArchetype.addModuleToParentPom( "myArtifactId1", new StringReader( pom ), new StringWriter() );
            fail( "Should fail to add a module to a JAR packaged project" );
        }
        catch ( InvalidPackaging e )
        {
            // great!
            assertEquals( "Unable to add module to the current project as it is not of packaging type 'pom'",
                    e.getLocalizedMessage() );
        }
    }

    public void testAddModuleToParentPOMJarPackaging()
        throws Exception
    {
        try
        {
            String pom = "<project>\n  <packaging>jar</packaging>\n</project>";
            DefaultOldArchetype.addModuleToParentPom( "myArtifactId1", new StringReader( pom ), new StringWriter() );
            fail( "Should fail to add a module to a JAR packaged project" );
        }
        catch ( InvalidPackaging e )
        {
            // great!
            assertEquals( "Unable to add module to the current project as it is not of packaging type 'pom'",
                    e.getLocalizedMessage() );
        }
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        archetype = (OldArchetype) lookup( OldArchetype.ROLE );
    }
}
