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
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

/**
 * Updates the local catalog
 *
 * @author rafale
 */
@Mojo( name = "update-local-catalog", defaultPhase = LifecyclePhase.INSTALL )
public class UpdateLocalCatalogMojo
    extends AbstractMojo
{
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;
    
    @Component
    private ArchetypeManager manager;

    /**
     * The archetype project to add/update to the local catalog.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Override
    public void execute()
        throws MojoExecutionException
    {
        if ( !Constants.MAVEN_ARCHETYPE_PACKAGING.equalsIgnoreCase( project.getPackaging() ) )
        {
            getLog().debug( "Wrong packaging type " + project.getPackaging() + ", skipping archetype " + project.getName() );
            return;
        }
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

        manager.updateLocalCatalog( session.getProjectBuildingRequest(), archetype );
    }
}