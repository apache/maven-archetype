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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.project.MavenProject;

/**
 * @author Jason van Zyl
 */
public class ArchetypeCreationRequest {

    private File localRepositoryBasedir;

    private MavenProject project;

    private List<String> languages = new ArrayList<>();

    private List<String> filtereds = new ArrayList<>();

    private String defaultEncoding = "UTF-8";

    private boolean preserveCData = false;

    private boolean keepParent = true;

    private boolean partialArchetype = false;

    private String packageName;

    private Properties properties;

    private String postPhase;

    private File outputDirectory;

    private File settingsFile;

    public String getPostPhase() {
        return postPhase;
    }

    public ArchetypeCreationRequest setPostPhase(String postPhase) {
        this.postPhase = postPhase;
        return this;
    }

    public File getLocalRepositoryBasedir() {
        return localRepositoryBasedir;
    }

    public ArchetypeCreationRequest setLocalRepositoryBasedir(File localRepository) {
        this.localRepositoryBasedir = localRepository;
        return this;
    }

    public MavenProject getProject() {
        return project;
    }

    public ArchetypeCreationRequest setProject(MavenProject project) {
        this.project = project;
        return this;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public ArchetypeCreationRequest setLanguages(List<String> languages) {
        this.languages = languages;
        return this;
    }

    public List<String> getFiltereds() {
        return filtereds;
    }

    public ArchetypeCreationRequest setFiltereds(List<String> filtereds) {
        this.filtereds = filtereds;
        return this;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public ArchetypeCreationRequest setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
        return this;
    }

    public boolean isPreserveCData() {
        return preserveCData;
    }

    public ArchetypeCreationRequest setPreserveCData(boolean preserveCData) {
        this.preserveCData = preserveCData;
        return this;
    }

    public boolean isKeepParent() {
        return keepParent;
    }

    public ArchetypeCreationRequest setKeepParent(boolean keepParent) {
        this.keepParent = keepParent;
        return this;
    }

    public boolean isPartialArchetype() {
        return partialArchetype;
    }

    public ArchetypeCreationRequest setPartialArchetype(boolean partialArchetype) {
        this.partialArchetype = partialArchetype;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public ArchetypeCreationRequest setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public String getPackageName() {
        return packageName;
    }

    public ArchetypeCreationRequest setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public ArchetypeCreationRequest setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
        return this;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public ArchetypeCreationRequest setSettingsFile(File settingsFile) {
        this.settingsFile = settingsFile;
        return this;
    }
}
