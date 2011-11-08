
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

import java.io.*;
import org.codehaus.plexus.util.*;

basedir = new File( basedir, "target/test-classes/projects/basic/project/basic" );

File main = new File( basedir, "src/main" );

// check <fileset packaged="true">
File app = new File( main, "java/build/archetype/App.java" );
if ( !app.isFile() )
{
    throw new Exception( app + " file is missing or not a file." );
}

// check __propertyName__ path replacement
File artifactId = new File( main, "resources/test-basic.properties" );
if ( !artifactId.isFile() )
{
    throw new Exception( artifactId + " file is missing or not a file." );
}

File packageInPathFormat = new File( main, "resources/build/archetype/build.archetype.properties" );
if ( !packageInPathFormat.isFile() )
{
    throw new Exception( packageInPathFormat + " file is missing or not a file." );
}

// ARCHETYPE-289 check empty directory creation
File empty = new File( main, "resources/empty-directory" );
if ( !empty.isDirectory() )
{
    throw new Exception( empty + " directory is missing or not a directory." );
}
File filteredEmpty = new File( main, "resources/basic-empty-directory" );
if ( !filteredEmpty.isDirectory() )
{
    throw new Exception( filteredEmpty + " directory is missing or not a directory." );
}

// ARCHETYPE-334 check generated project has been compiled
File appClass = new File( basedir, "target/classes/build/archetype/App.class" );
if ( !appClass.isFile() )
{
    throw new Exception( appClass + " not here: generated project not compiled?" );
}

File buildLog = new File(basedir, "build.log");

String content = FileUtils.fileRead( buildLog, "UTF-8" );

int idx = content.indexOf( "Yeah Baby it rocks!");
if ( idx < 0 )
{
    throw new Exception( "build.log missing out.println from verify.groovy" );
}
