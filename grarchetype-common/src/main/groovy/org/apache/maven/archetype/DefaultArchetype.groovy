package org.apache.maven.archetype

import org.codehaus.plexus.logging.AbstractLogEnabled
import org.apache.maven.archetype.generator.ProjectGenerator

/**
 *
 * @author rafale
 * @plexus.component
 */
class DefaultArchetype
extends AbstractLogEnabled
implements Archetype {

    /** @plexus.requirement */
    private ProjectGenerator generator

    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request ) {
        logger.error "generateProjectFromArchetype $request"

        ArchetypeGenerationResult result = new ArchetypeGenerationResult()

        try {
            generator.generateProject request
        } catch( exception ) {
            result.cause = exception
        }

        return result
    }

}

