package org.apache.maven.archetype.downloader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultDownloader
    implements Downloader
{
    /**
     * @plexus.requirement
     */
    private ArtifactResolver artifactResolver;

    /**
     * @plexus.requirement
     */
    private ArtifactFactory artifactFactory;

    public File download( String groupId,
                          String artifactId,
                          String version,
                          ArtifactRepository archetypeRepository,
                          ArtifactRepository localRepository,
                         List remoteRepositories )
        throws DownloadException, DownloadNotFoundException
   {
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, Artifact.SCOPE_RUNTIME, "jar" );

        List repositories = new ArrayList(remoteRepositories);
        if ( repositories.isEmpty() && archetypeRepository != null )
        {
            repositories.add(archetypeRepository);
        }
        else
        if ( repositories.isEmpty() && localRepository != null )
        {
            repositories.add(localRepository);
            
        }
        ArtifactRepository localRepo = localRepository;
        try
        {
            artifactResolver.resolve( artifact, repositories, localRepo );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new DownloadException( "Error downloading.", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new DownloadNotFoundException( "Requested download does not exist.", e );
        }

        return artifact.getFile();
    }
    public File downloadOld( String groupId,
                          String artifactId,
                          String version,
                          ArtifactRepository archetypeRepository,
                          ArtifactRepository localRepository,
                         List remoteRepositories )
        throws DownloadException, DownloadNotFoundException
   {
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, Artifact.SCOPE_RUNTIME, "jar" );
        try
        {
            artifactResolver.resolve( artifact, remoteRepositories, localRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new DownloadException( "Error downloading.", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new DownloadNotFoundException( "Requested download does not exist.", e );
        }

        return artifact.getFile();
    }
}
