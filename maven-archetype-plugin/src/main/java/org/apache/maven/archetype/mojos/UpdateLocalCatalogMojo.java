package org.apache.maven.archetype.mojos;

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

import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Updates the local catalog
 *
 * @phase install
 * @goal update-local-catalog
 *
 * @author rafale
 */
public class UpdateLocalCatalogMojo
    extends AbstractMojo
{
    /** @component */
    private ArchetypeManager manager;

    /**
     * The archetype project to add/update to the local catalog.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
        Archetype archetype = new Archetype();
        archetype.setGroupId( project.getGroupId() );
        archetype.setArtifactId( project.getArtifactId() );
        archetype.setVersion( project.getVersion() );

        if ( StringUtils.isNotEmpty( project.getDescription() ) )
        {
            archetype.setDescription( project.getDescription() );
        }
        else
        {
            archetype.setDescription( project.getName() );
        }

        manager.updateLocalCatalog( archetype );
    }
}