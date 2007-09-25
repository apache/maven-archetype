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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
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
                List availableArchetypeInRegistry;
                
                try
                {
                    availableArchetypeInRegistry = archetypeRegistryManager.readArchetypeRegistry( archetypeRegistryFile ).getArchetypes();
                }
                catch ( XmlPullParserException e )
                {
                    availableArchetypeInRegistry = archetypeRegistryManager.getDefaultArchetypeRegistry().getArchetypes();
                }

                if ( availableArchetypeInRegistry != null )
                {
                    org.apache.maven.archetype.registry.Archetype archetype = archetypeSelectionQueryer.selectArchetype(
                        availableArchetypeInRegistry );

                    ArchetypeDefinition ad = new ArchetypeDefinition();

                    ad.setArtifactId( archetype.getArtifactId() );

                    ad.setName( archetype.getArtifactId() );

                    ad.setGroupId( archetype.getGroupId() );

                    ad.setVersion( archetype.getVersion() );

                    ad.setRepository( archetype.getRepository() );

                    String goals = StringUtils.join( archetype.getGoals().iterator(), "," );

                    ad.setGoals( goals );

                    archetypePropertiesManager.writeProperties(
                        toProperties( ad ),
                        propertyFile
                    );

                    return ad;
                }
            }
        }
        else
        {
            if ( !archetypeDefinition.isDefined() )
            {
                if ( !archetypeDefinition.isPartiallyDefined() )
                {
                    throw new ArchetypeNotDefined( "The archetype is not defined" );
                }
                else
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
                    getLogger().info(
                        "Using default version " + archetypeDefinition.getVersion()
                    );
                }
            }

            if ( !archetypeDefinition.isDefined() )
            {
                throw new ArchetypeSelectionFailure( "The archetype must be selected here" );
            }
            else
            {
                getLogger().info(
                    "Archetype selected (" + archetypeDefinition.getGroupId() + ":"
                        + archetypeDefinition.getArtifactId() + ":" + archetypeDefinition
                        .getVersion() + ")"
                );
            }
        } // end if

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
