package org.apache.maven.archetype.source;

import org.apache.maven.archetype.catalog.Archetype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.settings.Settings;

/**
 * @plexus.component role-hint="wiki"
 * @author Jason van Zyl
 */
public class WikiArchetypeDataSource
    implements ArchetypeDataSource
{
    public static String URL = "url";

    private static String DEFAULT_ARCHETYPE_INVENTORY_PAGE = "http://docs.codehaus.org/pages/viewpagesrc.action?pageId=48400";

    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        String url = properties.getProperty( URL );

        if ( url == null )
        {
            url = DEFAULT_ARCHETYPE_INVENTORY_PAGE;
        }

        List archetypes = new ArrayList();

        StringBuffer sb = new StringBuffer();

        try
        {
            InputStream in = new URL( cleanupUrl( url ) ).openStream();

            BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

            char[] buffer = new char[1024];

            int len = 0;

            while ( ( len = reader.read( buffer ) ) > -1 )
            {
                sb.append( buffer, 0, len );
            }
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error retrieving list of archetypes from " + url );
        }

        Pattern ptn = Pattern.compile(
            "<br>\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_. ]+)\\|([-a-zA-Z0-9_.:/ \\[\\],]+)\\|([^|]+)\\|" );

        Matcher m = ptn.matcher( sb.toString() );

        while ( m.find() )
        {
            Archetype archetype = new Archetype();

            archetype.setArtifactId( m.group( 1 ).trim() );

            archetype.setGroupId( m.group( 2 ).trim() );

            String version = m.group( 3 ).trim();

            if ( version.equals( "" ) )
            {
                version = "RELEASE";
            }

            archetype.setVersion( version );

            archetype.setRepository( cleanupUrl( m.group( 4 ).trim() ) );

            archetype.setDescription( cleanup( m.group( 5 ).trim() ) );

            archetypes.add( archetype );
        }

        return archetypes;
    }

    static String cleanup( String val )
    {
        val = val.replaceAll( "\\r|\\n|\\s{2,}", "" );
        return val;
    }

    static String cleanupUrl( String val )
    {
        return val.replaceAll( "\\r|\\n|\\s{2,}|\\[|\\]|\\&nbsp;", "" );
    }

    public void updateCatalog( Properties properties, Archetype archetype, Settings settings )
        throws ArchetypeDataSourceException
    {
        throw new ArchetypeDataSourceException( "Not supported yet." );
    }

    public ArchetypeDataSourceDescriptor getDescriptor()
    {
        ArchetypeDataSourceDescriptor d = new ArchetypeDataSourceDescriptor();

        d.addParameter( URL, String.class, DEFAULT_ARCHETYPE_INVENTORY_PAGE, "The URL of the Wiki page which contains the Archetype information." );

        return d;
    }
}
