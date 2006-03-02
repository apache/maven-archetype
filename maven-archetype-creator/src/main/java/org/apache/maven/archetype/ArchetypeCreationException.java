package org.apache.maven.archetype;

/**
 * @author Jason van Zyl
 * @version $Revision:$
 */
public class ArchetypeCreationException
    extends Exception
{
    public ArchetypeCreationException( String message )
    {
        super( message );
    }

    public ArchetypeCreationException( Throwable cause )
    {
        super( cause );
    }

    public ArchetypeCreationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
