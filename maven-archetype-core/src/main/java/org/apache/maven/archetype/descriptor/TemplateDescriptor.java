package org.apache.maven.archetype.descriptor;

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

import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Contains the attributes of an archetype's template (either a source or resource file).
 * The attributes indicate if the template should be filtered and it's encoding.
 */
public class TemplateDescriptor
{

    /**
     * Determines if the template should be filtered or not.
     */
    private boolean filtered = true;

    /**
     * Determines the template's encoding.
     */
    private String encoding;

    /**
     * Creates a new instance of <code>TemplateDescriptor<code> that should be filtered
     * and has the default encoding.
     */
    public TemplateDescriptor()
    {
        setFiltered( true );

        setEncoding( getDefaultEncoding() );
    }

    /**
     * Returns the canonical name of the default character encoding of this Java
     * virtual machine.
     *
     * @return the name of the default character encoding.
     */
    private static String getDefaultEncoding()
    {
        String name = System.getProperty( "file.encoding" );

        if ( name == null )
        {
            OutputStreamWriter out = new OutputStreamWriter( System.out );

            name = out.getEncoding();
        }

        name = Charset.forName( name ).name();

        return name;
    }

    /**
     * Returns <code>true</code> if the template should be filtered and
     * <code>false</code> otherwise.
     *
     * @return <code>true</code> if the template should be filtered and
     *         <code>false</code> otherwise.
     */
    public boolean isFiltered()
    {
        return this.filtered;
    }

    /**
     * Defines whether the template should be filtered (processed by Velocity)
     * or not.
     *
     * @param filtered <code>true</code> if it should be processed by Velocity and
     *                 <code>fales</code> otherwise.
     */
    public void setFiltered( boolean filtered )
    {
        this.filtered = filtered;
    }

    /**
     * Returns the name of the  encoding of the template file (e.g.
     * <code>us-ascci</code>, <code>utf-8</code>, <code>iso-8859-1</code>).
     *
     * @return the name of the  encoding of the template file.
     */
    public String getEncoding()
    {
        return this.encoding;
    }

    /**
     * Sets the name of the encoding of the template file.
     *
     * @param encoding New value of property encoding.
     * @throws IllegalCharsetNameException if the given charset name is illegal
     * @throws UnsupportedCharsetException if no support for the named encoding
     *                                     is available in this instance of the Java virtual machine
     */
    public void setEncoding( String encoding )
        throws IllegalCharsetNameException, UnsupportedCharsetException
    {
        Charset.forName( encoding );

        this.encoding = encoding;
    }

}
