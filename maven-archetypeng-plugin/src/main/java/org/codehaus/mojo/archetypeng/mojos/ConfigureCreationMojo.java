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

package org.codehaus.mojo.archetypeng.mojos;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import org.codehaus.mojo.archetypeng.creator.ArchetypeCreationConfigurator;

import java.io.File;
import java.util.List;
import org.codehaus.mojo.archetypeng.ArchetypeRegistryManager;

/**
 * @author           rafale
 * @description      Configure archetype's creation properties.
 * @requiresProject  true
 * @goal             configure-creation
 */
public class ConfigureCreationMojo
extends AbstractMojo
{
    /**
     * @component
     */
    ArchetypeCreationConfigurator configurator;

    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * @parameter  expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter  default-value="archetype.properties" expression="${archetype.properties}"
     */
    private File propertyFile = null;

    /**
     * @parameter  expression="${archetype.languages}"
     */
    private String archetypeLanguages;

    /**
     * @parameter  expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;
    
    /**
     * @parameter expression="${interactive}" default-value="false"
     */
    private boolean interactive;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        try
        {
            List languages =
                archetypeRegistryManager.getLanguages (
                    archetypeLanguages,
                    archetypeRegistryFile
                );

            configurator.configureArchetypeCreation (
                project,
                new Boolean ( interactive ),
//                settings.getInteractiveMode (),
                System.getProperties (),
                propertyFile, 
                languages
            );
            getLog ().info ("Archetype created in target/generated-sources/archetypeng");
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }
}
