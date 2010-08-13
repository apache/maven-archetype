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
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/** @author Jason van Zyl */
public class CatalogArchetypeDataSinkTest
    extends PlexusTestCase
{
    public void testCatalogArchetypeDataSink()
        throws Exception
    {
        Archetype a0 = new Archetype();

        a0.setGroupId( "groupId" );

        a0.setArtifactId( "artifactId" );

        a0.setVersion( "1.0" );

        a0.setDescription( "description" );

        a0.setRepository( "http://magicbunny.com/maven2" );

        List<Archetype> archetypes = new ArrayList<Archetype>();

        archetypes.add( a0 );

        ArchetypeDataSink sink = new CatalogArchetypeDataSink();

        Writer writer = new StringWriter();

        sink.putArchetypes( archetypes, writer );

        StringReader reader = new StringReader( writer.toString() );

        ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

        ArchetypeCatalog catalog = catalogReader.read( reader );

        Archetype a1 = (Archetype) catalog.getArchetypes().get( 0 );

        assertEquals( "groupId", a1.getGroupId()  );

        assertEquals( "artifactId", a1.getArtifactId() );

        assertEquals( "1.0", a1.getVersion()  );

        assertEquals( "description", a1.getDescription()  );

        assertEquals( "http://magicbunny.com/maven2", a1.getRepository()  );
    }
}
