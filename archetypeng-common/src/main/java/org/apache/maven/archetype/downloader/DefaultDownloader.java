package org.apache.maven.archetype.downloader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;

import java.io.File;
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
                          File localRepository,
                          String[] remoteRepositories )
        throws DownloadException, DownloadNotFoundException

    {
        return download( groupId, artifactId, version, archetypeRepository, localRepository, remoteRepositories );
    }

    public File download( String groupId,
                          String artifactId,
                          String version,
                          ArtifactRepository archetypeRepository,
                          ArtifactRepository localRepository,
                         List remoteRepositories )
        throws DownloadException, DownloadNotFoundException
   {
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, Artifact.SCOPE_RUNTIME, "jar" );
System.err.println("ARTIFACT"+artifact);
System.err.println("ARTIFACT"+artifact.getArtifactId());
System.err.println("ARTIFACT"+artifact.getBaseVersion());
System.err.println("ARTIFACT"+artifact.getDependencyConflictId());
System.err.println("ARTIFACT"+artifact.getDownloadUrl());
System.err.println("ARTIFACT"+artifact.getGroupId());
System.err.println("ARTIFACT"+artifact.getId());
System.err.println("ARTIFACT"+artifact.getScope());
System.err.println("ARTIFACT"+artifact.getType());
System.err.println("ARTIFACT"+artifact.getVersion());
System.err.println("ARTIFACT"+artifact.getArtifactHandler());
System.err.println("ARTIFACT"+artifact.getAvailableVersions());
System.err.println("ARTIFACT"+artifact.getDependencyFilter());
System.err.println("ARTIFACT"+artifact.getDependencyTrail());
System.err.println("ARTIFACT"+artifact.getMetadataList());
System.err.println("ARTIFACT"+artifact.getRepository());
System.err.println("ARTIFACT"+artifact.getVersionRange());
System.err.println("remoteRepositories  "+remoteRepositories);
System.err.println("localRepository  "+localRepository);
System.err.println("archetypeRepository  "+archetypeRepository);

artifact.setRepository(archetypeRepository);
        try
        {
            artifactResolver.resolveAlways( artifact, remoteRepositories, localRepository );
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
