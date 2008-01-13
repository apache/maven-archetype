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
import org.apache.maven.archetype.common.ArchetypeFilesResolver;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.TemplateCreationException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.codehaus.plexus.util.IOUtil;

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
                if ( !archetypeDefinition.isDefined() )//<editor-fold text="...">
                {
                    getLogger().debug( "OldArchetype is not defined" );
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
                }//</editor-fold>

                if ( !archetypeConfiguration.isConfigured() )//<editor-fold text="...">
                {
                    getLogger().debug( "OldArchetype is not configured" );
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
                }//</editor-fold>

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

        writeProperties(
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

    public void readProperties( Properties properties,
                                File propertyFile )
        throws
        IOException
    {
        getLogger().debug( "Reading property file " + propertyFile );

        InputStream is = new FileInputStream( propertyFile );

        try
        {
            properties.load( is );

            getLogger().debug( "Read " + properties.size() + " properties" );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    public void writeProperties( Properties properties,
                                 File propertyFile )
        throws
        IOException
    {
        Properties storedProperties = new Properties();
        try
        {
            readProperties( storedProperties, propertyFile );
        }
        catch ( FileNotFoundException ex )
        {
            getLogger().debug( "Property file not found. Creating a new one" );
        }

        getLogger().debug( "Adding " + properties.size() + " properties" );

        Iterator propertiesIterator = properties.keySet().iterator();
        while ( propertiesIterator.hasNext() )
        {
            String propertyKey = (String) propertiesIterator.next();
            storedProperties.setProperty( propertyKey, properties.getProperty( propertyKey ) );
        }

        OutputStream os = new FileOutputStream( propertyFile );

        try
        {
            storedProperties.store( os, "" );

            getLogger().debug( "Stored " + storedProperties.size() + " properties" );
        }
        finally
        {
            IOUtil.close( os );
        }
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
            readProperties( properties, propertyFile );
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
