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
import org.apache.maven.archetype.creator.ArchetypeCreator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;

import java.util.List;

/**
 * @author           rafale
 * @description      Create archetype from project.
 * @requiresProject  true
 * @goal             create-archetype
 */
public class CreateArchetypeMojo
extends AbstractMojo
{
    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;
    /**
     * @component  role-hint="fileset"
     */
    ArchetypeCreator creator;

    /**
     * @parameter  expression="${archetype.filteredExtentions}"
     */
    private String archetypeFilteredExtentions;

    /**
     * @parameter  expression="${archetype.languages}"
     */
    private String archetypeLanguages;

    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * @parameter  default-value="UTF-8" expression="${archetype.encoding}"
     */
    private String defaultEncoding;

    /**
     * @parameter  expression="${archetype.ignoreReplica}"
     */
    private boolean ignoreReplica = true;

    /**
     * @parameter  expression="${archetype.preserveCData}"
     */
    private boolean preserveCData = false;

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

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        try
        {
            List languages =
                archetypeRegistryManager.getLanguages ( archetypeLanguages, archetypeRegistryFile );

            List filtereds =
                archetypeRegistryManager.getFilteredExtensions (
                    archetypeFilteredExtentions,
                    archetypeRegistryFile
                );

            creator.createArchetype (
                project,
                propertyFile,
                languages,
                filtereds,
                defaultEncoding,
                ignoreReplica,
                preserveCData,
                archetypeRegistryFile
            );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }
}
