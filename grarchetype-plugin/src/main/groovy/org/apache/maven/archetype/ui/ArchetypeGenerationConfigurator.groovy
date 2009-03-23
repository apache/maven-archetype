package org.apache.maven.archetype.ui

/**
 *
 * @author raphaelpieroni
 */
interface ArchetypeGenerationConfigurator {
    String ROLE = 'org.apache.maven.archetype.ui.ArchetypeGenerationConfigurator'

    void configureArchetype( request, interactiveMode, executionProperties )

}

