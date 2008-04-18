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

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeConfiguration;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeGenerationConfigurationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.old.OldArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

// TODO: this seems to have more responsibilities than just a configurator
/**
 * @plexus.component
 */
public class DefaultArchetypeGenerationConfigurator
extends AbstractLogEnabled
implements ArchetypeGenerationConfigurator
{
    /**
     * @plexus.requirement
     */
    OldArchetype oldArchetype;

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
    private ArchetypeGenerationQueryer archetypeGenerationQueryer;

    /**
     * @plexus.requirement
     */
    private ArchetypeRegistryManager archetypeRegistryManager;

    public void setArchetypeArtifactManager( ArchetypeArtifactManager archetypeArtifactManager )
    {
        this.archetypeArtifactManager = archetypeArtifactManager;
    }

    public void configureArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode,
        Properties executionProperties )
    throws ArchetypeNotDefined,
        UnknownArchetype,
        ArchetypeNotConfigured,
        IOException,
        PrompterException,
        ArchetypeGenerationConfigurationFailure
    {
        ArtifactRepository localRepository = request.getLocalRepository();

        ArtifactRepository archetypeRepository = null;

        List repositories = new ArrayList();

        Properties properties = new Properties( executionProperties );

        ArchetypeDefinition ad = new ArchetypeDefinition();

        ad.setGroupId( request.getArchetypeGroupId() );

        ad.setArtifactId( request.getArchetypeArtifactId() );

        ad.setVersion( request.getArchetypeVersion() );

        if( !ad.isDefined() )
        {
            if( !interactiveMode.booleanValue() )
            {
                throw new ArchetypeNotDefined( "No archetype was chosen" );
            }
            else
            {
                throw new ArchetypeNotDefined( "The archetype is not defined" );
            }
        }
        if( request.getArchetypeRepository() != null )
        {
            archetypeRepository = archetypeRegistryManager.createRepository( request.getArchetypeRepository(),
                    ad.getArtifactId() + "-repo" );
            repositories.add( archetypeRepository );
        }
        if( request.getRemoteArtifactRepositories() != null )
        {
            repositories.addAll( request.getRemoteArtifactRepositories() );
        }

        if( !archetypeArtifactManager.exists( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(), archetypeRepository,
                localRepository, repositories ) )
        {
            throw new UnknownArchetype( "The desired archetype does not exist (" + ad.getGroupId() + ":"
                + ad.getArtifactId() + ":" + ad.getVersion() + ")" );
        }

        request.setArchetypeVersion( ad.getVersion() );

        ArchetypeConfiguration archetypeConfiguration;

        if( archetypeArtifactManager.isFileSetArchetype( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                archetypeRepository, localRepository, repositories ) )
        {
            org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor = archetypeArtifactManager
                .getFileSetArchetypeDescriptor( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                    archetypeRepository, localRepository, repositories );

            archetypeConfiguration = archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else if( archetypeArtifactManager.isOldArchetype( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                archetypeRepository, localRepository, repositories ) )
        {
            org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor archetypeDescriptor = archetypeArtifactManager
                .getOldArchetypeDescriptor( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(), archetypeRepository,
                    localRepository, repositories );

            archetypeConfiguration = archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else
        {
            throw new ArchetypeGenerationConfigurationFailure( "The defined artifact is not an archetype" );
        }

        if( interactiveMode.booleanValue() )
        {
            boolean confirmed = false;

            while( !confirmed )
            {
                if( !archetypeConfiguration.isConfigured() )
                {
                    Iterator requiredProperties = archetypeConfiguration.getRequiredProperties().iterator();

                    while( requiredProperties.hasNext() )
                    {
                        String requiredProperty = (String) requiredProperties.next();

                        if( !archetypeConfiguration.isConfigured( requiredProperty ) )
                        {
                            archetypeConfiguration.setProperty( requiredProperty,
                                archetypeGenerationQueryer.getPropertyValue( requiredProperty,
                                    archetypeConfiguration.getDefaultValue( requiredProperty ) ) );
                        }
                    }
                }

                if( !archetypeConfiguration.isConfigured() )
                {
                    throw new ArchetypeGenerationConfigurationFailure(
                        "The archetype generation must be configured here" );
                }
                else if( !archetypeGenerationQueryer.confirmConfiguration( archetypeConfiguration ) )
                {
                    getLogger().debug( "Archetype generation configuration not confirmed" );
                    archetypeConfiguration.reset();
                    restoreCommandLineProperties( archetypeConfiguration, executionProperties );
                }
                else
                {
                    getLogger().debug( "Archetype generation configuration confirmed" );

                    confirmed = true;
                }
            }
        }
        else
        {
            if( !archetypeConfiguration.isConfigured() )
            {
                Iterator requiredProperties = archetypeConfiguration.getRequiredProperties().iterator();

                while( requiredProperties.hasNext() )
                {
                    String requiredProperty = (String) requiredProperties.next();

                    if( !archetypeConfiguration.isConfigured( requiredProperty )
                        && ( archetypeConfiguration.getDefaultValue( requiredProperty ) != null ) )
                    {
                        archetypeConfiguration.setProperty( requiredProperty,
                            archetypeConfiguration.getDefaultValue( requiredProperty ) );
                    }
                }

                // in batch mode, we assume the defaults, and if still not configured fail
                if( !archetypeConfiguration.isConfigured() )
                {
                    throw new ArchetypeNotConfigured( "Archetype " + request.getArchetypeGroupId() + ":"
                        + request.getArchetypeArtifactId() + ":" + request.getArchetypeVersion()
                        + " is not configured" );
                }
            }
        } // end if-else

        request.setGroupId( archetypeConfiguration.getProperty( Constants.GROUP_ID ) );

        request.setArtifactId( archetypeConfiguration.getProperty( Constants.ARTIFACT_ID ) );

        request.setVersion( archetypeConfiguration.getProperty( Constants.VERSION ) );

        request.setPackage( archetypeConfiguration.getProperty( Constants.PACKAGE ) );

        properties = archetypeConfiguration.getProperties();

        request.setProperties( properties );
    }

    private void restoreCommandLineProperties( ArchetypeConfiguration archetypeConfiguration,
        Properties executionProperties )
    {
        getLogger().debug( "Restoring command line properties" );

        Iterator properties = archetypeConfiguration.getRequiredProperties().iterator();
        while( properties.hasNext() )
        {
            String property = (String) properties.next();
            if( executionProperties.containsKey( property ) )
            {
                archetypeConfiguration.setProperty( property, executionProperties.getProperty( property ) );
                getLogger().debug( "Restored " + property + "=" + archetypeConfiguration.getProperty( property ) );
            }
        }
    }
}
