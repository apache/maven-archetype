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

import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.PropertyUtils;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * Generate sample project.
 *
 * @author rafale
 * @requiresProject false
 * @goal generate-project
 */
public class GenerateProjectMojo
    extends AbstractMojo
    implements ContextEnabled

{
    /** @component */
    ArchetypeRegistryManager archetypeRegistryManager;

    /** @component */
    ArchetypeGenerator generator;

    /**
     * The location of the registry file.
     *
     * @parameter expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * The directory to generate the project in.
     *
     * @parameter default-value="${user.dir}"
     */
    private String basedir = System.getProperty( "user.dir" );

    /**
     * Local maven repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Remote repositories defined in the project's pom (used only when called from an existing
     * project).
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List pomRemoteRepositories;

    /**
     * The property file that holds the plugin configuration.
     *
     * @parameter default-value="archetype.properties" expression="${archetype.properties}"
     */
    private File propertyFile = null;

    /**
     * Other remote repositories available for discovering dependencies and extensions.
     *
     * @parameter expression="${remoteRepositories}"
     */
    private String remoteRepositories;

    private MavenSession session;

    public void execute()
        throws
        MojoExecutionException,
        MojoFailureException
    {
        try
        {
            List repositories =
                archetypeRegistryManager.getRepositories(
                    pomRemoteRepositories,
                    remoteRepositories,
                    archetypeRegistryFile
                );

            generator.generateArchetype( propertyFile, localRepository, repositories, basedir );

            Properties archetypeProperties = PropertyUtils.loadProperties( propertyFile );

            if ( archetypeProperties != null )
            {
                getPluginContext().put( "artifactId", archetypeProperties.getProperty( "artifactId" ) );
            }
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
    }
}
