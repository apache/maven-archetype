package org.apache.maven.archetype.source;

import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.registry.Archetype;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author Jason van Zyl */
public class RegistryArchetypeDataSource
    implements ArchetypeDataSource
{
    private ArchetypeRegistryManager archetypeRegistryManager;

    public RegistryArchetypeDataSource( ArchetypeRegistryManager archetypeRegistryManager )
    {
        this.archetypeRegistryManager = archetypeRegistryManager;
    }

    public Map getArchetypes()
        throws ArchetypeDataSourceException
    {
        Map archetypes = new HashMap();

        for ( Iterator i = archetypeRegistryManager.getDefaultArchetypeRegistry().getArchetypes().iterator(); i.hasNext(); )
        {
            Archetype archetype = (Archetype) i.next();

            archetypes.put( archetype.getArtifactId(), archetype );
        }

        return archetypes;
    }
}