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
    public static String REPOSITORY_PROPERTY = "repository";

//    /**
//     * @plexus.requirement
//     */
//    private PlexusContainer container;
//
//    /**
//     * @plexus.requirement
//     */
//    private WagonManager wagonManager;

    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        try
        {
            String repository = properties.getProperty( REPOSITORY_PROPERTY );

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

//    // Stealed from the embedder in trunk on 2007-10-04
//    // then adapted
//    private void artifactTransferMechanism( MavenExecutionRequest request, Configuration configuration, Settings settings )
//        throws MavenEmbedderException
//    {
//        // ------------------------------------------------------------------------
//        // Artifact Transfer Mechanism
//        //
//        //
//        // ------------------------------------------------------------------------
//
//        if ( settings.isOffline() )
//        {
//            getLogger().info( "You are working in offline mode." );
//
//            wagonManager.setOnline( false );
//        }
//        else
//        {
//            wagonManager.findAndRegisterWagons( n );
//
//            wagonManager.setInteractive( settings.isInteractiveMode() );
//
//            wagonManager.setDownloadMonitor( request.getTransferListener() );
//
//            wagonManager.setOnline( true );
//        }
//
//        try
//        {
//            resolveParameters( settings );
//        }
//        catch ( Exception e )
//        {
//            throw new MavenEmbedderException(
//                "Unable to configure Maven for execution",
//                e );
//        }
//    }
//
//    // Stealed from the embedder in trunk on 2007-10-04
//    // then adapted
//    private void resolveParameters( Settings settings )
//        throws ComponentLookupException, ComponentLifecycleException, SettingsConfigurationException
//    {
//        WagonManager wagonManager = (WagonManager) container.lookup( WagonManager.ROLE );
//
//        try
//        {
//            Proxy proxy = settings.getActiveProxy();
//
//            if ( proxy != null )
//            {
//                if ( proxy.getHost() == null )
//                {
//                    throw new SettingsConfigurationException( "Proxy in settings.xml has no host" );
//                }
//
//                wagonManager.addProxy(
//                    proxy.getProtocol(),
//                    proxy.getHost(),
//                    proxy.getPort(),
//                    proxy.getUsername(),
//                    proxy.getPassword(),
//                    proxy.getNonProxyHosts() );
//            }
//
//            for ( Iterator i = settings.getServers().iterator(); i.hasNext(); )
//            {
//                Server server = (Server) i.next();
//
//                wagonManager.addAuthenticationInfo(
//                    server.getId(),
//                    server.getUsername(),
//                    server.getPassword(),
//                    server.getPrivateKey(),
//                    server.getPassphrase() );
//
//                wagonManager.addPermissionInfo(
//                    server.getId(),
//                    server.getFilePermissions(),
//                    server.getDirectoryPermissions() );
//
//                if ( server.getConfiguration() != null )
//                {
//                    wagonManager.addConfiguration(
//                        server.getId(),
//                        (Xpp3Dom) server.getConfiguration() );
//                }
//            }
//
//            RepositoryPermissions defaultPermissions = new RepositoryPermissions();
//
//            defaultPermissions.setDirectoryMode( "775" );
//
//            defaultPermissions.setFileMode( "664" );
//
//            wagonManager.setDefaultRepositoryPermissions( defaultPermissions );
//
//            for ( Iterator i = settings.getMirrors().iterator(); i.hasNext(); )
//            {
//                Mirror mirror = (Mirror) i.next();
//
//                wagonManager.addMirror(
//                    mirror.getId(),
//                    mirror.getMirrorOf(),
//                    mirror.getUrl() );
//            }
//        }
//        finally
//        {
//            container.release( wagonManager );
//        }
//    }

    public ArchetypeDataSourceDescriptor getDescriptor()
    {
        ArchetypeDataSourceDescriptor d = new ArchetypeDataSourceDescriptor();

        d.addParameter( REPOSITORY_PROPERTY, String.class, "http://repo1.maven.org/maven2", "The repository URL where the archetype catalog resides." );

        return d;
    }
}