 ------
 Archetype Repository
 ------
 Robert Scholte
 ------
 2017-04-01
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

  As of Maven Archetype Plugin 3.0.0 the archetype resolution has changed: it is not possible anymore to specify
  the repository via the command line (with <<<-DarchetypeRepository=repo.url>>>), but instead the repositories as
  already defined for Maven are used.
  This means also that the mirrors and proxies are respected, as well as the authentication on repositories.

  If you're not using a repository manager and the archetype is available at Maven Central, there's nothing you have to do.

  If you're using a repository manager and the archetype is available via this manager, there's nothing you have to do.

  Only in case of a third party archetype which is not managed by a repository you have to add a repository entry
  with <<<archetype>> id to your <<<settings.xml>>> once:

+---+
<settings>
  <mirrors>
    <mirror>
      <id>mrm-maven-plugin</id>
      <name>Mock Repository Manager</name>
      <url>http://www.mycompany.com/maven-repository-manager</url>
      <mirrorOf>*,!archetype</mirrorOf>
    </mirror>
  </mirrors>
  
  <!--
  <servers>
    <server>
      <id>archetype</id>
      <username/>
      <password/>
    </server>
  </servers>
  -->
  
  <profiles>
    <profile>
      <id>acme</id>
      <repositories>
        <repository>
          <id>archetype</id><!-- id expected by maven-archetype-plugin to avoid fetching from everywhere -->
          <url>https://www.acme.com/repo</url>
          <releases>
            <enabled>true</enabled>
            <checksumPolicy>fail</checksumPolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <checksumPolicy>warn</checksumPolicy>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>
</settings>
+---+
