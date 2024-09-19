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

/*
 * Copyright (C) 2000-2004 Jason Hunter & Brett McLaughlin.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions, and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions, and the disclaimer that follows
 *    these conditions in the documentation and/or other materials
 *    provided with the distribution.
 *
 * 3. The name "JDOM" must not be used to endorse or promote products
 *    derived from this software without prior written permission.  For
 *    written permission, please contact <request_AT_jdom_DOT_org>.
 *
 * 4. Products derived from this software may not be called "JDOM", nor
 *    may "JDOM" appear in their name, without prior written permission
 *    from the JDOM Project Management <request_AT_jdom_DOT_org>.
 *
 * In addition, we request (but do not require) that you include in the
 * end-user documentation provided with the redistribution and/or in the
 * software itself an acknowledgement equivalent to the following:
 *     "This product includes software developed by the
 *      JDOM Project (http://www.jdom.org/)."
 * Alternatively, the acknowledgment may be graphical using the logos
 * available at http://www.jdom.org/images/logos.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JDOM Project and was originally
 * created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 * Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 * on the JDOM Project, please see <http://www.jdom.org/>.
 */

import java.util.Stack;

import org.jdom2.Namespace;

/**
 * A non-public utility class used by <code>{@link XMLOutputter}</code>
 * to manage namespaces in a JDOM Document during output.
 *
 * @author Elliotte Rusty Harolde
 * @author Fred Trimble
 * @author Brett McLaughlin
 */
class NamespaceStack {
    /** The prefixes available */
    private Stack<String> prefixes;

    /** The URIs available */
    private Stack<String> uris;

    /** This creates the needed storage. */
    NamespaceStack() {
        prefixes = new Stack<>();
        uris = new Stack<>();
    }

    /**
     * This will add a new <code>{@link Namespace}</code>
     * to those currently available.
     *
     * @param ns <code>Namespace</code> to add.
     */
    public void push(Namespace ns) {
        prefixes.push(ns.getPrefix());
        uris.push(ns.getURI());
    }

    /**
     * This will remove the topmost (most recently added)
     * <code>{@link Namespace}</code>, and return its prefix.
     *
     * @return <code>String</code> - the popped namespace prefix.
     */
    public String pop() {
        String prefix = prefixes.pop();
        uris.pop();

        return prefix;
    }

    /**
     * This returns the number of available namespaces.
     *
     * @return <code>int</code> - size of the namespace stack.
     */
    public int size() {
        return prefixes.size();
    }

    /**
     * Given a prefix, this will return the namespace URI most
     * rencently (topmost) associated with that prefix.
     *
     * @param prefix <code>String</code> namespace prefix.
     * @return <code>String</code> - the namespace URI for that prefix.
     */
    public String getURI(String prefix) {
        int index = prefixes.lastIndexOf(prefix);
        if (index == -1) {
            return null;
        }
        return uris.elementAt(index);
    }

    /**
     * This will print out the size and current stack, from the
     * most recently added <code>{@link Namespace}</code> to
     * the "oldest," all to <code>System.out</code>.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        String sep = System.lineSeparator();
        buf.append("Stack: " + prefixes.size() + sep);
        for (int i = 0; i < prefixes.size(); i++) {
            buf.append(prefixes.elementAt(i) + "&" + uris.elementAt(i) + sep);
        }
        return buf.toString();
    }
}
