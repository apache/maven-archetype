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

        // (1) create a project from scratch
        // (2) create an archetype from the project
        // (3) create our own archetype catalog properties in memory
        // (4) create our own archetype catalog describing the archetype we just created
        // (5) deploy the archetype we just created         
        // (6) create a project form the archetype we just created
    }
}
