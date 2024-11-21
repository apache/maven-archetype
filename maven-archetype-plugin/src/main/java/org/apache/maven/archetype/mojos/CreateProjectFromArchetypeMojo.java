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
package org.apache.maven.archetype.mojos;

import javax.inject.Inject;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.ui.generation.ArchetypeGenerationConfigurator;
import org.apache.maven.archetype.ui.generation.ArchetypeSelector;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.eclipse.aether.RepositorySystem;

/**
 * Generates a new project from an archetype, or updates the actual project if using a partial archetype.
 * If the project is fully generated, it is generated in a directory corresponding to its artifactId.
 * If the project is updated with a partial archetype, it is done in the current directory.
 *
 * @author rafale
 */
@Mojo(name = "generate", requiresProject = false)
@Execute(phase = LifecyclePhase.GENERATE_SOURCES)
public class CreateProjectFromArchetypeMojo extends AbstractMojo implements ContextEnabled {
    private ArchetypeManager manager;

    private ArchetypeSelector selector;

    private ArchetypeGenerationConfigurator configurator;

    private Invoker invoker;

    private RepositorySystem repositorySystem;

    @Inject
    public CreateProjectFromArchetypeMojo(
            ArchetypeManager manager,
            ArchetypeSelector selector,
            ArchetypeGenerationConfigurator configurator,
            Invoker invoker,
            RepositorySystem repositorySystem) {
        this.manager = manager;
        this.selector = selector;
        this.invoker = invoker;
        this.repositorySystem = repositorySystem;
    }

    /**
     * The archetype's artifactId.
     */
    @Parameter(property = "archetypeArtifactId")
    private String archetypeArtifactId;

    /**
     * The archetype's groupId.
     */
    @Parameter(property = "archetypeGroupId")
    private String archetypeGroupId;

    /**
     * The archetype's version.
     */
    @Parameter(property = "archetypeVersion")
    private String archetypeVersion;

    /**
     * The archetype catalogs to use to build a list and let the user choose from.
     * It is a comma-separated list of one or more of the following catalog schemes
     * <ul>
     * <li><code>local</code> which is the shortcut to the local repository</li>
     * <li><code>remote</code> which is the shortcut for a repository with id {@code archetype},
     * the Maven Central repository (with id {@code central}) or the first matching mirror for either
     * of the repository ids {@code archetype} or {@code central}</li>
     * <li><code>internal</code> which is an <a href="https://maven.apache.org/archetype/archetype-common/internal.html">internal catalog</a></li>
     * </ul>
     * <p/>
     * If you want the catalog to come from a custom repository, please add
     * a dedicated repository with id {@code archetype} and optionally a server section with the same id to
     * your {@code settings.xml} and use the catalog scheme <code>remote</code>.
     * <pre>
     *   &lt;repository&gt;
     *     &lt;id&gt;archetype&lt;/id&gt;
     *     &lt;url&gt;https://repository.domain.com/path/to/repo/&lt;/url&gt;
     *   &lt;/repository&gt;
     *
     *   &lt;!-- in case of a repository with authentication --&gt;
     *   &lt;server&gt;
     *     &lt;id&gt;archetype&lt;/id&gt;
     *     &lt;username&gt;user.name&lt;/username&gt;
     *     &lt;password&gt;s3cr3t&lt;/password&gt;
     *   &lt;/server&gt;
     * </pre>
     * In order to use that repository <em>only</em> for resolving the catalog (and not for downloading regular Maven dependencies from it) it is recommended to
     * include the repository in a <a href="https://maven.apache.org/settings.html#active-profiles">profile which is not active by default.</a> and explicitly select it via {@code -P<profile-id>}
     * when this goal is called on the command-line.
     * In case of mirrors for either repository {@code archetype} or {@code central} the <a href="https://maven.apache.org/guides/mini/guide-mirror-settings.html">matching mirror's id</a> is considered
     * for server settings (like authentication).
     * If the repository's catalog file is empty or cannot be retrieved, <code>internal</code> catalog is transparently used as fallback.
     */
    @Parameter(property = "archetypeCatalog", defaultValue = "remote,local")
    private String archetypeCatalog;

    /**
     * If set to {@code true} will ask for values also for properties having defaults in the first place.
     * Only has an effect if {@link #interactiveMode} is used.
     */
    @Parameter(property = "askForDefaultPropertyValues", defaultValue = "false", required = true)
    private Boolean askForDefaultPropertyValues;

    /**
     * User settings used to check the interactiveMode.
     */
    @Parameter(property = "interactiveMode", defaultValue = "${settings.interactiveMode}", required = true)
    private Boolean interactiveMode;

    @Parameter(defaultValue = "${basedir}", property = "outputDirectory")
    private File outputDirectory;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Goals to immediately run on the project created from the archetype.
     */
    @Parameter(property = "goals")
    private String goals;

    /**
     * Applying some filter on displayed archetypes list: format is <code>artifactId</code> or <code>groupId:artifactId</code>.
     * <ul>
     * <li><code>org.apache:</code> -> displays all archetypes which contain org.apache in groupId</li>
     * <li><code>:jee</code> or <code>jee</code> -> displays all archetypes which contain jee in artifactId</li>
     * <li><code>org.apache:jee</code> -> displays all archetypes which contain org.apache in groupId AND jee in artifactId</li>
     * </ul>
     *
     * @since 2.1
     */
    @Parameter(property = "filter")
    private String filter;

    @Override
    public void execute() throws MojoExecutionException {
        Properties executionProperties = session.getUserProperties();

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest()
                .setArchetypeGroupId(archetypeGroupId)
                .setArchetypeArtifactId(archetypeArtifactId)
                .setArchetypeVersion(archetypeVersion)
                .setOutputDirectory(outputDirectory.getAbsolutePath())
                .setRemoteRepositories(project.getRemoteProjectRepositories())
                .setRemoteArtifactRepositories(project.getRemoteArtifactRepositories())
                .setMavenSession(session)
                .setRepositorySession(session.getRepositorySession())
                .setRepositorySystem(repositorySystem)
                .setProjectBuildingRequest(session.getProjectBuildingRequest())
                .setLocalRepository(session.getLocalRepository())
                .setOffline(session.isOffline())
                .setFilter(filter)
                .setAskForDefaultPropertyValues(askForDefaultPropertyValues);

        try {
            if (interactiveMode.booleanValue()) {
                getLog().info("Generating project in Interactive mode");
            } else {
                getLog().info("Generating project in Batch mode");
            }

            selector.selectArchetype(request, interactiveMode, archetypeCatalog);

            if (StringUtils.isBlank(request.getArchetypeArtifactId())) {
                // no archetype found: stopping
                return;
            }

            configurator.configureArchetype(request, interactiveMode, executionProperties);

            ArchetypeGenerationResult generationResult = manager.generateProjectFromArchetype(request);

            if (generationResult.getCause() != null) {
                throw new MojoExecutionException(generationResult.getCause().getMessage(), generationResult.getCause());
            }
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }

        String artifactId = request.getArtifactId();

        String postArchetypeGenerationGoals = request.getArchetypeGoals();

        if (postArchetypeGenerationGoals == null || postArchetypeGenerationGoals.isEmpty()) {
            postArchetypeGenerationGoals = goals;
        }

        if (postArchetypeGenerationGoals != null && !postArchetypeGenerationGoals.isEmpty()) {
            invokePostArchetypeGenerationGoals(postArchetypeGenerationGoals, artifactId);
        }
    }

    private void invokePostArchetypeGenerationGoals(String goals, String artifactId) throws MojoExecutionException {
        getLog().info("Invoking post-archetype-generation goals: " + goals);

        File projectBasedir = new File(outputDirectory, artifactId);

        if (projectBasedir.exists()) {
            InvocationRequest request = new DefaultInvocationRequest()
                    .setBaseDirectory(projectBasedir)
                    .setGoals(Arrays.asList(StringUtils.split(goals, ",")));

            try {
                InvocationResult result = invoker.execute(request);

                if (result.getExitCode() != 0) {
                    throw new MojoExecutionException("Failed to invoke goals", result.getExecutionException());
                }
            } catch (MavenInvocationException e) {
                throw new MojoExecutionException("Cannot run additions goals.", e);
            }
        } else {
            getLog().info("Post-archetype-generation goals aborted: unavailable basedir " + projectBasedir);
        }
    }
}
