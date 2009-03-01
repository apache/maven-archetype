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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.codehaus.plexus.util.StringUtils;

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

        if ( !ad.isDefined() )
        {
            if ( !interactiveMode.booleanValue() )
            {
                throw new ArchetypeNotDefined( "No archetype was chosen" );
            }
            else
            {
                throw new ArchetypeNotDefined( "The archetype is not defined" );
            }
        }
        if ( request.getArchetypeRepository() != null )
        {
            archetypeRepository = archetypeRegistryManager.createRepository( request.getArchetypeRepository(),
                    ad.getArtifactId() + "-repo" );
            repositories.add( archetypeRepository );
        }
        if ( request.getRemoteArtifactRepositories() != null )
        {
            repositories.addAll( request.getRemoteArtifactRepositories() );
        }

        if ( !archetypeArtifactManager.exists( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                                               archetypeRepository, localRepository, repositories ) )
        {
            throw new UnknownArchetype( "The desired archetype does not exist (" + ad.getGroupId() + ":"
                + ad.getArtifactId() + ":" + ad.getVersion() + ")" );
        }

        request.setArchetypeVersion( ad.getVersion() );

        ArchetypeConfiguration archetypeConfiguration;

        if ( archetypeArtifactManager.isFileSetArchetype( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                archetypeRepository, localRepository, repositories ) )
        {
            org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor = archetypeArtifactManager
                .getFileSetArchetypeDescriptor( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                    archetypeRepository, localRepository, repositories );

            archetypeConfiguration = archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else if ( archetypeArtifactManager.isOldArchetype( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
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

        if ( interactiveMode.booleanValue() )
        {
            boolean confirmed = false;

            while ( !confirmed )
            {
                List propertiesRequired = archetypeConfiguration.getRequiredProperties();
                getLogger().debug( "Required properties before content sort: " + propertiesRequired );
                Collections.sort( propertiesRequired, new RequiredPropertyComparator( archetypeConfiguration ) );
                getLogger().debug( "Required properties after content sort: " + propertiesRequired );
                    Iterator requiredProperties = propertiesRequired.iterator();
                if ( !archetypeConfiguration.isConfigured() )
                {
                    while ( requiredProperties.hasNext() )
                    {
                        String requiredProperty = (String) requiredProperties.next();

                        if ( !archetypeConfiguration.isConfigured( requiredProperty ) )
                        {
                            if ( "package".equals(requiredProperty) )
                            {
                                // if the asked property is 'package', then
                                // use its default and if not defined,
                                // use the 'groupId' property value.
                                String packageDefault = archetypeConfiguration.getDefaultValue( requiredProperty );
                                packageDefault = ( null == packageDefault || "".equals( packageDefault ) ) ?
                                    archetypeConfiguration.getProperty( "groupId" ) :
                                    archetypeConfiguration.getDefaultValue( requiredProperty );

                                archetypeConfiguration.setProperty( requiredProperty,
                                    archetypeGenerationQueryer.getPropertyValue( requiredProperty,
                                        getTransitiveDefaultValue(packageDefault, archetypeConfiguration) ) );
                            }
                            else
                            {
                                archetypeConfiguration.setProperty( requiredProperty,
                                    archetypeGenerationQueryer.getPropertyValue( requiredProperty,
                                        getTransitiveDefaultValue( archetypeConfiguration.getDefaultValue( requiredProperty ), archetypeConfiguration ) ) );
                            }
                        }
                        else
                        {
                            getLogger().info( "Using property: " + requiredProperty + " = " + archetypeConfiguration.getProperty( requiredProperty) );
                        }
                    }
                }
                else
                {

                    while ( requiredProperties.hasNext() )
                    {
                        String requiredProperty = (String) requiredProperties.next();
                        getLogger().info( "Using property: " + requiredProperty + " = " + archetypeConfiguration.getProperty( requiredProperty) );
                    }
                }

                if ( !archetypeConfiguration.isConfigured() )
                {
                    getLogger().warn( "Archetype is not fully configured" );
                }
                else if ( !archetypeGenerationQueryer.confirmConfiguration( archetypeConfiguration ) )
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
            if ( !archetypeConfiguration.isConfigured() )
            {
                Iterator requiredProperties = archetypeConfiguration.getRequiredProperties().iterator();

                while ( requiredProperties.hasNext() )
                {
                    String requiredProperty = (String) requiredProperties.next();

                    if ( !archetypeConfiguration.isConfigured( requiredProperty )
                        && ( archetypeConfiguration.getDefaultValue( requiredProperty ) != null ) )
                    {
                        archetypeConfiguration.setProperty( requiredProperty,
                            archetypeConfiguration.getDefaultValue( requiredProperty ) );
                    }
                }

                // in batch mode, we assume the defaults, and if still not configured fail
                if ( !archetypeConfiguration.isConfigured() )
                {
                    StringBuffer exceptionMessage = new StringBuffer();
                    exceptionMessage.append( "Archetype " );
                    exceptionMessage.append( request.getArchetypeGroupId() );
                    exceptionMessage.append( ":" );
                    exceptionMessage.append( request.getArchetypeArtifactId() );
                    exceptionMessage.append( ":" );
                    exceptionMessage.append( request.getArchetypeVersion() );
                    exceptionMessage.append( " is not configured" );

                    List missingProperties = new ArrayList( 0 );
                    requiredProperties = archetypeConfiguration.getRequiredProperties().iterator();
                    while ( requiredProperties.hasNext() )
                    {
                        String requiredProperty = (String)requiredProperties.next();
                        if ( !archetypeConfiguration.isConfigured( requiredProperty ) )
                        {
                            exceptionMessage.append( "\n\tProperty " );
                            exceptionMessage.append( requiredProperty );
                            missingProperties.add( requiredProperty );
                            exceptionMessage.append( " is missing." );
                            getLogger().warn( "Property " + requiredProperty
                                + " is missing. Add -D" + requiredProperty + "=someValue" );
                        }
                    }

                    throw new ArchetypeNotConfigured( exceptionMessage.toString(), missingProperties );
                }
            }
        }

        request.setGroupId( archetypeConfiguration.getProperty( Constants.GROUP_ID ) );

        request.setArtifactId( archetypeConfiguration.getProperty( Constants.ARTIFACT_ID ) );

        request.setVersion( archetypeConfiguration.getProperty( Constants.VERSION ) );

        request.setPackage( archetypeConfiguration.getProperty( Constants.PACKAGE ) );

        properties = archetypeConfiguration.getProperties();

        request.setProperties( properties );
    }

    private String getTransitiveDefaultValue( String defaultValue, ArchetypeConfiguration archetypeConfiguration )
    {
        String result = defaultValue;
        if ( null == result )
        {
            return null;
        }
        Iterator requiredProperties = archetypeConfiguration.getRequiredProperties().iterator();
        while ( requiredProperties.hasNext() )
        {
            String property = (String) requiredProperties.next();
            if ( result.indexOf( "${" + property + "}" ) >= 0 )
            {
                result = StringUtils.replace( result, "${" + property + "}",
                                              archetypeConfiguration.getProperty(property) );
            }
        }
        return result;
    }

    private void restoreCommandLineProperties( ArchetypeConfiguration archetypeConfiguration,
        Properties executionProperties )
    {
        getLogger().debug( "Restoring command line properties" );

        Iterator properties = archetypeConfiguration.getRequiredProperties().iterator();
        while ( properties.hasNext() )
        {
            String property = (String) properties.next();
            if ( executionProperties.containsKey( property ) )
            {
                archetypeConfiguration.setProperty( property, executionProperties.getProperty( property ) );
                getLogger().debug( "Restored " + property + "=" + archetypeConfiguration.getProperty( property ) );
            }
        }
    }

    public static class RequiredPropertyComparator implements Comparator
    {
        private final ArchetypeConfiguration archetypeConfiguration;

        public RequiredPropertyComparator( ArchetypeConfiguration archetypeConfiguration )
        {
            this.archetypeConfiguration = archetypeConfiguration;
        }

        public int compare( Object left, Object right )
        {
            if ( !( left instanceof String ) || !( right instanceof String ) )
            {
                return 0;
            }
            else
            {
                String leftDefault = archetypeConfiguration.getDefaultValue( (String) left );
                String rightDefault = archetypeConfiguration.getDefaultValue( (String) right );
                if ( null == leftDefault || null == rightDefault )
                {
                    return comparePropertyName( (String) left, (String) right );
                }
                else if ( leftDefault.indexOf( "${" + right + "}" ) >= 0 )
                {//left contains right
                    return 1;
                }
                else if ( rightDefault.indexOf( "${" + left + "}" ) >= 0 )
                {//right contains left
                    return -1;
                }
                else
                {
                    return comparePropertyName( (String) left, (String) right );
                }
            }
        }

        private int comparePropertyName( String left, String right )
        {
            if ( "groupId".equals( left ) )
            {
                return -1;
            }
            if ( "groupId".equals( right ) )
            {
                return 1;
            }
            if ( "artifactId".equals( left ) )
            {
                return -1;
            }
            if ( "artifactId".equals( right ) )
            {
                return 1;
            }
            if ( "version".equals( left ) )
            {
                return -1;
            }
            if ( "version".equals( right ) )
            {
                return 1;
            }
            if ( "package".equals( left ) )
            {
                return -1;
            }
            if ( "package".equals( right ) )
            {
                return 1;
            }
            return left.compareTo( right );
        }
    }
}
