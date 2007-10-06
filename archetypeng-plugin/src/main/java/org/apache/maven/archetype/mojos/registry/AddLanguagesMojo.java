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

package org.apache.maven.archetype.mojos.registry;

import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.registry.ArchetypeRegistry;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Adds one or more language directories in the registry.
 * The registered language directories are used to discriminate
 * packaging directories from unpackaged ones based on their name
 * during create-from-project.
 *
 * @author rafale
 * @requiresProject false
 * @goal add-languages
 */
public class AddLanguagesMojo
    extends AbstractMojo
{
    /** @component */
    ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * The language directory to add to the registry.
     * <p/>
     * This option is mutually exclusive with language directories.
     *
     * @parameter expression="${language}"
     */
    String language;

    /**
     * The language directories to add to the registry: lang1,lang2,...
     * <p/>
     * This option is mutually exclusive with language directory.
     *
     * @parameter expression="${languages}"
     */
    String languages;

    /**
     * The location of the registry file.
     *
     * @parameter expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    public void execute()
        throws
        MojoExecutionException,
        MojoFailureException
    {
        if ( StringUtils.isEmpty( language ) && StringUtils.isEmpty( languages ) )
        {
            throw new MojoFailureException( "-Dlanguage or -Dlanguages must be set" );
        }
        else if ( StringUtils.isNotEmpty( language ) && StringUtils.isNotEmpty( languages ) )
        {
            throw new MojoFailureException( "Only one of -Dlanguage or -Dlanguages can be set" );
        }

        try
        {
            List languagesToAdd = new ArrayList();
            if ( StringUtils.isNotEmpty( language ) )
            {
                languagesToAdd.add( language );
            }
            else
            {
                languagesToAdd.addAll( Arrays.asList( StringUtils.split( languages, "," ) ) );
            }

            ArchetypeRegistry registry;
            try
            {
                registry = archetypeRegistryManager.readArchetypeRegistry( archetypeRegistryFile );
            }
            catch ( FileNotFoundException ex )
            {
                registry = archetypeRegistryManager.getDefaultArchetypeRegistry();
            }

            Iterator languagesToAddIterator = languagesToAdd.iterator();
            while ( languagesToAddIterator.hasNext() )
            {
                String languageToAdd = (String) languagesToAddIterator.next();
                if ( registry.getLanguages().contains( languageToAdd ) )
                {
                    getLog().debug( "Language " + languageToAdd + " already exists" );
                }
                else
                {
                    registry.addLanguage( languageToAdd.trim() );
                    getLog().debug( "Language " + languageToAdd + " added" );
                }
            }
            archetypeRegistryManager.writeArchetypeRegistry( archetypeRegistryFile, registry );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
    }
}
