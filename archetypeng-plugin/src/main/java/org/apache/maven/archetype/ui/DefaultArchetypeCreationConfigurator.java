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

import org.apache.maven.archetype.common.ArchetypeConfiguration;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.common.ArchetypeFactory;
import org.apache.maven.archetype.common.ArchetypeFilesResolver;
import org.apache.maven.archetype.common.ArchetypePropertiesManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.TemplateCreationException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/** @plexus.component */
public class DefaultArchetypeCreationConfigurator
    extends AbstractLogEnabled
    implements ArchetypeCreationConfigurator
{
    /** @plexus.requirement */
    private ArchetypeCreationQueryer archetypeCreationQueryer;

    /** @plexus.requirement */
    private ArchetypeFactory archetypeFactory;

    /** @plexus.requirement */
    private ArchetypeFilesResolver archetypeFilesResolver;

    /** @plexus.requirement */
    private ArchetypePropertiesManager archetypePropertiesManager;

    public void configureArchetypeCreation(
        MavenProject project,
        Boolean interactiveMode,
        Properties commandLineProperties,
        File propertyFile,
        List languages
    )
        throws
        IOException,
        ArchetypeNotDefined,
        ArchetypeNotConfigured,
        PrompterException,
        TemplateCreationException
    {
        Properties properties =
            initialiseArchetypeProperties( commandLineProperties, propertyFile );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition( properties );

        if ( !archetypeDefinition.isDefined() )
        {
            archetypeDefinition = defineDefaultArchetype( project, properties );
        }

        ArchetypeConfiguration archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration(
                project,
                archetypeDefinition,
                properties
            );

        String resolvedPackage =
            archetypeFilesResolver.resolvePackage( project.getBasedir(), languages );

        if ( !archetypeConfiguration.isConfigured() )
        {
            archetypeConfiguration =
                defineDefaultConfiguration(
                    project,
                    archetypeDefinition,
                    resolvedPackage,
                    properties
                );
        }

        if ( interactiveMode.booleanValue() )
        {
            getLogger().debug( "Entering interactive mode" );

            boolean confirmed = false;
            while ( !confirmed )
            {
                if ( !archetypeDefinition.isDefined() )
                {
                    getLogger().debug( "Archetype is not defined" );
                    if ( !archetypeDefinition.isGroupDefined() )
                    {
                        getLogger().debug( "Asking for archetype's groupId" );
                        archetypeDefinition.setGroupId(
                            archetypeCreationQueryer.getArchetypeGroupId( project.getGroupId() )
                        );
                    }
                    if ( !archetypeDefinition.isArtifactDefined() )
                    {
                        getLogger().debug( "Asking for archetype's artifactId" );
                        archetypeDefinition.setArtifactId(
                            archetypeCreationQueryer.getArchetypeArtifactId(
                                project.getArtifactId() + Constants.ARCHETYPE_SUFFIX
                            )
                        );
                    }
                    if ( !archetypeDefinition.isVersionDefined() )
                    {
                        getLogger().debug( "Asking for archetype's version" );
                        archetypeDefinition.setVersion(
                            archetypeCreationQueryer.getArchetypeVersion( project.getVersion() )
                        );
                    }

                    archetypeFactory.updateArchetypeConfiguration(
                        archetypeConfiguration,
                        archetypeDefinition
                    );
                }

                if ( !archetypeConfiguration.isConfigured() )
                {
                    getLogger().debug( "Archetype is not configured" );
                    if ( !archetypeConfiguration.isConfigured( Constants.GROUP_ID ) )
                    {
                        getLogger().debug( "Asking for project's groupId" );
                        archetypeConfiguration.setProperty(
                            Constants.GROUP_ID,
                            archetypeCreationQueryer.getGroupId(
                                archetypeConfiguration.getDefaultValue( Constants.GROUP_ID )
                            )
                        );
                    }
                    if ( !archetypeConfiguration.isConfigured( Constants.ARTIFACT_ID ) )
                    {
                        getLogger().debug( "Asking for project's artifactId" );
                        archetypeConfiguration.setProperty(
                            Constants.ARTIFACT_ID,
                            archetypeCreationQueryer.getArtifactId(
                                archetypeConfiguration.getDefaultValue( Constants.ARTIFACT_ID )
                            )
                        );
                    }
                    if ( !archetypeConfiguration.isConfigured( Constants.VERSION ) )
                    {
                        getLogger().debug( "Asking for project's version" );
                        archetypeConfiguration.setProperty(
                            Constants.VERSION,
                            archetypeCreationQueryer.getVersion(
                                archetypeConfiguration.getDefaultValue( Constants.VERSION )
                            )
                        );
                    }
                    if ( !archetypeConfiguration.isConfigured( Constants.PACKAGE ) )
                    {
                        getLogger().debug( "Asking for project's package" );
                        archetypeConfiguration.setProperty(
                            Constants.PACKAGE,
                            archetypeCreationQueryer.getPackage(
                                StringUtils.isEmpty( resolvedPackage )
                                    ? archetypeConfiguration.getDefaultValue( Constants.PACKAGE )
                                    : resolvedPackage
                            )
                        );
                    }
                } // end if

                boolean stopAddingProperties = false;
                while ( !stopAddingProperties )
                {
                    getLogger().debug( "Asking for another required property" );
                    stopAddingProperties = !archetypeCreationQueryer.askAddAnotherProperty();

                    if ( !stopAddingProperties )
                    {
                        getLogger().debug( "Asking for required property key" );

                        String propertyKey = archetypeCreationQueryer.askNewPropertyKey();
                        getLogger().debug( "Asking for required property value" );

                        String replacementValue =
                            archetypeCreationQueryer.askReplacementValue(
                                propertyKey,
                                archetypeConfiguration.getDefaultValue( propertyKey )
                            );
                        archetypeConfiguration.setDefaultProperty( propertyKey, replacementValue );
                        archetypeConfiguration.setProperty( propertyKey, replacementValue );
                    }
                }

                getLogger().debug( "Asking for configuration confirmation" );
                if ( archetypeCreationQueryer.confirmConfiguration( archetypeConfiguration ) )
                {
                    confirmed = true;
                }
                else
                {
                    getLogger().debug( "Reseting archetype's definition and configuration" );
                    archetypeConfiguration.reset();
                    archetypeDefinition.reset();
                }
            } // end while
        }
        else
        {
            getLogger().debug( "Entering batch mode" );
            if ( !archetypeDefinition.isDefined() )
            {
                throw new ArchetypeNotDefined( "The archetype is not defined" );
            }
            else if ( !archetypeConfiguration.isConfigured() )
            {
                throw new ArchetypeNotConfigured( "The archetype is not configured" );
            }
        } // end if

        archetypePropertiesManager.writeProperties(
            archetypeConfiguration.toProperties(),
            propertyFile
        );
    }

    private ArchetypeDefinition defineDefaultArchetype(
        MavenProject project,
        Properties properties
    )
    {
        if ( StringUtils.isEmpty( properties.getProperty( Constants.ARCHETYPE_GROUP_ID ) ) )
        {
            getLogger().info( "Setting default archetype's groupId: " + project.getGroupId() );
            properties.setProperty( Constants.ARCHETYPE_GROUP_ID, project.getGroupId() );
        }
        if ( StringUtils.isEmpty( properties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID ) ) )
        {
            getLogger().info(
                "Setting default archetype's artifactId: " + project.getArtifactId()
            );
            properties.setProperty(
                Constants.ARCHETYPE_ARTIFACT_ID,
                project.getArtifactId() + "-archetype"
            );
        }
        if ( StringUtils.isEmpty( properties.getProperty( Constants.ARCHETYPE_VERSION ) ) )
        {
            getLogger().info( "Setting default archetype's version: " + project.getVersion() );
            properties.setProperty( Constants.ARCHETYPE_VERSION, project.getVersion() );
        }

        return archetypeFactory.createArchetypeDefinition( properties );
    }

    private ArchetypeConfiguration defineDefaultConfiguration(
        MavenProject project,
        ArchetypeDefinition archetypeDefinition,
        String resolvedPackage,
        Properties properties
    )
    {
        if ( StringUtils.isEmpty( properties.getProperty( Constants.GROUP_ID ) ) )
        {
            getLogger().info( "Setting default groupId: " + project.getGroupId() );
            properties.setProperty( Constants.GROUP_ID, project.getGroupId() );
        }

        if ( StringUtils.isEmpty( properties.getProperty( Constants.ARTIFACT_ID ) ) )
        {
            getLogger().info( "Setting default artifactId: " + project.getArtifactId() );
            properties.setProperty( Constants.ARTIFACT_ID, project.getArtifactId() );
        }

        if ( StringUtils.isEmpty( properties.getProperty( Constants.VERSION ) ) )
        {
            getLogger().info( "Setting default version: " + project.getVersion() );
            properties.setProperty( Constants.VERSION, project.getVersion() );
        }

        if ( StringUtils.isEmpty(
            properties.getProperty(
                Constants.PACKAGE/*,
                    properties.getProperty ( Constants.PACKAGE_NAME )*/
            )
        )
            )
        {
            if ( StringUtils.isEmpty( resolvedPackage ) )
            {
                resolvedPackage = project.getGroupId();
            }
            getLogger().info( "Setting default package: " + resolvedPackage );
            /*properties.setProperty ( Constants.PACKAGE_NAME, resolvedPackage );*/
            properties.setProperty( Constants.PACKAGE, resolvedPackage );
        }

        return
            archetypeFactory.createArchetypeConfiguration(
                project,
                archetypeDefinition,
                properties
            );
    }

    private Properties initialiseArchetypeProperties(
        Properties commandLineProperties,
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
        catch ( FileNotFoundException ex )
        {
            getLogger().debug( "archetype.properties does not exist" );
        }

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
}
