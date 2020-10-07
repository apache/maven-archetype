package org.apache.maven.archetype.generator;

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

import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.archetype.old.OldArchetype;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.ArchetypeException;
import org.apache.maven.archetype.exception.ArchetypeGenerationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component( role = ArchetypeGenerator.class )
public class DefaultArchetypeGenerator
    extends AbstractLogEnabled
    implements ArchetypeGenerator
{
    /**
     * Determines whether the layout is legacy or not.
     */
    @Requirement
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;

    @Requirement
    private ArchetypeArtifactManager archetypeArtifactManager;

    @Requirement
    private FilesetArchetypeGenerator filesetGenerator;

    @Requirement
    private OldArchetype oldArchetype;

    private File getArchetypeFile( ArchetypeGenerationRequest request, ArtifactRepository localRepository )
        throws ArchetypeException
    {
        if ( !isArchetypeDefined( request ) )
        {
            throw new ArchetypeNotDefined( "The archetype is not defined" );
        }

        List<ArtifactRepository> repos = new ArrayList<>();

        ArtifactRepository remoteRepo = null;
        if ( request != null && request.getArchetypeRepository() != null )
        {
            remoteRepo =
                createRepository( request.getArchetypeRepository(),
                                                           request.getArchetypeArtifactId() + "-repo" );

            repos.add( remoteRepo );
        }

        if ( !archetypeArtifactManager.exists( request.getArchetypeGroupId(), request.getArchetypeArtifactId(),
                                               request.getArchetypeVersion(), remoteRepo, localRepository, repos,
                                               request.getProjectBuildingRequest() ) )
        {
            throw new UnknownArchetype( "The desired archetype does not exist (" + request.getArchetypeGroupId() + ":"
                + request.getArchetypeArtifactId() + ":" + request.getArchetypeVersion() + ")" );
        }

        File archetypeFile =
            archetypeArtifactManager.getArchetypeFile( request.getArchetypeGroupId(), request.getArchetypeArtifactId(),
                                                       request.getArchetypeVersion(), remoteRepo, localRepository,
                                                       repos, request.getProjectBuildingRequest() );
        return archetypeFile;
    }

    private void generateArchetype( ArchetypeGenerationRequest request, File archetypeFile )
        throws ArchetypeException
    {
        if ( archetypeArtifactManager.isFileSetArchetype( archetypeFile ) )
        {
            processFileSetArchetype( request, archetypeFile );
        }
        else if ( archetypeArtifactManager.isOldArchetype( archetypeFile ) )
        {
            processOldArchetype( request, archetypeFile );
        }
        else
        {
            throw new ArchetypeGenerationFailure( "The defined artifact is not an archetype" );
        }
    }

    /** Common */
    public String getPackageAsDirectory( String packageName )
    {
        return StringUtils.replace( packageName, ".", "/" );
    }

    private boolean isArchetypeDefined( ArchetypeGenerationRequest request )
    {
        return StringUtils.isNotEmpty( request.getArchetypeGroupId() )
            && StringUtils.isNotEmpty( request.getArchetypeArtifactId() )
            && StringUtils.isNotEmpty( request.getArchetypeVersion() );
    }

    /** FileSetArchetype */
    private void processFileSetArchetype( ArchetypeGenerationRequest request, File archetypeFile )
        throws ArchetypeException
    {
        filesetGenerator.generateArchetype( request, archetypeFile );
    }

    private void processOldArchetype( ArchetypeGenerationRequest request, File archetypeFile )
            throws ArchetypeGenerationFailure, InvalidPackaging
    {
        oldArchetype.createArchetype( request, archetypeFile );
    }

    @Override
    public void generateArchetype( ArchetypeGenerationRequest request, File archetypeFile,
                                   ArchetypeGenerationResult result )
    {
        try
        {
            generateArchetype( request, archetypeFile );
        }
        catch ( ArchetypeException e )
        {
            result.setCause( e );
        }
    }

    @Override
    public void generateArchetype( ArchetypeGenerationRequest request, ArchetypeGenerationResult result )
    {
        try
        {
            File archetypeFile = getArchetypeFile( request, request.getLocalRepository() );

            generateArchetype( request, archetypeFile, result );
        }
        catch ( ArchetypeException ex )
        {
            result.setCause( ex );
        }
    }
    
    private ArtifactRepository createRepository( String url, String repositoryId )
    {
        
        
        // snapshots vs releases
        // offline = to turning the update policy off

        // TODO: we'll need to allow finer grained creation of repositories but this will do for now

        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;

        ArtifactRepositoryPolicy snapshotsPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );

        ArtifactRepositoryPolicy releasesPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );
        
        return new MavenArtifactRepository( repositoryId, url, defaultArtifactRepositoryLayout, snapshotsPolicy,
                                            releasesPolicy );
    }
}
