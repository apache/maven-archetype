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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.common.Constants;
import org.codehaus.plexus.util.StringUtils;

public class ArchetypeConfiguration
{
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String goals;

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }

    private String url;

    private String description;

    private List<String> requiredProperties;

    public void addRequiredProperty( String string )
    {
        getRequiredProperties().add( string );
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getGoals()
    {
        return goals;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getRequiredProperties()
    {
        if ( requiredProperties == null )
        {
            requiredProperties = new ArrayList<String>();
        }

        return requiredProperties;
    }

    public String getVersion()
    {
        return version;
    }

    public void removeRequiredProperty( String string )
    {
        getRequiredProperties().remove( string );
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

    public void setRequiredProperties( List<String> requiredProperties )
    {
        this.requiredProperties = requiredProperties;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public void reset()
    {
        properties.clear();
    }

    private Properties properties = new Properties();

    public void setProperty( String requiredProperty, String propertyValue )
    {
        properties.setProperty( requiredProperty, propertyValue );
    }

    public String getProperty( String property )
    {
        return properties.getProperty( property, null );
    }

    public Properties getProperties()
    {
        return properties;
    }

    public Properties toProperties()
    {
        Properties result = new Properties();

        result.putAll( properties );

        result.setProperty( Constants.ARCHETYPE_GROUP_ID, StringUtils.isNotEmpty( getGroupId() ) ? getGroupId() : "" );
        result.setProperty( Constants.ARCHETYPE_ARTIFACT_ID,
                            StringUtils.isNotEmpty( getArtifactId() ) ? getArtifactId() : "" );
        result.setProperty( Constants.ARCHETYPE_VERSION, StringUtils.isNotEmpty( getVersion() ) ? getVersion() : "" );

        if ( StringUtils.isNotEmpty( getGoals() ) )
        {
            result.setProperty( Constants.ARCHETYPE_POST_GENERATION_GOALS, getGoals() );
        }

        return result;
    }

    public boolean isConfigured()
    {
        for ( String requiredProperty : getRequiredProperties() )
        {
            if ( StringUtils.isEmpty( properties.getProperty( requiredProperty ) ) )
            {
                return false;
            }
        }

        return true;
    }

    public boolean isConfigured( String requiredProperties )
    {
        return StringUtils.isNotEmpty( properties.getProperty( requiredProperties ) );
    }

    private Properties defaultProperties = new Properties();

    public void setDefaultProperty( String requiredProperty, String propertyValue )
    {
        defaultProperties.setProperty( requiredProperty, propertyValue );
    }

    public String getDefaultValue( String requiredProperty )
    {
        return defaultProperties.getProperty( requiredProperty, null );
    }

    public Properties getDefaultValues()
    {
        return defaultProperties;
    }
}
