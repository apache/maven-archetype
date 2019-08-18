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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

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
        DefaultArtifactCoordinate jarCoordinate = new DefaultArtifactCoordinate();
        jarCoordinate.setGroupId( groupId );
        jarCoordinate.setArtifactId( artifactId );
        jarCoordinate.setVersion( version );
        
        DefaultArtifactCoordinate pomCoordinate = new DefaultArtifactCoordinate();
        pomCoordinate.setGroupId( groupId );
        pomCoordinate.setArtifactId( artifactId );
        pomCoordinate.setVersion( version );
        pomCoordinate.setExtension( "pom" );

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
            artifact = artifactResolver.resolveArtifact( buildingRequest, jarCoordinate ).getArtifact();
        }
        catch ( ArtifactResolverException e )
        {
            throw new DownloadException( "Error downloading " + jarCoordinate + ".", e );
        }

        // still required???
        try
        {
            artifactResolver.resolveArtifact( buildingRequest, pomCoordinate );
        }
        catch ( ArtifactResolverException e )
        {
            throw new DownloadException( "Error downloading POM for " + artifact.getId() + ".", e );
        }

        return artifact.getFile();
    }

    @Override
    public File downloadOld( String groupId, String artifactId, String version, ArtifactRepository archetypeRepository,
                             ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                             ProjectBuildingRequest buildingRequest )
        throws DownloadException, DownloadNotFoundException
    {
        DefaultArtifactCoordinate jarCoordinate = new DefaultArtifactCoordinate();
        jarCoordinate.setGroupId( groupId );
        jarCoordinate.setArtifactId( artifactId );
        jarCoordinate.setVersion( version );
        
        try
        {
            return artifactResolver.resolveArtifact( buildingRequest, jarCoordinate ).getArtifact().getFile();
        }
        catch ( ArtifactResolverException e )
        {
            throw new DownloadException( "Error downloading " + jarCoordinate + ".", e );
        }
    }
}
