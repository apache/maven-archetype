package org.apache.maven.archetype.old.descriptor;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.codehaus.plexus.PlexusTestCase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class ArchetypeXsdTest
    extends PlexusTestCase
{
    private static final String ARCHETYPE_XSD = "archetype-1.0.0.xsd";

    public void testXsd()
        throws Exception
    {
        File archetypeXsd = new File( getBasedir(), "/target/generated-site/resources/xsd/" + ARCHETYPE_XSD );

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating( true );
        factory.setNamespaceAware( true );
        SAXParser saxParser = factory.newSAXParser();
        saxParser.setProperty( "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                               "http://www.w3.org/2001/XMLSchema" );
        saxParser.setProperty( "http://java.sun.com/xml/jaxp/properties/schemaSource", archetypeXsd );

        try ( InputStream in = getClass().getResourceAsStream( "sample-archetype.xml" ); )
        {
            saxParser.parse( new InputSource( in ), new Handler() );
        }
    }

    private static class Handler
        extends DefaultHandler
    {
        @Override
        public void warning ( SAXParseException e )
            throws SAXException
        {
            throw e;
        }

        @Override
        public void error ( SAXParseException e )
            throws SAXException
        {
            throw e;
        }
    }
}
