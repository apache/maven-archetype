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

package org.apache.maven.archetype.common.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:brianf@apache.org">Brian Fox</a> */
public class TestListScanner
    extends TestCase
{
    public void testUnixPaths()
    {
        List archetypeResources = new ArrayList();

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

        ListScanner scanner = new ListScanner();
        scanner.setBasedir( "src/main/java" );
        scanner.setIncludes( "**/*.java" );
        scanner.setCaseSensitive( true );

        List result = scanner.scan( archetypeResources );

        assertEquals( 2, result.size() );
        assertTrue( result.contains( "src/main/java/App.java" ) );
        assertTrue( result.contains( "src/main/java/inner/package/App2.java" ) );
    }

    public void testWindowsPaths()
    {
        List archetypeResources = new ArrayList();

        archetypeResources.add( "pom.xml" );
        archetypeResources.add( "App.java" );
        archetypeResources.add( "src\\main\\c\\App.c" );
        archetypeResources.add( "src\\main\\java\\App.java" );
        archetypeResources.add( "src\\main\\java\\inner\\package\\App2.java" );
        archetypeResources.add( "src\\main\\mdo\\App.mdo" );
        archetypeResources.add( "src\\main\\resources\\App.properties" );
        archetypeResources.add( "src\\main\\resources\\inner\\dir\\App2.properties" );
        archetypeResources.add( "src\\test\\c\\AppTest.c" );
        archetypeResources.add( "src\\test\\java\\AppTest.java" );
        archetypeResources.add( "src\\test\\mdo\\AppTest.mdo" );
        archetypeResources.add( "src\\test\\resources\\AppTest.properties" );

        ListScanner scanner = new ListScanner();
        scanner.setBasedir( "src\\main\\java" );
        scanner.setIncludes( "**\\*.java" );
        scanner.setCaseSensitive( true );

        List result = scanner.scan( archetypeResources );

        assertEquals( 2, result.size() );
        assertTrue( result.contains( "src\\main\\java\\App.java" ) );
        assertTrue( result.contains( "src\\main\\java\\inner\\package\\App2.java" ) );
    }
}
