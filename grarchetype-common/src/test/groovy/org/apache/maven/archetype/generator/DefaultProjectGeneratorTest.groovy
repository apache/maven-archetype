package org.apache.maven.archetype.generator

import org.junit.Test
import static org.junit.Assert.*
import org.codehaus.plexus.PlexusTestCase
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.codehaus.plexus.util.FileUtils
import org.apache.maven.archetype.ArchetypeGenerationRequest
import org.apache.maven.archetype.ArchetypeGenerationResult
import org.apache.maven.archetype.generator.DefaultProjectGenerator
import org.apache.maven.archetype.pom.PomManager
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout

/**
 *
 * @author raphaelpieroni
 */
class DefaultProjectGeneratorTest extends AbstractMojoTestCase {
    def generator
    def pomManager
    def generationRequest
    def project
    def outputDirectory
    def projectDirectory
    def artifactRepositoryFactory
    def localRepository
    def remoteRepostories
    def defaultArtifactRepositoryLayout
    void setUp() {
        super.setUp()
        generator = lookup( ProjectGenerator.ROLE )
        pomManager = lookup( PomManager.ROLE )
        artifactRepositoryFactory = lookup( ArtifactRepositoryFactory.ROLE )
        defaultArtifactRepositoryLayout = lookup( ArtifactRepositoryLayout.ROLE )
    }
    void tearDown() {super.tearDown()}

    @Test void test_generateNestedArchetypeSimple() {
        println'generateNestedArchetypeSimple'
        prepareTest project:'generateNestedArchetypeSimple', artifactId:'artifact'

        generationRequest = new ArchetypeGenerationRequest(
            groupId:'archetypes',
            artifactId:'nested-main',
            version:'1.0',
            outputDirectory:outputDirectory,
            generationFilterProperties:[
                groupId:'file-value',
                artifactId:'artifact',
                version:'file-value',
                packageName:'file.value.package',
                propertyWithDefault:'file-value',
                propertyWithoutDefault:'file-value'] as Properties,
            localRepository:localRepository,
            repositories:remoteRepostories
        )
        generator.generateProject generationRequest


        //CORE
        def artifact = loadModel( 'pom.xml' )
        // assert no parent, modules, content
        assertParent artifact, null
        assertDefinition artifact, [groupId:'file-value', artifactId:'artifact', version:'file-value']
        assertModules artifact, ['artifact-n-inner', 'artifact-n2-inner', 'artifact-n-old', 'artifact-inner']

        def artifact_n_inner = loadModel( '/artifact-n-inner/pom.xml' )
        // assert parent, modules, content
        assertParent artifact_n_inner, [groupId:'file-value', artifactId:'artifact', version:'file-value']
        assertDefinition artifact_n_inner, [groupId:'file-value', artifactId:'artifact-n-inner', version:'file-value']
        assertModules artifact_n_inner, ['artifact-n-inner-n-innest', 'artifact-n-inner-n-old', 'artifact-n-inner-inner']

        def artifact_n2_inner = loadModel( '/artifact-n2-inner/pom.xml' )
        // assert parent, modules, content
        assertParent artifact_n2_inner, [groupId:'file-value', artifactId:'artifact', version:'file-value']
        assertDefinition artifact_n2_inner, [groupId:'file-value', artifactId:'artifact-n2-inner', version:'file-value']
        assertModules artifact_n2_inner, ['artifact-n2-inner-n-innest', 'artifact-n2-inner-n-old', 'artifact-n2-inner-inner']

        def artifact_n_old = loadModel( '/artifact-n-old/pom.xml' )
        // assert parent, content
        assertParent artifact_n_old, [groupId:'file-value', artifactId:'artifact', version:'file-value']
        assertDefinition artifact_n_old, [groupId:'file-value', artifactId:'artifact-n-old', version:'file-value']
        assertModules artifact_n_old, null

        def artifact_inner = loadModel( '/artifact-inner/pom.xml' )
        // assert parent, modules, content
        assertParent artifact_inner, [groupId:'file-value', artifactId:'artifact', version:'file-value']
        assertDefinition artifact_inner, [groupId:null, artifactId:'artifact-inner', version:'file-value']
        assertModules artifact_inner, ['artifact-n-innest', 'artifact-innest']

        def artifact_n_innest = loadModel( '/artifact-inner/artifact-n-innest/pom.xml' )
        // assert parent, modules, content
        assertParent artifact_n_innest, [groupId:'file-value', artifactId:'artifact-inner', version:'file-value']
        assertDefinition artifact_n_innest, [groupId:'file-value', artifactId:'artifact-n-innest', version:'file-value']
        assertModules artifact_n_innest, ['artifact-n-innest-inner', 'artifact-n-innest-n-innest', 'artifact-n-innest-n-old']

        def artifact_innest = loadModel( '/artifact-inner/artifact-innest/pom.xml' )
        // assert parent, content, partial
        assertParent artifact_innest, [groupId:'file-value', artifactId:'artifact-inner', version:'file-value']
        assertDefinition artifact_innest, [groupId:null, artifactId:'artifact-innest', version:'file-value']
        assertModules artifact_innest, null




        //NESTED FIRST LEVEL
        def artifact_n_inner_n_innest = loadModel( '/artifact-n-inner/artifact-n-inner-n-innest/pom.xml' )
        // assert parent, modules, content
        def artifact_n_inner_n_old = loadModel( '/artifact-n-inner/artifact-n-inner-n-old/pom.xml' )
        // assert parent, content
        def artifact_n_inner_inner = loadModel( '/artifact-n-inner/artifact-n-inner-inner/pom.xml' )
        // assert parent, modules, content
        def artifact_n_inner_n2_innest = loadModel( '/artifact-n-inner/artifact-n-inner-inner/artifact-n-inner-n2-innest/pom.xml' )
        // assert parent, modules, content
        def artifact_n_inner_innest = loadModel( '/artifact-n-inner/artifact-n-inner-inner/artifact-n-inner-innest/pom.xml' )
        // assert parent, content, partial

        def artifact_n2_inner_n_innest = loadModel( '/artifact-n2-inner/artifact-n2-inner-n-innest/pom.xml' )
        // assert parent, modules, content
        def artifact_n2_inner_n_old = loadModel( '/artifact-n2-inner/artifact-n2-inner-n-old/pom.xml' )
        // assert parent, content
        def artifact_n2_inner_inner = loadModel( '/artifact-n2-inner/artifact-n2-inner-inner/pom.xml' )
        // assert parent, modules, content
        def artifact_n2_inner_n2_innest = loadModel( '/artifact-n2-inner/artifact-n2-inner-inner/artifact-n2-inner-n2-innest/pom.xml' )
        // assert parent, modules, content
        def artifact_n2_inner_innest = loadModel( '/artifact-n2-inner/artifact-n2-inner-inner/artifact-n2-inner-innest/pom.xml' )
        // assert parent, content, partial

        def artifact_n_innest_n_innest = loadModel( '/artifact-inner/artifact-n-innest/artifact-n-innest-n-innest/pom.xml' )
        // assert parent, modules, content
        def artifact_n_innest_n_old = loadModel( '/artifact-inner/artifact-n-innest/artifact-n-innest-n-old/pom.xml' )
        // assert parent, content
        def artifact_n_innest_inner = loadModel( '/artifact-inner/artifact-n-innest/artifact-n-innest-inner/pom.xml' )
        // assert parent, modules, content
        def artifact_n_innest_n2_innest = loadModel( '/artifact-inner/artifact-n-innest/artifact-n-innest-inner/artifact-n-innest-n2-innest/pom.xml' )
        // assert parent, modules, content
        def artifact_n_innest_innest = loadModel( '/artifact-inner/artifact-n-innest/artifact-n-innest-inner/artifact-n-innest-innest/pom.xml' )
        // assert parent, content, partial




//        fail 'NO ASSERT YET'



    }


    private void assertModules( pom, expectedModules ) {
        println"POM=${pom?.modules?.dump()}"
        println"EXP=${expectedModules?.dump()}"

        if( !expectedModules ) {
            assertTrue !pom.modules
            return
        }
        if( !pom.modules ) fail 'When modules provided, pom should have modules'
        assertEquals expectedModules.size(), pom.modules.size()
        expectedModules.each{
            assertTrue pom.modules.contains( it )
        }
    }
    private void assertDefinition( pom, expected ) {
        if( !expected ) fail 'expected should be provided'
        assertEquals expected.groupId, pom.groupId
        assertEquals expected.artifactId, pom.artifactId
        assertEquals expected.version, pom.version
    }
    private void assertParent( pom, expectedParent ) {
        if( !expectedParent ) assertTrue !pom.parent ; return
        if( !pom.parent ) fail 'When parent provided, pom should have parent'
        assertEquals expectedParent.groupId, pom.parent.groupId
        assertEquals expectedParent.artifactId, pom.parent.artifactId
        assertEquals expectedParent.version, pom.parent.version
    }
    private def loadModel( pom ) {
        pomManager.loadModel( new File( projectDirectory, pom ) )
    }
    private void prepareTest( args ) {
        project = args.project
        outputDirectory = "${basedir}/target/projects/${project}"
        projectDirectory = new File( outputDirectory, args.artifactId )

        localRepository = createRepository( "file://${basedir}/target/repositories/local/${project}", 'local' )
        remoteRepostories = [createRepository( "file://${basedir}/target/repositories/central", 'central' )]
        new File( "${basedir}/target/repositories/local/${project}" ).mkdirs()

        assertDeleted projectDirectory
    }
    private void assertDeleted( file ) {
        if ( file.exists() ) {
            if ( file.isDirectory() ) {
                try {
                    FileUtils.deleteDirectory file
                } catch ( IOException e ) {
                    fail "Unable to delete directory:$file:${e.getLocalizedMessage()}"
                }
            }
        } else {
            try {
                FileUtils.forceDelete file
            } catch ( IOException e ) {
                fail "Unable to delete file:$file:${e.getLocalizedMessage()}"
                e.printStackTrace()
            }
        }
        if ( file.exists() ) {
            fail "File not deleted:$file"
        }
    }
    private def createRepository( url, repositoryId ) {

        def updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS
        def checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN

        def snapshotsPolicy = new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag )
        def releasesPolicy = new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag )

        def repo =
            artifactRepositoryFactory.createArtifactRepository(
                repositoryId,
                url,
                defaultArtifactRepositoryLayout,
                snapshotsPolicy,
                releasesPolicy
            )

        return repo
    }
}

