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
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// TODO: this seems to have more responsibilities than just a configurator
@Component( role = ArchetypeGenerationConfigurator.class, hint = "default" )
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

    /**
     * Determines whether the layout is legacy or not.
     */
    @Requirement
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;

    public void setArchetypeArtifactManager( ArchetypeArtifactManager archetypeArtifactManager )
    {
        this.archetypeArtifactManager = archetypeArtifactManager;
    }

    @Override
    public void configureArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode,
                                    Properties executionProperties )
        throws ArchetypeNotDefined, UnknownArchetype, ArchetypeNotConfigured, IOException, PrompterException,
        ArchetypeGenerationConfigurationFailure
    {
        ArtifactRepository localRepository = request.getLocalRepository();

        ArtifactRepository archetypeRepository = null;

        List<ArtifactRepository> repositories = new ArrayList<>();

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
            archetypeRepository = createRepository( request.getArchetypeRepository(),
                                                                             ad.getArtifactId() + "-repo" );
            repositories.add( archetypeRepository );
        }
        if ( request.getRemoteArtifactRepositories() != null )
        {
            repositories.addAll( request.getRemoteArtifactRepositories() );
        }

        if ( !archetypeArtifactManager.exists( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                                               archetypeRepository, localRepository, repositories,
                                               request.getProjectBuildingRequest() ) )
        {
            throw new UnknownArchetype( "The desired archetype does not exist (" + ad.getGroupId() + ":"
                + ad.getArtifactId() + ":" + ad.getVersion() + ")" );
        }

        request.setArchetypeVersion( ad.getVersion() );

        ArchetypeConfiguration archetypeConfiguration;

        if ( archetypeArtifactManager.isFileSetArchetype( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                                                          archetypeRepository, localRepository, repositories,
                                                          request.getProjectBuildingRequest() ) )
        {
            org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor =
                archetypeArtifactManager.getFileSetArchetypeDescriptor( ad.getGroupId(), ad.getArtifactId(),
                                                                        ad.getVersion(), archetypeRepository,
                                                                        localRepository, repositories,
                                                                        request.getProjectBuildingRequest() );

            archetypeConfiguration = archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else if ( archetypeArtifactManager.isOldArchetype( ad.getGroupId(), ad.getArtifactId(), ad.getVersion(),
                                                           archetypeRepository, localRepository, repositories,
                                                           request.getProjectBuildingRequest() ) )
        {
            org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor archetypeDescriptor =
                archetypeArtifactManager.getOldArchetypeDescriptor( ad.getGroupId(), ad.getArtifactId(),
                                                                    ad.getVersion(), archetypeRepository,
                                                                    localRepository, repositories,
                                                                    request.getProjectBuildingRequest() );

            archetypeConfiguration = archetypeFactory.createArchetypeConfiguration( archetypeDescriptor, properties );
        }
        else
        {
            throw new ArchetypeGenerationConfigurationFailure( "The defined artifact is not an archetype" );
        }

        List<String> propertiesRequired = archetypeConfiguration.getRequiredProperties();
        Collections.sort( propertiesRequired, new RequiredPropertyComparator( archetypeConfiguration ) );

        Context context = new VelocityContext();
        if ( interactiveMode.booleanValue() )
        {
            boolean confirmed = false;
            context.put( Constants.GROUP_ID, ad.getGroupId() );
            context.put( Constants.ARTIFACT_ID, ad.getArtifactId() );
            context.put( Constants.VERSION, ad.getVersion() );
            while ( !confirmed )
            {
                if ( archetypeConfiguration.isConfigured() )
                {
                    for ( String requiredProperty : propertiesRequired )
                    {
                        getLogger().info(
                            "Using property: " + requiredProperty + " = " + archetypeConfiguration.getProperty(
                                requiredProperty ) );
                    }
                }
                else
                {
                    for ( String requiredProperty : propertiesRequired )
                    {
                        String value;

                        if ( archetypeConfiguration.isConfigured( requiredProperty ) && !request.isAskForDefaultPropertyValues() )
                        {
                            getLogger().info(
                                "Using property: " + requiredProperty + " = " + archetypeConfiguration.getProperty(
                                    requiredProperty ) );

                            value = archetypeConfiguration.getProperty( requiredProperty );
                        }
                        else
                        {
                            String defaultValue = archetypeConfiguration.getDefaultValue( requiredProperty );

                            if ( Constants.PACKAGE.equals( requiredProperty ) && StringUtils.isEmpty( defaultValue ) )
                            {
                                defaultValue = archetypeConfiguration.getProperty( Constants.GROUP_ID );
                            }
                            value = archetypeGenerationQueryer.getPropertyValue( requiredProperty,
                                            expandEmbeddedTemplateExpressions( defaultValue, requiredProperty, context ),
                                            archetypeConfiguration.getPropertyValidationRegex( requiredProperty ) );
                        }

                        archetypeConfiguration.setProperty( requiredProperty, value );

                        context.put( requiredProperty, value );
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
                for ( String requiredProperty : propertiesRequired )
                {
                    if ( archetypeConfiguration.isConfigured( requiredProperty ) )
                    {
                        context.put( requiredProperty, archetypeConfiguration.getProperty( requiredProperty ) );
                    }
                    else
                    {
                        String defaultValue = archetypeConfiguration.getDefaultValue( requiredProperty );

                        if ( defaultValue != null )
                        {
                            String value = expandEmbeddedTemplateExpressions( defaultValue, requiredProperty, context );
                            archetypeConfiguration.setProperty( requiredProperty, value );
                            context.put( requiredProperty, value );
                        }
                    }
                }

                // in batch mode, we assume the defaults, and if still not configured fail
                if ( !archetypeConfiguration.isConfigured() )
                {
                    StringBuilder exceptionMessage = new StringBuilder();
                    exceptionMessage.append( "Archetype " );
                    exceptionMessage.append( request.getArchetypeGroupId() );
                    exceptionMessage.append( ":" );
                    exceptionMessage.append( request.getArchetypeArtifactId() );
                    exceptionMessage.append( ":" );
                    exceptionMessage.append( request.getArchetypeVersion() );
                    exceptionMessage.append( " is not configured" );

                    List<String> missingProperties = new ArrayList<>( 0 );
                    for ( String requiredProperty : archetypeConfiguration.getRequiredProperties() )
                    {
                        if ( !archetypeConfiguration.isConfigured( requiredProperty ) )
                        {
                            exceptionMessage.append( "\n\tProperty " );
                            exceptionMessage.append( requiredProperty );
                            missingProperties.add( requiredProperty );
                            exceptionMessage.append( " is missing." );
                            getLogger().warn( "Property " + requiredProperty + " is missing. Add -D" + requiredProperty
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

    private static String expandEmbeddedTemplateExpressions( String originalText, String textDescription, Context context )
    {
        if ( StringUtils.contains( originalText, "${" ) )
        {
            try ( StringWriter target = new StringWriter() )
            {
                Velocity.evaluate( context, target, textDescription, originalText );
                return target.toString();
            }
            catch ( IOException ex )
            {
                // closing StringWriter shouldn't actually generate any exception
                throw new RuntimeException( "Exception closing StringWriter", ex );
            }
        }
        return originalText;
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

    void setArchetypeGenerationQueryer( ArchetypeGenerationQueryer archetypeGenerationQueryer )
    {
        this.archetypeGenerationQueryer = archetypeGenerationQueryer;
    }

    public static class RequiredPropertyComparator
        implements Comparator<String>
    {
        private final ArchetypeConfiguration archetypeConfiguration;

        private Map<String, Set<String>> propertyReferenceMap;

        public RequiredPropertyComparator( ArchetypeConfiguration archetypeConfiguration )
        {
            this.archetypeConfiguration = archetypeConfiguration;
            propertyReferenceMap = computePropertyReferences();
        }

        @Override
        public int compare( String left, String right )
        {
            if ( references( right, left ) )
            {
                return 1;
            }

            if ( references( left, right ) )
            {
                return -1;
            }

            return Integer.compare( propertyReferenceMap.get( left ).size(), propertyReferenceMap.get( right ).size() );
        }

        private Map<String, Set<String>> computePropertyReferences()
        {
            Map<String, Set<String>> result = new HashMap<>();

            List<String> requiredProperties = archetypeConfiguration.getRequiredProperties();

            final InternalContextAdapterImpl velocityContextAdapter =
                            new InternalContextAdapterImpl( new VelocityContext() );

            final RuntimeServices velocityRuntime = RuntimeSingleton.getRuntimeServices();

            for ( String propertyName : requiredProperties )
            {
                final Set<String> referencedPropertyNames = new LinkedHashSet<>();

                String defaultValue = archetypeConfiguration.getDefaultValue( propertyName );
                if ( StringUtils.contains( defaultValue, "${" ) )
                {
                    try
                    {
                        final boolean dumpNamespace = false;
                        SimpleNode node = RuntimeSingleton.parse(
                                        new StringReader( defaultValue ), propertyName + ".default", dumpNamespace );

                        node.init( velocityContextAdapter, velocityRuntime );

                        node.jjtAccept( new BaseVisitor()
                        {
                            @Override
                            public Object visit( ASTReference node, Object data )
                            {
                                referencedPropertyNames.add( node.getRootString() );
                                return super.visit( node, data );
                            }
                        }, velocityRuntime );
                    }
                    catch ( ParseException e )
                    {
                        throw new IllegalStateException( "Unparsable default value for property " + propertyName, e );
                    }
                }

                referencedPropertyNames.retainAll( archetypeConfiguration.getRequiredProperties() );

                // handle the case that a property expression #set()s itself:
                referencedPropertyNames.remove( propertyName );
                result.put( propertyName, referencedPropertyNames );
            }

            return result;
        }

        /**
         * Learn whether one property references another. Semantically, "references
         * {@code targetProperty}, {@code sourceProperty} (does)."
         *
         * @param targetProperty {@link String} denoting property for which the state of
         *        being-referenced-by-the-property-denoted-by {@code sourceProperty} is desired
         * @param sourceProperty {@link String} denoting property for which the state of
         *        references-the-property-denoted-by {@code targetProperty} is desired
         * @return {@code boolean}
         */
        private boolean references( String targetProperty, String sourceProperty )
        {
            if ( targetProperty.equals( sourceProperty ) )
            {
                return false;
            }
            synchronized ( this )
            {
                if ( ! propertyReferenceMap.containsKey( sourceProperty ) )
                // something has changed
                {
                   this.propertyReferenceMap = computePropertyReferences(); 
                }
            }
            Set<String> referencedProperties = propertyReferenceMap.get( sourceProperty );
            if ( referencedProperties.contains( targetProperty ) )
            {
                return true;
            }
            for ( String referencedProperty : referencedProperties )
            {
                if ( references( targetProperty, referencedProperty ) )
                {
                    return true;
                }
            }
            return false;
        }
    }
    
    private ArtifactRepository createRepository( String url, String repositoryId )
    {
        
        
        // snapshots vs releases
        // offline = to turning the update policy off

        // TODO: we'll need to allow finer grained creation of repositories but this will do for now

        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;

        ArtifactRepositoryPolicy snapshotsPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );

        ArtifactRepositoryPolicy releasesPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );
        
        return new MavenArtifactRepository( repositoryId, url, defaultArtifactRepositoryLayout, snapshotsPolicy,
                                            releasesPolicy );
    }

}
