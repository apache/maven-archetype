package org.apache.maven.archetype.source;

import org.codehaus.plexus.PlexusTestCase;

import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.ArchetypeManager;

/** @author Jason van Zyl */
public class WikiArchetypeDataSourceTest
    extends PlexusTestCase
{
    public void testWikiArchetypeDataSource()
        throws Exception
    {
        Properties p = new Properties();
        ArchetypeDataSource ads = new InternalCatalogArchetypeDataSource();
        List archetypes = ads.getArchetypeCatalog( p ).getArchetypes();

        assertEquals( 47, archetypes.size() );

        ArchetypeManager a = (ArchetypeManager) lookup( ArchetypeManager.class.getName() );
        archetypes = a.getInternalCatalog().getArchetypes();

        assertEquals( 47, archetypes.size() );
    }
}
