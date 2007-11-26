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
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.collections.iterators.ArrayIterator;

/** @plexus.component */
public class DefaultArchetypeSelector
    extends AbstractLogEnabled
    implements ArchetypeSelector
{
    /** @plexus.requirement */
    private ArchetypeSelectionQueryer archetypeSelectionQueryer;
    /** @plexus.requirement */
    private org.apache.maven.archetype.Archetype archetype;

    /** @plexus.requirement role="org.apache.maven.archetype.source.ArchetypeDataSource" */
    private Map archetypeSources;

    public void selectArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode )
        throws
        ArchetypeNotDefined,
        UnknownArchetype,
        UnknownGroup,
        IOException,
        PrompterException,
        ArchetypeSelectionFailure{throw new UnsupportedOperationException("change method");}

    public void selectArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode, String catalogs )
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

        // buggy? what in else statement
        if ( interactiveMode.booleanValue() )
        {
            // buggy? what in else statement
            if ( !definition.isDefined() )
            {
                Map archetypes = new HashMap();

                Iterator ca = new ArrayIterator( StringUtils.split( catalogs, "," ) );
                while(ca.hasNext())
                {
                    String catalog = (String) ca.next();
                    
                    if ("internal".equalsIgnoreCase(catalog))
                    {//System.err.println("UNSING internal");
                        archetypes.put("internal", archetype.getInternalCatalog().getArchetypes());
                    }
                    else if ("local".equalsIgnoreCase(catalog))
                    {//System.err.println("UNSING local");
                        archetypes.put("local", archetype.getDefaultLocalCatalog().getArchetypes());
                    }
                    else if ("remote".equalsIgnoreCase(catalog))
                    {//System.err.println("UNSING remote");
                        archetypes.put("remote", archetype.getRemoteCatalog().getArchetypes());
                    }
                    else if (catalog.startsWith("file://"))
                    {//System.err.println("UNSING local "+catalog);
                        String path = catalog.substring(7);
                        archetypes.put("local", archetype.getLocalCatalog(path).getArchetypes());
                    }
                    else if (catalog.startsWith("http://"))
                    {//System.err.println("UNSING remote "+catalog);
                        archetypes.put("remote", archetype.getRemoteCatalog(catalog));
                    }
                }
                
                /*
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
                */

                if ( archetypes.size() == 0 )
                {
                    getLogger().debug( "Using internal catalog" );

                        archetypes.put("internal", archetype.getInternalCatalog().getArchetypes());
                }

                // buggy? what in else statement
                if ( archetypes.size() > 0 )
                {
                    Archetype selectedArchetype = archetypeSelectionQueryer.selectArchetype( archetypes );

                    definition.setArtifactId( selectedArchetype.getArtifactId() );

                    definition.setName( selectedArchetype.getArtifactId() );

                    definition.setGroupId( selectedArchetype.getGroupId() );

                    definition.setVersion( selectedArchetype.getVersion() );

                    definition.setRepository( selectedArchetype.getRepository() );

                    String goals = StringUtils.join( selectedArchetype.getGoals().iterator(), "," );

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
