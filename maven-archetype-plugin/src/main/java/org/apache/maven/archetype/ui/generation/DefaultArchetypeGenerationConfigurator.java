package org.apache.maven.archetype.ui.generation;

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
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeGenerationConfigurationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.old.OldArchetype;
import org.apache.maven.archetype.ui.ArchetypeConfiguration;
import org.apache.maven.archetype.ui.ArchetypeDefinition;
import org.apache.maven.archetype.ui.ArchetypeFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

// TODO: this seems to have more responsibilities than just a configurator
@Component( role = ArchetypeGenerationConfigurator.class )
public class DefaultArchetypeGenerationConfigurator
    extends AbstractLogEnabled
    implements ArchetypeGenerationConfigurator
{
    @Requirement
    OldArchetype oldArchetype;

    @Requirement
    private ArchetypeArtifactManager archetypeArtifactManager;

    @Requirement
    private ArchetypeFactory archetypeFactory;

    @Requirement
    private ArchetypeGenerationQueryer archetypeGenerationQueryer;

    @Requirement
    private ArchetypeRegistryManager archetypeRegistryManager;

    public void setArchetypeArtifactManager( ArchetypeArtifactManager archetypeArtifactManager )
    {
        this.archetypeArtifactManager = archetypeArtifactManager;
    }

    public void configureArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode,
                                    Properties executionProperties )
        throws ArchetypeNotDefined, UnknownArchetype, ArchetypeNotConfigured, IOException, PrompterException,
        ArchetypeGenerationConfigurationFailure
    {
        ArtifactRepository localRepository = request.getLocalRepository();

        ArtifactRepository archetypeRepository = null;

        List<ArtifactRepository> repositories = new ArrayList<ArtifactRepository>();

        Properties properties = new Properties( executionProperties );

        ArchetypeDefinition ad = new ArchetypeDefinition( request );

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
            archetypeRepository =
                archetypeRegistryManager.createRepository( request.getArchetypeRepository(),
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
            org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor =
                archetypeArtifactManager.getFileSetArchetypeDescriptor( ad.getGroupId(), ad.getArtifactId(),
                                                                        ad.getVersion(), archetypeRepository,
                                                                        localRepository, repositories );

            archetypeConfiguration = archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else if ( archetypeArtifactManager.isOldArchetype( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                                                           archetypeRepository, localRepository, repositories ) )
        {
            org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor archetypeDescriptor =
                archetypeArtifactManager.getOldArchetypeDescriptor( ad.getGroupId(), ad.getArtifactId(),
                                                                    ad.getVersion(), archetypeRepository,
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
                List<String> propertiesRequired = archetypeConfiguration.getRequiredProperties();
                getLogger().debug( "Required properties before content sort: " + propertiesRequired );
                Collections.sort( propertiesRequired, new RequiredPropertyComparator( archetypeConfiguration ) );
                getLogger().debug( "Required properties after content sort: " + propertiesRequired );

                if ( !archetypeConfiguration.isConfigured() )
                {
                    for ( String requiredProperty : propertiesRequired )
                    {
                        if ( !archetypeConfiguration.isConfigured( requiredProperty ) )
                        {
                            if ( "package".equals( requiredProperty ) )
                            {
                                // if the asked property is 'package', then
                                // use its default and if not defined,
                                // use the 'groupId' property value.
                                String packageDefault = archetypeConfiguration.getDefaultValue( requiredProperty );
                                packageDefault =
                                    ( null == packageDefault || "".equals( packageDefault ) ) ? archetypeConfiguration.getProperty( "groupId" )
                                                    : archetypeConfiguration.getDefaultValue( requiredProperty );

                                String value = getTransitiveDefaultValue( packageDefault, archetypeConfiguration );

                                value = archetypeGenerationQueryer.getPropertyValue( requiredProperty, value );

                                archetypeConfiguration.setProperty( requiredProperty, value );
                            }
                            else
                            {
                                String value = archetypeConfiguration.getDefaultValue( requiredProperty );

                                value = getTransitiveDefaultValue( value, archetypeConfiguration );

                                value = archetypeGenerationQueryer.getPropertyValue( requiredProperty, value );

                                archetypeConfiguration.setProperty( requiredProperty, value );
                            }
                        }
                        else
                        {
                            getLogger().info(
                                              "Using property: " + requiredProperty + " = "
                                                  + archetypeConfiguration.getProperty( requiredProperty ) );
                        }
                    }
                }
                else
                {

                    for ( String requiredProperty : propertiesRequired )
                    {
                        getLogger().info(
                                          "Using property: " + requiredProperty + " = "
                                              + archetypeConfiguration.getProperty( requiredProperty ) );
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
                for ( String requiredProperty : archetypeConfiguration.getRequiredProperties() )
                {
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

                    List<String> missingProperties = new ArrayList<String>( 0 );
                    for ( String requiredProperty : archetypeConfiguration.getRequiredProperties() )
                    {
                        if ( !archetypeConfiguration.isConfigured( requiredProperty ) )
                        {
                            exceptionMessage.append( "\n\tProperty " );
                            exceptionMessage.append( requiredProperty );
                            missingProperties.add( requiredProperty );
                            exceptionMessage.append( " is missing." );
                            getLogger().warn(
                                              "Property " + requiredProperty + " is missing. Add -D" + requiredProperty
                                                  + "=someValue" );
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
        for ( String property : archetypeConfiguration.getRequiredProperties() )
        {
            if ( result.indexOf( "${" + property + "}" ) >= 0 )
            {
                result = StringUtils.replace( result, "${" + property + "}",
                                              archetypeConfiguration.getProperty( property ) );
            }
        }
        return result;
    }

    private void restoreCommandLineProperties( ArchetypeConfiguration archetypeConfiguration,
                                               Properties executionProperties )
    {
        getLogger().debug( "Restoring command line properties" );

        for ( String property : archetypeConfiguration.getRequiredProperties() )
        {
            if ( executionProperties.containsKey( property ) )
            {
                archetypeConfiguration.setProperty( property, executionProperties.getProperty( property ) );
                getLogger().debug( "Restored " + property + "=" + archetypeConfiguration.getProperty( property ) );
            }
        }
    }

    public static class RequiredPropertyComparator
        implements Comparator<String>
    {
        private final ArchetypeConfiguration archetypeConfiguration;

        public RequiredPropertyComparator( ArchetypeConfiguration archetypeConfiguration )
        {
            this.archetypeConfiguration = archetypeConfiguration;
        }

        public int compare( String left, String right )
        {
            String leftDefault = archetypeConfiguration.getDefaultValue( left );

            if ( ( leftDefault != null ) && leftDefault.indexOf( "${" + right + "}" ) >= 0 )
            { //left contains right
                return 1;
            }

            String rightDefault = archetypeConfiguration.getDefaultValue( right );

            if ( ( rightDefault != null ) && rightDefault.indexOf( "${" + left + "}" ) >= 0 )
            { //right contains left
                return -1;
            }

            return comparePropertyName( left, right );
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
