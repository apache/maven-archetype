package org.apache.maven.archetype.source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.catalog.Archetype;

/**
 * @plexus.component role-hint="remote-catalog"
 * @author Jason van Zyl
 */
public class RemoteCatalogArchetypeDataSource
    extends CatalogArchetypeDataSource
{
    public static String REPOSITORY_PROPERTY = "repository";

    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        try
        {
            String repository = properties.getProperty( REPOSITORY_PROPERTY );

            if ( repository.endsWith( "/" ) )
            {
                repository = repository.substring( 0, repository.length(  ) - 1 );
            }

            URL url = new URL( repository + "/" + "archetype-catalog.xml" );

            return createArchetypeMap( readCatalog( new InputStreamReader( url.openStream(  ) ) ) );
        }
        catch ( MalformedURLException e )
        {
            throw new ArchetypeDataSourceException( "Invalid URL provided for archetype registry.", e );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error reading archetype registry.", e );
        }
    }

    public void updateCatalog( Properties properties, Archetype archetype, Settings settings )
        throws ArchetypeDataSourceException
    {
        throw new ArchetypeDataSourceException( "Not supported yet." );
    }
}