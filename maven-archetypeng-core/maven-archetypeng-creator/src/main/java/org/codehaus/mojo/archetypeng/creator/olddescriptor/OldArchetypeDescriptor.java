package org.codehaus.mojo.archetypeng.creator.olddescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rafale
 */
public class OldArchetypeDescriptor
{
    private String id;
    private List sources;
    private List testSources;
    private List resources;
    private List testResources;
    private List siteResources;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public List getSources ()
    {
        return sources;
    }
    public void addSource(String source)
    {
        if (sources == null)
        {
            sources = new ArrayList();
        }
        sources.add (source);
    }

    public List getTestSources ()
    {
        return testSources;
    }
    
    public void addTestSources (String source)
    {
        if (testSources == null)
        {
            testSources = new ArrayList();
        }
        testSources.add (source);
    }

    public List getResources ()
    {
        return resources;
    }
    
    public void addResources (String resource)
    {
        if (resources == null)
        {
            resources = new ArrayList();
        }
        resources.add (resource);
    }

    public List getTestResources ()
    {
        return testResources;
    }
    
    public void addTestResources (String resource)
    {
        if (testResources == null)
        {
            testResources = new ArrayList();
        }
        testResources.add (resource);
    }

    public List getSiteResources ()
    {
        return siteResources;
    }
    
    public void addSiteResources (String resource)
    {
        if (siteResources == null)
        {
            siteResources = new ArrayList();
        }
        siteResources.add (resource);
    }

    public void setSources (List sources)
    {
        this.sources = sources;
    }

    public void setTestSources (List testSources)
    {
        this.testSources = testSources;
    }

    public void setResources (List resources)
    {
        this.resources = resources;
    }

    public void setTestResources (List testResources)
    {
        this.testResources = testResources;
    }

    public void setSiteResources (List siteResources)
    {
        this.siteResources = siteResources;
    }
    
    public String toString()
    {
        return "(OldArchetypeDescriptor" +
            " id=" +id+
            " sources=" +sources+
            " testSources" +testSources+
            " resources" +resources+
            " testResources" +testResources+
            " siteResources" +siteResources+
            ")";
    }
    
}
