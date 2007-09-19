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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Arrays;

/**
 * Generates sample project from archetype.
 * It delegates to the three mojos of the generate lifecycle: select-archetype, configure-generation, generate-project
 *
 * @author rafale
 * @requiresProject false
 * @goal create
 * @execute phase="generate-sources" lifecycle="generate"
 */
public class CreateProjectFromArchetypeMojo
    extends AbstractMojo
    implements ContextEnabled
{
    /**
     * Maven invoker used to execution additional goals after the archetype has been created.
     *
     * @component
     */
    private Invoker invoker;

    /** @parameter expression="${basedir}" */
    private File basedir;

    /** 
     * Additional goals that can be specified by the user during the creation of the archetype.
     *
     * @parameter expression="${goals}"
     */
    private String goals;

    public void execute()
        throws
        MojoExecutionException,
        MojoFailureException
    {
        // At this point the archetype has been generated from the archetype and now we will
        // run some goals that the archetype creator has requested to be run once the project
        // has been created.

        File projectBasedir = new File( basedir, (String) getPluginContext().get( "artifactId" ) );

        if ( goals != null && projectBasedir.exists() )
        {
            InvocationRequest request = new DefaultInvocationRequest()
                .setBaseDirectory( projectBasedir )
                .setGoals( Arrays.asList( StringUtils.split( goals, "," ) ) );

            try
            {
                invoker.execute( request );
            }
            catch ( MavenInvocationException e )
            {
                throw new MojoExecutionException( "Cannot run additions goals." );
            }
        }
    }
}
