/*
 *  Copyright 2009 raphaelpieroni.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.maven.archetype.ui
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalContainer
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedStandaloneLocalConfiguration
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory
import org.codehaus.cargo.container.jetty.Jetty6xEmbeddedLocalDeployer
import org.codehaus.cargo.container.deployer.URLDeployableMonitor
import org.codehaus.cargo.container.property.ServletPropertySet
import org.codehaus.cargo.container.deployable.DeployableType
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.archetype.util.RepositoryCreator

/**
 *
 * @author raphaelpieroni
 */
class DefaultArchetypeGenerationConfiguratorTest extends AbstractMojoTestCase {
    def generator
    def repositoryCreator
    Jetty6xEmbeddedLocalContainer jettyContainer

    void setUp() {
        super.setUp()
        generator = lookup( ArchetypeGenerationConfigurator.ROLE )
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

    void test_loadRequiredProperties(){
        println'archetypeDefined'

        println"REPO=${getBasedir()}/target/repositories/local"
        def localRepository = repositoryCreator.createRepository( "file://${getBasedir()}/target/repositories/local".toString() )
        def centralRepository = repositoryCreator.createRepository( "http://localhost:18881/central" )

        def properties = generator.loadRequiredProperties(
            [groupId:'archetypes', artifactId:'nested-properties-main', version:'1.0', localRepository:localRepository, repositories:[centralRepository]])
        assertEquals 14, properties.size()

    }

}

