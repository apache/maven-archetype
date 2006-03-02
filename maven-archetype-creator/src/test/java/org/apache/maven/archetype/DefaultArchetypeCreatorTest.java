package org.apache.maven.archetype;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;

public class DefaultArchetypeCreatorTest
    extends PlexusTestCase
{
    public void testArchetypeCreator()
        throws Exception
    {
        ArchetypeCreator creator = (ArchetypeCreator) lookup( ArchetypeCreator.ROLE );

        File pom = new File( getBasedir(), "pom.xml" );        
    }
}
