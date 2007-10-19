package org.apache.maven.archetype.source;

import java.io.Writer;
import java.util.List;

/** @author Jason van Zyl */
public interface ArchetypeDataSink
{
    void putArchetypes( List archetypes, Writer writer )
        throws ArchetypeDataSinkException;
}
