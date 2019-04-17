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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArchetypeDescriptor
{
    private String id;

    private List<String> sources;

    private List<String> testSources;

    private List<String> resources;

    private List<String> testResources;

    private List<String> siteResources;

    /**
     * <code>Map</code> that associates the items in the <code>List</code>
     * <code>sources</code> with their attributes (instances of
     * <code>TemplateDescriptor</code>.
     */
    private Map<String, TemplateDescriptor> sourcesDescriptors;

    /**
     * <code>Map</code> that associates the items in the <code>List</code>
     * <code>testSources</code> with their attributes (instances of
     * <code>TemplateDescriptor</code>.
     */
    private Map<String, TemplateDescriptor> testSourcesDescriptors;

    /**
     * <code>Map</code> that associates the items in the <code>List</code>
     * <code>resources</code> with their attributes (instances of
     * <code>TemplateDescriptor</code>.
     */
    private Map<String, TemplateDescriptor> resourcesDescriptors;

    /**
     * <code>Map</code> that associates the items in the <code>List</code>
     * <code>testResources</code> with their attributes (instances of
     * <code>TemplateDescriptor</code>.
     */
    private Map<String, TemplateDescriptor> testResourcesDescriptors;

    /**
     * <code>Map</code> that associates the items in the <code>List</code>
     * <code>siteResources</code> with their attributes (instances of
     * <code>TemplateDescriptor</code>.
     */
    private Map<String, TemplateDescriptor> siteResourcesDescriptors;

    /**
     * This indicates the archetype can be a whole project or can be part
     * of another project. An example is a site archetype where the POM and
     * directory structure may already exist and you simply want to generate
     * the site directory structure.
     */
    private boolean allowPartial;

    public ArchetypeDescriptor()
    {
        sources = new ArrayList<>();

        resources = new ArrayList<>();

        testSources = new ArrayList<>();

        testResources = new ArrayList<>();

        siteResources = new ArrayList<>();

        sourcesDescriptors = new HashMap<>();

        testSourcesDescriptors = new HashMap<>();

        resourcesDescriptors = new HashMap<>();

        testResourcesDescriptors = new HashMap<>();

        siteResourcesDescriptors = new HashMap<>();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public void addSource( String source )
    {
        sources.add( source );

        putSourceDescriptor( source, new TemplateDescriptor() );
    }

    public List<String> getSources()
    {
        return sources;
    }

    public void putSourceDescriptor( String source, TemplateDescriptor descriptor )
    {
        sourcesDescriptors.put( source, descriptor );
    }

    public TemplateDescriptor getSourceDescriptor( String source )
    {
        return sourcesDescriptors.get( source );
    }

    public Map<String, TemplateDescriptor> getSourcesDescriptors()
    {
        return sourcesDescriptors;
    }

    public void addTestSource( String testSource )
    {
        testSources.add( testSource );

        putTestSourceDescriptor( testSource, new TemplateDescriptor() );
    }

    public List<String> getTestSources()
    {
        return testSources;
    }

    public void putTestSourceDescriptor( String testSource, TemplateDescriptor descriptor )
    {
        testSourcesDescriptors.put( testSource, descriptor );
    }

    public TemplateDescriptor getTestSourceDescriptor( String testSource )
    {
        return testSourcesDescriptors.get( testSource );
    }

    public Map<String, TemplateDescriptor> getTestSourcesDescriptors()
    {
        return testSourcesDescriptors;
    }

    public void addResource( String resource )
    {
        resources.add( resource );

        putResourceDescriptor( resource, new TemplateDescriptor() );
    }

    public List<String> getResources()
    {
        return resources;
    }

    public void putResourceDescriptor( String resource, TemplateDescriptor descriptor )
    {
        resourcesDescriptors.put( resource, descriptor );
    }

    public TemplateDescriptor getResourceDescriptor( String resource )
    {
        return resourcesDescriptors.get( resource );
    }

    public Map<String, TemplateDescriptor> getReourcesDescriptors()
    {
        return resourcesDescriptors;
    }

    public void addTestResource( String testResource )
    {
        testResources.add( testResource );
        putTestResourceDescriptor( testResource, new TemplateDescriptor() );
    }

    public List<String> getTestResources()
    {
        return testResources;
    }

    public void putTestResourceDescriptor( String testResource, TemplateDescriptor descriptor )
    {
        testResourcesDescriptors.put( testResource, descriptor );
    }

    public TemplateDescriptor getTestResourceDescriptor( String testResource )
    {
        return testResourcesDescriptors.get( testResource );
    }

    public Map<String, TemplateDescriptor> getTestReourcesDescriptors()
    {
        return testResourcesDescriptors;
    }

    public void addSiteResource( String siteResource )
    {
        siteResources.add( siteResource );

        putSiteResourceDescriptor( siteResource, new TemplateDescriptor() );
    }

    public List<String> getSiteResources()
    {
        return siteResources;
    }

    public void putSiteResourceDescriptor( String siteResource, TemplateDescriptor descriptor )
    {
        siteResourcesDescriptors.put( siteResource, descriptor );
    }

    public TemplateDescriptor getSiteResourceDescriptor( String siteResource )
    {
        return siteResourcesDescriptors.get( siteResource );
    }

    public Map<String, TemplateDescriptor> getSiteReourcesDescriptors()
    {
        return siteResourcesDescriptors;
    }

    public boolean isAllowPartial()
    {
        return allowPartial;
    }

    public void setAllowPartial( boolean allowPartial )
    {
        this.allowPartial = allowPartial;
    }
}
