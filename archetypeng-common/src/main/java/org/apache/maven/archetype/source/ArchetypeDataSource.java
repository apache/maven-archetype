package org.apache.maven.archetype.source;

import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.settings.Settings;

/**
 * Sources we can get Archetypes from. This may be the local registry, a Wiki, or,
 * a Maven Repository application. We might also want to get all the Archetypes based
 * on some predetermined criteria and that could be anything given the source. A simple
 * use-case might be to grab all Archetypes for a particular groupId, or Archetypes for
 * webapps, or who knows what.
 *
 * @author Jason van Zyl
 */
public interface ArchetypeDataSource
{
    String ROLE = ArchetypeDataSource.class.getName();

    List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException;

    void updateCatalog( Properties properties, Archetype archetype, Settings settings )
        throws ArchetypeDataSourceException;

    ArchetypeDataSourceDescriptor getDescriptor();
}