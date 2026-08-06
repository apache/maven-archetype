 ------
 Update local and remote Catalog
 ------
 Raphaël Piéroni
 ------
 10 February 2008
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


How the Archetype Plugin know about new archetypes?

~~TODO: add content
+---
Creation/Deployment behaviour
    5. archetype developer using local version 

 

 archetype created using archetpe:create-from-project -Darchetype.phase=install 

 the newly created archetype is copied in the local repository 

 and the archetype-catalog.xml file located at the root of the repository is updated with the archetype information 

 

 when using in archetype:create -Darchetype.catalog=local 

 the archetype must be specified using -Darchetype.groupId, -Darchetype.artifactId 

 only the local repository is used and the LATEST version in it is used 

 or selected with -Darchetype.selectVersion 

 or specified with -Darchetype.version
        configuration behaviour 

 returns an incomplete creation request
            determine archetype's groupId, artifactId, version 

 -> defaults to resolve, overridden by property file, overridden by -D 

 resolve package using (-Dlanguages languages can be found in property file instead) 

 -> defaults to resolve, overridden by property file, overridden by -D 

 determine additional properties 

 -> ask if interactive, overridden by property file only
        creation behaviour 

 post creation goals (package/install/deploy)
            determine filesets using: 

 -> package, languages, filtereds, multi-module 

 determine pom rewriting using: 

 -> keepParent, additional properties 

 create archetype files using: 

 -> package, common properties, additional properties 

 create archetype's metadata file 

 create archetype's pom using 

 -> archetype's id, copying devs, license, ... AND deploymentManagement
    6. archetype developer deployment the archetype 

 

 archetype create using archetype:create-from-project -Darchetype.phase=deploy 

 

 the deployment url used is taken from the initial project 

 credentials are automatically configured by Maven 

 the catalog file is located at the root of the repository URL and is named archetype-catalog.xml 

 

 the deployment url and credentials could be overridden to use staging repositories, but how? 

 

 proxies are automatically configured by Maven 

 

 synchronization between repositories of catalog files is easy as they don't define any repository URL
        deployment credentials:
            initialPom.deploymentManagement 

 -> remote URL or remote snapshot URL 

 settings.servers
        deployment artifact:
            initialPom.groupId or archetype.groupId in property file or in -D 

 initialPom.artifactId +'-archetype' or archetype.artifactId in property file or in -D 

 initialPom.version or archetype.version in property file or in -D
        deployment metadata:
            deploy versions metadata as any artifact 

 use deploymentManagement to update remote catalog
+---