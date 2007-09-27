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

package org.apache.maven.archetype.common.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;


import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/** @author <a href="mailto:jdcasey@apache.org">John Casey</a> */
public class TestXMLOutputter
    extends TestCase
{
    public void testOutput_Document_ShouldParseAndOutputCDATASection_NoMods()
        throws
        JDOMException,
        IOException
    {
        String content =
            "<document>" +
                "<element1>This is some text</element1>" +
                "<description>And then," +
                "<![CDATA[<more content goes here>]]>" +
                "</description><!--somecomment-->" +
                "</document>";

        Document doc = new SAXBuilder().build( new StringReader( content ) );

        StringWriter sw = new StringWriter();
        new XMLOutputter().output( doc, sw );

        System.out.println( "Resulting content is:\n\n\'" + sw.toString() + "\'\n\n" );

        assertTrue( sw.toString().indexOf( content ) > -1 );
    }
}
