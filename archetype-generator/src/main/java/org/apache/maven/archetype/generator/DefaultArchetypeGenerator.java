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

package org.apache.maven.archetype.generator;

import org.apache.maven.archetype.Archetype;
import org.apache.maven.archetype.ArchetypeDescriptorException;
import org.apache.maven.archetype.ArchetypeNotFoundException;
import org.apache.maven.archetype.ArchetypeTemplateProcessingException;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeConfiguration;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.common.ArchetypeFactory;
import org.apache.maven.archetype.common.ArchetypePropertiesManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.ArchetypeGenerationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.archetype.exception.OutputFileExists;
import org.apache.maven.archetype.exception.PomFileExists;
import org.apache.maven.archetype.exception.ProjectDirectoryExists;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @plexus.component
 */
public class DefaultArchetypeGenerator
extends AbstractLogEnabled
implements ArchetypeGenerator
{
    /**
     * @plexus.requirement
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

    /**
     * @plexus.requirement
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @plexus.requirement
     */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /**
     * @plexus.requirement
     */
    private FilesetArchetypeGenerator filesetGenerator;
    /**
     * @plexus.requirement
     */
    private Archetype oldArchetype;

    public void generateArchetype (
        File propertyFile,
        ArtifactRepository localRepository,
        List repositories,
        String basedir
    )
    throws IOException,
        ArchetypeNotDefined,
        UnknownArchetype,
        ArchetypeNotConfigured,
        ProjectDirectoryExists,
        PomFileExists,
        OutputFileExists,
        FileNotFoundException,
        XmlPullParserException,
        DocumentException,
        InvalidPackaging,
        ArchetypeGenerationFailure
    {
        Properties properties = initialiseArchetypeProperties ( propertyFile );

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
                "The desired archetype does not exist (" + archetypeDefinition.getGroupId () + ":"
                + archetypeDefinition.getArtifactId () + ":" + archetypeDefinition.getVersion ()
                + ")"
            );
        }

        if ( archetypeArtifactManager.isFileSetArchetype (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            )
        )
        {
            processFileSetArchetype (
                properties,
                localRepository,
                basedir,
                repositories,
                archetypeDefinition
            );
        }
        else if (
            archetypeArtifactManager.isOldArchetype (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            )
        )
        {
            processOldArchetype (
                localRepository,
                properties,
                basedir,
                archetypeDefinition,
                repositories
            );
        }
        else
        {
            throw new ArchetypeGenerationFailure ( "The defined artifact is not an archetype" );
        }
    }

    /**Common*/
    public String getPackageAsDirectory ( String packageName )
    {
        return StringUtils.replace ( packageName, ".", "/" );
    }

    /**Common*/
    private Properties initialiseArchetypeProperties ( File propertyFile )
    throws IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );
        return properties;
    }

    /**FileSetArchetype*/
    private void processFileSetArchetype (
        final Properties properties,
        final ArtifactRepository localRepository,
        final String basedir,
        final List repositories,
        final ArchetypeDefinition archetypeDefinition
    )
    throws UnknownArchetype,
        ArchetypeNotConfigured,
        ProjectDirectoryExists,
        PomFileExists,
        OutputFileExists,
        ArchetypeGenerationFailure
    {
        File archetypeFile =
            archetypeArtifactManager.getArchetypeFile (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );

        filesetGenerator.generateArchetype ( properties, archetypeFile, basedir );
    }

    /**OldArchetype*/
    private void processOldArchetype (
        final ArtifactRepository localRepository,
        final Properties properties,
        final String basedir,
        final ArchetypeDefinition archetypeDefinition,
        final List repositories
    )
    throws UnknownArchetype, ArchetypeGenerationFailure
    {
        ArchetypeConfiguration archetypeConfiguration;

        org.apache.maven.archetype.descriptor.ArchetypeDescriptor archetypeDescriptor =
            archetypeArtifactManager.getOldArchetypeDescriptor (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories
            );
        archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration ( archetypeDescriptor, properties );

        Map map = new HashMap ();

        map.put ( "basedir", basedir );

        map.put (
            "package",
            archetypeConfiguration.getProperties ().getProperty ( Constants.PACKAGE )
        );

        map.put (
            "packageName",
            archetypeConfiguration.getProperties ().getProperty ( Constants.PACKAGE )
        );

        map.put (
            "groupId",
            archetypeConfiguration.getProperties ().getProperty ( Constants.GROUP_ID )
        );

        map.put (
            "artifactId",
            archetypeConfiguration.getProperties ().getProperty ( Constants.ARTIFACT_ID )
        );

        map.put (
            "version",
            archetypeConfiguration.getProperties ().getProperty ( Constants.VERSION )
        );
        try
        {
            oldArchetype.createArchetype (
                archetypeDefinition.getGroupId (),
                archetypeDefinition.getArtifactId (),
                archetypeDefinition.getVersion (),
                localRepository,
                repositories,
                map
            );
        }
        catch ( ArchetypeDescriptorException ex )
        {
            throw new ArchetypeGenerationFailure (
                "Failed to generate project from the old archetype"
            );
        }
        catch ( ArchetypeTemplateProcessingException ex )
        {
            throw new ArchetypeGenerationFailure (
                "Failed to generate project from the old archetype"
            );
        }
        catch ( ArchetypeNotFoundException ex )
        {
            throw new ArchetypeGenerationFailure (
                "Failed to generate project from the old archetype"
            );
        }
    }
}
