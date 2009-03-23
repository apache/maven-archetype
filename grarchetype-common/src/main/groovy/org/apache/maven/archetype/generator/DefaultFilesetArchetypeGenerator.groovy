package org.apache.maven.archetype.generator

/**
 *
 * @author rafale
 * @plexus.component role-hint="fileset"
 */
class DefaultFilesetArchetypeGenerator
extends AbstractFilesetArchetypeGenerator {
    def loadArchetypeDescriptor( File archetypeFile ) {println"DefaultFilesetArchetypeGenerator ${archetypeFile}"
        new XmlSlurper().parse( archetypeArtifactManager.getFilesetArchetypeDescriptor( archetypeFile ) )
    }
}

