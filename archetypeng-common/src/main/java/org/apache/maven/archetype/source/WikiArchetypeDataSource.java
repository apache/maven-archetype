package org.apache.maven.archetype.source;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author Jason van Zyl */
public class WikiArchetypeDataSource
    implements ArchetypeDataSource
{
    private static String DEFAULT_ARCHETYPE_INVENTORY_PAGE = "http://docs.codehaus.org/pages/viewpagesrc.action?pageId=48400";

    private String url;

    public WikiArchetypeDataSource()
    {
        this( DEFAULT_ARCHETYPE_INVENTORY_PAGE );
    }

    public WikiArchetypeDataSource( String url )
    {
        this.url = url;
    }

    public Map getArchetypes()
        throws ArchetypeDataSourceException
    {
        Map archetypes = new LinkedHashMap();

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
            org.apache.maven.archetype.registry.Archetype arch = new org.apache.maven.archetype.registry.Archetype();

            arch.setArtifactId( m.group( 1 ).trim() );

            arch.setGroupId( m.group( 2 ).trim() );

            String version = m.group( 3 ).trim();

            if ( version.equals( "" ) )
            {
                version = "RELEASE";
            }

            arch.setVersion( version );

            arch.setRepository( cleanupUrl( m.group( 4 ).trim() ) );

            arch.setDescription( cleanup( m.group( 5 ).trim() ) );

            archetypes.put( arch.getArtifactId(), arch );
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
}
