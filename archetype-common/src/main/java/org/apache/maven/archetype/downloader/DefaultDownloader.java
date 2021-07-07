package org.apache.maven.archetype.downloader;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

/**
 * @author Jason van Zyl
 */
@Component( role = Downloader.class )
public class DefaultDownloader
    implements Downloader
{
    @Requirement
    private ArtifactResolver artifactResolver;

    @Override
    public File download( String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
                          ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                          ProjectBuildingRequest buildingRequest )
        throws DownloadException, DownloadNotFoundException
   {
       ArtifactRequest request = new ArtifactRequest();
       Artifact defaultArtifact = new DefaultArtifact( groupId, artifactId, "pom", version );
       request.setArtifact( defaultArtifact );
       DefaultRepositorySystemSession drss = new DefaultRepositorySystemSession();

        List<ArtifactRepository> repositories = new ArrayList<>( remoteRepositories );
        if ( repositories.isEmpty() && archetypeRepository != null )
        {
            repositories.add( archetypeRepository );
        }
        else if ( repositories.isEmpty() && localRepository != null )
        {
            repositories.add( localRepository );
        }

        ArtifactRepository localRepo = localRepository;
        
        buildingRequest.setLocalRepository( localRepo );
        buildingRequest.setRemoteRepositories( repositories );

        Artifact artifact;
        try
        {
            artifact = artifactResolver.resolveArtifact( drss, request ).getArtifact();
        }
        catch ( ArtifactResolutionException e )
        {
            throw new DownloadException( "Error downloading " + defaultArtifact + ".", e );
        }

        // still required???
        try
        {
            artifactResolver.resolveArtifact( drss, request );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new DownloadException( "Error downloading POM for " + artifact.getArtifactId() + ".", e );
        }

        return artifact.getFile();
    }

    @Override
    public File downloadOld( String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
                             ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                             ProjectBuildingRequest buildingRequest )
        throws DownloadException, DownloadNotFoundException
    {
        ArtifactRequest request = new ArtifactRequest();
        Artifact defaultArtifact = new DefaultArtifact( groupId, artifactId, null, version );
        request.setArtifact( defaultArtifact );
        DefaultRepositorySystemSession drss = new DefaultRepositorySystemSession();
        
        try
        {
            return artifactResolver.resolveArtifact( drss, request ).getArtifact().getFile();
        }
        catch ( ArtifactResolutionException e )
        {
            throw new DownloadException( "Error downloading " + request.toString() + ".", e );
        }
    }
}
