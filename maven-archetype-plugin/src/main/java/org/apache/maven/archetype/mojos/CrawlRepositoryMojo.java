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

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.repositorycrawler.RepositoryCrawler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * Crawl a Maven repository (filesystem, not HTTP) and creates a catalog file.
 *
 * @author           rafale
 * @requiresProject  false
 * @goal             crawl
 */
public class CrawlRepositoryMojo
    extends AbstractMojo
{
    /**
     * The archetype's catalog to update.
     *
     * @parameter  expression="${catalog}"
     */
    private File catalogFile;

    /**
     * @component
     */
    private RepositoryCrawler crawler;

    /**
     * The repository to crawl.
     *
     * @parameter  expression="${repository}" default-value="${settings.localRepository}"
     */
    private File repository;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        System.err.println( "repository " + repository );
        System.err.println( "catalogFile " + catalogFile );

        if ( repository == null )
        {
            throw new MojoFailureException( "The repository is not defined. Use -Drepository=/path/to/repository" );
        }

        ArchetypeCatalog catalog = crawler.crawl( repository );

        if ( catalogFile == null )
        {
            catalogFile = new File( repository, "archetype-catalog.xml" );
        }

        crawler.writeCatalog( catalog, catalogFile );
    }
}
