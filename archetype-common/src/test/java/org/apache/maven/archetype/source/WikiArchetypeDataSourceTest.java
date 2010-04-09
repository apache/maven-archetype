package org.apache.maven.archetype.source;

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
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.Properties;

/** @author Jason van Zyl */
public class WikiArchetypeDataSourceTest
    extends PlexusTestCase
{
    private static final String[][] REFERENCE =
        {
            { "appfuse-basic-jsf", "org.appfuse.archetypes", "2.0", "http://static.appfuse.org/releases",
                "AppFuse archetype for creating a web application with Hibernate, Spring and JSF" },
            { "maven-archetype-profiles", "org.apache.maven.archetypes", "RELEASE", "http://repo1.maven.org/maven2", "" },
            { "struts2-archetype-starter", "org.apache.struts", "2.0.9-SNAPSHOT",
                "http://people.apache.org/repo/m2-snapshot-repository",
                "A starter Struts 2 application with Sitemesh, DWR, and Spring" },
            { "maven-archetype-har", "net.sf.maven-har", "0.9", "", "Hibernate Archive" },
            { "maven-archetype-archetype", "org.apache.maven.archetypes", "RELEASE", "", "" }
        };

    public void testWikiArchetypeDataSource()
        throws Exception
    {
        File wikiSource = new File( getBasedir(), "src/test/resources/wiki/wiki-source.txt" );

        assertTrue( wikiSource.exists() );

        Properties p = new Properties();
        p.put( "url", wikiSource.toURL().toExternalForm() );

        ArchetypeDataSource ads = new WikiArchetypeDataSource();

        ArchetypeCatalog catalog = ads.getArchetypeCatalog( p );

        int catalogSize = catalog.getArchetypes().size();

        assertEquals( REFERENCE.length, catalogSize );

        for ( int i = 0; i < catalogSize; i++ )
        {
            String[] reference = REFERENCE[i];

            Archetype ar = (Archetype) catalog.getArchetypes().get( i );

            assertEquals( "#" + i + " artifactId", reference[0], ar.getArtifactId() );
            assertEquals( "#" + i + " groupId", reference[1], ar.getGroupId() );
            assertEquals( "#" + i + " version", reference[2], ar.getVersion() );
            assertEquals( "#" + i + " repository", reference[3], ar.getRepository() );
            assertEquals( "#" + i + " description", reference[4], ar.getDescription() );
        }
    }
}
