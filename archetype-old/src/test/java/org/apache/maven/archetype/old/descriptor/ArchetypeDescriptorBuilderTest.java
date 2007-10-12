package org.apache.maven.archetype.old.descriptor;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: ArchetypeDescriptorBuilderTest.java 390965 2006-04-03 06:55:06Z brett $
 */
public class ArchetypeDescriptorBuilderTest
    extends TestCase
{
    public ArchetypeDescriptorBuilderTest( String str )
    {
        super( str );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest( new ArchetypeDescriptorBuilderTest( "testBuilder" ) );
        suite.addTest( new ArchetypeDescriptorBuilderTest( "testBuild" ) );
        return suite;
    }

    public void testBuilder()
        throws Exception
    {
        String xml = "<archetype>" + "  <id>standard</id>" + "  <sources>" + "    <source>source0</source>" +
            "    <source>source1</source>" + "  </sources>" + "  <resources>" + "    <resource>resource0</resource>" +
            "    <resource>resource1</resource>" + "  </resources>" + "  <testSources>" +
            "    <source>testSource0</source>" + "    <source>testSource1</source>" + "  </testSources>" +
            "  <testResources>" + "    <resource>testResource0</resource>" + "    <resource>testResource1</resource>" +
            "  </testResources>" + "</archetype>";

        ArchetypeDescriptorBuilder builder = new ArchetypeDescriptorBuilder();

        ArchetypeDescriptor descriptor = builder.build( new StringReader( xml ) );

        assertEquals( "standard", descriptor.getId() );

        assertEquals( 2, descriptor.getSources().size() );

        assertEquals( "source0", descriptor.getSources().get( 0 ) );

        assertNotNull( descriptor.getSourceDescriptor( "source0" ) );

        assertEquals( true, descriptor.getSourceDescriptor( "source0" ).isFiltered() );

        assertNotNull( descriptor.getSourceDescriptor( "source0" ).getEncoding() );

        assertEquals( "source1", descriptor.getSources().get( 1 ) );

        assertNotNull( descriptor.getSourceDescriptor( "source1" ) );

        assertEquals( true, descriptor.getSourceDescriptor( "source1" ).isFiltered() );

        assertNotNull( descriptor.getSourceDescriptor( "source1" ).getEncoding() );

        assertEquals( 2, descriptor.getResources().size() );

        assertEquals( "resource0", descriptor.getResources().get( 0 ) );

        assertNotNull( descriptor.getResourceDescriptor( "resource0" ) );

        assertEquals( true, descriptor.getResourceDescriptor( "resource0" ).isFiltered() );

        assertNotNull( descriptor.getResourceDescriptor( "resource0" ).getEncoding() );

        assertEquals( "resource1", descriptor.getResources().get( 1 ) );

        assertNotNull( descriptor.getResourceDescriptor( "resource1" ) );

        assertEquals( true, descriptor.getResourceDescriptor( "resource1" ).isFiltered() );

        assertNotNull( descriptor.getResourceDescriptor( "resource1" ).getEncoding() );

        assertEquals( 2, descriptor.getTestSources().size() );

        assertEquals( "testSource0", descriptor.getTestSources().get( 0 ) );

        assertNotNull( descriptor.getTestSourceDescriptor( "testSource0" ) );

        assertEquals( true, descriptor.getTestSourceDescriptor( "testSource0" ).isFiltered() );

        assertNotNull( descriptor.getTestSourceDescriptor( "testSource0" ).getEncoding() );

        assertEquals( "testSource1", descriptor.getTestSources().get( 1 ) );

        assertNotNull( descriptor.getTestSourceDescriptor( "testSource1" ) );

        assertEquals( true, descriptor.getTestSourceDescriptor( "testSource1" ).isFiltered() );

        assertNotNull( descriptor.getTestSourceDescriptor( "testSource1" ).getEncoding() );

        assertEquals( 2, descriptor.getTestResources().size() );

        assertEquals( "testResource0", descriptor.getTestResources().get( 0 ) );

        assertNotNull( descriptor.getTestResourceDescriptor( "testResource0" ) );

        assertEquals( true, descriptor.getTestResourceDescriptor( "testResource0" ).isFiltered() );

        assertNotNull( descriptor.getTestResourceDescriptor( "testResource0" ).getEncoding() );

        assertEquals( "testResource1", descriptor.getTestResources().get( 1 ) );

        assertNotNull( descriptor.getTestResourceDescriptor( "testResource1" ) );

        assertEquals( true, descriptor.getTestResourceDescriptor( "testResource1" ).isFiltered() );

        assertNotNull( descriptor.getTestResourceDescriptor( "testResource1" ).getEncoding() );
    }

    public void testBuild()
        throws IOException, XmlPullParserException
    {
        String xml = "<archetype>" + "  <id>standard</id>" + "  <sources>" +
            "    <source encoding=\"utf-8\">source0</source>" + "    <source encoding=\"utf-8\">source1</source>" +
            "  </sources>" + "  <resources>" + "    <resource filtered=\"false\">resource0</resource>" +
            "    <resource encoding=\"iso-8859-1\">resource1</resource>" + "  </resources>" + "  <testSources>" +
            "    <source encoding=\"utf-8\">testSource0</source>" +
            "    <source encoding=\"utf-8\">testSource1</source>" + "  </testSources>" + "  <testResources>" +
            "    <resource encoding=\"us-ascii\">testResource0</resource>" +
            "    <resource filtered=\"false\">testResource1</resource>" + "  </testResources>" + "  <siteResources>" +
            "    <resource filtered=\"false\">siteResource0</resource>" +
            "    <resource encoding=\"utf-16\">siteResource1</resource>" + "  </siteResources>" + "</archetype>";

        ArchetypeDescriptorBuilder builder = new ArchetypeDescriptorBuilder();

        ArchetypeDescriptor descriptor = builder.build( new StringReader( xml ) );

        assertEquals( "standard", descriptor.getId() );

        assertEquals( 2, descriptor.getSources().size() );

        assertEquals( "source0", descriptor.getSources().get( 0 ) );

        assertNotNull( descriptor.getSourceDescriptor( "source0" ) );

        assertEquals( true, descriptor.getSourceDescriptor( "source0" ).isFiltered() );

        assertEquals( "utf-8", descriptor.getSourceDescriptor( "source0" ).getEncoding() );

        assertEquals( "source1", descriptor.getSources().get( 1 ) );

        assertNotNull( descriptor.getSourceDescriptor( "source1" ) );

        assertEquals( true, descriptor.getSourceDescriptor( "source1" ).isFiltered() );

        assertEquals( "utf-8", descriptor.getSourceDescriptor( "source1" ).getEncoding() );

        assertEquals( 2, descriptor.getResources().size() );

        assertEquals( "resource0", descriptor.getResources().get( 0 ) );

        assertNotNull( descriptor.getResourceDescriptor( "resource0" ) );

        assertEquals( false, descriptor.getResourceDescriptor( "resource0" ).isFiltered() );

        assertNotNull( descriptor.getResourceDescriptor( "resource0" ).getEncoding() );

        assertEquals( "resource1", descriptor.getResources().get( 1 ) );

        assertNotNull( descriptor.getResourceDescriptor( "resource1" ) );

        assertEquals( true, descriptor.getResourceDescriptor( "resource1" ).isFiltered() );

        assertEquals( "iso-8859-1", descriptor.getResourceDescriptor( "resource1" ).getEncoding() );

        assertEquals( 2, descriptor.getTestSources().size() );

        assertEquals( "testSource0", descriptor.getTestSources().get( 0 ) );

        assertNotNull( descriptor.getTestSourceDescriptor( "testSource0" ) );

        assertEquals( true, descriptor.getTestSourceDescriptor( "testSource0" ).isFiltered() );

        assertEquals( "utf-8", descriptor.getTestSourceDescriptor( "testSource0" ).getEncoding() );

        assertEquals( "testSource1", descriptor.getTestSources().get( 1 ) );

        assertNotNull( descriptor.getTestSourceDescriptor( "testSource1" ) );

        assertEquals( true, descriptor.getTestSourceDescriptor( "testSource1" ).isFiltered() );

        assertEquals( "utf-8", descriptor.getTestSourceDescriptor( "testSource1" ).getEncoding() );

        assertEquals( 2, descriptor.getTestResources().size() );

        assertEquals( "testResource0", descriptor.getTestResources().get( 0 ) );

        assertNotNull( descriptor.getTestResourceDescriptor( "testResource0" ) );

        assertEquals( true, descriptor.getTestResourceDescriptor( "testResource0" ).isFiltered() );

        assertEquals( "us-ascii", descriptor.getTestResourceDescriptor( "testResource0" ).getEncoding() );

        assertEquals( "testResource1", descriptor.getTestResources().get( 1 ) );

        assertNotNull( descriptor.getTestResourceDescriptor( "testResource1" ) );

        assertEquals( false, descriptor.getTestResourceDescriptor( "testResource1" ).isFiltered() );

        assertNotNull( descriptor.getTestResourceDescriptor( "testResource1" ).getEncoding() );

        assertEquals( 2, descriptor.getSiteResources().size() );

        assertEquals( "siteResource0", descriptor.getSiteResources().get( 0 ) );

        assertNotNull( descriptor.getSiteResourceDescriptor( "siteResource0" ) );

        assertEquals( false, descriptor.getSiteResourceDescriptor( "siteResource0" ).isFiltered() );

        assertNotNull( descriptor.getSiteResourceDescriptor( "siteResource0" ).getEncoding() );

        assertEquals( "siteResource1", descriptor.getSiteResources().get( 1 ) );

        assertNotNull( descriptor.getSiteResourceDescriptor( "siteResource1" ) );

        assertEquals( true, descriptor.getSiteResourceDescriptor( "siteResource1" ).isFiltered() );

        assertEquals( "utf-16", descriptor.getSiteResourceDescriptor( "siteResource1" ).getEncoding() );
    }
}
