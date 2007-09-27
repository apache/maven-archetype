package org.apache.maven.archetype.source;

/** @author Jason van Zyl */
public class ArchetypeDataSourceException
    extends Exception
{
    public ArchetypeDataSourceException( String s )
    {
        super( s );
    }

    public ArchetypeDataSourceException( String s,
                                         Throwable throwable )
    {
        super( s, throwable );
    }

    public ArchetypeDataSourceException( Throwable throwable )
    {
        super( throwable );
    }
}
