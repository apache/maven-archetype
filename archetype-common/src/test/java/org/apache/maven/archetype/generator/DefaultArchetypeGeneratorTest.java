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

package org.apache.maven.archetype.generator;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class DefaultArchetypeGeneratorTest
    extends AbstractMojoTestCase
{
    ArtifactRepository localRepository;

    String remoteRepository;

    ArchetypeGenerator instance;

    String outputDirectory;

    public void testArchetypeNotDefined()
        throws Exception
    {
        System.out.println( "testArchetypeNotDefined" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-2", "archetypes", null, "1.0" );
        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );

        if ( result.getCause() == null )
        {
            fail( "Exception must be thrown" );
        }
        assertEquals( "Exception not correct", "The archetype is not defined", result.getCause().getMessage() );
    }

    public void testGenerateArchetypeCompleteWithoutParent()
        throws Exception
    {
        System.out.println( "testGenerateArchetypeCompleteWithoutParent" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-4", "archetypes", "basic", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty( "property-without-default-1", "file-value" );
        additionalProperties.setProperty( "property-without-default-2", "file-value" );
        additionalProperties.setProperty( "property-without-default-3", "file-value" );
        additionalProperties.setProperty( "property-without-default-4", "file-value" );
        additionalProperties.setProperty( "property-with-default-1", "file-value" );
        additionalProperties.setProperty( "property-with-default-2", "file-value" );
        additionalProperties.setProperty( "property-with-default-3", "file-value" );
        additionalProperties.setProperty( "property-with-default-4", "file-value" );
        request.setProperties( additionalProperties );

        File projectDirectory = new File( outputDirectory, "file-value" );
        assertDeleted( projectDirectory );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );

        if ( result.getCause() != null )
        {
            result.getCause().printStackTrace();
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        assertTemplateContent( projectDirectory, "src/main/java/file/value/package/App.java" );
        assertTemplateContent( projectDirectory, "src/main/java/file/value/package/inner/package/App2.java" );
        assertTemplateContent( projectDirectory, "src/main/c/file/value/package/App.c" );
        assertTemplateContent( projectDirectory, "src/test/java/file/value/package/AppTest.java" );
        assertTemplateContent( projectDirectory, "src/test/c/file/value/package/AppTest.c" );
        assertTemplateContent( projectDirectory, "src/main/resources/App.properties" );
        assertTemplateContent( projectDirectory, "src/main/resources/inner/dir/App2.properties" );
        assertTemplateContent( projectDirectory, "src/main/mdo/App.mdo" );
        assertTemplateContent( projectDirectory, "src/test/resources/AppTest.properties" );
        assertTemplateContent( projectDirectory, "src/test/mdo/AppTest.mdo" );

        Model model = readPom( new File( projectDirectory, "pom.xml" ) );
        assertNull( model.getParent() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );
    }

    public void testGenerateArchetypeCompleteWithParent()
        throws Exception
    {
        System.out.println( "testGenerateArchetypeCompleteWithParent" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-5", "archetypes", "basic", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty( "property-without-default-1", "file-value" );
        additionalProperties.setProperty( "property-without-default-2", "file-value" );
        additionalProperties.setProperty( "property-without-default-3", "file-value" );
        additionalProperties.setProperty( "property-without-default-4", "file-value" );
        additionalProperties.setProperty( "property-with-default-1", "file-value" );
        additionalProperties.setProperty( "property-with-default-2", "file-value" );
        additionalProperties.setProperty( "property-with-default-3", "file-value" );
        additionalProperties.setProperty( "property-with-default-4", "file-value" );
        request.setProperties( additionalProperties );

        File projectFile = getProjectFile();
        File projectFileSample = getProjectSampleFile();
        copy( projectFileSample, projectFile );

        File projectDirectory = new File( outputDirectory, "file-value" );
        assertDeleted( projectDirectory );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );
        if ( result.getCause() != null )
        {
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        Model model = readPom( new File( projectDirectory, "pom.xml" ) );
        assertEquals( "org.apache.maven.archetype", model.getParent().getGroupId() );
        assertEquals( "test-generate-5-parent", model.getParent().getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", model.getParent().getVersion() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );

        Model parentModel = readPom( projectFile );
        assertTrue( parentModel.getModules().contains( "file-value" ) );
    }

    public void testGenerateArchetypePartialOnChild()
        throws Exception
    {
        System.out.println( "testGenerateArchetypePartialOnChild" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-8", "archetypes", "partial", "1.0" );

        File parentProjectFile = getProjectFile();
        File parentProjectFileSample = getProjectSampleFile();
        copy( parentProjectFileSample, parentProjectFile );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty( "property-without-default-1", "file-value" );
        additionalProperties.setProperty( "property-without-default-2", "file-value" );
        additionalProperties.setProperty( "property-without-default-3", "file-value" );
        additionalProperties.setProperty( "property-without-default-4", "file-value" );
        additionalProperties.setProperty( "property-with-default-1", "file-value" );
        additionalProperties.setProperty( "property-with-default-2", "file-value" );
        additionalProperties.setProperty( "property-with-default-3", "file-value" );
        additionalProperties.setProperty( "property-with-default-4", "file-value" );
        request.setProperties( additionalProperties );

        File projectDirectory = new File( outputDirectory, "file-value" );
        File projectFile = new File( projectDirectory, "pom.xml" );
        File projectFileSample = new File( projectDirectory, "pom.xml.sample" );
        copy( projectFileSample, projectFile );

        assertDeleted( new File( projectDirectory, "src" ) );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );

        if ( result.getCause() != null )
        {
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        Model model = readPom( projectFile );
        assertNotNull( model.getParent() );
        assertEquals( "org.apache.maven.archetype", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", model.getVersion() );
        assertTrue( model.getModules().isEmpty() );
        assertFalse( model.getDependencies().isEmpty() );
        assertFalse( model.getBuild().getPlugins().isEmpty() );
        assertFalse( model.getReporting().getPlugins().isEmpty() );
    }

    public void testGenerateArchetypePartialOnChildDontOverride()
        throws Exception
    {
        System.out.println( "testGenerateArchetypePartialOnChildDontOverride" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-9", "archetypes", "partial", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty( "property-without-default-1", "file-value" );
        additionalProperties.setProperty( "property-without-default-2", "file-value" );
        additionalProperties.setProperty( "property-without-default-3", "file-value" );
        additionalProperties.setProperty( "property-without-default-4", "file-value" );
        additionalProperties.setProperty( "property-with-default-1", "file-value" );
        additionalProperties.setProperty( "property-with-default-2", "file-value" );
        additionalProperties.setProperty( "property-with-default-3", "file-value" );
        additionalProperties.setProperty( "property-with-default-4", "file-value" );
        request.setProperties( additionalProperties );

        File projectDirectory = new File( outputDirectory, "file-value" );
        File projectFile = new File( projectDirectory, "pom.xml" );
        File projectFileSample = new File( projectDirectory, "pom.xml.sample" );
        copy( projectFileSample, projectFile );

        assertDeleted( new File( projectDirectory, "src" ) );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();
        instance.generateArchetype( request, result );
        if ( result.getCause() != null )
        {
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        Model model = readPom( projectFile );
        assertNotNull( model.getParent() );
        assertEquals( "org.apache.maven.archetype", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", model.getVersion() );
        assertTrue( model.getModules().isEmpty() );
        assertFalse( model.getDependencies().isEmpty() );
        assertEquals( "1.0", ( (Dependency) model.getDependencies().get( 0 ) ).getVersion() );
        assertFalse( model.getBuild().getPlugins().isEmpty() );
        assertEquals( "1.0", ( (Plugin) model.getBuild().getPlugins().get( 0 ) ).getVersion() );
        assertFalse( model.getReporting().getPlugins().isEmpty() );
        assertEquals( "1.0", ( (ReportPlugin) model.getReporting().getPlugins().get( 0 ) ).getVersion() );
    }

    public void testGenerateArchetypePartialOnParent()
        throws Exception
    {
        System.out.println( "testGenerateArchetypePartialOnParent" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-7", "archetypes", "partial", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty( "property-without-default-1", "file-value" );
        additionalProperties.setProperty( "property-without-default-2", "file-value" );
        additionalProperties.setProperty( "property-without-default-3", "file-value" );
        additionalProperties.setProperty( "property-without-default-4", "file-value" );
        additionalProperties.setProperty( "property-with-default-1", "file-value" );
        additionalProperties.setProperty( "property-with-default-2", "file-value" );
        additionalProperties.setProperty( "property-with-default-3", "file-value" );
        additionalProperties.setProperty( "property-with-default-4", "file-value" );
        request.setProperties( additionalProperties );

        File projectFile = new File( outputDirectory, "pom.xml" );
        File projectFileSample = new File( outputDirectory, "pom.xml.sample" );
        copy( projectFileSample, projectFile );

        assertDeleted( new File( outputDirectory, "src" ) );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );
        if ( result.getCause() != null )
        {
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        Model model = readPom( getProjectFile() );
        assertNull( model.getParent() );
        assertEquals( "org.apache.maven.archetype", model.getGroupId() );
        assertEquals( "test-generate-7", model.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", model.getVersion() );
        assertTrue( model.getModules().isEmpty() );
        assertFalse( model.getBuild().getPlugins().isEmpty() );
    }

    public void testGenerateArchetypePartialWithoutPoms()
        throws Exception
    {
        System.out.println( "testGenerateArchetypePartialWithoutPoms" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-6", "archetypes", "partial", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        File projectDirectory = new File( outputDirectory, "file-value" );
        File projectFile = new File( projectDirectory, "pom.xml" );

        assertDeleted( projectDirectory );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );
        if ( result.getCause() != null )
        {
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        Model model = readPom( projectFile );
        assertNull( model.getParent() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );
    }

    public void testGenerateArchetypeSite()
        throws Exception
    {
        System.out.println( "testGenerateArchetypeSite" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-10", "archetypes", "site", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty( "property-without-default-1", "file-value" );
        additionalProperties.setProperty( "property-without-default-2", "file-value" );
        additionalProperties.setProperty( "property-without-default-3", "file-value" );
        additionalProperties.setProperty( "property-without-default-4", "file-value" );
        additionalProperties.setProperty( "property-with-default-1", "file-value" );
        additionalProperties.setProperty( "property-with-default-2", "file-value" );
        additionalProperties.setProperty( "property-with-default-3", "file-value" );
        additionalProperties.setProperty( "property-with-default-4", "file-value" );
        request.setProperties( additionalProperties );

        File projectDirectory = new File( outputDirectory, "file-value" );
        File projectFile = new File( projectDirectory, "pom.xml" );

        assertDeleted( projectDirectory );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );
        if ( result.getCause() != null )
        {
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        assertTemplateContent( projectDirectory, "src/site/site.xml" );
        assertTemplateContent( projectDirectory, "src/site/apt/test.apt" );

        Model model = readPom( projectFile );
        assertNull( model.getParent() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );
    }

    public void testGenerateFileSetArchetype()
        throws Exception
    {
        System.out.println( "testGenerateFileSetArchetype" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-12", "archetypes", "fileset", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        Properties additionalProperties = new Properties();
        additionalProperties.setProperty( "property-without-default-1", "file-value" );
        additionalProperties.setProperty( "property-without-default-2", "file-value" );
        additionalProperties.setProperty( "property-without-default-3", "file-value" );
        additionalProperties.setProperty( "property-without-default-4", "file-value" );
        additionalProperties.setProperty( "property-with-default-1", "file-value" );
        additionalProperties.setProperty( "property-with-default-2", "file-value" );
        additionalProperties.setProperty( "property-with-default-3", "file-value" );
        additionalProperties.setProperty( "property-with-default-4", "file-value" );
        request.setProperties( additionalProperties );

        File projectDirectory = new File( outputDirectory, "file-value" );
        File projectFile = new File( projectDirectory, "pom.xml" );

        assertDeleted( projectDirectory );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );
        if ( result.getCause() != null )
        {
            result.getCause().printStackTrace( System.err );
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "src/main/java/file/value/package/App.java", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "src/main/java/file/value/package/inner/package/App2.java",
                                                            "file-value" );

        assertTemplateCopiedWithFileSetArchetype( projectDirectory, "src/main/java/file/value/package/App.ogg" );

        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory, "src/main/resources/App.properties",
                                                            "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "src/main/resources/file-value/touch.txt", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "src/main/resources/file-value/touch_root.txt",
                                                            "file-value" );

        assertTemplateCopiedWithFileSetArchetype( projectDirectory, "src/main/resources/some-dir/App.png" );

        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory, "src/site/site.xml", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory, "src/site/apt/usage.apt", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory, ".classpath", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory, "profiles.xml", "file-value" );

        Model model = readPom( projectFile );
        assertNull( model.getParent() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );

        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "subproject/src/main/java/file/value/package/App.java",
                                                            "subproject" );

        model = readPom( new File( projectDirectory, "subproject/pom.xml" ) );
        assertNotNull( model.getParent() );
        assertEquals( "file-value", model.getParent().getGroupId() );
        assertEquals( "file-value", model.getParent().getArtifactId() );
        assertEquals( "file-value", model.getParent().getVersion() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "subproject", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );

        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "subproject/subsubproject/src/main/java/file/value/package/App.java",
                                                            "subsubproject" );

        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "subproject/subsubproject/src/main/java/file/value/package/"
                                                                + "file-value/inner/subsubproject/innest/Somefile-valueClasssubsubproject.java",
                                                            "subsubproject" );

        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "subproject/subsubproject/src/main/java/file/value/package/"
                                                            /* + "file-value/inner/subsubproject/innest/" + */
                                                            + "ArbitraryProperty-file-value.java", "subsubproject" );

        // Test that undefined properties are safely ignored (and skipped)
        assertTemplateContentGeneratedWithFileSetArchetype( projectDirectory,
                                                            "subproject/subsubproject/src/main/java/file/value/package/"
                                                            /* + "file-value/inner/subsubproject/innest/" + */
                                                            + "SkipsUndefinedProperty-__undefined-property__-file-value.java",
                                                            "subsubproject" );

        model = readPom( new File( projectDirectory, "subproject/subsubproject/pom.xml" ) );
        assertNotNull( model.getParent() );
        assertEquals( "file-value", model.getParent().getGroupId() );
        assertEquals( "subproject", model.getParent().getArtifactId() );
        assertEquals( "file-value", model.getParent().getVersion() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "subsubproject", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );
    }

    public void testGenerateOldArchetype()
        throws Exception
    {
        System.out.println( "testGenerateOldArchetype" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-11", "archetypes", "old", "1.0" );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        File projectDirectory = new File( outputDirectory, "file-value" );
        File projectFile = new File( projectDirectory, "pom.xml" );

        assertDeleted( projectDirectory );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );
        if ( result.getCause() != null )
        {
            result.getCause().printStackTrace( System.err );
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        assertTemplateContentGeneratedWithOldArchetype( projectDirectory, "src/main/java/file/value/package/App.java" );
        assertTemplateContentGeneratedWithOldArchetype( projectDirectory, "src/main/resources/App.properties" );
        assertTemplateContentGeneratedWithOldArchetype( projectDirectory, "src/site/site.xml" );

        Model model = readPom( projectFile );
        assertNull( model.getParent() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );
    }

    public void testPropertiesNotDefined()
        throws Exception
    {
        System.out.println( "testPropertiesNotDefined" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-3", "archetypes", "basic", "1.0" );

        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        instance.generateArchetype( request, result );
        if ( result.getCause() == null )
        {
            fail( "No exception may be thrown: " + result.getCause().getMessage() );
        }

        assertTrue( "Exception not correct",
                    result.getCause().getMessage().startsWith( "Archetype archetypes:basic:1.0 is not configured" )
                        && result.getCause().getMessage().endsWith( "Property property-without-default-4 is missing." ) );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        outputDirectory = null;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        String repositories = new File( getBasedir(), "target/test-classes/repositories" ).toURI().toString();

        localRepository =
            new DefaultArtifactRepository( "local", repositories + "/local", new DefaultRepositoryLayout() );

        remoteRepository = repositories + "/central";

        instance = (ArchetypeGenerator) lookup( ArchetypeGenerator.ROLE );
        assertNotNull( instance );
        assertNotNull( getVariableValueFromObject( instance, "archetypeArtifactManager" ) );
        assertNotNull( getVariableValueFromObject( instance, "oldArchetype" ) );
        assertNotNull( getVariableValueFromObject( instance, "filesetGenerator" ) );
    }

    private ArchetypeGenerationRequest createArchetypeGenerationRequest( String project, String groupId,
                                                                         String artifactId, String version )
    {
        outputDirectory = getBasedir() + "/target/test-classes/projects/" + project;

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setLocalRepository( localRepository );
        request.setArchetypeRepository( remoteRepository );
        request.setOutputDirectory( outputDirectory );

        request.setArchetypeGroupId( groupId );
        request.setArchetypeArtifactId( artifactId );
        request.setArchetypeVersion( version );

        return request;
    }

    /**
     * This method attempts to delete a directory or file if it exists. If the file exists after
     * deletion, it throws a failure.
     *
     * @param file to delete.
     */
    private void assertDeleted( File file )
    {
        if ( file.exists() )
        {
            if ( file.isDirectory() )
            {
                try
                {
                    FileUtils.deleteDirectory( file );
                }
                catch ( IOException e )
                {
                    fail( "Unable to delete directory:" + file + ":" + e.getLocalizedMessage() );
                }
            }
        }
        else
        {
            try
            {
                FileUtils.forceDelete( file );
            }
            catch ( IOException e )
            {
                fail( "Unable to delete file:" + file + ":" + e.getLocalizedMessage() );
                e.printStackTrace();
            }
        }

        if ( file.exists() )
        {
            fail( "File not deleted:" + file );
        }
    }

    private void assertTemplateContent( final File projectDirectory, final String template )
        throws IOException
    {
        Properties properties = loadProperties( projectDirectory, template );
        assertEquals( "file-value", properties.getProperty( "groupId" ) );
        assertEquals( "file-value", properties.getProperty( "artifactId" ) );
        assertEquals( "file-value", properties.getProperty( "version" ) );
        assertEquals( "file.value.package", properties.getProperty( "package" ) );
        assertEquals( "file-value", properties.getProperty( "property-with-default-1" ) );
        assertEquals( "file-value", properties.getProperty( "property-with-default-2" ) );
        assertEquals( "file-value", properties.getProperty( "property-with-default-3" ) );
        assertEquals( "file-value", properties.getProperty( "property-with-default-4" ) );
        assertEquals( "file-value", properties.getProperty( "property-without-default-1" ) );
        assertEquals( "file-value", properties.getProperty( "property-without-default-2" ) );
        assertEquals( "file-value", properties.getProperty( "property-without-default-3" ) );
        assertEquals( "file-value", properties.getProperty( "property-without-default-4" ) );
    }

    private void assertTemplateContentGeneratedWithFileSetArchetype( File projectDirectory, String template,
                                                                     String artifactId )
        throws IOException
    {
        Properties properties = loadProperties( projectDirectory, template );
        assertEquals( "file-value", properties.getProperty( "groupId" ) );
        assertEquals( artifactId, properties.getProperty( "artifactId" ) );
        assertEquals( "file-value", properties.getProperty( "version" ) );
        assertEquals( "file.value.package", properties.getProperty( "package" ) );
        assertEquals( "file/value/package", properties.getProperty( "packageInPathFormat" ) );
    }

    private void assertTemplateContentGeneratedWithOldArchetype( final File projectDirectory, final String template )
        throws IOException
    {
        Properties properties = loadProperties( projectDirectory, template );
        assertEquals( "file-value", properties.getProperty( "groupId" ) );
        assertEquals( "file-value", properties.getProperty( "artifactId" ) );
        assertEquals( "file-value", properties.getProperty( "version" ) );
        assertEquals( "file.value.package", properties.getProperty( "package" ) );
    }

    private void assertTemplateCopiedWithFileSetArchetype( File projectDirectory, String template )
        throws IOException
    {
        Properties properties = loadProperties( projectDirectory, template );
        assertEquals( "${groupId}", properties.getProperty( "groupId" ) );
        assertEquals( "${artifactId}", properties.getProperty( "artifactId" ) );
        assertEquals( "${version}", properties.getProperty( "version" ) );
        assertEquals( "${package}", properties.getProperty( "package" ) );
        assertEquals( "${packageInPathFormat}", properties.getProperty( "packageInPathFormat" ) );
    }

    private void copy( final File in, final File out )
        throws IOException, FileNotFoundException
    {
        assertTrue( !out.exists() || out.delete() );
        assertFalse( out.exists() );

        FileUtils.copyFile( in, out );

        assertTrue( out.exists() );
        assertTrue( in.exists() );
    }

    private Properties loadProperties( File propertyFile )
        throws IOException, FileNotFoundException
    {
        Properties properties = new Properties();
        properties.load( new FileInputStream( propertyFile ) );
        return properties;
    }

    private Properties loadProperties( final File projectDirectory, final String template )
        throws IOException
    {
        File templateFile = new File( projectDirectory, template );
        if ( !templateFile.exists() )
        {
            fail( "Missing File: " + templateFile );
        }

        Properties properties = loadProperties( templateFile );
        return properties;
    }

    private File getProjectFile()
    {
        return new File( outputDirectory, "/pom.xml" );
    }

    private File getProjectSampleFile()
    {
        return new File( outputDirectory, "/pom.xml.sample" );
    }

    private File getPropertiesFile()
    {
        return new File( outputDirectory, "/archetype.properties" );
    }

    private File getPropertiesSampleFile()
    {
        return new File( outputDirectory, "/archetype.properties.sample" );
    }

    private Model readPom( final File pomFile )
        throws IOException, XmlPullParserException
    {
        Reader pomReader = null;
        try
        {
            pomReader = ReaderFactory.newXmlReader( pomFile );

            MavenXpp3Reader reader = new MavenXpp3Reader();

            return reader.read( pomReader );
        }
        finally
        {
            IOUtil.close( pomReader );
        }
    }
}
