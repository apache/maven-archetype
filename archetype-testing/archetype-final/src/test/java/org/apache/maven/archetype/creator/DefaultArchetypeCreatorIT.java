package org.apache.maven.archetype.creator;

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

import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
//import org.apache.maven.project.MavenProjectBuildingResult;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DefaultArchetypeCreatorIT
    extends AbstractMojoTestCase
{
    private List<String> filtereds;

    private List<String> languages;

    private DefaultArtifactRepository localRepository;

    protected void createFilesetArchetype( String project )
        throws Exception
    {
        System.out.println( ">>>>>> testCreateFilesetArchetype( \"" + project + "\" )" );
        
        ProjectBuilder builder = lookup( ProjectBuilder.class );

        File projectFile = getProjectFile( project );

        File projectFileSample = getProjectSampleFile( project );

        copy( projectFileSample, projectFile );

        FileUtils.deleteDirectory( new File( projectFile.getParentFile(), "target" ) );

        File propertyFile = getPropertiesFile( project );

        File propertyFileSample = getPropertiesSampleFile( project );

        copy( propertyFileSample, propertyFile );

        Properties p = PropertyUtils.loadProperties( propertyFile );

        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        buildingRequest.setLocalRepository( localRepository );
        
        MavenProject mavenProject = builder.build( projectFile, buildingRequest ).getProject();

        FilesetArchetypeCreator instance =
            (FilesetArchetypeCreator) lookup( ArchetypeCreator.class.getName(), "fileset" );

        languages = new ArrayList<String>();
        languages.add( "java" );
        languages.add( "aspectj" );
        languages.add( "csharp" );
        languages.add( "groovy" );
        languages.add( "resources" );

        filtereds = new ArrayList<String>();
        filtereds.add( "java" );
        filtereds.add( "xml" );
        filtereds.add( "txt" );
        filtereds.add( "groovy" );
        filtereds.add( "cs" );
        filtereds.add( "mdo" );
        filtereds.add( "aj" );
        filtereds.add( "jsp" );
        filtereds.add( "js" );
        filtereds.add( "gsp" );
        filtereds.add( "vm" );
        filtereds.add( "html" );
        filtereds.add( "xhtml" );
        filtereds.add( "properties" );
        filtereds.add( ".classpath" );
        filtereds.add( ".project" );
        filtereds.add( "MF" );

        ArchetypeCreationRequest request = new ArchetypeCreationRequest()
            .setProject( mavenProject )
            .setPackageName( p.getProperty( Constants.PACKAGE ) )
            .setProperties( p )
            .setLanguages( languages )
            .setFiltereds( filtereds )
            .setDefaultEncoding( "UTF-8" )
            .setPartialArchetype( false )
            .setPreserveCData( false )
            .setKeepParent( false )
            .setPostPhase( "verify" )
            .setLocalRepository( localRepository );

        ArchetypeCreationResult result = new ArchetypeCreationResult();

        instance.createArchetype( request, result );

        if ( result.getCause() != null )
        {
            throw result.getCause();
        }

        System.out.println( "<<<<<< testCreateFilesetArchetype( \"" + project + "\" )" );
    }

    public void testChangeJavaFilenameWhenCustomPropertyEqualsClassName()
        throws Exception
    {
        String project = "change-file-with-property";

        createFilesetArchetype( project );

        File template = getTemplateFile( project, "src/main/java/__someProperty__.java" );
        assertExists( template );
		assertContent( template, "class ${someProperty}" );
    }

    public void testExcludePatternsMustExcludeDirectory()
        throws Exception
    {
        String project = "exclude-patterns";

        createFilesetArchetype( project );

        File template = getTemplateFile( project, ".toexclude/dummy.file");
        assertNotExists(template);

        File template1 = getTemplateFile( project, "nottoexclude/dummy.file" );
        assertExists(template1);
    }

    public void testExcludePatternsContainingFilesSameExtension()
        throws Exception
    {
        String project = "exclude-patterns-2";

        createFilesetArchetype( project );

        assertNotExists( getTemplateFile( project, ".sonar/file.txt" ) );
        assertNotExists( getTemplateFile( project, "folder/.sonar/file.txt" ) );
        assertExists( getTemplateFile( project, "folder/file.txt" ) );
    }

    public void testIncludeFileWithNoExtension()
                    throws Exception
    {
        String project = "include-file-with-no-extension";

        createFilesetArchetype( project );

        File template1 = getTemplateFile( project, "src/main/csharp/filewithnoextension" );
        assertExists(template1);
    }

    public void testCreateFilesetArchetype1()
        throws Exception
    {
        String project = "create-1";

        createFilesetArchetype( project );

        File template = getTemplateFile( project, "src/main/java/subfolder1/App.java" );
        assertExists( template );
        assertContent( template, "// ${someProperty}" );
        assertContent( template, "package ${package}.subfolder1;" );
        assertNotContent( template, "${packageInPathFormat}" );
    }

    public void testCreateFilesetArchetype2()
        throws Exception
    {
        String project = "create-2";

        createFilesetArchetype( project );
    }

    public void testCreateFilesetArchetype3()
        throws Exception
    {
        String project = "create-3";

        createFilesetArchetype( project );

        File template = getTemplateFile( project, "pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven archetype Test create-3" );
        assertContent( template, "<packaging>pom</packaging>" );
        assertNotContent( template, "<parent>" );

        template = getTemplateFile( project, "src/site/site.xml" );
        assertExists( template );
        assertContent( template, "<!-- ${packageInPathFormat}/test" );
        assertContent( template, "${someProperty} -->" );

        template = getTemplateFile( project, "src/site/resources/site.png" );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, ".classpath" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template = getTemplateFile( project, "profiles.xml" );
        assertExists( template );
        assertContent( template, "<!-- ${packageInPathFormat}/test" );
        assertContent( template, "${someProperty} -->" );

        template = getTemplateFile( project, "libs/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven archetype Test create-3-libraries" );
        assertContent( template, "<packaging>pom</packaging>" );
        assertContent( template, "<parent>" );

        template = getTemplateFile( project, "libs/prj-a/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven archetype Test create-3-libraries-project-a" );
        assertNotContent( template, "<packaging>pom</packaging>" );
        assertContent( template, "<parent>" );

        template = getTemplateFile( project, "libs/prj-a/src/main/mdo/descriptor.xml" );
        assertExists( template );
        assertContent( template, "<!-- ${packageInPathFormat}/test" );
        assertContent( template, "${someProperty} -->" );

        template = getTemplateFile( project, "libs/prj-b/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven archetype Test create-3-libraries-project-b" );
        assertNotContent( template, "<packaging>pom</packaging>" );
        assertContent( template, "<parent>" );

        template = getTemplateFile( project, "libs/prj-b/src/main/java/test/com/Component.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}" );

        template = getTemplateFile( project, "libs/prj-b/src/main/java/test/com/package.html" );
        assertExists( template );
        assertContent( template, "<!-- ${packageInPathFormat}/test" );
        assertContent( template, "${someProperty} -->" );

        template = getTemplateFile( project, "libs/prj-b/src/test/java/test/common/ComponentTest.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}" );

        template = getTemplateFile( project, "application/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven archetype Test create-3-application" );
        assertNotContent( template, "<packaging>pom</packaging>" );
        assertContent( template, "<parent>" );

        template = getTemplateFile( project, "application/src/main/java/Main.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertNotContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}/test" );

        template = getTemplateFile( project, "application/src/main/java/test/application/Application.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}" );

        template = getTemplateFile( project, "application/src/main/java/test/application/audios/Application.ogg" );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/main/java/test/application/images/Application.png" );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/main/resources/log4j.properties" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertNotContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}/test" );

        template = getTemplateFile( project, "application/src/main/resources/META-INF/MANIFEST.MF" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertNotContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}/test" );

        template = getTemplateFile( project, "application/src/main/resources/test/application/some/Gro.groovy" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertNotContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}/test" );

        template = getTemplateFile( project, "application/src/main/resources/splash.png" );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/test/java/TestAll.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertNotContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}/test" );

        template = getTemplateFile( project, "application/src/test/java/test/application/ApplicationTest.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertContent( template, "package ${package}.test.application;" );
        assertContent( template, "${packageInPathFormat}/test/application" );

        template = getTemplateFile( project, "application/src/it-test/java/test/ItTest1.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertContent( template, "package ${package}.test;" );
        assertContent( template, "${packageInPathFormat}/test" );

        template = getTemplateFile( project, "application/src/it-test/java/ItTestAll.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertNotContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}/test" );

        template = getTemplateFile( project, "application/src/it-test/resources/ItTest1Result.txt" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
        assertNotContent( template, "${package}" );
        assertContent( template, "${packageInPathFormat}/test" );
    }

    public void testCreateFilesetArchetype4()
            throws Exception
    {
        String project = "create-4";

        createFilesetArchetype( project );

        File template = getTemplateFile( project, "pom.xml" );
        assertExists( template );
        assertContent( template, "Maven archetype Test create-4 ${someProperty}" );
        assertContent( template, "<packaging>pom</packaging>" );

        File earTemplate = getTemplateFile( project, "subModuleEAR/pom.xml" );
        assertExists( earTemplate );
        assertContent( earTemplate, "${groupId}" );
        assertContent( earTemplate, "${artifactId}" );
        assertContent( earTemplate, "${version}" );
        assertContent( earTemplate, "Maven archetype Test create-4-subModuleEAR" );
        assertContent( earTemplate, "<packaging>ear</packaging>" );
        assertContent( earTemplate, "<parent>" );

        File warTemplate = getTemplateFile( project, "subModuleWar/pom.xml" );
        assertExists( warTemplate );
        assertContent( warTemplate, "${groupId}" );
        assertContent( warTemplate, "${artifactId}" );
        assertContent( warTemplate, "${version}" );
        assertContent( warTemplate, "Maven archetype Test create-4-subModuleWar ${someProperty}" );
        assertContent( warTemplate, "<packaging>war</packaging>" );
        assertContent( warTemplate, "<parent>" );
    }

    public void testCreateFilesetArchetype5()
        throws Exception
    {
        String project = "create-5";

        createFilesetArchetype( project );

        File template = getTemplateFile( project, "dummy.file" );
        assertExists( template );
    }
    
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        localRepository = new DefaultArtifactRepository( "local",
           new File( getBasedir(), "target/test-classes/repositories/local" ).toURI().toString(),
           new DefaultRepositoryLayout() );
    }

    private void assertContent( File template, String content )
        throws FileNotFoundException, IOException
    {
        String templateContent = FileUtils.fileRead( template, "UTF-8" );
        assertTrue( "File " + template + " does not contain " + content,
                    StringUtils.countMatches( templateContent, content ) > 0 );
    }

    private void assertExists( File file )
    {
        assertTrue( "File doesn't exist: " + file.getAbsolutePath(), file.exists() );
    }

    private void assertNotContent( File template, String content )
        throws FileNotFoundException, IOException
    {
        String templateContent = FileUtils.fileRead( template, "UTF-8" );
        assertFalse( "File " + template + " contains " + content,
                     StringUtils.countMatches( templateContent, content ) > 0 );
    }

    private void copy( File in, File out )
        throws IOException, FileNotFoundException
    {
        assertTrue( !out.exists() || out.delete() );
        assertFalse( out.exists() );
        FileUtils.copyFile( in, out );
        assertTrue( out.exists() );
        assertTrue( in.exists() );
    }

    private File getDescriptorFile( String project )
    {
        return getFile( project, "target/generated-sources/archetype/src/main/resources/META-INF/maven/archetype.xml" );
    }

    private void assertNotExists( File file )
    {
        assertFalse( "File exists: " + file.getAbsolutePath(), file.exists() );
    }

    private File getFile( String project, String file )
    {
        return new File( getBasedir(), "target/test-classes/projects/" + project + "/" + file );
    }

    private File getProjectFile( String project )
    {
        return getFile( project, "pom.xml" );
    }

    private File getProjectSampleFile( String project )
    {
        return getFile( project, "pom.xml.sample" );
    }

    private File getPropertiesFile( String project )
    {
        return getFile( project, "archetype.properties" );
    }

    private File getPropertiesSampleFile( final String project )
    {
        return getFile( project, "archetype.properties.sample" );
    }

    private File getTemplateFile( String project, String template )
    {
        return getFile( project, "target/generated-sources/archetype/src/main/resources/archetype-resources/"
            + template );
    }
}
