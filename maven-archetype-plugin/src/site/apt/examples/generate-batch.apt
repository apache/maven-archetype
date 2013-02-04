 ------
 Generate project in batch mode
 ------
 Raphaël Piéroni
 ------
 2011-09-30
 ------

~~ Licensed to the Apache Software Foundation (ASF) under one
~~ or more contributor license agreements.  See the NOTICE file
~~ distributed with this work for additional information
~~ regarding copyright ownership.  The ASF licenses this file
~~ to you under the Apache License, Version 2.0 (the
~~ "License"); you may not use this file except in compliance
~~ with the License.  You may obtain a copy of the License at
~~
~~     http://www.apache.org/licenses/LICENSE-2.0
~~
~~ Unless required by applicable law or agreed to in writing,
~~ software distributed under the License is distributed on an
~~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~~ KIND, either express or implied.  See the License for the
~~ specific language governing permissions and limitations
~~ under the License.

~~ NOTE: For help with the syntax of this file, see:
~~ http://maven.apache.org/doxia/references/apt-format.html

Generate project in batch mode

   It is possible to get rid of the interactivity of the Maven Archetype Plugin
   by setting the <<<interactive>>> property to <<<false>>> or by using the -B
   flag.
   A couple of meaningful properties are then required:
   
   * The archetypeGroupId, archetypeArtifactId and archetypeVersion defines the 
   archetype to use for project generation.

   * The groupId, artifactId, version and package are the main properties to be 
   set. Each archetype require these properties. Some archetypes define other 
   properties; refer to the appropriate archetype's documentation if needed.

   []

+----
$ mvn archetype:generate -B -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.1 -DgroupId=com.company -DartifactId=project -Dversion=1.0-SNAPSHOT -Dpackage=com.company.project
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] >>> maven-archetype-plugin:2.2:generate (default-cli) @ standalone-pom >>>
[INFO] 
[INFO] <<< maven-archetype-plugin:2.2:generate (default-cli) @ standalone-pom <<<
[INFO] 
[INFO] --- maven-archetype-plugin:2.2:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Batch mode
[INFO] Archetype repository missing. Using the one from [org.apache.maven.archetypes:maven-archetype-quickstart:1.1] found in catalog remote
[INFO] ----------------------------------------------------------------------------
[INFO] Using following parameters for creating project from Old (1.x) Archetype: maven-archetype-quickstart:1.1
[INFO] ----------------------------------------------------------------------------
[INFO] Parameter: groupId, Value: com.company
[INFO] Parameter: packageName, Value: com.company.project
[INFO] Parameter: package, Value: com.company.project
[INFO] Parameter: artifactId, Value: project
[INFO] Parameter: basedir, Value: /Users/maven/dev
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] project created from Old (1.x) Archetype in dir: /Users/maven/dev/project
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 9.184s
[INFO] Finished at: Mon Feb 04 11:53:24 CET 2013
[INFO] Final Memory: 9M/265M
[INFO] ------------------------------------------------------------------------
+----