package org.apache.maven.archetype;

import org.apache.maven.archetype.creator.ArchetypeCreator;
import org.apache.maven.archetype.generator.ArchetypeGenerator;

/**
 * @plexus.component
 * @author Jason van Zyl
 */
public class DefaultArchetyper
    implements Archetyper
{
    /** @plexus.requirement */
    private ArchetypeCreator creator;

    /** @plexus.requirement */
    private ArchetypeGenerator generator;

    public ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request )
    {
        ArchetypeCreationResult result = new ArchetypeCreationResult();

        // This should take information from the request and that's it.
        //creator.createArchetype( );

        return result;
    }

    public ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request )
    {
        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        // This should take information from the request and that's it.
        //generator.generateArchetype( );

        return result;
    }
}
