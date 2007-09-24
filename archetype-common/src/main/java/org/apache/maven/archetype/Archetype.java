package org.apache.maven.archetype;

/** @author Jason van Zyl */
public interface Archetype
{
    String ROLE = Archetype.class.getName();

    ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request );

    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request );
}
