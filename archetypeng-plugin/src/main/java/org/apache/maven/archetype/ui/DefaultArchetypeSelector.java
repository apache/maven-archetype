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

import org.apache.maven.archetype.catalog.Archetype;
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
import org.apache.maven.artifact.repository.ArtifactRepository;
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
            if ( !archetypeDefinition.isDefined() )
            {
                List archetypes = new ArrayList();

                try
                {
                    File archetypeCatalogPropertiesFile = new File( System.getProperty( "user.home" ), ".m2/archetype-catalog.properties" );

                    if ( archetypeCatalogPropertiesFile.exists() )
                    {
                        Properties archetypeCatalogProperties = PropertyUtils.loadProperties( archetypeCatalogPropertiesFile );

                        String[] sources = StringUtils.split( archetypeCatalogProperties.getProperty( "sources" ), "," );

                        for ( int i = 0; i < sources.length; i++ )
                        {
                            String sourceRoleHint = sources[i];

                            ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( sourceRoleHint );

                            archetypes.addAll( source.getArchetypes( getArchetypeSourceProperties( sourceRoleHint, archetypeCatalogProperties ) ) );
                        }
                    }
                    else
                    {
                        ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( "wiki" );

                        archetypes.addAll( source.getArchetypes( new Properties() ) );
                    }
                }
                catch ( ArchetypeDataSourceException e )
                {
                    throw new ArchetypeSelectionFailure( "Error loading archetypes from data source(s).", e );
                }

                if ( archetypes.size() > 0 )
                {
                    Archetype archetype = archetypeSelectionQueryer.selectArchetype( archetypes );

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

        // Whether we are in batch or interactive mode we must take the repository from the definition
        // and put it into the list of repositories that we will search.

        repositories.add(
            archetypeRegistryManager.createRepository( archetypeDefinition.getRepository(), archetypeDefinition.getArtifactId() + "-repo" ) );

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

    private Properties getArchetypeSourceProperties( String sourceRoleHint, Properties archetypeCatalogProperties )
    {
        Properties p = new Properties();

        for ( Iterator i = archetypeCatalogProperties.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();

            if ( key.startsWith( sourceRoleHint ) )
            {
                String k = key.substring( sourceRoleHint.length() + 1 );

                p.setProperty( k, archetypeCatalogProperties.getProperty( key ) );
            }
        }

        return p;
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

        properties.setProperty (
            Constants.ARCHETYPE_REPOSITORY,
            (org.codehaus.plexus.util.StringUtils.isNotEmpty( ad.getRepository() ) ? ad.getRepository() : "" )
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
