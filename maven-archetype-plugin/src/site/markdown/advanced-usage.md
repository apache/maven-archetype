---
title: Advanced Usage
author: 
  - Raphaël Piéroni
date: 2008
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Archetype creation

Creating an archetype from an existing project involves three steps:

- the archetype resolution
- the archetype installation:deployment
- the archetype usage

## Usage

Calling `mvn archetype:create-from-project` the plugin first resolves the package by guessing the project directory.

It then generates the directory tree of the archetype in the `target/generated-sources/archetype` directory.

```shell
$ mvn archetype:create-from-project
[INFO] --- archetype:3.4.1:create-from-project (default-cli) @ project ---
[INFO] Setting default groupId: com.company
[INFO] Setting default artifactId: project
[INFO] Setting default version: 1.0
[INFO] Setting default package: com.company.project
[INFO] Archetype project created in /home/user/projects/project/target/generated-sources/archetype
[INFO] BUILD SUCCESS
```

Then move to that generated directory and call `mvn install` on the created archetype.

```shell
$ cd target/generated-sources/archetype/
$ mvn install
[INFO] --- archetype:3.4.1:jar (default-jar) @ project-archetype ---
[INFO] Building archetype jar: /home/user/projects/project/target/generated-sources/archetype/target/project-archetype-1.0.jar
[INFO] --- archetype:3.4.1:integration-test (default-integration-test) @ project-archetype ---
[INFO] Processing Archetype IT project: basic
[INFO] --- install:3.1.4:install (default-install) @ project-archetype ---
[INFO] Installing /home/user/projects/project/target/generated-sources/archetype/target/project-archetype-1.0.jar to /home/user/.m2/repository/com/company/project-archetype/1.0/project-archetype-1.0.jar
[INFO] --- archetype:3.4.1:update-local-catalog (default-update-local-catalog) @ project-archetype ---
[INFO] Updated local archetypes catalog /home/user/.m2/repository/archetype-catalog.xml
[INFO] BUILD SUCCESS
```

Finally move to a fresh directory and use your archetype.

```shell
$ mkdir /tmp/archetype
$ cd /tmp/archetype
$ mvn archetype:generate -DarchetypeCatalog=local
Choose archetype:
1: local -> com.company:project-archetype (project-archetype)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): 1: 1
Define value for property 'groupId': org.example
Define value for property 'artifactId': my-app
Define value for property 'version' 1.0-SNAPSHOT: 1.0-SNAPSHOT
Define value for property 'package' org.example: org.example
Confirm properties configuration:
groupId: org.example
artifactId: my-app
version: 1.0-SNAPSHOT
package: org.example
 Y: 
[INFO] Using following parameters for creating project from Archetype: project-archetype:1.0
[INFO] Project created from Archetype in dir: /tmp/archetype/my-app
[INFO] BUILD SUCCESS
```

## Testing

When you create an archetype from a project, the plugin adds a first test project named `basic` in `src/test/resources/projects`. The `archetype:integration-test` goal, which runs during `mvn verify` or `mvn install` of the archetype, generates a project from the archetype for each test project and can compare it with a reference copy. See the [Guide to Testing Archetypes](https://maven.apache.org/guides/mini/guide-testing-archetypes.html).

## Configuration

The `create-from-project` goal enable the user to provide some sensible configuration using the system properties or a property file.

The system properties permits the user to define the groupId, artifactId, version of the created archetype. They also permits to override the resolved package and to create a partial archetype.

When used, the property file takes precedence over the system properties. It is also the only way to define additional properties in the archetype.

## Post-generation script

If the user wants to customize the generated project even further, a groovy script named `archetype-post-generate.groovy` can be added in `src/main/resources/META-INF/`. This script will end up in the generated archetype's `META-INF` folder and will be executed upon creating a project from this archetype. This groovy script has access to the `ArchetypeGenerationRequest` object, as well as all the `System.getProperties()` and all the archetype generation properties the user has specified.
