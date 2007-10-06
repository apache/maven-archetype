package org.apache.maven.archetype;

/** @author Jason van Zyl */
public class ArchetypeCreationResult
{
    private Exception cause;

    public Exception getCause()
    {
        return cause;
    }

    public void setCause( Exception cause )
    {
        this.cause = cause;
    }
}
