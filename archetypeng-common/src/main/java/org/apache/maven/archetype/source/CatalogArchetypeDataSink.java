package org.apache.maven.archetype.source;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/** @author Jason van Zyl */
public class CatalogArchetypeDataSink
    extends AbstractLogEnabled
    implements ArchetypeDataSink
{
    private ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();

    public void putArchetypes( List archetypes,
                               Writer writer )
        throws ArchetypeDataSinkException
    {
        ArchetypeCatalog catalog = new ArchetypeCatalog();

        for ( Iterator i = archetypes.iterator(); i.hasNext(); )
        {
            Archetype archetype = (Archetype) i.next();

            catalog.addArchetype( archetype );
        }

        try
        {
            catalogWriter.write( writer, catalog );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSinkException( "Error writing archetype catalog.", e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    public void putArchetypes( ArchetypeDataSource source,
                               Properties properties,
                               Writer writer )
        throws ArchetypeDataSourceException, ArchetypeDataSinkException
    {
        List archetypes = source.getArchetypes( properties );

        putArchetypes( archetypes, writer );
    }
}
