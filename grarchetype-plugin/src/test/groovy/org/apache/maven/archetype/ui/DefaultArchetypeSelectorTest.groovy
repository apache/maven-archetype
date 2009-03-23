package org.apache.maven.archetype.ui
import org.junit.Test
import static org.junit.Assert.*
import org.codehaus.plexus.PlexusTestCase
import org.apache.maven.plugin.testing.AbstractMojoTestCase

/**
 *
 * @author raphaelpieroni
 */
class DefaultArchetypeSelectorTest extends AbstractMojoTestCase {
    def selector
    
    void setUp() {
        super.setUp()
        selector = lookup( ArchetypeSelector.ROLE )
    }
    void tearDown() {
        super.tearDown()
    }


    @Test void test_archetypeDefined() {
        println'archetypeDefined'

        assertFalse selector.archetypeDefined([])
        assertFalse selector.archetypeDefined([groupId:null, artifactId:null, version:null])
        assertFalse selector.archetypeDefined([groupId:"archetypes", artifactId:null, version:null])
        assertFalse selector.archetypeDefined([groupId:null, artifactId:"archetype", version:null])
        assertFalse selector.archetypeDefined([groupId:null, artifactId:null, version:"1.0"])
        assertFalse selector.archetypeDefined([groupId:"archetypes", artifactId:"archetype", version:null])
        assertFalse selector.archetypeDefined([groupId:null, artifactId:"archetype", version:"1.0"])
        assertFalse selector.archetypeDefined([groupId:"archetypes", artifactId:null, version:"1.0"])
        assertTrue selector.archetypeDefined([groupId:"archetypes", artifactId:"archetype", version:"1.0"])
    }
    
}

