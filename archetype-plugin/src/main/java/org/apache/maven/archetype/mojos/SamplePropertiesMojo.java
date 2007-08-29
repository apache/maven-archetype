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

import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeConfiguration;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.common.ArchetypeFactory;
import org.apache.maven.archetype.common.ArchetypePropertiesManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeGenerationConfigurationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author           rafale
 * @requiresProject  false
 * @goal             sample-properties
 */
public class SamplePropertiesMojo
extends AbstractMojo
{
    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * @component
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

    /**
     * @component
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @component
     */
    private ArchetypePropertiesManager archetypePropertiesManager;
    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * Local maven repository.
     *
     * @parameter  expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter  expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List pomRemoteRepositories;

    /**
     * @parameter  default-value="archetype.properties" expression="${archetype.properties}"
     */
    private File propertyFile = null;

    /**
     * Other remote repositories available for discovering dependencies and extensions.
     *
     * @parameter  expression="${remoteRepositories}"
     */
    private String remoteRepositories;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    {
        try
        {
            List repositories =
                archetypeRegistryManager.getRepositories (
                    pomRemoteRepositories,
                    remoteRepositories,
                    archetypeRegistryFile
                );

            Properties properties =
                initialiseArchetypeProperties ( System.getProperties (), propertyFile );

            ArchetypeDefinition archetypeDefinition =
                archetypeFactory.createArchetypeDefinition ( properties );
            if ( !archetypeDefinition.isDefined () )
            {
                throw new ArchetypeNotDefined ( "The archetype is not defined" );
            }

            if ( !archetypeArtifactManager.exists (
                    archetypeDefinition.getGroupId (),
                    archetypeDefinition.getArtifactId (),
                    archetypeDefinition.getVersion (),
                    localRepository,
                    repositories
                )
            )
            {
                throw new UnknownArchetype (
                    "The desired archetype does not exist (" + archetypeDefinition.getGroupId ()
                    + ":" + archetypeDefinition.getArtifactId () + ":"
                    + archetypeDefinition.getVersion ()
                    + ")"
                );
            }

            ArchetypeConfiguration archetypeConfiguration;

            if ( archetypeArtifactManager.isOldArchetype (
                    archetypeDefinition.getGroupId (),
                    archetypeDefinition.getArtifactId (),
                    archetypeDefinition.getVersion (),
                    localRepository,
                    repositories
                )
            )
            {
                org.apache.maven.archetype.descriptor.ArchetypeDescriptor archetypeDescriptor =
                    archetypeArtifactManager.getOldArchetypeDescriptor (
                        archetypeDefinition.getGroupId (),
                        archetypeDefinition.getArtifactId (),
                        archetypeDefinition.getVersion (),
                        localRepository,
                        repositories
                    );
                archetypeConfiguration =
                    archetypeFactory.createArchetypeConfiguration (
                        archetypeDescriptor,
                        properties
                    );
            }
            else if (
                archetypeArtifactManager.isFileSetArchetype (
                    archetypeDefinition.getGroupId (),
                    archetypeDefinition.getArtifactId (),
                    archetypeDefinition.getVersion (),
                    localRepository,
                    repositories
                )
            )
            {
                org.apache.maven.archetype.metadata.ArchetypeDescriptor archetypeDescriptor =
                    archetypeArtifactManager.getFileSetArchetypeDescriptor (
                        archetypeDefinition.getGroupId (),
                        archetypeDefinition.getArtifactId (),
                        archetypeDefinition.getVersion (),
                        localRepository,
                        repositories
                    );
                archetypeConfiguration =
                    archetypeFactory.createArchetypeConfiguration (
                        archetypeDescriptor,
                        properties
                    );
            }
            else
            {
                throw new ArchetypeGenerationConfigurationFailure (
                    "The defined artifact is not an archetype"
                );
            }

            archetypeConfiguration.setProperty ( Constants.GROUP_ID, "com.company" );
            archetypeConfiguration.setProperty ( Constants.ARTIFACT_ID, "project" );
            archetypeConfiguration.setProperty ( Constants.VERSION, "1.0-SNAPSHOT" );
            archetypeConfiguration.setProperty ( Constants.PACKAGE, "com.company.project" );

            Iterator requiredProperties =
                archetypeConfiguration.getRequiredProperties ().iterator ();
            while ( requiredProperties.hasNext () )
            {
                String requiredProperty = (String) requiredProperties.next ();

                if ( StringUtils.isEmpty ( archetypeConfiguration.getProperty ( requiredProperty ) )
                )
                {
                    archetypeConfiguration.setProperty (
                        requiredProperty,
                        StringUtils.isEmpty (
                            archetypeConfiguration.getDefaultValue ( requiredProperty )
                        ) ? "To be defined"
                          : archetypeConfiguration.getDefaultValue ( requiredProperty )
                    );
                }
            }

            archetypePropertiesManager.writeProperties (
                archetypeConfiguration.toProperties (),
                propertyFile
            );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }

    private Properties initialiseArchetypeProperties (
        Properties commandLineProperties,
        File propertyFile
    )
    throws FileNotFoundException, IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );

        Iterator commandLinePropertiesIterator =
            new ArrayList ( commandLineProperties.keySet () ).iterator ();
        while ( commandLinePropertiesIterator.hasNext () )
        {
            String propertyKey = (String) commandLinePropertiesIterator.next ();
            properties.setProperty (
                propertyKey,
                commandLineProperties.getProperty ( propertyKey )
            );
        }
        return properties;
    }
}
