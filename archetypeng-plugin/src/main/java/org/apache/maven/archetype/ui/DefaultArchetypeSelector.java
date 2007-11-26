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
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.ArchetypeSelectionFailure;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.exception.UnknownGroup;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
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
    private ArchetypeSelectionQueryer archetypeSelectionQueryer;

    /** @plexus.requirement role="org.apache.maven.archetype.source.ArchetypeDataSource" */
    private Map archetypeSources;

    public void selectArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode )
        throws
        ArchetypeNotDefined,
        UnknownArchetype,
        UnknownGroup,
        IOException,
        PrompterException,
        ArchetypeSelectionFailure
    {
        ArchetypeDefinition definition = new ArchetypeDefinition();
        
        definition.setArtifactId( request.getArchetypeArtifactId() );
 
        definition.setGroupId( request.getArchetypeGroupId() );
        
        definition.setVersion( request.getArchetypeVersion() );
        
        if ( interactiveMode.booleanValue() )
        {
            if ( !definition.isDefined() )
            {
                List archetypes = new ArrayList();

                File archetypeCatalogPropertiesFile = new File( System.getProperty( "user.home" ), ".m2/archetype-catalog.properties" );

                if ( archetypeCatalogPropertiesFile.exists() )
                {
                    Properties archetypeCatalogProperties = PropertyUtils.loadProperties( archetypeCatalogPropertiesFile );

                    getLogger().debug( "Using catalogs " + archetypeCatalogProperties );

                    String[] sources = StringUtils.split( archetypeCatalogProperties.getProperty( "sources" ), "," );

                    for ( int i = 0; i < sources.length; i++ )
                    {
                        String sourceRoleHint = sources[i];

                        getLogger().debug( "Reading catalog " + sourceRoleHint );

                        try
                        {
                            ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( sourceRoleHint );

                            archetypes.addAll(
                                source.getArchetypeCatalog( getArchetypeDataSourceProperties( sourceRoleHint, archetypeCatalogProperties ) ).getArchetypes() );
                        }
                        catch ( ArchetypeDataSourceException e )
                        {
                            getLogger().warn( "Unable to get archetypes from " + sourceRoleHint + " source. [" + e.getMessage() + "]" );
                        }
                    }
                }

                if ( archetypes.size() == 0 )
                {
                    getLogger().debug( "Using wiki catalog" );

                    try
                    {
                        ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( "internal-catalog" );

                        archetypes.addAll( source.getArchetypeCatalog( new Properties() ).getArchetypes() );
                    }
                    catch ( ArchetypeDataSourceException e )
                    {
                        getLogger().warn( "Unable to get archetypes from default wiki  source. [" + e.getMessage() + "]" );
                    }
                }

                if ( archetypes.size() > 0 )
                {
                    Archetype archetype = archetypeSelectionQueryer.selectArchetype( archetypes );

                    definition.setArtifactId( archetype.getArtifactId() );

                    definition.setName( archetype.getArtifactId() );

                    definition.setGroupId( archetype.getGroupId() );

                    definition.setVersion( archetype.getVersion() );

                    definition.setRepository( archetype.getRepository() );

                    String goals = StringUtils.join( archetype.getGoals().iterator(), "," );

                    definition.setGoals( goals );
                }
            }
        }

        // Make sure the groupId and artifactId are valid, the version may just default to
        // the latest release.

        if ( !definition.isPartiallyDefined() )
        {
            throw new ArchetypeSelectionFailure( "No valid archetypes could be found to choose." );
        }

        request.setArchetypeGroupId( definition.getGroupId() );

        request.setArchetypeArtifactId( definition.getArtifactId() );

        request.setArchetypeVersion( definition.getVersion() );

        request.setArchetypeGoals( definition.getGoals() );

        request.setArchetypeName( definition.getName() );

        request.setArchetypeRepository( definition.getRepository() );

        request.setRemoteRepository( definition.getRepository() );        
    }

    private Properties getArchetypeDataSourceProperties( String sourceRoleHint,
                                                         Properties archetypeCatalogProperties )
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
}
