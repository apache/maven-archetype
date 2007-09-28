package org.apache.maven.archetype.source;

import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.registry.Archetype;
import org.apache.maven.archetype.registry.ArchetypeRegistry;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @plexus.component role-hint="registry"
 * @author Jason van Zyl
 */
public class RegistryArchetypeDataSource
    implements ArchetypeDataSource
{
    /** @plexus.requirement */
    protected ArchetypeRegistryManager archetypeRegistryManager;

    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        try
        {
            return createArchetypeMap( archetypeRegistryManager.readArchetypeRegistry() );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error reading archetype registry.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeDataSourceException( "Error parsing archetype registry", e );
        }
    }

    protected List createArchetypeMap( ArchetypeRegistry registry )
        throws ArchetypeDataSourceException
    {
        List archetypes = new ArrayList();

         for ( Iterator i = registry.getArchetypes().iterator(); i.hasNext(); )
         {
             Archetype archetype = (Archetype) i.next();

             archetypes.add( archetype );
         }

         return archetypes;
    }
}