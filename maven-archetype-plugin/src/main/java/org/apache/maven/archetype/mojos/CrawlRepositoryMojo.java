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
 * Crawl a Maven repository (filesystem, not HTTP) and creates a catalog file.
 *
 * @author rafale
 * @requiresProject false
 * @goal crawl
 */
public class CrawlRepositoryMojo extends AbstractMojo
{
    /**
     * The archetype's catalog to update.
     *
     * @parameter expression="${catalog}"
     */
    private File catalogFile;

    /** @component */
    private RepositoryCrawler crawler;

    /**
     * The repository to crawl.
     *
     * @parameter expression="${repository}" default-value="${settings.localRepository}"
     */
    private File repository;

    /** @parameter expression="${remoteRepository}" */
    private String remoteRepository;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        System.err.println("repository " + repository);
        System.err.println("catalogFile " + catalogFile);
        System.err.println("remoteRepository " + remoteRepository);

        if (repository == null)
        {
            throw new MojoFailureException("The repository is not defined. Use -Drepository=/path/to/repository");
        }

        final ArchetypeCatalog catalog;

        // process archiva repositories in a batch
        if (repository.getAbsolutePath().matches(".*/archiva/data/repositories/$"))
        {
            catalog = new ArchetypeCatalog();
            final List<File> repositories = Arrays.asList(repository.listFiles(new FileFilter()
            {
                public boolean accept(final File pathname)
                {
                    return pathname.isDirectory();
                }
            }));
            // add all the repositories to a single ArchetypeCatalog to merge all the repositories
            // into one archetype-catalog.xml file
            for (final File dir : repositories)
            {
                final ArchetypeCatalog ac = crawler.crawl(dir);
                if (remoteRepository != null)
                {
                    for (final Archetype a : ac.getArchetypes())
                    {
                        a.setRepository(remoteRepository + dir.getName());
                    }
                }
                catalog.getArchetypes().addAll(ac.getArchetypes());
            }
        }
        else // process a single repository
        {
            catalog = crawler.crawl(repository);
            if (remoteRepository != null)
            {
                for (final Archetype a : catalog.getArchetypes())
                {
                    a.setRepository(remoteRepository);
                }
            }
        }

        if (catalogFile == null)
        {
            catalogFile = new File(repository, "archetype-catalog.xml");
        }

        crawler.writeCatalog(catalog, catalogFile);
    }
}
