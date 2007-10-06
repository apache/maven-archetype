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

import org.apache.maven.archetype.Archetype;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeConfiguration;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.common.ArchetypeFactory;
import org.apache.maven.archetype.common.ArchetypePropertiesManager;
import org.apache.maven.archetype.exception.ArchetypeGenerationConfigurationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.common.Constants;

/** @plexus.component */
public class DefaultArchetypeGenerationConfigurator
    extends AbstractLogEnabled
    implements ArchetypeGenerationConfigurator
{
    /** @plexus.requirement */
    Archetype oldArchetype;

    /** @plexus.requirement */
    private ArchetypeArtifactManager archetypeArtifactManager;

    /** @plexus.requirement */
    private ArchetypeFactory archetypeFactory;

    /** @plexus.requirement */
    private ArchetypeGenerationQueryer archetypeGenerationQueryer;

    /** @plexus.requirement */
    private ArchetypePropertiesManager archetypePropertiesManager;

    public ArchetypeConfiguration configureArchetype(
        Boolean interactiveMode,
        File propertyFile,
        Properties commandLineProperties,
        ArtifactRepository localRepository,
        List repositories
    )
        throws
        ArchetypeNotDefined,
        UnknownArchetype,
        ArchetypeNotConfigured,
        IOException,
        PrompterException,
        ArchetypeGenerationConfigurationFailure
    {
        // propertyFile is no longer needed
        Properties properties =
            initialiseArchetypeProperties( commandLineProperties, propertyFile );

        // The archetype should be defined at this point and exist!
        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition( properties );

        if ( !archetypeDefinition.isDefined() )
        {
            throw new ArchetypeNotDefined( "The archetype is not defined" );
        }

        if ( !archetypeArtifactManager.exists( archetypeDefinition, localRepository, repositories ) )
        {
            throw new UnknownArchetype(
                "The desired archetype does not exist (" + archetypeDefinition.getGroupId() + ":"
                    + archetypeDefinition.getArtifactId() + ":" + archetypeDefinition.getVersion()
                    + ")"
            );
        }

        ArchetypeConfiguration archetypeConfiguration;

        if (
            archetypeArtifactManager.isFileSetArchetype(
                archetypeDefinition.getGroupId(),
                archetypeDefinition.getArtifactId(),
                archetypeDefinition.getVersion(),
                localRepository,
                repositories
            )
            )
        {
            org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor =
                archetypeArtifactManager.getFileSetArchetypeDescriptor(
                    archetypeDefinition.getGroupId(),
                    archetypeDefinition.getArtifactId(),
                    archetypeDefinition.getVersion(),
                    localRepository,
                    repositories
                );
            archetypeConfiguration =
                archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else if ( archetypeArtifactManager.isOldArchetype(
            archetypeDefinition.getGroupId(),
            archetypeDefinition.getArtifactId(),
            archetypeDefinition.getVersion(),
            localRepository,
            repositories
        )
            )
        {
            org.apache.maven.archetype.descriptor.ArchetypeDescriptor archetypeDescriptor =
                archetypeArtifactManager.getOldArchetypeDescriptor(
                    archetypeDefinition.getGroupId(),
                    archetypeDefinition.getArtifactId(),
                    archetypeDefinition.getVersion(),
                    localRepository,
                    repositories
                );
            archetypeConfiguration =
                archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else
        {
            throw new ArchetypeGenerationConfigurationFailure(
                "The defined artifact is not an archetype"
            );
        }

        if ( interactiveMode.booleanValue() )
        {
            boolean confirmed = false;
            while ( !confirmed )
            {
                if ( !archetypeConfiguration.isConfigured() )
                {
                    Iterator requiredProperties =
                        archetypeConfiguration.getRequiredProperties().iterator();

                    while ( requiredProperties.hasNext() )
                    {
                        String requiredProperty = (String) requiredProperties.next();

                        if ( !archetypeConfiguration.isConfigured( requiredProperty ) )
                        {
                            archetypeConfiguration.setProperty(
                                requiredProperty,
                                archetypeGenerationQueryer.getPropertyValue(
                                    requiredProperty,
                                    archetypeConfiguration.getDefaultValue( requiredProperty )
                                )
                            );
                        }
                    }
                }
                if ( !archetypeConfiguration.isConfigured() )
                {
                    throw new ArchetypeGenerationConfigurationFailure(
                        "The archetype generation must be configured here"
                    );
                }
                else if (
                    !archetypeGenerationQueryer.confirmConfiguration( archetypeConfiguration )
                    )
                {
                    getLogger().debug( "Archetype generation configuration not confirmed" );
                    archetypeConfiguration.reset();
                }
                else
                {
                    getLogger().debug( "Archetype generation configuration confirmed" );
                    confirmed = true;
                }
            } // end while
        }
        else
        {
            if ( !archetypeConfiguration.isConfigured() )
            {
                throw new ArchetypeNotConfigured( "The archetype is not configurated" );
            }
        }

//        propertyFile is no longer needed
//        archetypePropertiesManager.writeProperties(
//            archetypeConfiguration.toProperties(),
//            propertyFile
//        );

        return archetypeConfiguration;
    }

    private Properties initialiseArchetypeProperties(
        Properties commandLineProperties,
        File propertyFile
    )
        throws
        FileNotFoundException,
        IOException
    {
        Properties properties = new Properties();
//        propertyFile is no longer needed
//        archetypePropertiesManager.readProperties( properties, propertyFile );

        Iterator commandLinePropertiesIterator =
            new ArrayList( commandLineProperties.keySet() ).iterator();
        while ( commandLinePropertiesIterator.hasNext() )
        {
            String propertyKey = (String) commandLinePropertiesIterator.next();
            properties.setProperty(
                propertyKey,
                commandLineProperties.getProperty( propertyKey )
            );
        }
        return properties;
    }

    public void configureArchetype(
        ArchetypeGenerationRequest request,
        Boolean interactiveMode,
        Properties commandLineProperties,
        List repositories
    )
        throws
        ArchetypeNotDefined,
        UnknownArchetype,
        ArchetypeNotConfigured,
        IOException,
        PrompterException,
        ArchetypeGenerationConfigurationFailure
    {
        Properties p = new Properties( commandLineProperties );
        p.setProperty( Constants.ARCHETYPE_GROUP_ID, request.getArchetypeGroupId() );
        p.setProperty( Constants.ARCHETYPE_ARTIFACT_ID, request.getArchetypeArtifactId() );
        p.setProperty( Constants.ARCHETYPE_VERSION, request.getArchetypeVersion() );

        // propertyFile is no longer needed, set to null:
        ArchetypeConfiguration archetypeConfiguration = configureArchetype(
            interactiveMode,
            null,
            p,
            request.getLocalRepository(),
            repositories
        );

        request.setGroupId( archetypeConfiguration.getProperty( Constants.GROUP_ID ) );
        request.setArtifactId( archetypeConfiguration.getProperty( Constants.ARTIFACT_ID ) );
        request.setVersion( archetypeConfiguration.getProperty( Constants.VERSION ) );
        request.setPackage( archetypeConfiguration.getProperty( Constants.PACKAGE ) );
        Properties properties = archetypeConfiguration.getProperties();
        properties.remove( Constants.GROUP_ID );
        properties.remove( Constants.ARTIFACT_ID );
        properties.remove( Constants.VERSION );
        properties.remove( Constants.PACKAGE );

        request.setProperties( properties );

    }
}
