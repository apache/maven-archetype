package org.apache.maven.archetype.source;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** @author Jason van Zyl */
public class CatalogArchetypeDataSinkTest
    extends PlexusTestCase
{
    public void testCatalogArchetypeDataSink()
        throws Exception
    {
        Archetype a0 = new Archetype();

        a0.setGroupId( "groupId" );

        a0.setArtifactId( "artifactId" );

        a0.setVersion( "1.0" );

        a0.setDescription( "description" );

        a0.setRepository( "http://magicbunny.com/maven2" );

        List archetypes = new ArrayList();

        archetypes.add( a0 );

        ArchetypeDataSink sink = new CatalogArchetypeDataSink();

        Writer writer = new StringWriter();

        sink.putArchetypes( archetypes, writer );

        StringReader reader = new StringReader( writer.toString() );

        ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

        ArchetypeCatalog catalog = catalogReader.read( reader );

        Archetype a1 = (Archetype) catalog.getArchetypes().get( 0 );

        assertEquals( "groupId", a1.getGroupId()  );

        assertEquals( "artifactId", a1.getArtifactId() );

        assertEquals( "1.0", a1.getVersion()  );

        assertEquals( "description", a1.getDescription()  );

        assertEquals( "http://magicbunny.com/maven2", a1.getRepository()  );
    }

    public void testPuttingTheWikiSourceIntoACatalogSink()
        throws Exception
    {
        Writer writer = new StringWriter();

        ArchetypeDataSink sink = new CatalogArchetypeDataSink();

        Properties p = new Properties();

        File wikiSource = new File( getBasedir(), "src/test/sources/wiki/wiki-source.txt" );

        assertTrue( wikiSource.exists() );

        p.setProperty( WikiArchetypeDataSource.URL, wikiSource.toURI().toURL().toExternalForm() );

        ArchetypeDataSource ads = new WikiArchetypeDataSource();

        sink.putArchetypes( ads, p, writer );

        StringReader reader = new StringReader( writer.toString() );

        ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

        ArchetypeCatalog catalog = catalogReader.read( reader );

        int catalogSize = catalog.getArchetypes().size();

        assertEquals( 37, catalogSize );
    }
}
