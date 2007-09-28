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

package org.apache.maven.archetype.ui;

import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.common.ArchetypeFactory;
import org.apache.maven.archetype.common.ArchetypePropertiesManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.ArchetypeSelectionFailure;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.exception.UnknownGroup;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.apache.maven.archetype.source.RegistryArchetypeDataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** @plexus.component */
public class DefaultArchetypeSelector
    extends AbstractLogEnabled
    implements ArchetypeSelector
{
    /** @plexus.requirement */
    private ArchetypeArtifactManager archetypeArtifactManager;

    /** @plexus.requirement */
    private ArchetypeFactory archetypeFactory;

    /** @plexus.requirement */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /** @plexus.requirement */
    private ArchetypeRegistryManager archetypeRegistryManager;

    /** @plexus.requirement */
    private ArchetypeSelectionQueryer archetypeSelectionQueryer;

    /** @plexus.requirement role="org.apache.maven.archetype.source.ArchetypeDataSource" */
    private Map archetypeSources;

    public ArchetypeDefinition selectArchetype(
        String archetypeGroupId,
        String archetypeArtifactId,
        String archetypeVersion,
        Boolean interactiveMode,
        File propertyFile,
        File archetypeRegistryFile,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        ArchetypeNotDefined,
        UnknownArchetype,
        UnknownGroup,
        IOException,
        PrompterException,
        ArchetypeSelectionFailure
    {
        Properties properties =
            initialiseArchetypeId(
                archetypeGroupId,
                archetypeArtifactId,
                archetypeVersion,
                propertyFile
            );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition( properties );

        if ( interactiveMode.booleanValue() )
        {
            if ( archetypeDefinition.isPartiallyDefined() )
            {
                getLogger().debug( "Archetype is partially defined" );
                archetypeDefinition.setVersion(
                    archetypeArtifactManager.getReleaseVersion(
                        archetypeDefinition.getGroupId(),
                        archetypeDefinition.getArtifactId(),
                        localRepository,
                        repositories
                    )
                );
            }
            else
            {
                getLogger().debug( "Archetype is not defined" );
            }

            // We are going to let the user select from a list of available archetypes first

            if ( !archetypeDefinition.isDefined() )
            {
                Collection archetypes;

                try
                {
                    // Now where would this configuration come from
                    // - We need a list of archetypes that can be added to and changed, we need to pull
                    //   these from different sources
                    //
                    // - We then need a way to configure the sources
                    //  - we need to separate the registry from the configuration as we need to provide one place
                    //    to


                    ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( "registry" );

                    archetypes = source.getArchetypes( null );
                }
                catch ( ArchetypeDataSourceException e )
                {
                    throw new ArchetypeSelectionFailure( "Error loading archetypes from data source(s).", e );
                }

                if ( archetypes.size() > 0 )
                {
                    org.apache.maven.archetype.registry.Archetype archetype = archetypeSelectionQueryer.selectArchetype(
                        archetypes );

                    archetypeDefinition.setArtifactId( archetype.getArtifactId() );

                    archetypeDefinition.setName( archetype.getArtifactId() );

                    archetypeDefinition.setGroupId( archetype.getGroupId() );

                    archetypeDefinition.setVersion( archetype.getVersion() );

                    archetypeDefinition.setRepository( archetype.getRepository() );

                    String goals = StringUtils.join( archetype.getGoals().iterator(), "," );

                    archetypeDefinition.setGoals( goals );
                }
            }
        }

        if ( !archetypeArtifactManager.exists(
            archetypeDefinition.getGroupId(),
            archetypeDefinition.getArtifactId(),
            archetypeDefinition.getVersion(),
            localRepository,
            repositories
        )
            )
        {
            throw new UnknownArchetype(
                "The desired archetype does not exist (" + archetypeDefinition.getGroupId() + ":"
                    + archetypeDefinition.getArtifactId() + ":" + archetypeDefinition.getVersion()
                    + ")"
            );
        }
        else
        {
            archetypePropertiesManager.writeProperties(
                toProperties( archetypeDefinition ),
                propertyFile
            );
                        
            return archetypeDefinition;
        }
    }

    public static Properties toProperties( ArchetypeDefinition ad )
    {
        java.util.Properties properties = new java.util.Properties ();

        properties.setProperty (
            Constants.ARCHETYPE_GROUP_ID,
            (org.codehaus.plexus.util.StringUtils.isNotEmpty( ad.getGroupId () ) ? ad.getGroupId () : "" )
        );

        properties.setProperty (
            Constants.ARCHETYPE_ARTIFACT_ID,
            (org.codehaus.plexus.util.StringUtils.isNotEmpty( ad.getArtifactId () ) ? ad.getArtifactId () : "" )
        );

        properties.setProperty (
            Constants.ARCHETYPE_VERSION,
            (org.codehaus.plexus.util.StringUtils.isNotEmpty( ad.getVersion () ) ? ad.getVersion () : "" )
        );

        properties.setProperty (
            Constants.ARCHETYPE_POST_GENERATION_GOALS,
            (org.codehaus.plexus.util.StringUtils.isNotEmpty( ad.getGoals() ) ? ad.getGoals() : "" )
        );

        return properties;
    }

    private Properties initialiseArchetypeId(
        String archetypeGroupId,
        String archetypeArtifactId,
        String archetypeVersion,
        File propertyFile
    )
        throws
        IOException
    {
        Properties properties = new Properties();
        try
        {
            archetypePropertiesManager.readProperties( properties, propertyFile );
        }
        catch ( FileNotFoundException e )
        {
            getLogger().debug( "archetype.properties does not exist" );
        }

        if ( archetypeGroupId != null )
        {
            properties.setProperty( Constants.ARCHETYPE_GROUP_ID, archetypeGroupId );
        }

        if ( archetypeArtifactId != null )
        {
            properties.setProperty( Constants.ARCHETYPE_ARTIFACT_ID, archetypeArtifactId );
        }

        if ( archetypeVersion != null )
        {
            properties.setProperty( Constants.ARCHETYPE_VERSION, archetypeVersion );
        }

        return properties;
    }
}
