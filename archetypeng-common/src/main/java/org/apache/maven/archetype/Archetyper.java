package org.apache.maven.archetype;

import java.util.Collection;

/** @author Jason van Zyl */
public interface Archetyper
{
    String ROLE = Archetyper.class.getName();

    ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request );

    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request );

    Collection getArchetypeDataSources();
}
