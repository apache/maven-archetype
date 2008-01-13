package org.apache.maven.archetype.source;

/** @author Jason van Zyl */
public class ArchetypeDataSinkException
    extends Exception
{
    public ArchetypeDataSinkException( String s )
    {
        super( s );
    }

    public ArchetypeDataSinkException( String s,
                                       Throwable throwable )
    {
        super( s, throwable );
    }

    public ArchetypeDataSinkException( Throwable throwable )
    {
        super( throwable );
    }
}
