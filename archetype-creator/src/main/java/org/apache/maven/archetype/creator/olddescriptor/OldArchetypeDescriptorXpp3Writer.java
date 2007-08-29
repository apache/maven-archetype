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

package org.apache.maven.archetype.creator.olddescriptor;

import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

import java.io.Writer;

import java.util.Iterator;

/**
 * @author  rafale
 */
public class OldArchetypeDescriptorXpp3Writer
{
    private String NAMESPACE;

    public void write ( Writer writer, OldArchetypeDescriptor descriptor )
    throws java.io.IOException
    {
        XmlSerializer serializer = new MXSerializer ();
        serializer.setProperty (
            "http://xmlpull.org/v1/doc/properties.html#serializer-indentation",
            "  "
        );
        serializer.setProperty (
            "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator",
            "\n"
        );
        serializer.setOutput ( writer );
        serializer.startDocument ( "UTF-8", null );
        writeArchetypeDescriptor ( descriptor, "archetype", serializer );
        serializer.endDocument ();
    }

    private void writeArchetypeDescriptor (
        OldArchetypeDescriptor descriptor,
        String tagName,
        XmlSerializer serializer
    )
    throws java.io.IOException
    {
        if ( descriptor != null )
        {
            serializer.startTag ( NAMESPACE, tagName );
            if ( descriptor.getId () != null )
            {
                serializer.startTag ( NAMESPACE, "id" ).text ( descriptor.getId () ).endTag (
                    NAMESPACE,
                    "id"
                );
            }
            if ( ( descriptor.getSources () != null ) && ( descriptor.getSources ().size () > 0 ) )
            {
                serializer.startTag ( NAMESPACE, "sources" );
                for ( Iterator iter = descriptor.getSources ().iterator (); iter.hasNext (); )
                {
                    String source = (String) iter.next ();
                    serializer.startTag ( NAMESPACE, "source" ).text ( source ).endTag (
                        NAMESPACE,
                        "source"
                    );
                }
                serializer.endTag ( NAMESPACE, "sources" );
            }
            if ( ( descriptor.getTestSources () != null )
                && ( descriptor.getTestSources ().size () > 0 )
            )
            {
                serializer.startTag ( NAMESPACE, "testSources" );
                for ( Iterator iter = descriptor.getTestSources ().iterator (); iter.hasNext (); )
                {
                    String source = (String) iter.next ();
                    serializer.startTag ( NAMESPACE, "source" ).text ( source ).endTag (
                        NAMESPACE,
                        "source"
                    );
                }
                serializer.endTag ( NAMESPACE, "testSources" );
            }
            if ( ( descriptor.getResources () != null )
                && ( descriptor.getResources ().size () > 0 )
            )
            {
                serializer.startTag ( NAMESPACE, "resources" );
                for ( Iterator iter = descriptor.getResources ().iterator (); iter.hasNext (); )
                {
                    String source = (String) iter.next ();
                    serializer.startTag ( NAMESPACE, "resource" ).text ( source ).endTag (
                        NAMESPACE,
                        "resource"
                    );
                }
                serializer.endTag ( NAMESPACE, "resources" );
            }
            if ( ( descriptor.getTestResources () != null )
                && ( descriptor.getTestResources ().size () > 0 )
            )
            {
                serializer.startTag ( NAMESPACE, "testResources" );
                for ( Iterator iter = descriptor.getTestResources ().iterator (); iter.hasNext (); )
                {
                    String source = (String) iter.next ();
                    serializer.startTag ( NAMESPACE, "resource" ).text ( source ).endTag (
                        NAMESPACE,
                        "resource"
                    );
                }
                serializer.endTag ( NAMESPACE, "testResources" );
            }
            if ( ( descriptor.getSiteResources () != null )
                && ( descriptor.getSiteResources ().size () > 0 )
            )
            {
                serializer.startTag ( NAMESPACE, "siteResources" );
                for ( Iterator iter = descriptor.getSiteResources ().iterator (); iter.hasNext (); )
                {
                    String source = (String) iter.next ();
                    serializer.startTag ( NAMESPACE, "resource" ).text ( source ).endTag (
                        NAMESPACE,
                        "resource"
                    );
                }
                serializer.endTag ( NAMESPACE, "siteResources" );
            }
            serializer.endTag ( NAMESPACE, tagName );
        } // end if
    }
}
