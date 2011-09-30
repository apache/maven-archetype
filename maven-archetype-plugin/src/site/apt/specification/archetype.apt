 ------
 Archetype
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


What is an archetype?

* A Maven project 'en devenir'

    I.e. an abstract representation of a kind of project
    that can be instantiated into a concrete customized
    Maven project.

    An archetype knows which files will be part of the
    instantiated project and which properties to fill
    to properly customize the project.

    Each archetype defines a set of common properties:

    * <<<groupId>>> which will be the groupId of the project

    * <<<artifactId>>> which will be the artifactId of the project

    * <<<version>>> which will be the version of the project

    * <<<package>>> which will be the base package for all source files of the project

    []



* A JAR file

    The minimal content of the JAR file is:

+---
.
|-- META-INF
|   `-- maven
|       `-- archetype-metadata.xml  [1]
`-- archetype-resources
    |-- ...                         [2]
    `-- pom.xml                     [3]
+---

    [[1]] The metadata file that defines:

        * the default values of the common properties

        * a set of additional properties with their default values

        * a set of archetype resources

        []

    [[2]] The resource files defined by the archetype metadata

    [[3]] The POM file of a Maven project

    []


  <<Note:>> The common properties should be renamed to <<<${target.groupId}>>>, ...
  to avoid name clash.
