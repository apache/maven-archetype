package org.apache.maven.archetype;

import org.codehaus.plexus.PlexusTestCase;

/** @author Jason van Zyl */
public class ArchetyperTest
    extends PlexusTestCase
{
    public void testArchetyper()
        throws Exception
    {
        Archetyper archetype = (Archetyper) lookup( Archetyper.ROLE );
    }
}
