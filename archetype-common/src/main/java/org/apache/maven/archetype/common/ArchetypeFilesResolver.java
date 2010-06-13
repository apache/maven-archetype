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

import org.apache.maven.archetype.metadata.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** @author rafale */
public interface ArchetypeFilesResolver
{
    String ROLE = ArchetypeFilesResolver.class.getName();

    List<String> getFilesWithExtension( List<String> files, String extension );

    List<String> getFilteredFiles( List<String> files, String filtered );

    List<String> filterFiles( String moduleOffset, FileSet fileSet, List<String> archetypeResources );

    List<String> findOtherResources( int level, List<String> files, String languages );

    List<String> findOtherResources( int level, List<String> files, List<String> sourcesFiles, String languages );

    List<String> findOtherSources( int level, List<String> files, String languages );

    List<String> findResourcesMainFiles( List<String> files, String languages );

    List<String> findResourcesTestFiles( List<String> files, String languages );

    List<String> findSiteFiles( List<String> files, String languages );

    List<String> findSourcesMainFiles( List<String> files, String languages );

    List<String> findSourcesTestFiles( List<String> files, String languages );

    List<String> getPackagedFiles( List<String> files, String packageName );

    String resolvePackage( File file, List<String> languages )
        throws IOException;

    List<String> getUnpackagedFiles( List<String> files, String packageName );
}
