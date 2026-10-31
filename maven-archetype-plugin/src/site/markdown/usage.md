---
title: Usage
author: 
  - Raphaël Piéroni
  - Olivier Lamy
  - Hervé Boutemy
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

# Usage

Creating a project from an archetype involves four steps:

1. Prepare the repository reference.
1. Select the archetype.
1. Configure the archetype.
1. Create the project from the collected information.

## Prepare the repository reference

In general an archetype is coming from a remote repository. If that repository can be reached via the setup of your Maven, you're ready to start. In cases where the repository is not managed and you want to refer to it directly, you have to add the repository to your `settings.xml`. Read the small set of instructions on the [Archetype Repository](./archetype-repository.html)-page.

## Select and configure the archetype

When you call `archetype:generate`, the plugin first asks you to choose an archetype from the catalogs. By default, these are the `remote` catalog from Maven Central, followed by the `local` one: see [Generate project using an alternative catalog](./examples/generate-alternative-catalog.html). Enter the number of the archetype.

It then asks you to enter the values for the groupId, the artifactId and the version of the project to create and the base package for the sources.

It then asks for confirmation of the configuration and performs the creation of the project.

The catalog of Maven Central lists thousands of archetypes. The following example uses the `filter` property, described in [Filtering to reduce archetype list](#filtering), to keep the list short. It selects the quickstart archetype in version 1.5 and sets `groupId` to `com.company`, `artifactId` to `project`, `version` to `1.0` and `package` to `com.company.project`.

```shell
$ mvn archetype:generate -Dfilter=org.apache.maven.archetypes:quickstart
[INFO] --- archetype:3.4.1:generate (default-cli) @ standalone-pom ---
[INFO] Generating project in Interactive mode
[INFO] No archetype defined. Using maven-archetype-quickstart (org.apache.maven.archetypes:maven-archetype-quickstart:1.0)
Choose archetype:
1: remote -> org.apache.maven.archetypes:maven-archetype-quickstart (An archetype which contains a sample Maven project.)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): 1: 1
Choose org.apache.maven.archetypes:maven-archetype-quickstart version:
1: 1.0-alpha-1
...
9: 1.5
10: 1.6-SNAPSHOT
Choose a number: 10: 9
Define value for property 'groupId': com.company
Define value for property 'artifactId': project
Define value for property 'version' 1.0-SNAPSHOT: 1.0
Define value for property 'package' com.company: com.company.project
Confirm properties configuration:
javaCompilerVersion: 17
junitVersion: 5.11.0
groupId: com.company
artifactId: project
version: 1.0
package: com.company.project
 Y: 
[INFO] Using following parameters for creating project from Archetype: maven-archetype-quickstart:1.5
[INFO] Project created from Archetype in dir: /home/user/projects/project
[INFO] BUILD SUCCESS
```

Here's the resulting tree of the created project.

```text
project
├── .mvn
│   ├── jvm.config
│   └── maven.config
├── pom.xml
└── src
    ├── main
    │   └── java
    │       └── com
    │           └── company
    │               └── project
    │                   └── App.java
    └── test
        └── java
            └── com
                └── company
                    └── project
                        └── AppTest.java
```

<a id="filtering"></a>

## Filtering to reduce archetype list

You can reduce the list of displayed archetypes. The filter use the following format: `[groupId:]artifactId`. If you use single word without `:`, only artifactId will be checked. The filtering applied is a case sensitive contains on the artifactId (and groupId if set).

Two options

1. With a mojo parameter:

    ```shell
    $ mvn archetype:generate -Dfilter=org.apache:struts
    ```

    The displayed list will contain only archetypes with a groupId containing `org.apache` AND an artifactId containing `struts`

1. Through the prompt:

    ```shell
    $ mvn archetype:generate
    ```

    The full list is displayed and in the prompt response, you will be able to answer with a filter.

    ```unknown
      Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): org.apache:struts
    ```

    Notice that if your filter doesn't match any archetype, the previous list will be displayed again.
