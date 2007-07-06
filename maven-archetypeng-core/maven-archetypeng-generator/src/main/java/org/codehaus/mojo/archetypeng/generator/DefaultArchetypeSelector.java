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

package org.codehaus.mojo.archetypeng.generator;

import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.mojo.archetypeng.Archetype;
import org.codehaus.mojo.archetypeng.ArchetypeArtifactManager;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.ArchetypeRegistryManager;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.ArchetypeSelectionFailure;
import org.codehaus.mojo.archetypeng.exception.UnknownArchetype;
import org.codehaus.mojo.archetypeng.exception.UnknownGroup;

import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeSelector
extends AbstractLogEnabled
implements ArchetypeSelector
{
    /**
     * @plexus.requirement
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

    /**
     * @plexus.requirement
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @plexus.requirement
     */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /**
     * @plexus.requirement
     */
    private ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * @plexus.requirement
     */
    private ArchetypeSelectionQueryer archetypeSelectionQueryer;

    public void selectArchetype (
        String archetypeGroupId,
        String archetypeArtifactId,
        String archetypeVersion,
        Boolean interactiveMode,
        File propertyFile,
        File archetypeRegistryFile,
        ArtifactRepository localRepository,
        List repositories
    )
    throws ArchetypeNotDefined,
        UnknownArchetype,
        IOException,
        FileNotFoundException,
        PrompterException,
        ArchetypeSelectionFailure
    {
        Properties properties =
            initialiseArchetypeId (
                archetypeGroupId,
                archetypeArtifactId,
                archetypeVersion,
                propertyFile
            );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition ( properties );

        if ( interactiveMode.booleanValue () )
        {
            if ( archetypeDefinition.isPartiallyDefined () )
            {
                getLogger ().debug ( "Archetype is partially defined" );
                archetypeDefinition.setVersion (
                    archetypeArtifactManager.getReleaseVersion (
                        archetypeDefinition.getGroupId (),
                        archetypeDefinition.getArtifactId (),
                        localRepository,
                        repositories
                    )
                );
            }
            else
            {
                getLogger ().debug ( "Archetype is not defined" );
            }

            while ( !archetypeDefinition.isDefined () )
            {
                try
                {
                    if ( !archetypeDefinition.isGroupDefined () )
                    {
                        getLogger ().debug ( "Archetype group not defined" );

                        List groups =
                            archetypeRegistryManager.getArchetypeGroups ( archetypeRegistryFile );
                        getLogger ().debug ( "Groups=" + groups );

                        archetypeDefinition.setGroupId (
                            archetypeSelectionQueryer.selectGroup ( groups )
                        );
                    }
                    else
                    {
                        getLogger ().debug (
                            "Archetype group: " + archetypeDefinition.getGroupId ()
                        );
                    }

                    if ( !archetypeDefinition.isArtifactDefined () )
                    {
                        getLogger ().debug ( "Archetype artifact not defined" );

                        List archetypes =
                            archetypeArtifactManager.getArchetypes (
                                archetypeDefinition.getGroupId (),
                                localRepository,
                                repositories
                            );
                        getLogger ().debug ( "Archetypes=" + archetypes );

                        Archetype archetype =
                            archetypeSelectionQueryer.selectArtifact ( archetypes );

                        archetypeDefinition.setArtifactId ( archetype.getArtifactId () );
                        archetypeDefinition.setName ( archetype.getName () );
                    }
                    else
                    {
                        getLogger ().debug (
                            "Archetype artifact: " + archetypeDefinition.getArtifactId ()
                        );
                    }

                    if ( archetypeDefinition.isPartiallyDefined () )
                    {
                        getLogger ().debug ( "Archetype version not defined" );

                        List versions =
                            archetypeArtifactManager.getVersions (
                                archetypeDefinition.getGroupId (),
                                archetypeDefinition.getArtifactId (),
                                localRepository,
                                repositories
                            );
                        getLogger ().debug ( "Versions=" + versions );

                        archetypeDefinition.setVersion (
                            archetypeSelectionQueryer.selectVersion ( versions )
                        );
                    }
                    else
                    {
                        getLogger ().debug (
                            "Archetype version: " + archetypeDefinition.getVersion ()
                        );
                    }

                    if ( !archetypeDefinition.isDefined () )
                    {
                        throw new ArchetypeSelectionFailure (
                            "The archetype must be selected here"
                        );
                    }
                    else if ( !archetypeSelectionQueryer.confirmSelection ( archetypeDefinition ) )
                    {
                        getLogger ().debug ( "Archetype selection not confirmed" );
                        archetypeDefinition.reset ();
                    }
                    else
                    {
                        getLogger ().debug ( "Archetype selection confirmed" );
                    }
                }
                catch ( UnknownGroup e )
                {
                    getLogger ().warn ( "Unknown group" );
                    archetypeDefinition.reset ();
                }
                catch ( UnknownArchetype e )
                {
                    getLogger ().warn ( "Unknown archetype" );
                    archetypeDefinition.reset ();
                }
            } // end while
        }
        else
        {
            if ( !archetypeDefinition.isDefined () )
            {
                if ( !archetypeDefinition.isPartiallyDefined () )
                {
                    throw new ArchetypeNotDefined ( "The archetype is not defined" );
                }
                else
                {
                    getLogger ().debug ( "Archetype is partially defined" );
                    archetypeDefinition.setVersion (
                        archetypeArtifactManager.getReleaseVersion (
                            archetypeDefinition.getGroupId (),
                            archetypeDefinition.getArtifactId (),
                            localRepository,
                            repositories
                        )
                    );
                    getLogger ().info (
                        "Using default version " + archetypeDefinition.getVersion ()
                    );
                }
            }
            if ( !archetypeDefinition.isDefined () )
            {
                throw new ArchetypeSelectionFailure ( "The archetype must be selected here" );
            }
            else
            {
                getLogger ().info (
                    "Archetype selected (" + archetypeDefinition.getGroupId () + ":"
                    + archetypeDefinition.getArtifactId () + ":" + archetypeDefinition
                    .getVersion () + ")"
                );
            }
        } // end if

        if ( !archetypeArtifactManager.exists (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            )
        )
        {
            throw new UnknownArchetype (
                "The desired archetype does not exist (" + archetypeDefinition.getGroupId () + ":"
                + archetypeDefinition.getArtifactId () + ":" + archetypeDefinition.getVersion ()
                + ")"
            );
        }
        else
        {
            archetypePropertiesManager.writeProperties (
                archetypeDefinition.toProperties (),
                propertyFile
            );
        }
    }

    private Properties initialiseArchetypeId (
        String archetypeGroupId,
        String archetypeArtifactId,
        String archetypeVersion,
        File propertyFile
    )
    throws IOException
    {
        Properties properties = new Properties ();
        try
        {
            archetypePropertiesManager.readProperties ( properties, propertyFile );
        }
        catch ( FileNotFoundException e )
        {
            getLogger ().debug ( "archetype.properties does not exist" );
        }

        if ( archetypeGroupId != null )
        {
            properties.setProperty ( Constants.ARCHETYPE_GROUP_ID, archetypeGroupId );
        }

        if ( archetypeArtifactId != null )
        {
            properties.setProperty ( Constants.ARCHETYPE_ARTIFACT_ID, archetypeArtifactId );
        }

        if ( archetypeVersion != null )
        {
            properties.setProperty ( Constants.ARCHETYPE_VERSION, archetypeVersion );
        }

        return properties;
    }
}
