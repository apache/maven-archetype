
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

File archetype = new File( basedir, "target/generated-sources/archetype/src/main/resources/archetype-resources/" );

// ARCHETYPE-513
String[] excluded = new String[] { "/", "src/", "src/main/", "src/main/resources/" };
for ( String exclude : excluded )
{
    File app = new File( archetype, exclude + "toexclude" );
    if ( app.exists() )
    {
        throw new Exception( app + " folder exists when it should have been excluded." );
    }
}

String[] included = new String[] { "file.txt", "file.xml" };
for ( String include : included )
{
    File app = new File( archetype, "src/main/resources/" + include );
    if ( !app.isFile() )
    {
        throw new Exception( app + " file does not exist when it should have been included." );
    }
}

return true;
