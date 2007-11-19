package org.apache.maven.archetype.source;

import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.List;
import java.util.Properties;

/** @author Jason van Zyl */
public class WikiArchetypeDataSourceTest
    extends PlexusTestCase
{
    public void testWikiArchetypeDataSource()
        throws Exception
    {
        Properties p = new Properties();

        ArchetypeDataSource ads = new InternalCatalogArchetypeDataSource();

        List archetypes = ads.getArchetypes( p );

        assertEquals( 37, archetypes.size() );
    }
}
