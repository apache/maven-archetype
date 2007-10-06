package org.apache.maven.archetype;

import java.util.Collection;
import java.util.Properties;

/** @author Jason van Zyl */
public interface Archetyper
{
    String ROLE = Archetyper.class.getName();

    ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request );

    ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request );

    /**
     * Get all available archetypes using the standard ~/.m2/archetype-catalog.properties as the
     * definition for the sources to be used and the configuration for each
     * {@org.apache.maven.archetype.source.ArchetypeDataSource} listed.
     *
     * @return A collection of available archetypes collected from all available source.
     */
    Collection getAvailableArchetypes();

    /**
     * Get all available archetypes using a specified catalog properties as the
     * definition for the sources to be used and the configuration for each
     * {@org.apache.maven.archetype.source.ArchetypeDataSource} listed.
     *
     * @return A collection of available archetypes collected from all available source.
     */
    Collection getAvailableArchetypes( Properties properties );
}
