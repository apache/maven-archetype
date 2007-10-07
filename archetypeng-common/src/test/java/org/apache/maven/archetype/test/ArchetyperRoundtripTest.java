package org.apache.maven.archetype.test;

import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.Archetyper;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

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

        ArchetypeCreationResult result = archetype.createArchetypeFromProject( acr );

        if ( result.getCause() != null )
        {
            fail( result.getCause().getMessage() );
        }

        // (3) create our own archetype catalog properties in memory
        // (4) create our own archetype catalog describing the archetype we just created
        // (5) deploy the archetype we just created
        // (6) create a project form the archetype we just created
    }       
}
