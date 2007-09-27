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

package org.apache.maven.archetype.creator;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultArchetypeCreatorTest
    extends AbstractMojoTestCase
{
    private List filtereds;

    private List languages;
    private DefaultArtifactRepository localRepository;

    private List repositories;

    public void testCreateFilesetArchetype()
        throws
        Exception
    {
        System.out.println( "testCreateFilesetArchetype" );

        MavenProjectBuilder builder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        String project = "create-3";

        File projectFile = getProjectFile( project );
        File projectFileSample = getProjectSampleFile( project );
        copy( projectFileSample, projectFile );

        FileUtils.deleteDirectory( new File( projectFile.getParentFile(), "target" ) );

        File propertyFile = getPropertiesFile( project );
        File propertyFileSample = getPropertiesSampleFile( project );
        copy( propertyFileSample, propertyFile );

        MavenProject mavenProject =
            builder.buildWithDependencies( projectFile, localRepository, null );
        FilesetArchetypeCreator instance =
            (FilesetArchetypeCreator) lookup( ArchetypeCreator.class.getName(), "fileset" );

        languages = new ArrayList();
        languages.add( "java" );
        languages.add( "aspectj" );
        languages.add( "csharp" );
        languages.add( "groovy" );
        languages.add( "resources" );

        filtereds = new ArrayList();
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
        instance.createArchetype(
            mavenProject,
            propertyFile,
            languages,
            filtereds,
            "UTF-8",
            true,
            false,
            false,
            false,
            getFile( project, "archetype-registry.xml" )
        );

        File template;

        template = getTemplateFile( project, "pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven ArchetypeNG Test create-3" );
        assertContent( template, "<packaging>pom</packaging>" );
        assertNotContent( template, "<parent>" );

        template = getTemplateFile( project, "src/site/site.xml" );
        assertExists( template );
        assertContent( template, "<!-- ${someProperty} -->" );

        template = getTemplateFile( project, "src/site/resources/site.png" );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, ".classpath" );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, "profiles.xml" );
        assertExists( template );
        assertContent( template, "<!-- ${someProperty} -->" );

        template = getTemplateFile( project, "libs/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven ArchetypeNG Test create-3-libraries" );
        assertContent( template, "<packaging>pom</packaging>" );
        assertNotContent( template, "<parent>" );

        template = getTemplateFile( project, "libs/prj-a/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven ArchetypeNG Test create-3-libraries-project-a" );
        assertNotContent( template, "<packaging>pom</packaging>" );
        assertNotContent( template, "<parent>" );

        template = getTemplateFile( project, "libs/prj-a/src/main/mdo/descriptor.xml" );
        assertExists( template );
        assertContent( template, "<!-- ${someProperty} -->" );

        template = getTemplateFile( project, "libs/prj-b/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven ArchetypeNG Test create-3-libraries-project-b" );
        assertNotContent( template, "<packaging>pom</packaging>" );
        assertNotContent( template, "<parent>" );

        template = getTemplateFile( project, "libs/prj-b/src/main/java/test/com/Component.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template = getTemplateFile( project, "libs/prj-b/src/main/java/test/com/package.html" );
        assertExists( template );
        assertContent( template, "<!-- ${someProperty} -->" );

        template =
            getTemplateFile( project, "libs/prj-b/src/test/java/test/common/ComponentTest.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/pom.xml" );
        assertExists( template );
        assertContent( template, "${groupId}" );
        assertContent( template, "${artifactId}" );
        assertContent( template, "${version}" );
        assertContent( template, "Maven ArchetypeNG Test create-3-application" );
        assertNotContent( template, "<packaging>pom</packaging>" );
        assertNotContent( template, "<parent>" );

        template = getTemplateFile( project, "application/src/main/java/Main.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template =
            getTemplateFile(
                project,
                "application/src/main/java/test/application/Application.java"
            );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template =
            getTemplateFile(
                project,
                "application/src/main/java/test/application/audios/Application.ogg"
            );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template =
            getTemplateFile(
                project,
                "application/src/main/java/test/application/images/Application.png"
            );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/main/resources/log4j.properties" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template =
            getTemplateFile( project, "application/src/main/resources/META-INF/MANIFEST.MF" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template =
            getTemplateFile( project, "application/src/main/resources/test/application/some/Gro.groovy" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/main/resources/splash.png" );
        assertExists( template );
        assertNotContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/test/java/TestAll.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template =
            getTemplateFile(
                project,
                "application/src/test/java/test/application/ApplicationTest.java"
            );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/it-test/java/test/ItTest1.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template = getTemplateFile( project, "application/src/it-test/java/ItTestAll.java" );
        assertExists( template );
        assertContent( template, "${someProperty}" );

        template =
            getTemplateFile( project, "application/src/it-test/resources/ItTest1Result.txt" );
        assertExists( template );
        assertContent( template, "${someProperty}" );
    }

    protected void tearDown()
        throws
        Exception
    {
        super.tearDown();
    }

    protected void setUp()
        throws
        Exception
    {
        super.setUp();

        localRepository =
            new DefaultArtifactRepository(
                "local",
                new File( getBasedir(), "target/test-classes/repositories/local" ).toURI()
                    .toString(),
                new DefaultRepositoryLayout()
            );

        repositories =
            Arrays.asList(
                new ArtifactRepository[]
                    {
                        new DefaultArtifactRepository(
                            "central",
                            new File( getBasedir(), "target/test-classes/repositories/central" )
                                .toURI().toString(),
                            new DefaultRepositoryLayout()
                        )
                    }
            );
    }

    private boolean assertContent( File template,
                                   String content )
        throws
        FileNotFoundException,
        IOException
    {
        String templateContent = IOUtil.toString( new FileReader( template ) );
        return StringUtils.countMatches( templateContent, content ) > 0;
    }

    private void assertExists( File file )
    {
        assertTrue( "File doesn't exist:" + file.getAbsolutePath(), file.exists() );
    }

    private boolean assertNotContent( File template,
                                      String content )
        throws
        FileNotFoundException,
        IOException
    {
        return !assertContent( template, content );
    }

    private void copy( File in,
                       File out )
        throws
        IOException,
        FileNotFoundException
    {
        assertTrue( !out.exists() || out.delete() );
        assertFalse( out.exists() );
        IOUtil.copy( new FileReader( in ), new FileWriter( out ) );
        assertTrue( out.exists() );
        assertTrue( in.exists() );
    }

    private File getDescriptorFile( String project )
    {
        return
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/target/generated-sources/archetypeng/"
                    + "src/main/resources/"
                    + "META-INF/maven/archetype.xml"
            );
    }

    private String getPath( String basedir,
                            String child )
    {
        return new File( basedir, child ).getPath();
    }

    private File getFile( String project,
                          String file )
    {
        return new File( getBasedir(), "target/test-classes/projects/" + project + "/" + file );
    }

    private File getProjectFile( String project )
    {
        return new File( getBasedir(), "target/test-classes/projects/" + project + "/pom.xml" );
    }

    private File getProjectSampleFile( String project )
    {
        return
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/pom.xml.sample"
            );
    }

    private File getPropertiesFile( String project )
    {
        return
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/archetype.properties"
            );
    }

    private File getPropertiesSampleFile( final String project )
    {
        File propertyFileSample =
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/archetype.properties.sample"
            );
        return propertyFileSample;
    }

    private File getTemplateFile( String project,
                                  String template )
    {
        return
            new File(
                getBasedir(),
                "target/test-classes/projects/" + project + "/target/generated-sources/archetypeng/"
                    + "src/main/resources/"
                    + "archetype-resources/" + template
            );
    }
}
