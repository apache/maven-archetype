package org.apache.maven.archetype.source;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author Jason van Zyl
 * @plexus.component role-hint="catalog"
 */
public class CatalogArchetypeDataSource
    implements ArchetypeDataSource
{
    public static String ARCHETYPE_CATALOG_PROPERTY = "file";

    public static String ARCHETYPE_CATALOG_FILENAME = "archetype-catalog.xml";

    private ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        String s = properties.getProperty( ARCHETYPE_CATALOG_PROPERTY );

        s = StringUtils.replace( s, "${user.home}", System.getProperty( "user.home" ) );

        File catalogFile = new File( s );

        if ( catalogFile.exists() )
        {

            try
            {
                ArchetypeCatalog catalog = readCatalog( new FileReader( catalogFile ) );

                return createArchetypeMap( catalog );
            }
            catch ( FileNotFoundException e )
            {
                throw new ArchetypeDataSourceException( "The specific archetype catalog does not exist.", e );
            }
        }
        else
        {
            return new ArrayList();
        }
    }

    protected List createArchetypeMap( ArchetypeCatalog archetypeCatalog )
        throws ArchetypeDataSourceException
    {
        List archetypes = new ArrayList();

        for ( Iterator i = archetypeCatalog.getArchetypes().iterator(); i.hasNext(); )
        {
            Archetype archetype = (Archetype) i.next();

            archetypes.add( archetype );
        }

        return archetypes;
    }

    protected ArchetypeCatalog readCatalog( Reader reader )
        throws ArchetypeDataSourceException
    {
        try
        {
            return catalogReader.read( reader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error reading archetype catalog.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeDataSourceException( "Error parsing archetype catalog.", e );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }
}