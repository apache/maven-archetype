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

package org.apache.maven.archetype;

import org.apache.maven.artifact.repository.ArtifactRepository;

import java.util.Properties;
import org.apache.maven.archetype.catalog.Archetype;

/** @author Jason van Zyl */
public class ArchetypeGenerationRequest
{
    private String outputDirectory;

    private ArtifactRepository localRepository;

    // Archetype definition
    private String archetypeName;

    private String archetypeGroupId;

    private String archetypeArtifactId;

    private String archetypeVersion;

    private String archetypeGoals = "";

    private String archetypeRepository;

    // Archetype configuration
    private String groupId;

    private String artifactId;

    private String version;

    private String packageName;

    private Properties properties = new Properties();

    private String remoteRepository;

    public ArchetypeGenerationRequest()
    {
    }

    public ArchetypeGenerationRequest( Archetype archetype )
    {
        this.archetypeGroupId = archetype.getGroupId();

        this.archetypeArtifactId = archetype.getArtifactId();

        this.archetypeVersion = archetype.getVersion();

        this.remoteRepository = archetype.getRepository();

        this.archetypeRepository = archetype.getRepository();
    }

    public String getArchetypeGroupId()
    {
        return archetypeGroupId;
    }

    public ArchetypeGenerationRequest setArchetypeGroupId( String archetypeGroupId )
    {
        this.archetypeGroupId = archetypeGroupId;

        return this;
    }

    public String getArchetypeArtifactId()
    {
        return archetypeArtifactId;
    }

    public ArchetypeGenerationRequest setArchetypeArtifactId( String archetypeArtifactId )
    {
        this.archetypeArtifactId = archetypeArtifactId;

        return this;
    }

    public String getArchetypeVersion()
    {
        return archetypeVersion;
    }

    public ArchetypeGenerationRequest setArchetypeVersion( String archetypeVersion )
    {
        this.archetypeVersion = archetypeVersion;

        return this;
    }

    public String getArchetypeGoals()
    {
        return archetypeGoals;
    }

    public ArchetypeGenerationRequest setArchetypeGoals( String archetypeGoals )
    {
        this.archetypeGoals = archetypeGoals;

        return this;
    }

    public String getArchetypeName()
    {
        return archetypeName;
    }

    public ArchetypeGenerationRequest setArchetypeName( String archetypeName )
    {
        this.archetypeName = archetypeName;

        return this;
    }

    public String getArchetypeRepository()
    {
        return archetypeRepository;
    }

    public ArchetypeGenerationRequest setArchetypeRepository( String archetypeRepository )
    {
        this.archetypeRepository = archetypeRepository;

        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public ArchetypeGenerationRequest setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;

        return this;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public ArchetypeGenerationRequest setGroupId( String groupId )
    {
        this.groupId = groupId;

        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public ArchetypeGenerationRequest setVersion( String version )
    {
        this.version = version;

        return this;
    }

    public String getPackage()
    {
        return packageName;
    }

    public ArchetypeGenerationRequest setPackage( String packageName )
    {
        this.packageName = packageName;

        return this;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public ArchetypeGenerationRequest setProperties( Properties additionalProperties )
    {
        this.properties = additionalProperties;

        return this;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public ArchetypeGenerationRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;

        return this;
    }

    public String getRemoteRepository()
    {
        return remoteRepository;
    }

    public ArchetypeGenerationRequest setRemoteRepository( String remoteRepository )
    {
        this.remoteRepository = remoteRepository;

        return this;
    }


    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public ArchetypeGenerationRequest setOutputDirectory( String outputDirectory )
    {
        this.outputDirectory = outputDirectory;

        return this;
    }
}
