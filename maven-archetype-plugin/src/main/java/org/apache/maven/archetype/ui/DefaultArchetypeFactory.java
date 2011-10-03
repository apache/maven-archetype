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

import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.Iterator;
import java.util.Properties;

@Component( role = ArchetypeFactory.class )
public class DefaultArchetypeFactory
    extends AbstractLogEnabled
    implements ArchetypeFactory
{
    public ArchetypeDefinition createArchetypeDefinition( Properties properties )
    {
        ArchetypeDefinition definition = new ArchetypeDefinition();

        definition.setGroupId( properties.getProperty( Constants.ARCHETYPE_GROUP_ID ) );

        definition.setArtifactId( properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID ) );

        definition.setVersion( properties.getProperty( Constants.ARCHETYPE_VERSION ) );

        definition.setRepository( properties.getProperty( Constants.ARCHETYPE_REPOSITORY ) );

        definition.setUrl( properties.getProperty( Constants.ARCHETYPE_URL ) );

        definition.setDescription( properties.getProperty( Constants.ARCHETYPE_DESCRIPTION ) );

        return definition;
    }

    private void addOldRequiredProperty( ArchetypeConfiguration configuration, Properties properties, String key,
                                         String defaultValue, boolean initPropertyWithDefault )
    {
        getLogger().debug( "Adding requiredProperty " + key );

        configuration.addRequiredProperty( key );

        String property = properties.getProperty( key );

        if ( property != null )
        {
            configuration.setProperty( key, property );
            configuration.setDefaultProperty( key, property );
        }
        else if ( defaultValue != null )
        {
            if ( initPropertyWithDefault )
            {
                configuration.setProperty( key, defaultValue );
            }
            configuration.setDefaultProperty( key, defaultValue );
        }

        getLogger().debug( "Setting property " + key + "=" + configuration.getProperty( key ) );
    }

    public ArchetypeConfiguration createArchetypeConfiguration( org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor archetypeDescriptor,
                                                                Properties properties )
    {
        getLogger().debug( "Creating ArchetypeConfiguration from legacy descriptor and Properties" );

        ArchetypeConfiguration configuration = createArchetypeConfiguration( properties );

        configuration.setName( archetypeDescriptor.getId() );

        addOldRequiredProperty( configuration, properties, Constants.GROUP_ID, null, false );

        addOldRequiredProperty( configuration, properties, Constants.ARTIFACT_ID, null, false );

        addOldRequiredProperty( configuration, properties, Constants.VERSION, "1.0-SNAPSHOT", false );

        addOldRequiredProperty( configuration, properties, Constants.PACKAGE,
                                configuration.getProperty( Constants.GROUP_ID ), true );

        return configuration;
    }

    private void addRequiredProperty( ArchetypeConfiguration configuration, Properties properties, String key,
                                      String defaultValue, boolean initPropertyWithDefault )
    {
        if ( !configuration.isConfigured( key ) && configuration.getDefaultValue( key ) == null )
        {
            addOldRequiredProperty( configuration, properties, key, defaultValue, initPropertyWithDefault );
        }
    }

    public ArchetypeConfiguration createArchetypeConfiguration( org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor,
                                                                Properties properties )
    {
        getLogger().debug( "Creating ArchetypeConfiguration from fileset descriptor and Properties" );

        ArchetypeConfiguration configuration = createArchetypeConfiguration( properties );

        configuration.setName( archetypeDescriptor.getName() );

        for ( RequiredProperty requiredProperty : archetypeDescriptor.getRequiredProperties() )
        {
            String key = requiredProperty.getKey();
            getLogger().debug( "Adding requiredProperty " + key );

            configuration.addRequiredProperty( key );

            String defaultValue = requiredProperty.getDefaultValue();

            if ( properties.getProperty( key ) != null )
            {
                // using value defined in properties, which overrides any default
                String value = properties.getProperty( key );
                configuration.setProperty( key, value );
                getLogger().debug( "Setting property " + key + "=" + value );
            }
            else if ( ( defaultValue != null ) && !containsInnerProperty( defaultValue ) )
            {
                // using default value
                 configuration.setProperty( key, defaultValue );
                 getLogger().debug( "Setting property " + key + "=" + defaultValue );
            }

            if ( defaultValue != null )
            {
                configuration.setDefaultProperty( key, defaultValue );
                getLogger().debug( "Setting defaultProperty " + key + "=" + defaultValue );
            }
        }

        addRequiredProperty( configuration, properties, Constants.GROUP_ID, null, false );

        addRequiredProperty( configuration, properties, Constants.ARTIFACT_ID, null, false );

        addRequiredProperty( configuration, properties, Constants.VERSION, "1.0-SNAPSHOT", false );

        addRequiredProperty( configuration, properties, Constants.PACKAGE,
                             configuration.getProperty( Constants.GROUP_ID ), true );

        String postGenerationGoals = properties.getProperty( Constants.ARCHETYPE_POST_GENERATION_GOALS );
        if ( postGenerationGoals != null )
        {
            configuration.setProperty( Constants.ARCHETYPE_POST_GENERATION_GOALS, postGenerationGoals );
        }

        return configuration;
    }

    private void addRequiredProperty( ArchetypeConfiguration configuration, Properties properties, String key,
                                      String defaultValue )
    {
        getLogger().debug( "Adding requiredProperty " + key );

        configuration.addRequiredProperty( key );

        if ( defaultValue != null )
        {
            configuration.setDefaultProperty( key, defaultValue );
        }

        if ( properties.getProperty( key ) != null )
        {
            configuration.setProperty( key, properties.getProperty( key ) );

            getLogger().debug( "Setting property " + key + "=" + configuration.getProperty( Constants.GROUP_ID ) );
        }
    }

    private void setProperty( ArchetypeConfiguration configuration, Properties properties, String key )
    {
        String property = properties.getProperty( key );

        if ( property != null )
        {
            configuration.setProperty( key, property );
        }
    }

    public ArchetypeConfiguration createArchetypeConfiguration( MavenProject project,
                                                                ArchetypeDefinition archetypeDefinition,
                                                                Properties properties )
    {
        getLogger().debug( "Creating ArchetypeConfiguration from ArchetypeDefinition, MavenProject and Properties" );

        ArchetypeConfiguration configuration = createArchetypeConfiguration( properties );

        for ( Iterator<?> requiredProperties = properties.keySet().iterator(); requiredProperties.hasNext(); )
        {
            String requiredProperty = (String) requiredProperties.next();

            if ( !requiredProperty.contains( "." ) )
            {
                getLogger().debug( "Adding requiredProperty " + requiredProperty );
                configuration.addRequiredProperty( requiredProperty );

                configuration.setProperty( requiredProperty, properties.getProperty( requiredProperty ) );
                getLogger().debug( "Setting property " + requiredProperty + "=" +
                                       configuration.getProperty( requiredProperty ) );
            }
        }

        addRequiredProperty( configuration, properties, Constants.GROUP_ID, project.getGroupId() );

        addRequiredProperty( configuration, properties, Constants.ARTIFACT_ID, project.getArtifactId() );

        addRequiredProperty( configuration, properties, Constants.VERSION, project.getVersion() );

        addRequiredProperty( configuration, properties, Constants.PACKAGE, null );

        setProperty( configuration, properties, Constants.ARCHETYPE_GROUP_ID );

        setProperty( configuration, properties, Constants.ARCHETYPE_ARTIFACT_ID );

        setProperty( configuration, properties, Constants.ARCHETYPE_VERSION );

        setProperty( configuration, properties, Constants.ARCHETYPE_URL );

        setProperty( configuration, properties, Constants.ARCHETYPE_DESCRIPTION );

        return configuration;
    }

    private ArchetypeConfiguration createArchetypeConfiguration( Properties properties )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration();

        configuration.setGroupId( properties.getProperty( Constants.ARCHETYPE_GROUP_ID ) );

        configuration.setArtifactId( properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID ) );

        configuration.setVersion( properties.getProperty( Constants.ARCHETYPE_VERSION ) );

        configuration.setUrl( properties.getProperty( Constants.ARCHETYPE_URL ) );

        configuration.setDescription( properties.getProperty( Constants.ARCHETYPE_DESCRIPTION ) );

        return configuration;
    }

    public void updateArchetypeConfiguration( ArchetypeConfiguration archetypeConfiguration,
                                              ArchetypeDefinition archetypeDefinition )
    {
        archetypeConfiguration.setGroupId( archetypeDefinition.getGroupId() );
        archetypeConfiguration.setArtifactId( archetypeDefinition.getArtifactId() );
        archetypeConfiguration.setVersion( archetypeDefinition.getVersion() );
    }

    /**
     * Check if the given value references a property, ie contains <code>${...}</code>.
     * 
     * @param defaultValue the value to check
     * @return <code>true</code> if the value contains <code>${</code> followed by <code>}</code>
     */
    private boolean containsInnerProperty( String defaultValue )
    {
        if ( defaultValue == null )
        {
            return false;
        }
        int start = defaultValue.indexOf( "${" );
        return ( start >= 0 ) && ( defaultValue.indexOf( "}", start ) >= 0 );
    }
}
