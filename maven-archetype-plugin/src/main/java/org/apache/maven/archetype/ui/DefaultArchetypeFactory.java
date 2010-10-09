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

import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.Iterator;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeFactory
    extends AbstractLogEnabled
    implements ArchetypeFactory
{
    private void addOldRequiredProperty( ArchetypeConfiguration configuration, Properties properties, String key,
                                         String defaultValue )
    {
        getLogger().debug( "Adding requiredProperty " + key );

        configuration.addRequiredProperty( key );

        String property = properties.getProperty( key );
        if ( property == null )
        {
            property = defaultValue;
        }

        if ( property != null )
        {
            configuration.setProperty( key, property );
            configuration.setDefaultProperty( key, property );
        }

        getLogger().debug( "Setting property " + key + "=" + configuration.getProperty( key ) );
    }

    public ArchetypeConfiguration createArchetypeConfiguration( org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor archetypeDescriptor,
                                                                Properties properties )
    {
        getLogger().debug( "Creating ArchetypeConfiguration from legacy descriptor and Properties" );

        ArchetypeConfiguration configuration = new ArchetypeConfiguration();

        configuration.setGroupId( properties.getProperty( Constants.ARCHETYPE_GROUP_ID, null ) );
        configuration.setArtifactId( properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, null ) );
        configuration.setVersion( properties.getProperty( Constants.ARCHETYPE_VERSION, null ) );

        configuration.setName( archetypeDescriptor.getId() );

        addOldRequiredProperty( configuration, properties, Constants.GROUP_ID, null );

        addOldRequiredProperty( configuration, properties, Constants.ARTIFACT_ID, null );

        addOldRequiredProperty( configuration, properties, Constants.VERSION, "1.0-SNAPSHOT" );

        addOldRequiredProperty( configuration, properties, Constants.PACKAGE, configuration.getProperty( Constants.GROUP_ID ) );

        return configuration;
    }

    public ArchetypeConfiguration createArchetypeConfiguration(
        org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor, Properties properties )
    {
        getLogger().debug( "Creating ArchetypeConfiguration from fileset descriptor and Properties" );

        ArchetypeConfiguration configuration = new ArchetypeConfiguration();

        configuration.setGroupId( properties.getProperty( Constants.ARCHETYPE_GROUP_ID, null ) );
        configuration.setArtifactId( properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, null ) );
        configuration.setVersion( properties.getProperty( Constants.ARCHETYPE_VERSION, null ) );

        configuration.setName( archetypeDescriptor.getName() );

        for ( RequiredProperty requiredProperty : archetypeDescriptor.getRequiredProperties() )
        {
            String key = requiredProperty.getKey();
            getLogger().debug( "Adding requiredProperty " + key );

            configuration.addRequiredProperty( key );

            String defaultValue = requiredProperty.getDefaultValue();

            if ( null != properties.getProperty( key, defaultValue ) && !containsInnerProperty( defaultValue ) )
            {
                String value = properties.getProperty( key, defaultValue );
                configuration.setProperty( key, value );
                getLogger().debug( "Setting property " + key + "=" + value );
            }
            if ( null != requiredProperty.getDefaultValue() )
            {
                String value = requiredProperty.getDefaultValue();
                configuration.setDefaultProperty( key, value );
                getLogger().debug( "Setting defaultProperty " + key + "=" + value );
            }
        }

        if ( !configuration.isConfigured( Constants.GROUP_ID )
                        && null == configuration.getDefaultValue( Constants.GROUP_ID ) )
        {
            configuration.addRequiredProperty( Constants.GROUP_ID );
            getLogger().debug( "Adding requiredProperty " + Constants.GROUP_ID );
            if ( null
                != properties.getProperty( Constants.GROUP_ID, configuration.getDefaultValue( Constants.GROUP_ID ) )
                && !containsInnerProperty( configuration.getDefaultValue( Constants.GROUP_ID ) ) )
            {
                configuration.setProperty( Constants.GROUP_ID,
                    properties.getProperty( Constants.GROUP_ID, configuration.getDefaultValue( Constants.GROUP_ID ) ) );
                configuration.setDefaultProperty( Constants.GROUP_ID, configuration.getProperty( Constants.GROUP_ID ) );
            }
            getLogger().debug( "Setting property " + Constants.GROUP_ID + "="
                + configuration.getProperty( Constants.GROUP_ID ) );
        }

        if ( !configuration.isConfigured( Constants.ARTIFACT_ID )
                        && null == configuration.getDefaultValue( Constants.ARTIFACT_ID ) )
        {
            configuration.addRequiredProperty( Constants.ARTIFACT_ID );
            getLogger().debug( "Adding requiredProperty " + Constants.ARTIFACT_ID );
            if ( null
                != properties.getProperty( Constants.ARTIFACT_ID,
                    configuration.getDefaultValue( Constants.ARTIFACT_ID ) )
                && !containsInnerProperty( configuration.getDefaultValue( Constants.ARTIFACT_ID ) ) )
            {
                configuration.setProperty( Constants.ARTIFACT_ID, properties.getProperty( Constants.ARTIFACT_ID ) );
                configuration.setDefaultProperty( Constants.ARTIFACT_ID,
                    configuration.getProperty( Constants.ARTIFACT_ID ) );
            }
            getLogger().debug( "Setting property " + Constants.ARTIFACT_ID + "="
                + configuration.getProperty( Constants.ARTIFACT_ID ) );
        }

        if ( !configuration.isConfigured( Constants.VERSION )
                        && null == configuration.getDefaultValue( Constants.VERSION ) )
        {
            configuration.addRequiredProperty( Constants.VERSION );
            getLogger().debug( "Adding requiredProperty " + Constants.VERSION );
            if ( null != properties.getProperty( Constants.VERSION,
                    configuration.getDefaultValue( Constants.VERSION ) )
                && !containsInnerProperty( configuration.getDefaultValue( Constants.VERSION ) ) )
            {
                configuration.setProperty( Constants.VERSION,
                    properties.getProperty( Constants.VERSION, configuration.getDefaultValue( Constants.VERSION ) ) );
                configuration.setDefaultProperty( Constants.VERSION, configuration.getProperty( Constants.VERSION ) );
            }
            else
            {
                configuration.setDefaultProperty( Constants.VERSION, "1.0-SNAPSHOT" );
            }
            getLogger().debug( "Setting property " + Constants.VERSION + "="
                + configuration.getProperty( Constants.VERSION ) );
        }

        if ( !configuration.isConfigured( Constants.PACKAGE )
                        && null == configuration.getDefaultValue( Constants.PACKAGE ) )
        {
            configuration.addRequiredProperty( Constants.PACKAGE );
            getLogger().debug( "Adding requiredProperty " + Constants.PACKAGE );
            if ( null != properties.getProperty( Constants.PACKAGE,
                    configuration.getDefaultValue( Constants.PACKAGE ) )
                && !containsInnerProperty( configuration.getDefaultValue( Constants.PACKAGE ) ) )
            {
                configuration.setProperty( Constants.PACKAGE,
                    properties.getProperty( Constants.PACKAGE, configuration.getDefaultValue( Constants.PACKAGE ) ) );
                configuration.setDefaultProperty( Constants.PACKAGE, configuration.getProperty( Constants.PACKAGE ) );
            }
            else if ( null != configuration.getProperty( Constants.GROUP_ID )
                && !containsInnerProperty( configuration.getDefaultValue( Constants.PACKAGE ) ) )
            {
                configuration.setProperty( Constants.PACKAGE, configuration.getProperty( Constants.GROUP_ID ) );
                configuration.setDefaultProperty( Constants.PACKAGE, configuration.getProperty( Constants.PACKAGE ) );
            }
            getLogger().debug( "Setting property " + Constants.PACKAGE + "="
                + configuration.getProperty( Constants.PACKAGE ) );
        }

        String postGenerationGoals = properties.getProperty( Constants.ARCHETYPE_POST_GENERATION_GOALS );
        if ( postGenerationGoals != null )
        {
            configuration.setProperty( Constants.ARCHETYPE_POST_GENERATION_GOALS, postGenerationGoals );
        }

        return configuration;
    }

    public ArchetypeConfiguration createArchetypeConfiguration( MavenProject project,
        ArchetypeDefinition archetypeDefinition, Properties properties )
    {
        ArchetypeConfiguration configuration = new ArchetypeConfiguration();
        getLogger().debug( "Creating ArchetypeConfiguration from ArchetypeDefinition, MavenProject and Properties" );

        configuration.setGroupId( properties.getProperty( Constants.ARCHETYPE_GROUP_ID ) );
        configuration.setArtifactId( properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID ) );
        configuration.setVersion( properties.getProperty( Constants.ARCHETYPE_VERSION ) );

        Iterator requiredProperties = properties.keySet().iterator();

        while ( requiredProperties.hasNext() )
        {
            String requiredProperty = (String) requiredProperties.next();

            if ( requiredProperty.indexOf( "." ) < 0 )
            {
                configuration.addRequiredProperty( requiredProperty );
                getLogger().debug( "Adding requiredProperty " + requiredProperty );
                configuration.setProperty( requiredProperty, properties.getProperty( requiredProperty ) );
                getLogger().debug( "Setting property " + requiredProperty + "="
                    + configuration.getProperty( requiredProperty ) );
            }
        }

        configuration.addRequiredProperty( Constants.GROUP_ID );
        getLogger().debug( "Adding requiredProperty " + Constants.GROUP_ID );
        configuration.setDefaultProperty( Constants.GROUP_ID, project.getGroupId() );
        if ( null != properties.getProperty( Constants.GROUP_ID, null ) )
        {
            configuration.setProperty( Constants.GROUP_ID, properties.getProperty( Constants.GROUP_ID ) );
            getLogger().debug( "Setting property " + Constants.GROUP_ID + "="
                + configuration.getProperty( Constants.GROUP_ID ) );
        }

        configuration.addRequiredProperty( Constants.ARTIFACT_ID );
        getLogger().debug( "Adding requiredProperty " + Constants.ARTIFACT_ID );
        configuration.setDefaultProperty( Constants.ARTIFACT_ID, project.getArtifactId() );
        if ( null != properties.getProperty( Constants.ARTIFACT_ID, null ) )
        {
            configuration.setProperty( Constants.ARTIFACT_ID, properties.getProperty( Constants.ARTIFACT_ID ) );
            getLogger().debug( "Setting property " + Constants.ARTIFACT_ID + "="
                + configuration.getProperty( Constants.ARTIFACT_ID ) );
        }

        configuration.addRequiredProperty( Constants.VERSION );
        getLogger().debug( "Adding requiredProperty " + Constants.VERSION );
        configuration.setDefaultProperty( Constants.VERSION, project.getVersion() );
        if ( null != properties.getProperty( Constants.VERSION, null ) )
        {
            configuration.setProperty( Constants.VERSION, properties.getProperty( Constants.VERSION ) );
            getLogger().debug( "Setting property " + Constants.VERSION + "="
                + configuration.getProperty( Constants.VERSION ) );
        }

        configuration.addRequiredProperty( Constants.PACKAGE );
        getLogger().debug( "Adding requiredProperty " + Constants.PACKAGE );
        if ( null != properties.getProperty( Constants.PACKAGE ) )
        {
            configuration.setProperty( Constants.PACKAGE, properties.getProperty( Constants.PACKAGE ) );

            getLogger().debug( "Setting property " + Constants.PACKAGE + "="
                + configuration.getProperty( Constants.PACKAGE ) );
        }

        if ( null != properties.getProperty( Constants.ARCHETYPE_GROUP_ID, null ) )
        {
            configuration.setProperty( Constants.ARCHETYPE_GROUP_ID, properties.getProperty( Constants.ARCHETYPE_GROUP_ID ) );
        }

        if ( null != properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, null ) )
        {
            configuration.setProperty( Constants.ARCHETYPE_ARTIFACT_ID, properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID ) );
        }

        if ( null != properties.getProperty( Constants.ARCHETYPE_VERSION, null ) )
        {
            configuration.setProperty( Constants.ARCHETYPE_VERSION, properties.getProperty( Constants.ARCHETYPE_VERSION ) );
        }
        return configuration;
    }

    public ArchetypeDefinition createArchetypeDefinition( Properties properties )
    {
        ArchetypeDefinition definition = new ArchetypeDefinition();

        definition.setGroupId( properties.getProperty( Constants.ARCHETYPE_GROUP_ID, null ) );

        definition.setArtifactId( properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, null ) );

        definition.setVersion( properties.getProperty( Constants.ARCHETYPE_VERSION, null ) );

        definition.setRepository( properties.getProperty( Constants.ARCHETYPE_REPOSITORY, null ) );

        return definition;
    }

    public void updateArchetypeConfiguration( ArchetypeConfiguration archetypeConfiguration,
        ArchetypeDefinition archetypeDefinition )
    {
        archetypeConfiguration.setGroupId( archetypeDefinition.getGroupId() );
        archetypeConfiguration.setArtifactId( archetypeDefinition.getArtifactId() );
        archetypeConfiguration.setVersion( archetypeDefinition.getVersion() );
    }

    private boolean containsInnerProperty( String defaultValue )
    {
        if ( null == defaultValue )
        {
            return false;
        }
        return ( defaultValue.indexOf( "${" ) >= 0 ) && ( defaultValue.indexOf( "}" ) >= 0 );
    }
}
