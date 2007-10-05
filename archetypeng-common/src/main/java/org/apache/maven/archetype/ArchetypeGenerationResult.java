package org.apache.maven.archetype;

import org.apache.maven.archetype.exception.ArchetypeGenerationFailure;

/** @author Jason van Zyl */
public class ArchetypeGenerationResult
{
    private Exception cause;

    public Exception getCause( )
    {
        return cause;
    }

    public void setCause( Exception cause )
    {
        this.cause = cause;
    }
}
