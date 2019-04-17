package org.apache.maven.archetype.generator;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;

public class DefaultArchetypeGeneratorTest
    extends AbstractMojoTestCase
{
    // archetypes prepared by antrun execution (see pom.xml) from src/test/archetypes 
    private final static Archetype ARCHETYPE_BASIC = new Archetype( "archetypes", "basic", "1.0" );

    private final static Archetype ARCHETYPE_PARTIAL = new Archetype( "archetypes", "partial", "1.0" );

    private final static Archetype ARCHETYPE_SITE = new Archetype( "archetypes", "site", "1.0" );

    private final static Archetype ARCHETYPE_FILESET = new Archetype( "archetypes", "fileset", "1.0" );

    private final static Archetype ARCHETYPE_OLD = new Archetype( "archetypes", "old", "1.0" );

    private final static Archetype ARCHETYPE_FILESET_WITH_POSTCREATE_SCRIPT =
        new Archetype( "archetypes", "fileset_with_postscript", "1.0" );

    private final static Properties ADDITIONAL_PROPERTIES = new Properties();
    static
    {
        ADDITIONAL_PROPERTIES.setProperty( "property-without-default-1", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property-without-default-2", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property-without-default-3", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property-without-default-4", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property-with-default-1", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property-with-default-2", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property-with-default-3", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property-with-default-4", "file-value" );
        ADDITIONAL_PROPERTIES.setProperty( "property_underscored_1", "prop1" );
        ADDITIONAL_PROPERTIES.setProperty( "property_underscored-2", "prop2" );
    }

    ArtifactRepository localRepository;

    String remoteRepository;

    ArchetypeGenerator generator;

    String outputDirectory;

    File projectDirectory;

    private void generateProjectFromArchetype( ArchetypeGenerationRequest request )
        throws Exception
    {
        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        generator.generateArchetype( request, result );

        if ( result.getCause() != null )
        {
            throw result.getCause();
        }
    }

    private ArchetypeGenerationResult generateProjectFromArchetypeWithFailure( ArchetypeGenerationRequest request )
        throws Exception
    {
        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        generator.generateArchetype( request, result );

        if ( result.getCause() == null )
        {
            fail( "Exception must be thrown." );
        }

        return result;
    }

    public void testArchetypeNotDefined()
        throws Exception
    {
        System.out.println( "testArchetypeNotDefined" );

        Archetype archetype = new Archetype( "archetypes", null, "1.0" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-2", archetype );

        ArchetypeGenerationResult result = generateProjectFromArchetypeWithFailure( request );

        assertEquals( "Exception not correct", "The archetype is not defined", result.getCause().getMessage() );
    }

    public void testGenerateArchetypeCompleteWithoutParent()
        throws Exception
    {
        System.out.println( "testGenerateArchetypeCompleteWithoutParent" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-4", ARCHETYPE_BASIC );

        FileUtils.forceDelete( projectDirectory );

        generateProjectFromArchetype( request );

        assertTemplateContent( "src/main/java/file/value/package/App.java" );
        assertTemplateContent( "src/main/java/file/value/package/inner/package/App2.java" );
        assertTemplateContent( "src/main/c/file/value/package/App.c" );
        assertTemplateContent( "src/test/java/file/value/package/AppTest.java" );
        assertTemplateContent( "src/test/c/file/value/package/AppTest.c" );
        assertTemplateContent( "src/main/resources/App.properties" );
        assertTemplateContent( "src/main/resources/inner/dir/App2.properties" );
        assertTemplateContent( "src/main/mdo/App.mdo" );
        assertTemplateContent( "src/test/resources/AppTest.properties" );
        assertTemplateContent( "src/test/mdo/AppTest.mdo" );

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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-5", ARCHETYPE_BASIC );

        File projectFile = getProjectFile();
        File projectFileSample = getProjectSampleFile();
        copy( projectFileSample, projectFile );

        FileUtils.forceDelete( projectDirectory );

        generateProjectFromArchetype( request );

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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-8", ARCHETYPE_PARTIAL );

        File parentProjectFile = getProjectFile();
        File parentProjectFileSample = getProjectSampleFile();
        copy( parentProjectFileSample, parentProjectFile );

        File projectFile = new File( projectDirectory, "pom.xml" );
        File projectFileSample = new File( projectDirectory, "pom.xml.sample" );
        copy( projectFileSample, projectFile );

        FileUtils.forceDelete( new File( projectDirectory, "src" ) );

        generateProjectFromArchetype( request );

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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-9", ARCHETYPE_PARTIAL );

        File projectFile = new File( projectDirectory, "pom.xml" );
        File projectFileSample = new File( projectDirectory, "pom.xml.sample" );
        copy( projectFileSample, projectFile );

        FileUtils.forceDelete( new File( projectDirectory, "src" ) );

        generateProjectFromArchetype( request );

        Model model = readPom( projectFile );
        assertNotNull( model.getParent() );
        assertEquals( "org.apache.maven.archetype", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", model.getVersion() );
        assertTrue( model.getModules().isEmpty() );
        assertFalse( model.getDependencies().isEmpty() );
        assertEquals( "1.0", model.getDependencies().get( 0 ).getVersion() );
        assertFalse( model.getBuild().getPlugins().isEmpty() );
        assertEquals( "1.0", model.getBuild().getPlugins().get( 0 ).getVersion() );
        assertFalse( model.getReporting().getPlugins().isEmpty() );
        assertEquals( "1.0", model.getReporting().getPlugins().get( 0 ).getVersion() );
    }

    public void testGenerateArchetypePartialOnParent()
        throws Exception
    {
        System.out.println( "testGenerateArchetypePartialOnParent" );

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-7", ARCHETYPE_PARTIAL );

        File projectFile = new File( outputDirectory, "pom.xml" );
        File projectFileSample = new File( outputDirectory, "pom.xml.sample" );
        copy( projectFileSample, projectFile );

        FileUtils.forceDelete( new File( outputDirectory, "src" ) );

        generateProjectFromArchetype( request );

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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-6", ARCHETYPE_PARTIAL );

        File projectFile = new File( projectDirectory, "pom.xml" );

        FileUtils.forceDelete( projectDirectory );

        generateProjectFromArchetype( request );

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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-10", ARCHETYPE_SITE );

        File projectFile = new File( projectDirectory, "pom.xml" );

        FileUtils.forceDelete( projectDirectory );

        generateProjectFromArchetype( request );

        assertTemplateContent( "src/site/site.xml" );
        assertTemplateContent( "src/site/apt/test.apt" );

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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-12", ARCHETYPE_FILESET );

        File projectFile = new File( projectDirectory, "pom.xml" );

        FileUtils.forceDelete( projectDirectory );

        generateProjectFromArchetype( request );

        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/java/file/value/package/App.java", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/java/file/value/package/inner/package/App2.java",
                                                            "file-value" );

        assertTemplateCopiedWithFileSetArchetype( "src/main/java/file/value/package/App.ogg" );

        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/resources/App.properties",
                                                            "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/resources/file-value/touch.txt", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/resources/file-value/touch_root.txt",
                                                            "file-value" );

        assertTemplateCopiedWithFileSetArchetype( "src/main/resources/some-dir/App.png" );

        assertTemplateContentGeneratedWithFileSetArchetype( "src/site/site.xml", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/site/apt/usage.apt", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( ".classpath", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "profiles.xml", "file-value" );

        Model model = readPom( projectFile );
        assertNull( model.getParent() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/src/main/java/file/value/package/App.java",
                                                            "subproject" );

        model = readPom( new File( projectDirectory, "subproject/pom.xml" ) );
        assertNotNull( model.getParent() );
        assertEquals( "file-value", model.getParent().getGroupId() );
        assertEquals( "file-value", model.getParent().getArtifactId() );
        assertEquals( "file-value", model.getParent().getVersion() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "subproject", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/App.java",
                                                            "subsubproject" );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/"
                                                                + "file-value/inner/subsubproject/innest/Somefile-valueClasssubsubproject.java",
                                                            "subsubproject" );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/"
                                                            /* + "file-value/inner/subsubproject/innest/" + */
                                                            + "ArbitraryProperty-file-value.java", "subsubproject" );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/"
                                                            /* + "file-value/inner/subsubproject/innest/" + */
                                                            + "M_subsubproject_prop1Testprop2file-value.java", "subsubproject" );

        // Test that undefined properties are safely ignored (and skipped)
        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/"
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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-11", ARCHETYPE_OLD );

        File projectFile = new File( projectDirectory, "pom.xml" );

        FileUtils.forceDelete( projectDirectory );

        generateProjectFromArchetype( request );

        assertTemplateContentGeneratedWithOldArchetype( "src/main/java/file/value/package/App.java" );
        assertTemplateContentGeneratedWithOldArchetype( "src/main/resources/App.properties" );
        assertTemplateContentGeneratedWithOldArchetype( "src/site/site.xml" );

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

        ArchetypeGenerationRequest request = createArchetypeGenerationRequest( "generate-3", ARCHETYPE_BASIC );
        
        request.setProperties( new Properties() );

        ArchetypeGenerationResult result = generateProjectFromArchetypeWithFailure( request );

        assertTrue( "Exception not correct",
                    result.getCause().getMessage().startsWith( "Archetype archetypes:basic:1.0 is not configured" )
                        && result.getCause().getMessage().endsWith( "Property property-without-default-4 is missing." ) );
    }

    public void testGenerateArchetypeWithPostScriptIncluded()
        throws Exception
    {
        System.out.println( "testGenerateArchetypeWithPostScriptIncluded" );

        ArchetypeGenerationRequest request =
            createArchetypeGenerationRequest( "generate-13", ARCHETYPE_FILESET_WITH_POSTCREATE_SCRIPT );

        File projectFile = new File( projectDirectory, "pom.xml" );

        FileUtils.forceDelete( projectDirectory );

        generateProjectFromArchetype( request );

        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/java/file/value/package/App.java", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/java/file/value/package/inner/package/App2.java",
                                                            "file-value" );

        assertTemplateCopiedWithFileSetArchetype( "src/main/java/file/value/package/App.ogg" );

        File templateFile = new File( projectDirectory, "src/main/java/file/value/package/ToDelete.java" );
        assertFalse( templateFile + " should have been removed (by post-generate.groovy script", templateFile.exists() );

        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/resources/App.properties", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/resources/file-value/touch.txt", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/main/resources/file-value/touch_root.txt",
                                                            "file-value" );

        assertTemplateCopiedWithFileSetArchetype( "src/main/resources/some-dir/App.png" );

        assertTemplateContentGeneratedWithFileSetArchetype( "src/site/site.xml", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "src/site/apt/usage.apt", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( ".classpath", "file-value" );
        assertTemplateContentGeneratedWithFileSetArchetype( "profiles.xml", "file-value" );

        Model model = readPom( projectFile );
        assertNull( model.getParent() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "file-value", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/src/main/java/file/value/package/App.java",
                                                            "subproject" );

        model = readPom( new File( projectDirectory, "subproject/pom.xml" ) );
        assertNotNull( model.getParent() );
        assertEquals( "file-value", model.getParent().getGroupId() );
        assertEquals( "file-value", model.getParent().getArtifactId() );
        assertEquals( "file-value", model.getParent().getVersion() );
        assertEquals( "file-value", model.getGroupId() );
        assertEquals( "subproject", model.getArtifactId() );
        assertEquals( "file-value", model.getVersion() );

        assertTemplateContentGeneratedWithFileSetArchetype(
            "subproject/subsubproject/src/main/java/file/value/package/App.java", "subsubproject" );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/"
                                                                + "file-value/inner/subsubproject/innest/Somefile-valueClasssubsubproject.java",
                                                            "subsubproject" );

        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/"
                                                            /* + "file-value/inner/subsubproject/innest/" + */
                                                                + "ArbitraryProperty-file-value.java",
                                                            "subsubproject" );

        // Test that undefined properties are safely ignored (and skipped)
        assertTemplateContentGeneratedWithFileSetArchetype( "subproject/subsubproject/src/main/java/file/value/package/"
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

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        outputDirectory = null;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        String repositories = new File( getBasedir(), "target/test-classes/repositories" ).toURI().toString();

        localRepository =
            new DefaultArtifactRepository( "local", repositories + "/local", new DefaultRepositoryLayout() );

        remoteRepository = repositories + "/central";

        generator = (ArchetypeGenerator) lookup( ArchetypeGenerator.ROLE );
        assertNotNull( generator );
        assertNotNull( getVariableValueFromObject( generator, "archetypeArtifactManager" ) );
        assertNotNull( getVariableValueFromObject( generator, "oldArchetype" ) );
        assertNotNull( getVariableValueFromObject( generator, "filesetGenerator" ) );
    }

    private ArchetypeGenerationRequest createArchetypeGenerationRequest( String project, Archetype archetype )
    {
        outputDirectory = getBasedir() + "/target/test-classes/projects/" + project;

        projectDirectory = new File( outputDirectory, "file-value" );

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setLocalRepository( localRepository );
        request.setArchetypeRepository( remoteRepository );
        request.setOutputDirectory( outputDirectory );

        request.setArchetypeGroupId( archetype.groupId );
        request.setArchetypeArtifactId( archetype.artifactId );
        request.setArchetypeVersion( archetype.version );

        request.setGroupId( "file-value" );
        request.setArtifactId( "file-value" );
        request.setVersion( "file-value" );
        request.setPackage( "file.value.package" );

        request.setProperties( ADDITIONAL_PROPERTIES );
        
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        MavenRepositorySystemSession repositorySession = new MavenRepositorySystemSession();
        repositorySession.setLocalRepositoryManager( new SimpleLocalRepositoryManager( localRepository.getBasedir() ) );
        buildingRequest.setRepositorySession( repositorySession );
        request.setProjectBuildingRequest( buildingRequest );

        return request;
    }

    private void assertTemplateContent( final String template )
        throws IOException
    {
        Properties properties = loadProperties( template );
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

    private void assertTemplateContentGeneratedWithFileSetArchetype( String template, String artifactId )
        throws IOException
    {
        Properties properties = loadProperties( template );
        assertEquals( "file-value", properties.getProperty( "groupId" ) );
        assertEquals( artifactId, properties.getProperty( "artifactId" ) );
        assertEquals( "file-value", properties.getProperty( "version" ) );
        assertEquals( "file.value.package", properties.getProperty( "package" ) );
        assertEquals( "file/value/package", properties.getProperty( "packageInPathFormat" ) );
    }

    private void assertTemplateContentGeneratedWithOldArchetype( final String template )
        throws IOException
    {
        Properties properties = loadProperties( template );
        assertEquals( "file-value", properties.getProperty( "groupId" ) );
        assertEquals( "file-value", properties.getProperty( "artifactId" ) );
        assertEquals( "file-value", properties.getProperty( "version" ) );
        assertEquals( "file.value.package", properties.getProperty( "package" ) );
    }

    private void assertTemplateCopiedWithFileSetArchetype( String template )
        throws IOException
    {
        Properties properties = loadProperties( template );
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
        try ( InputStream in = new FileInputStream( propertyFile ) )
        {
            properties.load( in );
            return properties;
        }
    }

    private Properties loadProperties( final String template )
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

    private Model readPom( final File pomFile )
        throws IOException, XmlPullParserException
    {
        try ( Reader pomReader = ReaderFactory.newXmlReader( pomFile ) )
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();

            return reader.read( pomReader );
        }
    }
    
    private static class Archetype
    {
        public final String groupId;
        public final String artifactId;
        public final String version;
        
        public Archetype( String groupId, String artifactId, String version )
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }
}
