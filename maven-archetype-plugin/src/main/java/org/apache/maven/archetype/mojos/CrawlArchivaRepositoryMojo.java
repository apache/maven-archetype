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

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.repositorycrawler.RepositoryCrawler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Crawl a Archiva repository (filesystem, not HTTP) and creates a master catalog file for the server.
 *
 * @author jarrodroberson
 * @requiresProject false
 * @goal crawl-archiva
 */
public class CrawlArchivaRepositoryMojo extends AbstractMojo
{
    /** @component */
    private RepositoryCrawler crawler;

    /**
     * The repository to crawl.
     *
     * @parameter expression="${archivaHome}"
     */
    private File archivaHome;

    /** @parameter expression="${archivaHost}" */
    private String remoteRepository;

    /**
     * filter by groupId regular expression
     * @parameter expression="${groupId}"
     */
    private String filter;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (archivaHome == null)
        {
            throw new MojoFailureException("The repository is not defined. Use -archiva=/path/to/archiva");
        }

        final File repositories = new File(archivaHome, "data/repositories/");

        // process archiva style repositories in a batch, create a list of repositories to process
        final List<File> repos = Arrays.asList(repositories.listFiles(new FileFilter()
        {
            public boolean accept(final File pathname)
            {
                return pathname.isDirectory();
            }
        }));

        final ArchetypeCatalog catalog = new ArchetypeCatalog();
        // add all the repositories to a single ArchetypeCatalog to merge all the repositories
        // into one archetype-catalog.xml file
        for (final File dir : repos)
        {
            final ArchetypeCatalog ac = crawler.crawl(dir);
            if (filter != null)
            {
                for (final Archetype a : ac.getArchetypes())
                {
                    if (!a.getGroupId().matches(filter)) { ac.removeArchetype(a); }
                }
            }
            // write a repository specific file to the individual repositories
            crawler.writeCatalog(ac, new File(dir, "archetype-catalog.xml"));
            // set the remote repository url if supplied
            if (remoteRepository != null)
            {
                for (final Archetype a : ac.getArchetypes())
                {
                    a.setRepository(remoteRepository + "repository/" + dir.getName());
                }
            }
            catalog.getArchetypes().addAll(ac.getArchetypes());
        }

        // write out a merged archetype-catalog.xml from all the repositories processed
        crawler.writeCatalog(catalog, new File(archivaHome, "apps/archiva/archetype-catalog.xml"));
    }
}
