package org.apache.maven.archetype.catalog
import org.junit.Test
import static org.junit.Assert.*
import org.codehaus.plexus.PlexusTestCase
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.apache.maven.archetype.ArchetypeGenerationRequest
import org.apache.maven.archetype.ArchetypeGenerationResult
import org.apache.maven.archetype.generator.DefaultProjectGenerator
import org.apache.maven.archetype.pom.PomManager
import org.apache.maven.archetype.util.RepositoryCreator
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalContainer
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedStandaloneLocalConfiguration
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalDeployer
import org.codehaus.cargo.container.deployer.URLDeployableMonitor
import org.codehaus.cargo.container.property.ServletPropertySet
import org.codehaus.cargo.container.deployable.DeployableType
import org.apache.maven.artifact.repository.ArtifactRepository

/**
 *
 * @author raphaelpieroni
 */
class DefaultArchetypeCatalogerTest extends AbstractMojoTestCase {
    Jetty6xEmbeddedLocalContainer jettyContainer
    def archetypeCataloger
    def repositoryCreator

    void setUp() {
        super.setUp()
        archetypeCataloger = lookup( ArchetypeCataloger.ROLE )
        repositoryCreator = lookup( RepositoryCreator.ROLE )

        //        Start Cargo

        def configuration =
            new Jetty6xEmbeddedStandaloneLocalConfiguration( "target/repository-webapp" )
        configuration.setProperty ServletPropertySet.PORT, "18881"

        System.setProperty "org.apache.maven.archetype.reporitory.directory",
            getTestPath( "target/repositories/" )
        jettyContainer = new Jetty6xEmbeddedLocalContainer( configuration )
        jettyContainer.timeout = 180000L
        jettyContainer.start()

        def factory = new DefaultDeployableFactory()
        def war = factory.createDeployable( jettyContainer.id,
            "target/wars/grarchetype-repository.war",
            DeployableType.WAR )

        war.context = "/"

        def deployer = new Jetty6xEmbeddedLocalDeployer( jettyContainer )
        deployer.deploy war,
            new URLDeployableMonitor( new URL( "http://localhost:18881/central/archetype-catalog.xml" ) )
        deployer.start war
    }
    void tearDown() {
        super.tearDown()
        //        Stop Cargo
        jettyContainer.stop()
    }

    @Test void test_getArchetypesByCatalog() {
        println "getArchetypesByCatalog"

        assertNull archetypeCataloger.getArchetypesByCatalog("")
        assertEquals 3, archetypeCataloger.getArchetypesByCatalog("internal").size()
//        assertEquals 9, archetypeCataloger.getArchetypesByCatalog("http://localhost:18881/central").size()
//        assertEquals 5, archetypeCataloger.getArchetypesByCatalog("https://aze").host.size()
//        assertEquals 3, archetypeCataloger.getArchetypesByCatalog("file://aze").path.size()
//        assertEquals 1, archetypeCataloger.getArchetypesByCatalog("local").local.size()
//        assertEquals 2, archetypeCataloger.getArchetypesByCatalog("remote").remote.size()
    }

    @Test void test_archetypeRecursivelyExists() {
        println "archetypeRecursivelyExists"

        def archetypeDefinitions = archetypeCataloger.getArchetypesByCatalog( "http://localhost:18881/central/archetype-catalog.xml" )
        assert archetypeDefinitions
        println"archetypeDefinitions${archetypeDefinitions.dump()}"
        def archetype = archetypeCataloger.searchArchetype( [groupId:"archetypes", artifactId:"nested-complete-main", version:"1.0"], archetypeDefinitions )
        assert archetype
        println"archetype${archetype.dump()}"
        def localRepository = repositoryCreator.createRepository( "${getBasedir()}/target/repositories/local".toString() )
        def centralRepository = repositoryCreator.createRepository( "http://localhost:18881/central" )
        assertTrue archetypeCataloger.archetypeRecursivelyExists( archetype, archetypeDefinitions, localRepository, [centralRepository] )
    }

    @Test void test_archetypeRecursivelyExists_missing() {
        println "archetypeRecursivelyExists_missing"

        def archetypeDefinitions = archetypeCataloger.getArchetypesByCatalog( "http://localhost:18881/central/archetype-catalog.xml" )
        assert archetypeDefinitions
        println"archetypeDefinitions${archetypeDefinitions.dump()}"
        def archetype = archetypeCataloger.searchArchetype( [groupId:"archetypes", artifactId:"nested-missing-main", version:"1.0"], archetypeDefinitions )
        assert archetype
        println"archetype${archetype.dump()}"
        def localRepository = repositoryCreator.createRepository( "${getBasedir()}/target/repositories/local".toString() )
        def centralRepository = repositoryCreator.createRepository( "http://localhost:18881/central" )
        assertFalse archetypeCataloger.archetypeRecursivelyExists( archetype, archetypeDefinitions, localRepository, [centralRepository] )
    }
}

