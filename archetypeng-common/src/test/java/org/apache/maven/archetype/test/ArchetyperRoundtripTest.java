package org.apache.maven.archetype.test;

import org.apache.commons.io.IOUtils;
import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.Archetyper;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Properties;

/** @author Jason van Zyl */
public class ArchetyperRoundtripTest
    extends PlexusTestCase
{
    public void testArchetyper()
        throws Exception
    {
        Archetyper archetype = (Archetyper) lookup( Archetyper.ROLE );

        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.ROLE );

        MavenProjectBuilder projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        ArtifactRepository localRepository = registryManager.createRepository(
            new File( getBasedir(), "target/local-repo" ).toURI().toURL().toExternalForm(), "local-repo" );

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

        File sourceProject = new File( getBasedir(), "src/test/projects/test-project" );

        File workingProject = new File( getBasedir(), "target/projects/test-project" );

        if ( !workingProject.exists() )
        {
            workingProject.mkdirs();
        }

        FileUtils.copyDirectoryStructure( sourceProject, workingProject );

        // (2) create an archetype from the project

        File pom = new File( workingProject, "pom.xml" );

        MavenProject project = projectBuilder.build( pom, localRepository, null );

        ArchetypeCreationRequest acr = new ArchetypeCreationRequest()
            .setProject( project )
            .setLocalRepository( localRepository );

        ArchetypeCreationResult creationResult = archetype.createArchetypeFromProject( acr );

        if ( creationResult.getCause() != null )
        {
            fail( creationResult.getCause().getMessage() );
        }

        // (3) create our own archetype catalog properties in memory

        File catalogDirectory = new File( getBasedir(), "target/catalog" );

        File catalogFile = new File( catalogDirectory, "archetype-catalog.xml" );

        File catalogProperties = new File( catalogDirectory, "archetype-catalog.properties" );

        catalogDirectory.mkdirs();

        Properties p = new Properties();

        p.setProperty( "sources", "catalog" );

        p.setProperty( "catalog.file", catalogFile.getAbsolutePath() );

        OutputStream os = new FileOutputStream( catalogProperties );

        p.store( os, "Generated catalog properties" );

        // (4) create our own archetype catalog describing the archetype we just created

        ArchetypeCatalog catalog = new ArchetypeCatalog();

        Archetype generatedArchetype = new Archetype();

        generatedArchetype.setGroupId( project.getGroupId() );

        generatedArchetype.setArtifactId( project.getArtifactId() );

        generatedArchetype.setVersion( project.getVersion() );

        catalog.addArchetype( generatedArchetype );

        ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();

        Writer writer = new FileWriter( catalogFile );

        catalogWriter.write( writer, catalog );

        IOUtils.closeQuietly( writer );

        // (5) install the archetype we just created

        File generatedArchetypeDirectory = new File( project.getBasedir(), "target/generated-sources/archetypeng" );

        installArchetype( generatedArchetypeDirectory, new File( localRepository.getBasedir() ) );

        // (6) create a project form the archetype we just created

        /*
        ArchetypeGenerationRequest agr = new ArchetypeGenerationRequest();

        ArchetypeGenerationResult generationResult = archetype.generateProjectFromArchetype( agr );

        */

    }

    private void installArchetype( File basedir,
                                   File localRepository )
        throws Exception
    {
        Invoker invoker = (Invoker) lookup( Invoker.ROLE );

        if ( basedir.exists() )
        {
            InvocationRequest request = new DefaultInvocationRequest()
                .setBaseDirectory( basedir )
                .setGoals( Arrays.asList( new String[]{"install"} ) );
                //.setLocalRepositoryDirectory( localRepository );

            Properties envars = CommandLineUtils.getSystemEnvVars();

            invoker.setMavenHome( new File( envars.getProperty( "M2_HOME" ) ) );

            invoker.execute( request );
        }
    }
}
