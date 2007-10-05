package org.apache.maven.archetype.source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.PlexusContainer;

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

}