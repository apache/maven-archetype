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

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: ArchetypeDescriptorBuilder.java 390965 2006-04-03 06:55:06Z brett $
 */
public class ArchetypeDescriptorBuilder
{
    public ArchetypeDescriptor build( Reader reader )
        throws IOException, XmlPullParserException
    {
        ArchetypeDescriptor descriptor = new ArchetypeDescriptor();

        Xpp3Dom dom = Xpp3DomBuilder.build( reader );

        descriptor.setId( dom.getChild( "id" ).getValue() );

        Xpp3Dom allowPartialDom = dom.getChild( "allowPartial" );

        if ( allowPartialDom != null )
        {
            String allowPartial = allowPartialDom.getValue();

            if ( "true".equals( allowPartial ) || "1".equals( allowPartial ) || "on".equals( allowPartial ) )
            {
                descriptor.setAllowPartial( true );
            }
        }

        // ----------------------------------------------------------------------
        // Main
        // ----------------------------------------------------------------------

        Xpp3Dom sources = dom.getChild( "sources" );

        if ( sources != null )
        {
            Xpp3Dom[] sourceList = sources.getChildren( "source" );

            for ( int i = 0; i < sourceList.length; i++ )
            {
                addSourceToDescriptor( sourceList[i], descriptor );
            }
        }

        Xpp3Dom resources = dom.getChild( "resources" );

        if ( resources != null )
        {
            Xpp3Dom[] resourceList = resources.getChildren( "resource" );

            for ( int i = 0; i < resourceList.length; i++ )
            {
                addResourceToDescriptor( resourceList[i], descriptor );
            }
        }

        // ----------------------------------------------------------------------
        // Test
        // ----------------------------------------------------------------------

        Xpp3Dom testSources = dom.getChild( "testSources" );

        if ( testSources != null )
        {
            Xpp3Dom[] testSourceList = testSources.getChildren( "source" );

            for ( int i = 0; i < testSourceList.length; i++ )
            {
                addTestSourceToDescriptor( testSourceList[i], descriptor );
            }
        }

        Xpp3Dom testResources = dom.getChild( "testResources" );

        if ( testResources != null )
        {
            Xpp3Dom[] testResourceList = testResources.getChildren( "resource" );

            for ( int i = 0; i < testResourceList.length; i++ )
            {
                addTestResourceToDescriptor( testResourceList[i], descriptor );
            }
        }

        // ----------------------------------------------------------------------
        // Site
        // ----------------------------------------------------------------------

        Xpp3Dom siteResources = dom.getChild( "siteResources" );

        if ( siteResources != null )
        {
            Xpp3Dom[] siteResourceList = siteResources.getChildren( "resource" );

            for ( int i = 0; i < siteResourceList.length; i++ )
            {
                addSiteResourceToDescriptor( siteResourceList[i], descriptor );
            }
        }

        return descriptor;
    }

    /**
     * Adds the source element <code>source</code> to the list of sources in the
     * <code>descriptor</code> and sets its <code>TemplateDescriptor</code> to
     * <i>filtered</i> and with the encoding specified in the <code>encoding</code>
     * attribute or the Java virtual machine's default if it is not defined.
     *
     * @param source     a <code>&lt;source&gt;</code> element from the <code>&lt;sources&gt;</code>
     * @param descriptor the <code>ArchetypeDescriptor</code> to add the source template to.
     * @throws XmlPullParserException if the encoding specified is not valid or supported.
     */
    private static void addSourceToDescriptor( Xpp3Dom source, ArchetypeDescriptor descriptor )
        throws XmlPullParserException
    {
        descriptor.addSource( source.getValue() );

        TemplateDescriptor sourceDesc = descriptor.getSourceDescriptor( source.getValue() );

        sourceDesc.setFiltered( true );

        if ( source.getAttribute( "encoding" ) != null )
        {
            try
            {
                sourceDesc.setEncoding( source.getAttribute( "encoding" ) );
            }
            catch ( IllegalCharsetNameException icne )
            {
                throw new XmlPullParserException( source.getAttribute( "encoding" ) + " is not a valid encoding." );
            }
            catch ( UnsupportedCharsetException uce )
            {
                throw new XmlPullParserException( source.getAttribute( "encoding" ) + " is not a supported encoding." );
            }
        }
    }

    /**
     * Adds the resource element <code>resource</code> to the list of resources in the
     * <code>descriptor</code> and sets its <code>TemplateDescriptor</code> to
     * <i>filtered</i> if the attribute <code>filtered</code> was not
     * specified or its value is <code>&quot;true&quot;</code>, or <code>false</code>
     * if its value is <code>&quot;false&quot;</code>, and the encoding specified
     * in the <code>encoding</code> attribute or the Java virtual machine's default if
     * it is not defined. If the <code>resource</code> is a property file (ends in
     * <code>.properties</code>) its encoding will be set to <code>iso-8859-1</code>
     * even if some other encoding is specified in the attribute.
     *
     * @param resource   a <code>&lt;resource&gt;</code> element from the <code>&lt;resources&gt;</code>
     * @param descriptor the <code>ArchetypeDescriptor</code> to add the resource template to.
     * @throws XmlPullParserException if the encoding specified is not valid or supported or if the
     *                                value of the attribute <code>filtered</code> is no valid.
     */
    private static void addResourceToDescriptor( Xpp3Dom resource, ArchetypeDescriptor descriptor )
        throws XmlPullParserException
    {
        descriptor.addResource( resource.getValue() );

        if ( resource.getAttribute( "filtered" ) != null )
        {
            TemplateDescriptor resourceDesc = descriptor.getResourceDescriptor( resource.getValue() );

            try
            {
                resourceDesc.setFiltered( getValueFilteredAttribute( resource.getAttribute( "filtered" ) ) );
            }
            catch ( IllegalArgumentException iae )
            {
                throw new XmlPullParserException( iae.getMessage() );
            }
        }

        if ( resource.getAttribute( "encoding" ) != null )
        {
            TemplateDescriptor resourceDesc = descriptor.getResourceDescriptor( resource.getValue() );

            try
            {
                resourceDesc.setEncoding( resource.getAttribute( "encoding" ) );
            }
            catch ( IllegalCharsetNameException icne )
            {
                throw new XmlPullParserException( resource.getAttribute( "encoding" ) + " is not a valid encoding." );
            }
            catch ( UnsupportedCharsetException uce )
            {
                throw new XmlPullParserException(
                    resource.getAttribute( "encoding" ) + " is not a supported encoding." );
            }
        }

        if ( resource.getValue().endsWith( ".properties" ) )
        {
            TemplateDescriptor resourceDesc = descriptor.getResourceDescriptor( resource.getValue() );

            resourceDesc.setEncoding( "iso-8859-1" );
        }
    }

    /**
     * Adds the test-source element <code>source</code> to the list of sources in the
     * <code>descriptor</code> and sets its <code>TemplateDescriptor</code> to
     * <i>filtered</i> and with the encoding specified in the <code>encoding</code>
     * attribute or the Java virtual machine's default if it is not defined.
     *
     * @param testSource a <code>&lt;source&gt;</code> element from the <code>&lt;testSources&gt;</code>
     * @param descriptor the <code>ArchetypeDescriptor</code> to add the test-source template to.
     * @throws XmlPullParserException if the encoding specified is not valid or supported.
     */
    private static void addTestSourceToDescriptor( Xpp3Dom testSource, ArchetypeDescriptor descriptor )
        throws XmlPullParserException
    {
        descriptor.addTestSource( testSource.getValue() );
        TemplateDescriptor testSourceDesc = descriptor.getTestSourceDescriptor( testSource.getValue() );
        testSourceDesc.setFiltered( true );
        if ( testSource.getAttribute( "encoding" ) != null )
        {
            try
            {
                testSourceDesc.setEncoding( testSource.getAttribute( "encoding" ) );
            }
            catch ( IllegalCharsetNameException icne )
            {
                throw new XmlPullParserException( testSource.getAttribute( "encoding" ) + " is not a valid encoding." );
            }
            catch ( UnsupportedCharsetException uce )
            {
                throw new XmlPullParserException(
                    testSource.getAttribute( "encoding" ) + " is not a supported encoding." );
            }
        }
    }

    /**
     * Adds the test-resource element <code>resource</code> to the list of test-resources in the
     * <code>descriptor</code> and sets its <code>TemplateDescriptor</code> to
     * <i>filtered</i> if the attribute <code>filtered</code> was not
     * specified or its value is <code>&quot;true&quot;</code>, or <code>false</code>
     * if its value is <code>&quot;false&quot;</code>, and the encoding specified
     * in the <code>encoding</code> attribute or the Java virtual machine's default if
     * it is not defined. If the <code>resource</code> is a property file (ends in
     * <code>.properties</code>) its encoding will be set to <code>iso-8859-1</code>
     * even if some other encoding is specified in the attribute.
     *
     * @param testResource a <code>&lt;resource&gt;</code> element from the <code>&lt;testResources&gt;</code>
     * @param descriptor   the <code>ArchetypeDescriptor</code> to add the test-resource template to.
     * @throws XmlPullParserException if the encoding specified is not valid or supported or if the
     *                                value of the attribute <code>filtered</code> is no valid.
     */
    private static void addTestResourceToDescriptor( Xpp3Dom testResource, ArchetypeDescriptor descriptor )
        throws XmlPullParserException
    {
        descriptor.addTestResource( testResource.getValue() );

        if ( testResource.getAttribute( "filtered" ) != null )
        {
            TemplateDescriptor testResourceDesc = descriptor.getTestResourceDescriptor( testResource.getValue() );

            try
            {
                testResourceDesc.setFiltered( getValueFilteredAttribute( testResource.getAttribute( "filtered" ) ) );
            }
            catch ( IllegalArgumentException iae )
            {
                throw new XmlPullParserException( iae.getMessage() );
            }
        }

        if ( testResource.getAttribute( "encoding" ) != null )
        {
            TemplateDescriptor testResourceDesc = descriptor.getTestResourceDescriptor( testResource.getValue() );

            try
            {
                testResourceDesc.setEncoding( testResource.getAttribute( "encoding" ) );

            }
            catch ( IllegalCharsetNameException icne )
            {
                throw new XmlPullParserException(
                    testResource.getAttribute( "encoding" ) + " is not a valid encoding." );
            }
            catch ( UnsupportedCharsetException uce )
            {
                throw new XmlPullParserException(
                    testResource.getAttribute( "encoding" ) + " is not a supported encoding." );
            }
        }

        if ( testResource.getValue().endsWith( ".properties" ) )
        {
            TemplateDescriptor testResourceDesc = descriptor.getTestResourceDescriptor( testResource.getValue() );

            testResourceDesc.setEncoding( "iso-8859-1" );
        }
    }

    /**
     * Adds the site-resource element <code>resource</code> to the list of site-resources in the
     * <code>descriptor</code> and sets its <code>TemplateDescriptor</code> to
     * <i>filtered</i> if the attribute <code>filtered</code> was not
     * specified or its value is <code>&quot;true&quot;</code>, or <code>false</code>
     * if its value is <code>&quot;false&quot;</code>, and the encoding specified
     * in the <code>encoding</code> attribute or the Java virtual machine's default if
     * it is not defined. If the <code>resource</code> is a property file (ends in
     * <code>.properties</code>) its encoding will be set to <code>iso-8859-1</code>
     * even if some other encoding is specified in the attribute.
     *
     * @param siteResource a <code>&lt;resource&gt;</code> element from the <code>&lt;siteResources&gt;</code>
     * @param descriptor   the <code>ArchetypeDescriptor</code> to add the site-resource template to.
     * @throws XmlPullParserException if the encoding specified is not valid or supported or if the
     *                                value of the attribute <code>filtered</code> is no valid.
     */
    private static void addSiteResourceToDescriptor( Xpp3Dom siteResource, ArchetypeDescriptor descriptor )
        throws XmlPullParserException
    {
        descriptor.addSiteResource( siteResource.getValue() );

        if ( siteResource.getAttribute( "filtered" ) != null )
        {
            TemplateDescriptor siteResourceDesc = descriptor.getSiteResourceDescriptor( siteResource.getValue() );

            try
            {
                siteResourceDesc.setFiltered( getValueFilteredAttribute( siteResource.getAttribute( "filtered" ) ) );
            }
            catch ( IllegalArgumentException iae )
            {
                throw new XmlPullParserException( iae.getMessage() );
            }
        }
        if ( siteResource.getAttribute( "encoding" ) != null )
        {
            TemplateDescriptor siteResourceDesc = descriptor.getSiteResourceDescriptor( siteResource.getValue() );

            try
            {
                siteResourceDesc.setEncoding( siteResource.getAttribute( "encoding" ) );
            }
            catch ( IllegalCharsetNameException icne )
            {
                throw new XmlPullParserException(
                    siteResource.getAttribute( "encoding" ) + " is not a valid encoding." );
            }
            catch ( UnsupportedCharsetException uce )
            {
                throw new XmlPullParserException(
                    siteResource.getAttribute( "encoding" ) + " is not a supported encoding." );
            }
        }
        if ( siteResource.getValue().endsWith( ".properties" ) )
        {
            TemplateDescriptor siteResourceDesc = descriptor.getSiteResourceDescriptor( siteResource.getValue() );

            siteResourceDesc.setEncoding( "iso-8859-1" );
        }
    }

    private static boolean getValueFilteredAttribute( String str )
        throws IllegalArgumentException
    {
        boolean ret = false;

        if ( str.equals( "true" ) )
        {
            ret = true;
        }
        else if ( str.equals( "false" ) )
        {
            ret = false;
        }
        else
        {
            throw new IllegalArgumentException( str + " is not an accepted value for the attribute 'filtered'" );
        }
        return ret;
    }
}
