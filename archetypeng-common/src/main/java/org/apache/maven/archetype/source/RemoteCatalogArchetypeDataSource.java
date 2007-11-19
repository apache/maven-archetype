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

package org.apache.maven.archetype.source;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.settings.Settings;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * @plexus.component role-hint="remote-catalog"
 * @author Jason van Zyl
 */
public class RemoteCatalogArchetypeDataSource
    extends CatalogArchetypeDataSource
{
    /** @plexus.requirement */
    private WagonManager wagonManager;

    public static String REPOSITORY_PROPERTY = "repository";

    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        String repository = properties.getProperty( REPOSITORY_PROPERTY );
        
        if ( repository == null )
        {
            throw new ArchetypeDataSourceException( "To use the remote catalog you must specify the 'remote-catalog.repository' property correctly in your ~/.m2/archetype-catalog.properties file." );
        }
        
        try
        {
            if ( repository.endsWith( "/" ) )
            {
                repository = repository.substring( 0, repository.length(  ) - 1 );
            }
                        
            URL url = new URL( repository + "/" + "archetype-catalog.xml" );

            return createArchetypeMap( readCatalog( new InputStreamReader( url.openStream(  ) ) ) );
        }
        catch ( MalformedURLException e )
        {
            throw new ArchetypeDataSourceException( "Invalid URL provided for archetype registry.", e );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error reading archetype registry.", e );
        }
    }

    public void updateCatalog( Properties properties, Archetype archetype, Settings settings )
        throws ArchetypeDataSourceException
    {
        throw new ArchetypeDataSourceException( "Not supported yet." );
    }

    public ArchetypeDataSourceDescriptor getDescriptor()
    {
        ArchetypeDataSourceDescriptor d = new ArchetypeDataSourceDescriptor();

        d.addParameter( REPOSITORY_PROPERTY, String.class, "http://repo1.maven.org/maven2", "The repository URL where the archetype catalog resides." );

        return d;
    }
}
