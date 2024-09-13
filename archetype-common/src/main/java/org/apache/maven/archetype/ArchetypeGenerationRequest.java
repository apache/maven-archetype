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

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/** @author Jason van Zyl */
public class ArchetypeGenerationRequest {

    private RepositorySystemSession repositorySession;

    private boolean interactiveMode;

    private boolean askForDefaultPropertyValues;

    private String outputDirectory;

    private List<RemoteRepository> remoteArtifactRepositories = Collections.emptyList();

    // Archetype definition
    private String archetypeName;

    private String archetypeGroupId;

    private String archetypeArtifactId;

    private String archetypeVersion;

    private String archetypeGoals = "";

    /**
     * The URL to the archetype repository
     *
     * @deprecated see ARCHETYPE-439
     */
    @Deprecated
    private String archetypeRepository;

    // Archetype configuration
    private String groupId;

    private String artifactId;

    private String version;

    private String packageName;

    private Properties properties = new Properties();

    /**
     *  @since 2.1
     */
    private String filter;

    public ArchetypeGenerationRequest() {
        // no op
    }

    public ArchetypeGenerationRequest(Archetype archetype) {
        this.archetypeGroupId = archetype.getGroupId();

        this.archetypeArtifactId = archetype.getArtifactId();

        this.archetypeVersion = archetype.getVersion();

        this.archetypeRepository = archetype.getRepository();
    }

    public RepositorySystemSession getRepositorySession() {
        return repositorySession;
    }

    public ArchetypeGenerationRequest setRepositorySession(RepositorySystemSession repoSession) {
        this.repositorySession = repoSession;
        return this;
    }

    public String getArchetypeGroupId() {
        return archetypeGroupId;
    }

    public ArchetypeGenerationRequest setArchetypeGroupId(String archetypeGroupId) {
        this.archetypeGroupId = archetypeGroupId;
        return this;
    }

    public String getArchetypeArtifactId() {
        return archetypeArtifactId;
    }

    public ArchetypeGenerationRequest setArchetypeArtifactId(String archetypeArtifactId) {
        this.archetypeArtifactId = archetypeArtifactId;
        return this;
    }

    public String getArchetypeVersion() {
        return archetypeVersion;
    }

    public ArchetypeGenerationRequest setArchetypeVersion(String archetypeVersion) {
        this.archetypeVersion = archetypeVersion;
        return this;
    }

    public String getArchetypeGoals() {
        return archetypeGoals;
    }

    public ArchetypeGenerationRequest setArchetypeGoals(String archetypeGoals) {
        this.archetypeGoals = archetypeGoals;
        return this;
    }

    public String getArchetypeName() {
        return archetypeName;
    }

    public ArchetypeGenerationRequest setArchetypeName(String archetypeName) {
        this.archetypeName = archetypeName;
        return this;
    }

    /**
     *
     * @return the URL of the archetype repository
     * @deprecated see ARCHETYPE-439
     */
    @Deprecated
    public String getArchetypeRepository() {
        return archetypeRepository;
    }

    /**
     *
     * @param archetypeRepository the URL of the archetype repository
     * @return this request
     * @deprecated see ARCHETYPE-439
     */
    @Deprecated
    public ArchetypeGenerationRequest setArchetypeRepository(String archetypeRepository) {
        this.archetypeRepository = archetypeRepository;

        return this;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public ArchetypeGenerationRequest setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public ArchetypeGenerationRequest setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ArchetypeGenerationRequest setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getPackage() {
        return packageName;
    }

    public ArchetypeGenerationRequest setPackage(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public ArchetypeGenerationRequest setProperties(Properties additionalProperties) {
        this.properties = additionalProperties;

        return this;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public ArchetypeGenerationRequest setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public boolean isInteractiveMode() {
        return interactiveMode;
    }

    public ArchetypeGenerationRequest setInteractiveMode(boolean interactiveMode) {
        this.interactiveMode = interactiveMode;
        return this;
    }

    public boolean isAskForDefaultPropertyValues() {
        return askForDefaultPropertyValues;
    }

    public ArchetypeGenerationRequest setAskForDefaultPropertyValues(boolean askForDefaultPropertyValues) {
        this.askForDefaultPropertyValues = askForDefaultPropertyValues;
        return this;
    }

    public List<RemoteRepository> getRemoteArtifactRepositories() {
        return remoteArtifactRepositories;
    }

    public ArchetypeGenerationRequest setRemoteArtifactRepositories(List<RemoteRepository> remoteArtifactRepositories) {
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        return this;
    }

    public String getFilter() {
        return filter;
    }

    public ArchetypeGenerationRequest setFilter(String filter) {
        this.filter = filter;
        return this;
    }
}
