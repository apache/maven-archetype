---
title: Create an archetype from a multi-module project
author: 
  - Raphaël Piéroni
date: 2011-09-30
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

# Create an archetype from a multi-module project

Creating an archetype for a multi-module project is as simple as [creating one for a single-module project](../advanced-usage.html).

Just move to the root directory of a multi-module project and call `mvn archetype:create-from-project`. In the archetype used as an example, some files need to be non filtered. This is achieved by giving the archetype plugin some values in the `archetype.filteredExtensions` property.

The example project has a parent `com.company:multi` and two modules, `multi-core` and `multi-app`:

```text
.
├── multi-app
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── company
│                       └── project
│                           └── AppService.java
├── multi-core
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── com
│                   └── company
│                       └── project
│                           └── CoreService.java
└── pom.xml
```

```shell
$ mvn archetype:create-from-project -Darchetype.filteredExtensions=java
[INFO] Reactor Build Order:
[INFO] multi                                                              [pom]
[INFO] multi-core                                                         [jar]
[INFO] multi-app                                                          [jar]
[INFO] --- archetype:3.4.1:create-from-project (default-cli) @ multi ---
[INFO] Setting default groupId: com.company
[INFO] Setting default artifactId: multi
[INFO] Setting default version: 1.0-SNAPSHOT
[INFO] Setting default package: com.company.project
[INFO] Archetype project created in /home/user/projects/multi/target/generated-sources/archetype
[INFO] Reactor Summary for multi 1.0-SNAPSHOT:
[INFO] multi .............................................. SUCCESS
[INFO] multi-core ......................................... SKIPPED
[INFO] multi-app .......................................... SKIPPED
[INFO] BUILD SUCCESS
```

The goal runs only in the root project, as the Reactor Summary shows. Install the archetype as usual.

```shell
$ cd target/generated-sources/archetype/
$ mvn install
[INFO] --- archetype:3.4.1:jar (default-jar) @ multi-archetype ---
[INFO] --- archetype:3.4.1:integration-test (default-integration-test) @ multi-archetype ---
[INFO] Processing Archetype IT project: basic
[INFO] --- install:3.1.4:install (default-install) @ multi-archetype ---
[INFO] --- archetype:3.4.1:update-local-catalog (default-update-local-catalog) @ multi-archetype ---
[INFO] BUILD SUCCESS
```

And use it. This example selects the archetype in batch mode from the local catalog.

```shell
$ cd /home/user/projects/
$ mvn archetype:generate -B -DarchetypeCatalog=local -DarchetypeGroupId=com.company -DarchetypeArtifactId=multi-archetype -DarchetypeVersion=1.0-SNAPSHOT -DgroupId=org.example -DartifactId=demo -Dversion=1.0-SNAPSHOT
[INFO] Generating project in Batch mode
[INFO] Archetype repository not defined. Using the one from [com.company:multi-archetype:1.0-SNAPSHOT] found in catalog local
[INFO] Parameter: groupId, Value: org.example
[INFO] Parameter: artifactId, Value: demo
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] Parameter: package, Value: org.example
[INFO] Project created from Archetype in dir: /home/user/projects/demo
[INFO] BUILD SUCCESS
```

As one can see in the tree of the archetype, the archetype plugin has renamed the original project modules to "\__rootArtifactId\_\_-core" and "\__rootArtifactId\_\_-app". And during the generation that name is converted to the provided artifactId.

```text
archetype-resources
├── __rootArtifactId__-app
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── AppService.java
├── __rootArtifactId__-core
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── CoreService.java
└── pom.xml
```

The resulting directory tree of the project is shown here.

```text
demo
├── demo-app
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── org
│                   └── example
│                       └── AppService.java
├── demo-core
│   ├── pom.xml
│   └── src
│       └── main
│           └── java
│               └── org
│                   └── example
│                       └── CoreService.java
└── pom.xml
```

This can be compared to the original tree of the project from which the archetype is originating, shown above.
