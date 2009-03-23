package org.apache.maven.archetype

/**
 *
 * @author rafale
 */
interface Archetype {
    String ROLE = 'org.apache.maven.archetype.Archetype'

    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request )
	
}

