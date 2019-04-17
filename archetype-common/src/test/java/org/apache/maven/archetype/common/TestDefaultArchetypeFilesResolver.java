package org.apache.maven.archetype.common;

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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.archetype.metadata.FileSet;

public class TestDefaultArchetypeFilesResolver
    extends TestCase
{
    public void testResourceFiltering()
        throws Exception
    {
        FileSet fileSet = new FileSet();

        fileSet.addInclude( "**/*.java" );

        fileSet.setDirectory( "src/main/java" );
        fileSet.setEncoding( "UTF-8" );
        fileSet.setPackaged( true );
        fileSet.setFiltered( true );

        List<String> archetypeResources = new ArrayList<>();

        archetypeResources.add( "pom.xml" );
        archetypeResources.add( "App.java" );
        archetypeResources.add( "src/main/c/App.c" );
        archetypeResources.add( "src/main/java/App.java" );
        archetypeResources.add( "src/main/java/inner/package/App2.java" );
        archetypeResources.add( "src/main/mdo/App.mdo" );
        archetypeResources.add( "src/main/resources/App.properties" );
        archetypeResources.add( "src/main/resources/inner/dir/App2.properties" );
        archetypeResources.add( "src/test/c/AppTest.c" );
        archetypeResources.add( "src/test/java/AppTest.java" );
        archetypeResources.add( "src/test/mdo/AppTest.mdo" );
        archetypeResources.add( "src/test/resources/AppTest.properties" );

        System.out.println( "FileSet:" + fileSet );
        System.out.println( "Resources:" + archetypeResources );

        ArchetypeFilesResolver resolver = new DefaultArchetypeFilesResolver();

        List<String> fileSetResources = resolver.filterFiles( "", fileSet, archetypeResources );

        System.out.println( "Result:" + fileSetResources );

        assertEquals( 2, fileSetResources.size() );
    }

}
