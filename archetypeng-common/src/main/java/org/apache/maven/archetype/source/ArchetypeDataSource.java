package org.apache.maven.archetype.source;

import java.util.Map;

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
    Map getArchetypes()
        throws ArchetypeDataSourceException;
}
