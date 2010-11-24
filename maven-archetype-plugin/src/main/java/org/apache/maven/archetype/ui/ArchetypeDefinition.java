package org.apache.maven.archetype.ui;

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

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.codehaus.plexus.util.StringUtils;

public class ArchetypeDefinition
{
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String repository;

    private String goals;

    private String url;

    private String description;

    public ArchetypeDefinition()
    {
    }

    public ArchetypeDefinition( ArchetypeGenerationRequest request )
    {
        setGroupId( request.getArchetypeGroupId() );
        setArtifactId( request.getArchetypeArtifactId() );
        setVersion( request.getArchetypeVersion() );
    }

    public String getArtifactId()
    {
        return this.artifactId;
    }

    public String getGoals()
    {
        return this.goals;
    }

    public String getGroupId()
    {
        return this.groupId;
    }

    public String getName()
    {
        return this.name;
    }

    public String getRepository()
    {
        return this.repository;
    }

    public String getVersion()
    {
        return this.version;
    }

    public String getUrl()
    {
        return this.url;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public void setGoals( String goals )
    {
        this.goals = goals;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setRepository( String repository )
    {
        this.repository = repository;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public void reset()
    {
        setGroupId( null );
        setArtifactId( null );
        setVersion( null );
    }

    public boolean isArtifactDefined()
    {
        return StringUtils.isNotEmpty( getArtifactId() );
    }

    public boolean isDefined()
    {
        return isPartiallyDefined() && isVersionDefined();
    }

    public boolean isGroupDefined()
    {
        return StringUtils.isNotEmpty( getGroupId() );
    }

    public boolean isPartiallyDefined()
    {
        return isGroupDefined() && isArtifactDefined();
    }

    public boolean isVersionDefined()
    {
        return StringUtils.isNotEmpty( getVersion() );
    }

    public void updateRequest( ArchetypeGenerationRequest request )
    {
        request.setArchetypeGroupId( getGroupId() );

        request.setArchetypeArtifactId( getArtifactId() );

        request.setArchetypeVersion( getVersion() );

        request.setArchetypeGoals( getGoals() );

        request.setArchetypeName( getName() );

        if ( StringUtils.isNotEmpty( getRepository() ) )
        {
            request.setArchetypeRepository( getRepository() );
        }
    }
}
