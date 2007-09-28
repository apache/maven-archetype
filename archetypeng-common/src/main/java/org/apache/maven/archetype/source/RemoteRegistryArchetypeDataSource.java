package org.apache.maven.archetype.source;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * @plexus.component role-hint="remote-registry"
 * @author Jason van Zyl
 */
public class RemoteRegistryArchetypeDataSource
    extends RegistryArchetypeDataSource
{        
    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        try
        {
            URL url = new URL( properties.getProperty( "url" ) );

            return createArchetypeMap( archetypeRegistryManager.readArchetypeRegistry( new InputStreamReader( url.openStream() ) ) );
        }
        catch ( MalformedURLException e )
        {
            throw new ArchetypeDataSourceException( "Invalid URL provided for archetype registry.", e );
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
}