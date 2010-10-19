package org.apache.maven.archetype.mojos;

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

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.old.OldArchetype;
import org.apache.maven.archetype.old.ArchetypeDescriptorException;
import org.apache.maven.archetype.old.ArchetypeNotFoundException;
import org.apache.maven.archetype.old.ArchetypeTemplateProcessingException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The archetype creation goal looks for an archetype with a given groupId,
 * artifactId, and version and retrieves it from the remote repository. Once the
 * archetype is retrieved, it is then processed against a set of user parameters
 * to create a working Maven project.
 *
 * @description Creates a project from an archetype.
 * @requiresProject false
 * @goal create
 * @deprecated Please use the generate mojo instead.
 */
public class MavenArchetypeMojo
    extends AbstractMojo
{
    /**
     * Used to create the Archetype specified by the groupId, artifactId, and
     * version from the remote repository.
     *
     * @component
     */
    private OldArchetype archetype;

    /**
     * Used to create ArtifactRepository objects given the urls of the remote
     * repositories.
     *
     * @component
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * Determines whether the layout is legacy or not.
     *
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout" roleHint="default"
     */
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;


    /**
     * Maven's local repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * The Archetype Group Id to be used.
     *
     * @parameter expression="${archetypeGroupId}" default-value="org.apache.maven.archetypes"
     * @required
     */
    private String archetypeGroupId;

    /**
     * The Archetype Artifact Id to be used.
     *
     * @parameter expression="${archetypeArtifactId}" default-value="maven-archetype-quickstart"
     * @required
     */
    private String archetypeArtifactId;

    /**
     * The Archetype Version to be used.
     *
     * @parameter expression="${archetypeVersion}" default-value="RELEASE"
     * @required
     */
    private String archetypeVersion;

    /**
     * The Group Id of the project to be build.
     *
     * @parameter expression="${groupId}"
     */
    private String groupId;

    /**
     * The Artifact Id of the project to be build.
     *
     * @parameter expression="${artifactId}"
     */
    private String artifactId;

    /**
     * The Version of the project to be build.
     *
     * @parameter expression="${version}" default-value="1.0-SNAPSHOT"
     * @required
     */
    private String version;

    /**
     * The Package Name of the project to be build.
     *
     * @parameter expression="${packageName}" alias="package"
     */
    private String packageName;

    /**
     * The remote repositories available for discovering dependencies and extensions as indicated
     * by the POM.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     */
    private List<ArtifactRepository> pomRemoteRepositories;

    /**
     * Other remote repositories available for discovering dependencies and extensions.
     *
     * @parameter expression="${remoteRepositories}"
     */
    private String remoteRepositories;

    /**
     * The project to be created an archetype of.
     *
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter expression="${basedir}" default-value="${user.dir}"
     */
    private String basedir;

    public void execute()
        throws MojoExecutionException
    {
        getLog().warn( "This goal is deprecated. Please use mvn archetype:generate instead" );
        // TODO: prompt for missing values
        // TODO: configurable license

        // ----------------------------------------------------------------------
        // archetypeGroupId
        // archetypeArtifactId
        // archetypeVersion
        //
        // localRepository
        // remoteRepository
        // parameters
        // ----------------------------------------------------------------------

        if ( project.getFile() != null && groupId == null )
        {
            groupId = project.getGroupId();
        }

        if ( packageName == null )
        {
            getLog().info( "Defaulting package to group ID: " + groupId );

            packageName = groupId;
        }


        List<ArtifactRepository> archetypeRemoteRepositories = new ArrayList<ArtifactRepository>( pomRemoteRepositories );

        if ( remoteRepositories != null )
        {
            getLog().info( "We are using command line specified remote repositories: " + remoteRepositories );

            archetypeRemoteRepositories = new ArrayList<ArtifactRepository>();

            String[] s = StringUtils.split( remoteRepositories, "," );

            for ( int i = 0; i < s.length; i++ )
            {
                archetypeRemoteRepositories.add( createRepository( s[i], "id" + i ) );
            }
        }

        try
        {
            ArchetypeGenerationRequest request = new ArchetypeGenerationRequest()
                .setPackage( packageName )
                .setGroupId( groupId )
                .setArtifactId( artifactId )
                .setVersion( version )
                .setArchetypeGroupId( archetypeGroupId )
                .setArchetypeArtifactId( archetypeArtifactId )
                .setArchetypeVersion( archetypeVersion )
                .setLocalRepository( localRepository )
                .setRemoteArtifactRepositories( archetypeRemoteRepositories )
                .setOutputDirectory( basedir );

            archetype.createArchetype( request, createRepository( "http://repo1.maven.org/maven2", "central" ) );
        }
        catch ( UnknownArchetype e )
        {
            throw new MojoExecutionException( "Error creating from archetype", e );
        }
        catch ( ArchetypeNotFoundException e )
        {
            throw new MojoExecutionException( "Error creating from archetype", e );
        }
        catch ( ArchetypeDescriptorException e )
        {
            throw new MojoExecutionException( "Error creating from archetype", e );
        }
        catch ( ArchetypeTemplateProcessingException e )
        {
            throw new MojoExecutionException( "Error creating from archetype", e );
        }
    }

    //TODO: this should be put in John's artifact utils and used from there instead of being repeated here. Creating
    // artifact repositories is somewhat cumbersome atm.
    private ArtifactRepository createRepository( String url, String repositoryId )
    {
        // snapshots vs releases
        // offline = to turning the update policy off

        //TODO: we'll need to allow finer grained creation of repositories but this will do for now

        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS;

        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN;

        ArtifactRepositoryPolicy snapshotsPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );

        ArtifactRepositoryPolicy releasesPolicy =
            new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag );

        return artifactRepositoryFactory.createArtifactRepository( repositoryId, url, defaultArtifactRepositoryLayout,
                                                                   snapshotsPolicy, releasesPolicy );
    }
}

