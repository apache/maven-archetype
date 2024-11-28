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
package org.apache.maven.archetype.repositorycrawler;

import java.io.File;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * @author  rafale
 */
public class DefaultRepositoryCrawlerTest extends AbstractMojoTestCase {
    /**
     * Test of crawl method, of class DefaultRepositoryCrawler.
     */
    public void testCrawl() throws Exception {
        File repository = getTestFile("target/test-classes/repositories/central");
        RepositoryCrawler instance = (RepositoryCrawler) lookup(RepositoryCrawler.class.getName());

        ArchetypeCatalog result = instance.crawl(repository);
        assertTrue(
                "result.getArchetypes().size() = " + result.getArchetypes().size() + " should be in [5,8], result = "
                        + result,
                (5 <= result.getArchetypes().size()) && (result.getArchetypes().size() <= 8));

        // TODO: should write to another directory
        //        instance.writeCatalog(result, new File(repository, "archetype-catalog.xml"));
    }
}
