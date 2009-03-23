package org.apache.maven.archetype.ui
import org.apache.maven.archetype.ArchetypeGenerationRequest

/**
 *
 * @author raphaelpieroni
 */
interface ArchetypeSelector {
    String ROLE = 'org.apache.maven.archetype.ui.ArchetypeSelector'

    void selectArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode, String catalogs )
}

