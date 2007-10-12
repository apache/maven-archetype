package org.apache.maven.archetype;

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
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: ArchetypeTest.java 393739 2006-04-13 06:53:02Z brett $
 */
public class ArchetypeTest
    extends PlexusTestCase
{
    private Archetype archetype;

    public void testArchetype()
        throws Exception
    {
        FileUtils.deleteDirectory( getTestFile( "target/quickstart" ) );

        Map parameters = new HashMap();

        parameters.put( "name", "jason" );

        parameters.put( "groupId", "maven" );

        parameters.put( "artifactId", "quickstart" );

        parameters.put( "version", "1.0-alpha-1-SNAPSHOT" );

        parameters.put( "package", "org.apache.maven.quickstart" );

        parameters.put( "basedir", getTestFile( "target" ).getAbsolutePath() );

        // ----------------------------------------------------------------------
        // This needs to be encapsulated in a maven test case.
        // ----------------------------------------------------------------------

        ArtifactRepositoryLayout layout =
            (ArtifactRepositoryLayout) container.lookup( ArtifactRepositoryLayout.ROLE, "legacy" );

        String mavenRepoLocal = getTestFile( "target/local-repository" ).toURL().toString();

        ArtifactRepository localRepository = new DefaultArtifactRepository( "local", mavenRepoLocal, layout );

        List remoteRepositories = new ArrayList();

        String mavenRepoRemote = getTestFile( "src/test/repository" ).toURL().toString();

        ArtifactRepository remoteRepository = new DefaultArtifactRepository( "remote", mavenRepoRemote, layout );

        remoteRepositories.add( remoteRepository );

        String archetypeGroupId = "org.apache.maven.archetypes";
        String archetypeArtifactId = "maven-archetype-quickstart";
        String archetypeVersion = "1.0-alpha-1-SNAPSHOT";
        archetype.createArchetype( archetypeGroupId, archetypeArtifactId, archetypeVersion, localRepository,
                                   remoteRepositories, parameters );

        // ----------------------------------------------------------------------
        // Set up the Velocity context
        // ----------------------------------------------------------------------

        Context context = new VelocityContext();

        for ( Iterator iterator = parameters.keySet().iterator(); iterator.hasNext(); )
        {
            String key = (String) iterator.next();

            Object value = parameters.get( key );

            context.put( key, value );
        }

        // ----------------------------------------------------------------------
        // Validate POM generation
        // ----------------------------------------------------------------------

        ArtifactFactory artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.class.getName() );
        Artifact archetypeArtifact = artifactFactory.createArtifact( archetypeGroupId, archetypeArtifactId,
                                                                     archetypeVersion, Artifact.SCOPE_RUNTIME, "jar" );

        StringWriter writer = new StringWriter();

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(
            getContextClassloader( archetypeArtifact, localRepository, remoteRepositories ) );

        try
        {
            VelocityComponent velocity = (VelocityComponent) lookup( VelocityComponent.class.getName() );

            velocity.getEngine().mergeTemplate( Archetype.ARCHETYPE_RESOURCES + "/" + Archetype.ARCHETYPE_POM, context,
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
        File pomFile = getTestFile( artifactDir.getAbsolutePath(), Archetype.ARCHETYPE_POM );

        try
        {
            FileReader pomReader = new FileReader( pomFile );

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
                                               List remoteRepositories )
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
            throw new ArchetypeNotFoundException( "Archetype does not exist: " + e.getMessage(), e );
        }

        URLClassLoader archetypeJarLoader;
        try
        {
            URL[] urls = new URL[1];

            urls[0] = archetypeArtifact.getFile().toURL();

            archetypeJarLoader = new URLClassLoader( urls );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDescriptorException(
                "Error reading the " + Archetype.ARCHETYPE_DESCRIPTOR + " descriptor.", e );
        }

        return archetypeJarLoader;
    }

    public void testAddModuleToParentPOM()
        throws DocumentException, IOException, ArchetypeTemplateProcessingException
    {
        String pom = "<project>\n  <packaging>pom</packaging>\n</project>";

        StringWriter out = new StringWriter();
        assertTrue( DefaultArchetype.addModuleToParentPom( "myArtifactId1", new StringReader( pom ), out ) );

        assertEquals( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" +
            "  <packaging>pom</packaging>\n" + "  <modules>\n" + "    <module>myArtifactId1</module>\n" +
            "  </modules>\n" + "</project>", out.toString() );

        pom = "<project>\n  <modelVersion>4.0.0</modelVersion>\n" + "  <packaging>pom</packaging>\n" + "</project>";

        out = new StringWriter();
        assertTrue( DefaultArchetype.addModuleToParentPom( "myArtifactId2", new StringReader( pom ), out ) );

        assertEquals( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<project>\n" +
            "  <modelVersion>4.0.0</modelVersion>\n" + "  <packaging>pom</packaging>\n" + "  <modules>\n" +
            "    <module>myArtifactId2</module>\n" + "  </modules>\n" + "</project>", out.toString() );

        pom = "<project><modelVersion>4.0.0</modelVersion>\n" + "  <packaging>pom</packaging>\n" + "  <modules>\n" +
            "  </modules>\n" + "</project>";

        out = new StringWriter();
        assertTrue( DefaultArchetype.addModuleToParentPom( "myArtifactId3", new StringReader( pom ), out ) );

        assertEquals( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project><modelVersion>4.0.0</modelVersion>\n" +
            "  <packaging>pom</packaging>\n" + "  <modules>\n" + "    <module>myArtifactId3</module>\n" +
            "  </modules>\n" + "</project>", out.toString() );

        pom = "<project><modelVersion>4.0.0</modelVersion>\n" + "  <packaging>pom</packaging>\n" + "  <modules>\n" +
            "    <module>myArtifactId3</module>\n" + "  </modules>\n" + "</project>";

        out = new StringWriter();
        assertTrue( DefaultArchetype.addModuleToParentPom( "myArtifactId4", new StringReader( pom ), out ) );

        assertEquals( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project><modelVersion>4.0.0</modelVersion>\n" +
            "  <packaging>pom</packaging>\n" + "  <modules>\n" + "    <module>myArtifactId3</module>\n" +
            "    <module>myArtifactId4</module>\n" + "  </modules>\n" + "</project>", out.toString() );

        pom = "<project><modelVersion>4.0.0</modelVersion>\n" + "  <packaging>pom</packaging>\n" + "  <modules>\n" +
            "    <module>myArtifactId3</module>\n" + "  </modules>\n" + "</project>";

        out = new StringWriter();
        assertFalse( DefaultArchetype.addModuleToParentPom( "myArtifactId3", new StringReader( pom ), out ) );

        // empty means unchanged
        assertEquals( "", out.toString() );
    }

    public void testAddModuleToParentPOMNoPackaging()
        throws DocumentException, IOException
    {
        try
        {
            String pom = "<project>\n</project>";
            DefaultArchetype.addModuleToParentPom( "myArtifactId1", new StringReader( pom ), new StringWriter() );
            fail( "Should fail to add a module to a JAR packaged project" );
        }
        catch ( ArchetypeTemplateProcessingException e )
        {
            // great!
            assertTrue( true );
        }
    }

    public void testAddModuleToParentPOMJarPackaging()
        throws DocumentException, IOException
    {
        try
        {
            String pom = "<project>\n  <packaging>jar</packaging>\n</project>";
            DefaultArchetype.addModuleToParentPom( "myArtifactId1", new StringReader( pom ), new StringWriter() );
            fail( "Should fail to add a module to a JAR packaged project" );
        }
        catch ( ArchetypeTemplateProcessingException e )
        {
            // great!
            assertTrue( true );
        }
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();
        archetype = (Archetype) lookup( Archetype.ROLE );
    }
}
