package org.apache.maven.archetype.generator
import org.apache.maven.archetype.ArchetypeGenerationRequest

/**
 *
 * @author raphaelpieroni
 */
interface ProjectGenerator {
    static String ROLE = 'org.apache.maven.archetype.generator.ProjectGenerator'

    void generateProject( ArchetypeGenerationRequest request )
	
}

