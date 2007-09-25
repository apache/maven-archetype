package org.apache.maven.archetype;

/** @author Jason van Zyl */
public interface ArchetypeFacade
{
    String ROLE = ArchetypeFacade.class.getName();

    ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request );

    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request );
}
