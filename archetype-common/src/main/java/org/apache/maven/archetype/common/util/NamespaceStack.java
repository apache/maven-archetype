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

import org.jdom.Namespace;

import java.util.Stack;

/**
 * A non-public utility class used by both <code>{@link XMLOutputter}</code> and
 * <code>{@link SAXOutputter}</code> to manage namespaces in a JDOM Document
 * during output.
 *
 * @author Elliotte Rusty Harolde
 * @author Fred Trimble
 * @author Brett McLaughlin
 * @version $Revision: 1.13 $, $Date: 2004/02/06 09:28:32 $
 */
class NamespaceStack
{

    private static final String CVS_ID =
        "@(#) $RCSfile: NamespaceStack.java,v $ $Revision: 1.13 $ $Date: 2004/02/06 09:28:32 $ $Name: jdom_1_0 $";

    /** The prefixes available */
    private Stack prefixes;

    /** The URIs available */
    private Stack uris;

    /** This creates the needed storage. */
    NamespaceStack()
    {
        prefixes = new Stack();
        uris = new Stack();
    }

    /**
     * This will add a new <code>{@link Namespace}</code>
     * to those currently available.
     *
     * @param ns <code>Namespace</code> to add.
     */
    public void push( Namespace ns )
    {
        prefixes.push( ns.getPrefix() );
        uris.push( ns.getURI() );
    }

    /**
     * This will remove the topmost (most recently added)
     * <code>{@link Namespace}</code>, and return its prefix.
     *
     * @return <code>String</code> - the popped namespace prefix.
     */
    public String pop()
    {
        String prefix = (String) prefixes.pop();
        uris.pop();

        return prefix;
    }

    /**
     * This returns the number of available namespaces.
     *
     * @return <code>int</code> - size of the namespace stack.
     */
    public int size()
    {
        return prefixes.size();
    }

    /**
     * Given a prefix, this will return the namespace URI most
     * rencently (topmost) associated with that prefix.
     *
     * @param prefix <code>String</code> namespace prefix.
     * @return <code>String</code> - the namespace URI for that prefix.
     */
    public String getURI( String prefix )
    {
        int index = prefixes.lastIndexOf( prefix );
        if ( index == -1 )
        {
            return null;
        }
        String uri = (String) uris.elementAt( index );
        return uri;
    }

    /**
     * This will print out the size and current stack, from the
     * most recently added <code>{@link Namespace}</code> to
     * the "oldest," all to <code>System.out</code>.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        String sep = System.getProperty( "line.separator" );
        buf.append( "Stack: " + prefixes.size() + sep );
        for ( int i = 0; i < prefixes.size(); i++ )
        {
            buf.append( prefixes.elementAt( i ) + "&" + uris.elementAt( i ) + sep );
        }
        return buf.toString();
    }
}

