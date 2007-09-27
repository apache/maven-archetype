package org.apache.maven.archetype.source;

import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.registry.Archetype;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;

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

        List list;

        try
        {
            list = archetypeRegistryManager.readArchetypeRegistry().getArchetypes();
        }
        catch ( Exception e )
        {
            throw new ArchetypeDataSourceException( "Error reading ~/.m2/archetype.xml" );
        }

        for ( Iterator i = list.iterator(); i.hasNext(); )
        {
            Archetype archetype = (Archetype) i.next();

            archetypes.put( archetype.getArtifactId(), archetype );
        }

        return archetypes;
    }
}