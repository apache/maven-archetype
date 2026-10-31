---
title: Create an archetype with a property file
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

# Create an archetype with a property file

Creating an archetype using a property file is possible by defining a text file containing the properties that will lead the archetype creation, and by calling the Archetype Plugin with the command line property `archetype.properties` giving it the path to the property file.

The directory above the would be archetype project:

```text
.
├── archetype.properties
└── some-project
    ├── pom.xml
    └── src
        ├── main
        │   ├── groovy
        │   │   └── com
        │   │       └── company
        │   │           └── App.groovy
        │   ├── java
        │   │   └── com
        │   │       └── company
        │   │           └── App.java
        │   └── resources
        │       └── App.properties
        └── test
            └── java
                └── com
                    └── company
                        └── AppTest.java
```

The content of the property file:

```shell
$ cat archetype.properties
archetype.groupId=my.group.id
archetype.artifactId=archetype-with-properties
archetype.version=2.0

archetype.filteredExtensions=java
archetype.languages=groovy

an_additional_property=my specific value
```

Creating the archetype:

```shell
$ cd some-project
$ mvn archetype:create-from-project -Darchetype.properties=../archetype.properties
[INFO] --- archetype:3.4.1:create-from-project (default-cli) @ some-project ---
[INFO] Setting default groupId: com.company
[INFO] Setting default artifactId: some-project
[INFO] Setting default version: 1.0-SNAPSHOT
[INFO] Setting default package: com.company
[INFO] Archetype project created in /home/user/projects/some-project/target/generated-sources/archetype
[INFO] BUILD SUCCESS

$ cd target/generated-sources/archetype/
$ mvn install
[INFO] --- archetype:3.4.1:jar (default-jar) @ archetype-with-properties ---
[INFO] --- archetype:3.4.1:integration-test (default-integration-test) @ archetype-with-properties ---
[INFO] Processing Archetype IT project: basic
[INFO] --- install:3.1.4:install (default-install) @ archetype-with-properties ---
[INFO] --- archetype:3.4.1:update-local-catalog (default-update-local-catalog) @ archetype-with-properties ---
[INFO] BUILD SUCCESS
```

Using that archetype. The additional property is proposed with its default value; this example overrides it on the command line:

```shell
$ mvn archetype:generate -B -DarchetypeCatalog=local -DarchetypeGroupId=my.group.id -DarchetypeArtifactId=archetype-with-properties -DarchetypeVersion=2.0 -DgroupId=my.new.group -DartifactId=my-new-artifact -Dan_additional_property="my new value"
[INFO] Generating project in Batch mode
[INFO] Parameter: groupId, Value: my.new.group
[INFO] Parameter: artifactId, Value: my-new-artifact
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] Parameter: package, Value: my.new.group
[INFO] Parameter: an_additional_property, Value: my new value
[INFO] Project created from Archetype in dir: /home/user/projects/my-new-artifact
[INFO] BUILD SUCCESS
```

In interactive mode, the additional property is provided with its default value. As one want to override the value, one just have to answer `N` to the first confirmation, then the whole bunch of properties is proposed.

The tree of the generated project is shown here. The property `archetype.languages` gives the list of the languages. The languages are the directories in `src/main` and `src/test` that have the capacity to contain a "packaged" directory tree. This means that the `package` value is used to generate the new directory for that language.

```text
my-new-artifact
├── pom.xml
└── src
    ├── main
    │   ├── groovy
    │   │   └── my
    │   │       └── new
    │   │           └── group
    │   │               └── App.groovy
    │   ├── java
    │   │   └── my
    │   │       └── new
    │   │           └── group
    │   │               └── App.java
    │   └── resources
    │       └── App.properties
    └── test
        └── java
            └── my
                └── new
                    └── group
                        └── AppTest.java
```

The contents of both App.groovy and App.java shows that the only filtered files are java files. The property `archetype.filteredExtensions` gives the list of file extensions that permit the filtering. In the example, only the files whose names ending with `.java` are filtered.

```shell
$ cat src/main/groovy/my/new/group/App.groovy
package com.company

class App {
  def value = 'my specific value'
}

$ cat src/main/java/my/new/group/App.java
package my.new.group;

public class App {
    String value = "my new value";
}
```

The languages and the filtered extensions are used even if that don't make any sense. All the properties defined in the property file can be used as command line properties.

When undefined, the `archetype.languages` and `archetype.filteredExtensions` properties are given sensible default values:

- `archetype.filteredExtensions` have: `java, xml, txt, groovy, cs, mdo, aj, jsp, gsp, vm, html, xhtml, properties, .classpath, .project`. Notice the dotted filtered extensions that contains the complete file names.
- `archetype.languages` have: `java, groovy, csharp, aspectj`.
