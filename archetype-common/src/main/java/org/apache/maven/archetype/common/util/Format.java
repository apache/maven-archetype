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

import java.lang.reflect.Method;

import org.jdom2.output.EscapeStrategy;

/**
 * <p>Class to encapsulate XMLOutputter format options.
 * Typical users can use the standard format configurations obtained by
 * {@link #getRawFormat} (no whitespace changes),
 * {@link #getPrettyFormat} (whitespace beautification), and
 * {@link #getCompactFormat} (whitespace normalization).</p>
 *
 * <p>Several modes are available to effect the way textual content is printed.
 * See the documentation for {@link TextMode} for details.</p>
 *
 * @author Jason Hunter
 */
public class Format implements Cloneable {

    /**
     * Returns a new Format object that performs no whitespace changes, uses
     * the UTF-8 encoding, doesn't expand empty elements, includes the
     * declaration and encoding, and uses the default entity escape strategy.
     * Tweaks can be made to the returned Format instance without affecting
     * other instances.
     *
     * @return a Format with no whitespace changes
     */
    public static Format getRawFormat() {
        return new Format();
    }

    /**
     * Returns a new Format object that performs whitespace beautification with
     * 2-space indents, uses the UTF-8 encoding, doesn't expand empty elements,
     * includes the declaration and encoding, and uses the default entity
     * escape strategy.
     * Tweaks can be made to the returned Format instance without affecting
     * other instances.
     *
     * @return a Format with whitespace beautification
     */
    public static Format getPrettyFormat() {
        Format f = new Format();
        f.setIndent(STANDARD_INDENT);
        f.setTextMode(TextMode.TRIM);
        return f;
    }

    /**
     * Returns a new Format object that performs whitespace normalization, uses
     * the UTF-8 encoding, doesn't expand empty elements, includes the
     * declaration and encoding, and uses the default entity escape strategy.
     * Tweaks can be made to the returned Format instance without affecting
     * other instances.
     *
     * @return a Format with whitespace normalization
     */
    public static Format getCompactFormat() {
        Format f = new Format();
        f.setTextMode(TextMode.NORMALIZE);
        return f;
    }

    /** standard value to indent by, if we are indenting */
    private static final String STANDARD_INDENT = "  ";

    /** standard string with which to end a line */
    private static final String STANDARD_LINE_SEPARATOR = "\r\n";

    /** standard encoding */
    private static final String STANDARD_ENCODING = "UTF-8";

    /** The default indent is no spaces (as original document) */
    String indent = null;

    /** New line separator */
    String lineSeparator = STANDARD_LINE_SEPARATOR;

    /** The encoding format */
    String encoding = STANDARD_ENCODING;

    /**
     * Whether or not to output the XML declaration
     * - default is <code>false</code>
     */
    boolean omitDeclaration = false;

    /**
     * Whether or not to output the encoding in the XML declaration
     * - default is <code>false</code>
     */
    boolean omitEncoding = false;

    /**
     * Whether or not to expand empty elements to
     * &lt;tagName&gt;&lt;/tagName&gt; - default is <code>false</code>
     */
    boolean expandEmptyElements = false;

    /**
     * Whether TrAX output escaping disabling/enabling PIs are ignored
     * or processed - default is <code>false</code>
     */
    boolean ignoreTrAXEscapingPIs = false;

    /** text handling mode */
    TextMode mode = TextMode.PRESERVE;

    /** entity escape logic */
    EscapeStrategy escapeStrategy = new DefaultEscapeStrategy(encoding);

    /** Creates a new Format instance with default (raw) behavior. */
    private Format() {}

    /**
     * Sets the {@link EscapeStrategy} to use for character escaping.
     *
     * @param strategy the EscapeStrategy to use
     * @return a pointer to this Format for chaining
     */
    public Format setEscapeStrategy(EscapeStrategy strategy) {
        escapeStrategy = strategy;
        return this;
    }

    /**
     * Returns the current escape strategy
     *
     * @return the current escape strategy
     */
    public EscapeStrategy getEscapeStrategy() {
        return escapeStrategy;
    }

    /**
     * <p>This will set the newline separator (<code>lineSeparator</code>).
     * The default is <code>\r\n</code>. Note that if the "newlines"
     * property is false, this value is irrelevant.  To make it output
     * the system default line ending string, call
     * <code>setLineSeparator(System.getProperty("line.separator"))</code></p>
     *
     * <p>To output "UNIX-style" documents, call
     * <code>setLineSeparator("\n")</code>.  To output "Mac-style"
     * documents, call <code>setLineSeparator("\r")</code>.  DOS-style
     * documents use CR-LF ("\r\n"), which is the default.</p>
     *
     * <p>Note that this only applies to newlines generated by the
     * outputter.  If you parse an XML document that contains newlines
     * embedded inside a text node, and you do not set TextMode.NORMALIZE,
     * then the newlines will be output
     * verbatim, as "\n" which is how parsers normalize them.
     * </p>
     *
     * @param separator <code>String</code> line separator to use.
     * @return a pointer to this Format for chaining
     * @see #setTextMode
     */
    public Format setLineSeparator(String separator) {
        this.lineSeparator = separator;
        return this;
    }

    /**
     * Returns the current line separator.
     *
     * @return the current line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * This will set whether the XML declaration
     * (<code>&lt;&#063;xml version="1&#046;0"
     * encoding="UTF-8"&#063;&gt;</code>)
     * includes the encoding of the document. It is common to omit
     * this in uses such as WML and other wireless device protocols.
     *
     * @param omitEncoding <code>boolean</code> indicating whether or not
     *                     the XML declaration should indicate the document encoding.
     * @return a pointer to this Format for chaining
     */
    public Format setOmitEncoding(boolean omitEncoding) {
        this.omitEncoding = omitEncoding;
        return this;
    }

    /**
     * Returns whether the XML declaration encoding will be omitted.
     *
     * @return whether the XML declaration encoding will be omitted
     */
    public boolean getOmitEncoding() {
        return omitEncoding;
    }

    /**
     * This will set whether the XML declaration
     * (<code>&lt;&#063;xml version="1&#046;0"&#063;gt;</code>)
     * will be omitted or not. It is common to omit this in uses such
     * as SOAP and XML-RPC calls.
     *
     * @param omitDeclaration <code>boolean</code> indicating whether or not
     *                        the XML declaration should be omitted.
     * @return a pointer to this Format for chaining
     */
    public Format setOmitDeclaration(boolean omitDeclaration) {
        this.omitDeclaration = omitDeclaration;
        return this;
    }

    /**
     * Returns whether the XML declaration will be omitted.
     *
     * @return whether the XML declaration will be omitted
     */
    public boolean getOmitDeclaration() {
        return omitDeclaration;
    }

    /**
     * This will set whether empty elements are expanded from
     * <code>&lt;tagName/&gt;</code> to
     * <code>&lt;tagName&gt;&lt;/tagName&gt;</code>.
     *
     * @param expandEmptyElements <code>boolean</code> indicating whether or not
     *                            empty elements should be expanded.
     * @return a pointer to this Format for chaining
     */
    public Format setExpandEmptyElements(boolean expandEmptyElements) {
        this.expandEmptyElements = expandEmptyElements;
        return this;
    }

    /**
     * Returns whether empty elements are expanded.
     *
     * @return whether empty elements are expanded
     */
    public boolean getExpandEmptyElements() {
        return expandEmptyElements;
    }

    /**
     * <p>This will set whether JAXP TrAX processing instructions for
     * disabling/enabling output escaping are ignored.  Disabling
     * output escaping allows using XML text as element content and
     * outputing it verbatim, i&#46;e&#46; as element children would be.</p>
     *
     * <p>When processed, these processing instructions are removed from
     * the generated XML text and control whether the element text
     * content is output verbatim or with escaping of the pre-defined
     * entities in XML 1.0.  The text to be output verbatim shall be
     * surrounded by the
     * <code>&lt;?javax.xml.transform.disable-output-escaping ?&gt;</code>
     * and <code>&lt;?javax.xml.transform.enable-output-escaping ?&gt;</code>
     * PIs.</p>
     *
     * <p>When ignored, the processing instructions are present in the
     * generated XML text and the pre-defined entities in XML 1.0 are
     * escaped.</p>
     *
     * Default: <code>false</code>.
     *
     * @param ignoreTrAXEscapingPIs <code>boolean</code> indicating
     *                              whether or not TrAX ouput escaping PIs are ignored.
     * @see javax.xml.transform.Result#PI_ENABLE_OUTPUT_ESCAPING
     * @see javax.xml.transform.Result#PI_DISABLE_OUTPUT_ESCAPING
     */
    public void setIgnoreTrAXEscapingPIs(boolean ignoreTrAXEscapingPIs) {
        this.ignoreTrAXEscapingPIs = ignoreTrAXEscapingPIs;
    }

    /**
     * Returns whether JAXP TrAX processing instructions for
     * disabling/enabling output escaping are ignored.
     *
     * @return whether or not TrAX ouput escaping PIs are ignored.
     */
    public boolean getIgnoreTrAXEscapingPIs() {
        return ignoreTrAXEscapingPIs;
    }

    /**
     * This sets the text output style.  Options are available as static
     * {@link TextMode} instances.  The default is {@link TextMode#PRESERVE}.
     *
     * @return a pointer to this Format for chaining
     */
    public Format setTextMode(Format.TextMode mode) {
        this.mode = mode;
        return this;
    }

    /**
     * Returns the current text output style.
     *
     * @return the current text output style
     */
    public Format.TextMode getTextMode() {
        return mode;
    }

    /**
     * This will set the indent <code>String</code> to use; this
     * is usually a <code>String</code> of empty spaces. If you pass
     * null, or the empty string (""), then no indentation will
     * happen.  Default: none (null)
     *
     * @param indent <code>String</code> to use for indentation.
     * @return a pointer to this Format for chaining
     */
    public Format setIndent(String indent) {
        // if passed the empty string, change it to null, for marginal
        // performance gains later (can compare to null first instead
        // of calling equals())
        if ("".equals(indent)) {
            indent = null;
        }
        this.indent = indent;
        return this;
    }

    /**
     * Returns the indent string in use.
     *
     * @return the indent string in use
     */
    public String getIndent() {
        return indent;
    }

    /**
     * Sets the output encoding.  The name should be an accepted XML
     * encoding.
     *
     * @param encoding the encoding format.  Use XML-style names like
     *                 "UTF-8" or "ISO-8859-1" or "US-ASCII"
     * @return a pointer to this Format for chaining
     */
    public Format setEncoding(String encoding) {
        this.encoding = encoding;
        escapeStrategy = new DefaultEscapeStrategy(encoding);
        return this;
    }

    /**
     * Returns the configured output encoding.
     *
     * @return the output encoding
     */
    public String getEncoding() {
        return encoding;
    }

    @Override
    protected Object clone() {
        Format format = null;

        try {
            format = (Format) super.clone();
        } catch (CloneNotSupportedException ce) {
        }

        return format;
    }

    /**
     * Handle common charsets quickly and easily.  Use reflection
     * to query the JDK 1.4 CharsetEncoder class for unknown charsets.
     * If JDK 1.4 isn't around, default to no special encoding.
     */
    class DefaultEscapeStrategy implements EscapeStrategy {
        private int bits;
        Object encoder;
        Method canEncode;

        public DefaultEscapeStrategy(String encoding) {
            if ("UTF-8".equalsIgnoreCase(encoding) || "UTF-16".equalsIgnoreCase(encoding)) {
                bits = 16;
            } else if ("ISO-8859-1".equalsIgnoreCase(encoding) || "Latin1".equalsIgnoreCase(encoding)) {
                bits = 8;
            } else if ("US-ASCII".equalsIgnoreCase(encoding) || "ASCII".equalsIgnoreCase(encoding)) {
                bits = 7;
            } else {
                bits = 0;
                // encoder = Charset.forName(encoding).newEncoder();
                try {
                    Class<?> charsetClass = Class.forName("java.nio.charset.Charset");
                    Class<?> encoderClass = Class.forName("java.nio.charset.CharsetEncoder");
                    Method forName = charsetClass.getMethod("forName", new Class[] {String.class});
                    Object charsetObj = forName.invoke(null, new Object[] {encoding});
                    Method newEncoder = charsetClass.getMethod("newEncoder");
                    encoder = newEncoder.invoke(charsetObj);
                    canEncode = encoderClass.getMethod("canEncode", new Class[] {char.class});
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public boolean shouldEscape(char ch) {
            if (bits == 16) {
                return false;
            }
            if (bits == 8) {
                return (ch > 255);
            }
            if (bits == 7) {
                return (ch > 127);
            } else {
                if (canEncode != null && encoder != null) {
                    try {
                        Boolean val = (Boolean) canEncode.invoke(encoder, new Object[] {Character.valueOf(ch)});
                        return !val.booleanValue();
                    } catch (Exception ignored) {
                    }
                }
                // Return false if we don't know.  This risks not escaping
                // things which should be escaped, but also means people won't
                // start getting loads of unnecessary escapes.
                return false;
            }
        }
    }

    /**
     * <p>
     * Class to signify how text should be handled on output.  The following
     * table provides details.</p>
     * <table>
     * <caption>TextMode details</caption>
     * <tr>
     * <th>
     * Text Mode
     * </th>
     * <th>
     * Resulting behavior.
     * </th>
     * </tr>
     * <tr>
     * <td>
     * <i>PRESERVE (Default)</i>
     * </td>
     * <td>
     * All content is printed in the format it was created, no whitespace
     * or line separators are are added or removed.
     * </td>
     * </tr>
     * <tr>
     * <td>
     * TRIM_FULL_WHITE
     * </td>
     * <td>
     * Content between tags consisting of all whitespace is not printed.
     * If the content contains even one non-whitespace character, it is
     * printed verbatim, whitespace and all.
     * </td>
     * </tr>
     * <tr>
     * <td>
     * TRIM
     * </td>
     * <td>
     * Same as TrimAllWhite, plus leading/trailing whitespace are
     * trimmed.
     * </td>
     * </tr>
     * <tr>
     * <td>
     * NORMALIZE
     * </td>
     * <td>
     * Same as TextTrim, plus addition interior whitespace is compressed
     * to a single space.
     * </td>
     * </tr>
     * </table>
     *
     * <p>In most cases textual content is aligned with the surrounding tags
     * (after the appropriate text mode is applied). In the case where the only
     * content between the start and end tags is textual, the start tag, text,
     * and end tag are all printed on the same line. If the document being
     * output already has whitespace, it's wise to turn on TRIM mode so the
     * pre-existing whitespace can be trimmed before adding new whitespace.</p>
     *
     * <p>When a element has a xml:space attribute with the value of "preserve",
     * all formating is turned off and reverts back to the default until the
     * element and its contents have been printed. If a nested element contains
     * another xml:space with the value "default" formatting is turned back on
     * for the child element and then off for the remainder of the parent
     * element.</p>
     */
    public static class TextMode {
        /** Mode for literal text preservation. */
        public static final TextMode PRESERVE = new TextMode("PRESERVE");

        /** Mode for text trimming (left and right trim). */
        public static final TextMode TRIM = new TextMode("TRIM");

        /**
         * Mode for text normalization (left and right trim plus internal
         * whitespace is normalized to a single space.
         *
         * @see org.jdom2.Element#getTextNormalize
         */
        public static final TextMode NORMALIZE = new TextMode("NORMALIZE");

        /**
         * Mode for text trimming of content consisting of nothing but
         * whitespace but otherwise not changing output.
         */
        public static final TextMode TRIM_FULL_WHITE = new TextMode("TRIM_FULL_WHITE");

        private final String name;

        private TextMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
