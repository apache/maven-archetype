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

package org.apache.maven.archetype;

import java.io.IOException;
import org.apache.maven.archetype.creator.ArchetypeCreator;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.apache.maven.archetype.source.WikiArchetypeDataSource;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultArchetyper
    extends AbstractLogEnabled
    implements Archetyper
{
    /** @plexus.requirement role-hint="fileset" */
    private ArchetypeCreator creator;

    /** @plexus.requirement */
    private ArchetypeGenerator generator;

    /** @plexus.requirement role="org.apache.maven.archetype.source.ArchetypeDataSource" */
    private Map archetypeSources;

    /**
     * The Jar archiver.
     *
     * @plexus.requirement role="org.codehaus.plexus.archiver.Archiver" role-hint="jar"
     */
    private JarArchiver jarArchiver;

    public ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request )
    {
        ArchetypeCreationResult result = new ArchetypeCreationResult(  );

        creator.createArchetype( request, result );

        return result;
    }

    public ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request )
    {
        ArchetypeGenerationResult result = new ArchetypeGenerationResult(  );

        generator.generateArchetype( request, result );

        return result;
    }

    public Collection getArchetypes( ArchetypeDataSource source, Properties sourceConfiguration )
        throws ArchetypeDataSourceException
    {
        return source.getArchetypes( sourceConfiguration );
    }

    public Collection getArchetypeDataSources( )
    {
        return archetypeSources.values(  );
    }

    public List getAvailableArchetypes( )
    {
        File archetypeCatalogPropertiesFile = new File( System.getProperty( "user.home" ), ".m2/archetype-catalog.properties" );

        Properties archetypeCatalogProperties;

        if ( archetypeCatalogPropertiesFile.exists(  ) )
        {
            archetypeCatalogProperties = PropertyUtils.loadProperties( archetypeCatalogPropertiesFile );
        }
        else
        {
            archetypeCatalogProperties = new Properties(  );

            archetypeCatalogProperties.setProperty( "sources", "wiki" );

            archetypeCatalogProperties.setProperty( "wiki.url", WikiArchetypeDataSource.DEFAULT_ARCHETYPE_INVENTORY_PAGE );
        }

        return getAvailableArchetypes( archetypeCatalogProperties );
    }

    public List getAvailableArchetypes( Properties archetypeCatalogProperties )
    {
        List archetypes = new ArrayList(  );

        String[] sources = StringUtils.split( archetypeCatalogProperties.getProperty( "sources" ), "," );

        for ( int i = 0; i < sources.length; i++ )
        {
            String sourceRoleHint = sources[i];

            try
            {
                ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( sourceRoleHint );

                archetypes.addAll( source.getArchetypes( getArchetypeDataSourceProperties( sourceRoleHint, archetypeCatalogProperties ) ) );
            }
            catch ( ArchetypeDataSourceException e )
            {
                // do nothing, gracefully move on
            }
        }

        // If we haven't found any Archetypes then we will currently attempt to use the Wiki source.
        // Eventually we will use a more reliable remote catalog from the central repository.
        if ( archetypes.size(  ) == 0 )
        {
            try
            {
                ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( "wiki" );

                archetypes.addAll( source.getArchetypes( new Properties(  ) ) );
            }
            catch ( ArchetypeDataSourceException e )
            {
                // do nothing, gracefully move on
            }
        }

        return archetypes;
    }

    public Properties getArchetypeDataSourceProperties( String sourceRoleHint, Properties archetypeCatalogProperties )
    {
        Properties p = new Properties(  );

        for ( Iterator i = archetypeCatalogProperties.keySet(  ).iterator(  ); i.hasNext(  ); )
        {
            String key = (String) i.next();

            if ( key.startsWith( sourceRoleHint ) )
            {
                String k = key.substring( sourceRoleHint.length(  ) + 1 );

                p.setProperty( k, archetypeCatalogProperties.getProperty( key ) );
            }
        }

        return p;
    }

    public File archiveArchetype( File archetypeDirectory, MavenProject project, File outputDirectory, String finalName, MavenArchiveConfiguration archive )
        throws ArchiverException, ManifestException,
        DependencyResolutionRequiredException, IOException
    {
        File jarFile = new File( outputDirectory, finalName + ".jar" );

        MavenArchiver archiver = new MavenArchiver(  );

        archiver.setArchiver( jarArchiver );

        archiver.setOutputFile( jarFile );

        archive.setForced( true );

        if ( !archetypeDirectory.exists(  ) )
        {
            getLogger(  ).warn( "JAR will be empty - no content was marked for inclusion!" );
        }
        else
        {
            archiver.getArchiver(  ).addDirectory( archetypeDirectory );
        }

        archiver.createArchive( project, archive );

        return jarFile;
    }
}