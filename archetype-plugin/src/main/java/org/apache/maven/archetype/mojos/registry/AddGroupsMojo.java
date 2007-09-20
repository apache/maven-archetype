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
 * Adds one or more groups in the registry.
 * The registered repositories are searched to find archetypes of registered groups.
 *
 * @author rafale
 * @requiresProject false
 * @goal add-groups
 */
public class AddGroupsMojo
    extends AbstractMojo
{
    /** @component */
    ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * The group to add to the registry.
     * <p/>
     * This option is mutually exclusive with groups.
     *
     * @parameter expression="${group}"
     */
    String group;

    /**
     * The groups to add to the registry: group1,group2,...
     * <p/>
     * This option is mutually exclusive with group.
     *
     * @parameter expression="${groups}"
     */
    String groups;

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
        if ( StringUtils.isEmpty( group ) && StringUtils.isEmpty( groups ) )
        {
            throw new MojoFailureException( "-Dgroup or -Dgroups must be set" );
        }
        else if ( StringUtils.isNotEmpty( group ) && StringUtils.isNotEmpty( groups ) )
        {
            throw new MojoFailureException( "Only one of -Dgroup or -Dgroups can be set" );
        }

        try
        {
            List groupsToAdd = new ArrayList();
            if ( StringUtils.isNotEmpty( group ) )
            {
                groupsToAdd.add( group );
            }
            else
            {
                groupsToAdd.addAll( Arrays.asList( StringUtils.split( groups, "," ) ) );
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

            Iterator groupsToAddIterator = groupsToAdd.iterator();
            while ( groupsToAddIterator.hasNext() )
            {
                String groupToAdd = (String) groupsToAddIterator.next();
                if ( registry.getArchetypeGroups().contains( groupToAdd ) )
                {
                    getLog().debug( "Group " + groupToAdd + " already exists" );
                }
                else
                {
                    registry.addArchetypeGroup( groupToAdd.trim() );
                    getLog().debug( "Group " + groupToAdd + " added" );
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
