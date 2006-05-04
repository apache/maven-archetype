package org.apache.maven.plugin.archetype;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.archetype.Archetype;
import org.apache.maven.archetype.ArchetypeDescriptorException;
import org.apache.maven.archetype.ArchetypeNotFoundException;
import org.apache.maven.archetype.ArchetypeTemplateProcessingException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The archetype creation goal looks for an archetype with a given groupId, artifactId, and
 * version and retrieves it from the remote repository. Once the archetype is retrieve it is process against
 * a set of user parameters to create a working Maven project.
 *
 * @requiresProject false
 * @goal create
 */
public class MavenArchetypeMojo
    extends AbstractMojo
{
    /**
     * @component
     */
    private Archetype archetype;

    /**
     * @component
     */
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    /**
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout" roleHint="default"
     */
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout;


    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${archetypeGroupId}" default-value="org.apache.maven.archetypes"
     * @required
     */
    private String archetypeGroupId;

    /**
     * @parameter expression="${archetypeArtifactId}" default-value="maven-archetype-quickstart"
     * @required
     */
    private String archetypeArtifactId;

    /**
     * @parameter expression="${archetypeVersion}" default-value="RELEASE"
     * @required
     */
    private String archetypeVersion;

    /**
     * @parameter expression="${groupId}"
     */
    private String groupId;

    /**
     * @parameter expression="${artifactId}"
     */
    private String artifactId;

    /**
     * @parameter expression="${version}" default-value="1.0-SNAPSHOT"
     * @required
     */
    private String version;

    /**
     * @parameter expression="${packageName}" alias="package"
     */
    private String packageName;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     */
    private List pomRemoteRepositories;

    /**
     * @parameter expression="${remoteRepositories}"
     */
    private String remoteRepositories;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
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

        String basedir = System.getProperty( "user.dir" );

        if ( packageName == null )
        {
            getLog().info( "Defaulting package to group ID: " + groupId );

            packageName = groupId;
        }

        // TODO: context mojo more appropriate?
        Map map = new HashMap();

        map.put( "basedir", basedir );

        map.put( "package", packageName );

        map.put( "packageName", packageName );

        map.put( "groupId", groupId );

        map.put( "artifactId", artifactId );

        map.put( "version", version );

        List archetypeRemoteRepositories = new ArrayList( pomRemoteRepositories );

        if ( remoteRepositories != null )
        {
            getLog().info( "We are using command line specified remote repositories: " + remoteRepositories );

            archetypeRemoteRepositories = new ArrayList();

            String[] s = StringUtils.split( remoteRepositories, "," );

            for ( int i = 0; i < s.length; i++ )
            {
                archetypeRemoteRepositories.add( createRepository( s[i], "id" + i ) );
            }
        }

        try
        {
            archetype.createArchetype( archetypeGroupId, archetypeArtifactId, archetypeVersion, localRepository,
                                       archetypeRemoteRepositories, map );
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
    // artifact repositories is someowhat cumbersome atm.
    public ArtifactRepository createRepository( String url, String repositoryId )
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

