package org.apache.maven.archetype;

import org.apache.maven.archetype.creator.ArchetypeCreator;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.archetype.source.ArchetypeDataSource;

import java.util.Collection;
import java.util.Map;

/**
 * @plexus.component
 * @author Jason van Zyl
 */
public class DefaultArchetyper
    implements Archetyper
{
    /** @plexus.requirement role-hint="fileset" */
    private ArchetypeCreator creator;

    /** @plexus.requirement */
    private ArchetypeGenerator generator;

    /** @plexus.requirement role="org.apache.maven.archetype.source.ArchetypeDataSource" */
    private Map archetypeSources;

    public ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request )
    {
        ArchetypeCreationResult result = new ArchetypeCreationResult();

        creator.createArchetype( request, result );

        return result;
    }

    public ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request )
    {
        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        generator.generateArchetype( request, result );

        return result;
    }

    public Collection getArchetypeDataSources()
    {
        return archetypeSources.values();
    }

    public ArchetypeDataSource getArchetypeDataSource( String roleHint )
    {
        return (ArchetypeDataSource) archetypeSources.get(  roleHint );
    }
}
