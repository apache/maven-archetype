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

import org.apache.maven.archetype.model.ArchetypeModel;
import org.apache.maven.archetype.model.io.xpp3.ArchetypeXpp3Writer;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.archiver.Archiver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * The Archetype Creator will take an existing Maven project and turn it into a archetype, or project
 * template that can be used by anyone. It is the easiest way to take a fully functional Maven projet and
 * transform it into a archetype.
 *
 * @author jason van zyl
 * @plexus.component
 * @todo dealing adequately with multi module builds
 */
public class DefaultArchetypeCreator
    implements ArchetypeCreator
{
    /**
     * @plexus.requirement
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @plexus.requirement role-hint="jar"
     */
    private Archiver archiver;

    private MavenXpp3Reader modelReader;

    private MavenXpp3Writer modelWriter;

    ArchetypeXpp3Writer archetypeModelWriter;

    public DefaultArchetypeCreator()
    {
        modelReader = new MavenXpp3Reader();

        modelWriter = new MavenXpp3Writer();

        archetypeModelWriter = new ArchetypeXpp3Writer();
    }

    public File createArchetype( File basedir, ArtifactRepository localRepository, File targetDirectory,
                                 Properties properties )
        throws ArchetypeCreationException
    {
        File pom = new File( basedir, "pom.xml" );

        MavenProject project;

        try
        {
            project = projectBuilder.build( pom, localRepository, null );
        }
        catch ( ProjectBuildingException e )
        {
            throw new ArchetypeCreationException( "Cannot read POM of the source project.", e );
        }

        return createArchetype( project, localRepository, targetDirectory, properties );
    }

    private String clipToBasedir( String path, File basedir )
        throws IOException
    {
        return new File( path ).getCanonicalPath().substring( basedir.getCanonicalPath().length() + 1 );
    }

    public File createArchetype( MavenProject project, ArtifactRepository localRepository, File targetDirectory,
                                 Properties properties )
        throws ArchetypeCreationException
    {
        if ( !targetDirectory.exists() )
        {
            if ( !targetDirectory.mkdirs() )
            {
                throw new ArchetypeCreationException( "Cannot create target directory for archetype creation." );
            }
        }

        // basedir for the source project
        File basedir = project.getFile().getParentFile();

        // Source directory as stated in the source POM, this may not be the default src/main/java
        String sourceDirectory = null;

        // Test source directory as state in the source POM, this may not be the default src/test/java
        String testSourceDirectory = null;

        // ----------------------------------------------------------------------
        // Create the archetype archetypeModel
        //
        // -> add all the main sources to the model
        // -> add all the main resources to the model
        // -> add all the test sources to the model
        // -> add all the test resources to the model
        // ----------------------------------------------------------------------

        ArchetypeModel archetypeModel = new ArchetypeModel();

        archetypeModel.setId( project.getArtifactId() );

        try
        {
            // ----------------------------------------------------------------------
            // Sources
            // ----------------------------------------------------------------------

            // Typically src/main/java, but could be different
            sourceDirectory = clipToBasedir( project.getBuild().getSourceDirectory(), basedir );

            List sources = getFiles( project.getBuild().getSourceDirectory() );

            for ( Iterator i = sources.iterator(); i.hasNext(); )
            {
                archetypeModel.addSource( clipToBasedir( (String) i.next(), basedir ) );
            }

            // ----------------------------------------------------------------------
            // Resources
            // ----------------------------------------------------------------------

            List resourceElements = project.getBuild().getResources();

            for ( Iterator i = resourceElements.iterator(); i.hasNext(); )
            {
                Resource resource = (Resource) i.next();

                List resources = getFiles( resource.getDirectory() );

                for ( Iterator j = resources.iterator(); j.hasNext(); )
                {
                    archetypeModel.addResource( clipToBasedir( (String) j.next(), basedir ) );
                }
            }

            // ----------------------------------------------------------------------
            // Test Sources
            // ----------------------------------------------------------------------

            // Typicall src/test/java, but could be different
            testSourceDirectory = clipToBasedir( project.getBuild().getTestSourceDirectory(), basedir );

            List testSources = getFiles( project.getBuild().getTestSourceDirectory() );

            for ( Iterator i = testSources.iterator(); i.hasNext(); )
            {
                archetypeModel.addTestSource( clipToBasedir( (String) i.next(), basedir ) );
            }

            // ----------------------------------------------------------------------
            // Test Resources
            // ----------------------------------------------------------------------

            List testResourceElements = project.getBuild().getResources();

            for ( Iterator i = testResourceElements.iterator(); i.hasNext(); )
            {
                Resource resource = (Resource) i.next();

                List resources = getFiles( resource.getDirectory() );

                for ( Iterator j = resources.iterator(); j.hasNext(); )
                {
                    archetypeModel.addTestResource( clipToBasedir( (String) j.next(), basedir ) );
                }
            }
        }
        catch ( IOException e )
        {
            throw new ArchetypeCreationException( "Error reading source directory specified in the source POM.", e );
        }

        // ----------------------------------------------------------------------
        // Write out the archetype model
        // ----------------------------------------------------------------------

        File archetypeModelFile = new File( targetDirectory, "archetype.xml" );

        try
        {
            archetypeModelWriter.write( new FileWriter( archetypeModelFile ), archetypeModel );

        }
        catch ( IOException e )
        {
            throw new ArchetypeCreationException( "Error writing archetype model file.", e );
        }

        // ----------------------------------------------------------------------
        // Create the POM which needs to have ${variables} plugged into the POM.
        // We need to put in the ${variables} into the model that will be
        // substituted in when a user creates a project from this archetype
        //
        // -> groupId
        // -> artifactId
        // -> version
        // -> ${name}
        // ----------------------------------------------------------------------

        Model model = null;

        try
        {
            model = modelReader.read( new FileReader( project.getFile() ) );
        }
        catch ( Exception e )
        {
            // do nothing as the POM was already parsed, I just need the model here because the model
            // coming in with the POM seems to polluted with runtime values.
        }

        model.setGroupId( "${groupId}" );

        model.setArtifactId( "${artifactId}" );

        model.setVersion( "${version}" );

        model.setName( "${name}" );

        File pomFile = new File( targetDirectory, "pom.xml" );

        try
        {
            modelWriter.write( new FileWriter( pomFile ), model );
        }
        catch ( IOException e )
        {
            throw new ArchetypeCreationException( "Error writing archetype.", e );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        File finalDir = new File( basedir, "target/archetype-project" );

        if ( !finalDir.exists() )
        {
            finalDir.mkdirs();
        }

        try
        {
            String basePackage = properties.getProperty( "package" );

            // Create a directory where we can drop the filtered project files
            File filterDir = new File( basedir, "target/filtered-project" );

            // Use the model/project values to copy the sources
            filterCopy( new File( basedir, sourceDirectory ), new File( filterDir, sourceDirectory ), properties );

            // Use the model/project values to copy the test sources
            filterCopy( new File( basedir, testSourceDirectory ), new File( filterDir, testSourceDirectory ),
                        properties );

            // Now we need to remove the portion of the path that corresonds to package that was used in the
            // archetype master so that when a user generates from it they can specify their own package path
            // to be used. So we need to copy from:
            //
            // [filterDir]/src/main/java/${package}/..
            // to
            // [finalDir]/srcm/main/java/

            File finalSourceDir = new File( finalDir, sourceDirectory );
            FileUtils.copyDirectoryStructure( new File( sourceDirectory, basePackage ), finalSourceDir );

            File finalTestSourceDir = new File( finalDir, testSourceDirectory );
            FileUtils.copyDirectoryStructure( new File( testSourceDirectory, basePackage ), finalTestSourceDir );
        }
        catch ( IOException e )
        {
            throw new ArchetypeCreationException( "Error copying and filtering archetype resources.", e );
        }

        File archetypeJar = new File( targetDirectory, "archetype.jar" );

        try
        {
            // ----------------------------------------------------------------------
            // New Format
            // ----------------------------------------------------------------------

            archiver.addFile( archetypeModelFile, "META-INF/maven/archetype.xml" );

            archiver.addFile( pomFile, "pom.xml" );

            archiver.addDirectory( finalDir );

            // ----------------------------------------------------------------------
            // Old Format
            // ----------------------------------------------------------------------

            archiver.addFile( pomFile, "archetype-resources/pom.xml" );

            archiver.addFile( archetypeModelFile, "META-INF/archetype.xml" );

            archiver.addDirectory( finalDir, "archetype-resources/" );

            archiver.setDestFile( archetypeJar );

            archiver.createArchive();
        }
        catch ( Exception e )
        {
            throw new ArchetypeCreationException( "Error creating archetype JAR.", e );
        }

        return archetypeJar;
    }

    private List getFiles( String directory )
        throws IOException
    {
        File f = new File( directory );

        if ( !f.exists() )
        {
            return Collections.EMPTY_LIST;
        }

        return FileUtils.getFileNames( f, "**/**", "**/.svn/**", true );
    }

    private void filterCopy( File source, File target, Properties properties )
        throws IOException
    {
        String basePackage = properties.getProperty( "package" );
        FileUtils.copyDirectoryStructure( source, target, new FilteringCopier( basePackage, "${package}" ) );
    }
}

