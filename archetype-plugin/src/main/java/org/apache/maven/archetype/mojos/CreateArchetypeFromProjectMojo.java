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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Creates sample archetype from current project.
 * It delegates to the two mojos of the create lifecycle: configure-creation, create-archetype
 * @author           rafale
 * @requiresProject  true
 * @goal             create-from-project
 * @execute          phase="generate-sources" lifecycle="create"
 */
public class CreateArchetypeFromProjectMojo
extends AbstractMojo
{
    /**
     * dummy parameter used to fulfill the maven-plugin-plugin
     * @parameter
     */
    private String dummy;

    public void execute ()
    throws MojoExecutionException, MojoFailureException
    { }
}
