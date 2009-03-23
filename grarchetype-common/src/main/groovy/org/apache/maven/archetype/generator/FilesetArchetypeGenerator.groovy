package org.apache.maven.archetype.generator
import org.apache.maven.archetype.ArchetypeGenerationRequest

/**
 *
 * @author rafale
 */
interface FilesetArchetypeGenerator {
    static String ROLE = 'org.apache.maven.archetype.generator.FilesetArchetypeGenerator'

    void generateProject( ArchetypeGenerationRequest request )
	
}

