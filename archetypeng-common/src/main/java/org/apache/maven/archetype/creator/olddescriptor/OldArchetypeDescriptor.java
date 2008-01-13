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

import java.util.ArrayList;
import java.util.List;

/** @author rafale */
public class OldArchetypeDescriptor
{
    private String id;
    private List resources;
    private List siteResources;
    private List sources;
    private List testResources;
    private List testSources;

    public void addResources( String resource )
    {
        if ( resources == null )
        {
            resources = new ArrayList();
        }
        resources.add( resource );
    }

    public void addSiteResources( String resource )
    {
        if ( siteResources == null )
        {
            siteResources = new ArrayList();
        }
        siteResources.add( resource );
    }

    public void addSource( String source )
    {
        if ( sources == null )
        {
            sources = new ArrayList();
        }
        sources.add( source );
    }

    public void addTestResources( String resource )
    {
        if ( testResources == null )
        {
            testResources = new ArrayList();
        }
        testResources.add( resource );
    }

    public void addTestSources( String source )
    {
        if ( testSources == null )
        {
            testSources = new ArrayList();
        }
        testSources.add( source );
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public List getResources()
    {
        return resources;
    }

    public void setResources( List resources )
    {
        this.resources = resources;
    }

    public List getSiteResources()
    {
        return siteResources;
    }

    public void setSiteResources( List siteResources )
    {
        this.siteResources = siteResources;
    }

    public List getSources()
    {
        return sources;
    }

    public void setSources( List sources )
    {
        this.sources = sources;
    }

    public List getTestResources()
    {
        return testResources;
    }

    public void setTestResources( List testResources )
    {
        this.testResources = testResources;
    }

    public List getTestSources()
    {
        return testSources;
    }

    public void setTestSources( List testSources )
    {
        this.testSources = testSources;
    }

    public String toString()
    {
        return
            "(OldArchetypeDescriptor"
                + " id=" + id
                + " sources=" + sources
                + " testSources" + testSources
                + " resources" + resources
                + " testResources" + testResources
                + " siteResources" + siteResources
                + ")";
    }
}
