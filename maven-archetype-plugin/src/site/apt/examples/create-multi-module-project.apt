 ------
 Create an archetype from a multi-module project
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

Create an archetype from a multi-module project

   Creating an archetype for a multi-module project is as simple as 
   {{{../advanced-usage.html}creating one for a single-module project}}.

   Just move to the root directory of a multi-module project and call
   <<<mvn archetype:create-from-project>>>. In the archetype used as an example,
   some files need to be non filtered. This is achieved by giving the archetype
   plugin some values in the <<<archetype.filteredExtensions>>> property.

+--
$ mvn archetype:create-from-project -Darchetype.filteredExtensions=java
[INFO] Scanning for projects...
[INFO] Reactor build order:
[INFO]   archetype
[INFO]   archetype :: business domain model
[INFO]   archetype :: business services
[INFO]   archetype :: persistence layer
[INFO]   archetype :: infrastructure
[INFO]   archetype :: remoting
[INFO]   archetype :: web services
[INFO]   archetype :: web application
[INFO]   archetype :: enterprise application
[INFO] Searching repository for plugin with prefix: 'archetype'.
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype
[INFO]    task-segment: [archetype:create-from-project] (aggregator-style)
[INFO] ------------------------------------------------------------------------
[INFO] Preparing archetype:create-from-project
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: business domain model
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: business services
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: persistence layer
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: infrastructure
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: remoting
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: web services
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: web application
[INFO] ------------------------------------------------------------------------
[INFO] [gwt:eclipse {execution: default}]
[INFO] Unpack native libraries required to run hosted browser
[INFO] [gwt:generateAsync {execution: default}]
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype :: enterprise application
[INFO] ------------------------------------------------------------------------
[INFO] No goals needed for project - skipping
[INFO] Setting property: classpath.resource.loader.class => 'org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader'.
[INFO] Setting property: velocimacro.messages.on => 'false'.
[INFO] Setting property: resource.loader => 'classpath'.
[INFO] Setting property: resource.manager.logwhenfound => 'false'.
[INFO] [archetype:create-from-project]
[INFO] Setting default groupId: com.capgemini.archetype
[INFO] Setting default artifactId: archetype
[INFO] Setting default version: 1.0.0-SNAPSHOT
[INFO] Setting default package: com.capgemini
[INFO] Archetype created in target/generated-sources/archetype
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7 seconds
[INFO] Finished at: Mon Sep 15 19:11:17 CEST 2008
[INFO] Final Memory: 19M/36M
[INFO] ------------------------------------------------------------------------
+--

   Install the archetype as usual.

+--
$ cd target/generated-sources/archetype/
$ mvn install
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Building archetype-archetype
[INFO]    task-segment: [install]
[INFO] ------------------------------------------------------------------------
[INFO] [resources:resources]
[INFO] Using default encoding to copy filtered resources.
[INFO] [resources:testResources]
[INFO] Using default encoding to copy filtered resources.
[INFO] Setting property: classpath.resource.loader.class => 'org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader'.
[INFO] Setting property: velocimacro.messages.on => 'false'.
[INFO] Setting property: resource.loader => 'classpath'.
[INFO] Setting property: resource.manager.logwhenfound => 'false'.
[INFO] [archetype:jar]
[INFO] [archetype:add-archetype-metadata]
[INFO] [archetype:integration-test]
[INFO] [install:install]
[INFO] Installing /Users/raphaelpieroni/Projects/platina/platina-archetype/target/generated-sources/archetype/target/archetype-archetype-1.0.0-SNAPSHOT.jar to /Users/raphaelpieroni/.m2/repository/com/capgemini/archetype/archetype-archetype/1.0.0-SNAPSHOT/archetype-archetype-1.0.0-SNAPSHOT.jar
[INFO] [archetype:update-local-catalog]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4 seconds
[INFO] Finished at: Mon Sep 15 19:11:54 CEST 2008
[INFO] Final Memory: 10M/20M
[INFO] ------------------------------------------------------------------------
+--

    And use it.

+--
$ cd /tmp/archetype/
$ mvn archetype:generate -DarchetypeCatalog=local
[INFO] Scanning for projects...
[INFO] Searching repository for plugin with prefix: 'archetype'.
[INFO] ------------------------------------------------------------------------
[INFO] Building Maven Default Project
[INFO]    task-segment: [archetype:generate] (aggregator-style)
[INFO] ------------------------------------------------------------------------
[INFO] Preparing archetype:generate
[INFO] No goals needed for project - skipping
[INFO] Setting property: classpath.resource.loader.class => 'org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader'.
[INFO] Setting property: velocimacro.messages.on => 'false'.
[INFO] Setting property: resource.loader => 'classpath'.
[INFO] Setting property: resource.manager.logwhenfound => 'false'.
[INFO] [archetype:generate]
[INFO] Generating project in Interactive mode
[INFO] No archetype defined. Using maven-archetype-quickstart (org.apache.maven.archetypes:maven-archetype-quickstart:1.0)
Choose archetype:
1: local -> archetype (archetype)
2: local -> archetype-archetype (archetype-archetype)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): 2
Define value for groupId: : com.company
Define value for artifactId: : project
Define value for version:  1.0-SNAPSHOT: :
Define value for package:  com.company: :
Confirm properties configuration:
groupId: com.company
artifactId: project
version: 1.0-SNAPSHOT
package: com.company
 Y: :
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/pom.xml [line 22,column 9] : ${project.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-business/pom.xml [line 18,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-business/pom.xml [line 30,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 18,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 35,column 20] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 51,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 57,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 97,column 26] : ${project.build.directory} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 98,column 29] : ${project.build.outputDirectory} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 101,column 29] : ${project.build.directory} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-domain/pom.xml [line 128,column 33] : ${project.build.directory} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-persistence/pom.xml [line 18,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-persistence/pom.xml [line 30,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-persistence/pom.xml [line 35,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-persistence/pom.xml [line 40,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-persistence/pom.xml [line 57,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-infrastructure/pom.xml [line 18,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-infrastructure/pom.xml [line 29,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-infrastructure/pom.xml [line 45,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-infrastructure/pom.xml [line 50,column 16] : ${aspectj.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-infrastructure/pom.xml [line 55,column 16] : ${aspectj.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-remoting/pom.xml [line 18,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-remoting/pom.xml [line 31,column 20] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-remoting/pom.xml [line 40,column 20] : ${cxf.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-remoting/pom.xml [line 45,column 20] : ${cxf.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 19,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 27,column 18] : ${project.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 37,column 16] : ${project.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 70,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 104,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 109,column 16] : ${struts.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 114,column 16] : ${struts.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 126,column 16] : ${gwt.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 132,column 16] : ${gwt.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 139,column 16] : ${gwt.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 147,column 16] : ${gwt.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 175,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 181,column 16] : ${webflow.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 213,column 16] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webapp/pom.xml [line 238,column 28] : ${basedir} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webservices/pom.xml [line 18,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webservices/pom.xml [line 30,column 26] : ${cxf.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webservices/pom.xml [line 44,column 20] : ${cxf.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webservices/pom.xml [line 49,column 20] : ${cxf.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webservices/pom.xml [line 54,column 20] : ${cxf.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-webservices/pom.xml [line 60,column 20] : ${spring.version} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-application/pom.xml [line 19,column 9] : ${project.parent.artifactId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-application/pom.xml [line 27,column 24] : ${project.groupId} is not a valid reference.
[WARNING] org.apache.velocity.runtime.exception.ReferenceException: reference : template = archetype-resources/__rootArtifactId__-application/pom.xml [line 39,column 16] : ${project.groupId} is not a valid reference.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 33 seconds
[INFO] Finished at: Mon Sep 15 19:13:54 CEST 2008
[INFO] Final Memory: 8M/15M
[INFO] ------------------------------------------------------------------------
+--

    As one can see, Velocity, the internal processor of archetype files,
    complains about not finding some properties. One should not worry about that. What is
    more interesting, is that the archetype plugin has renamed the original
    project module to "__rootArtifactId__". And during the generation that
    name is converted to the provided artifactId.

    The resulting directory tree of the project is shown here.

+--
$ tree
.
|____project
| |____pom.xml
| |____project-application
| | |____pom.xml
| |____project-business
| | |____pom.xml
| | |____src
| | | |____main
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____services
| | | | | | | | | |____BusinessService.java
| | | | |____resources
| | | | | |____META-INF
| | | | | | |____spring
| | | | | | | |____module-context.xml
| | | |____test
| | | | |____resources
| | | | | |____log4j.xml
| |____project-domain
| | |____pom.xml
| | |____src
| | | |____main
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____domain
| | | | | | | | | |____ConfigurableBean.java
| | | | | | | | | |____User.java
| | | | |____resources
| | | | | |____META-INF
| | | | | | |____orm.xml
| | | |____test
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____domain
| | | | | | | | | |____AbstractConfigurableBeanTest.java
| | | | |____resources
| | | | | |____log4j.xml
| |____project-infrastructure
| | |____pom.xml
| | |____src
| | | |____main
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____infrastructure
| | | | | | | | | |____CompositeConfigurationFactoryBean.java
| | | | | | | | | |____DefaultSecurityPolicy.java
| | | | | | | | | |____Log4jMBean.java
| | | | | | | | | |____SpringBeanConfigurer.java
| | | | | | | | | |____SystemArchitecture.java
| | | | |____resources
| | | | | |____archetype.properties
| | | | | |____META-INF
| | | | | | |____aop.xml
| | | | | | |____spring
| | | | | | | |____module-context.xml
| | | |____test
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____quickstart
| | | | | | | | |____SanityTest.java
| | | | |____resources
| | | | | |____log4j.xml
| |____project-persistence
| | |____pom.xml
| | |____src
| | | |____main
| | | | |____resources
| | | | | |____META-INF
| | | | | | |____persistence.xml
| | | | | | |____spring
| | | | | | | |____module-context.xml
| | | |____test
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____persistence
| | | | | | | | | |____JpaClientRepositoryTest.java
| | | | | | | | | |____MappingsTest.java
| | | | |____resources
| | | | | |____jdbc-tests.xml
| | | | | |____log4j.xml
| | | | | |____unitils.properties
| |____project-remoting
| | |____pom.xml
| | |____src
| | | |____main
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____remoting
| | | | | | | | | |____Connector.java
| | | | |____resources
| | | | | |____META-INF
| | | | | | |____spring
| | | | | | | |____module-context.xml
| | | |____test
| | | | |____resources
| | | | | |____log4j.xml
| |____project-webapp
| | |____com.capgemini.archetype.gwt.Application.launch
| | |____pom.xml
| | |____src
| | | |____main
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____web
| | | | | | | | | |____struts
| | | | | | | | | | |____AbstractPostBackAction.java
| | | | | | | | | | |____GlobalExceptionHandler.java
| | | | | | | | | | |____SafeActionMapping.java
| | | | |____resources
| | | | | |____META-INF
| | | | | | |____log4j.xml
| | | | | | |____spring
| | | | | | | |____module-context.xml
| | | | |____webapp
| | | | | |____error.jsp
| | | | | |____index.html
| | | | | |____notFound.jsp
| | | | | |____WEB-INF
| | | | | | |____jonas-web.xml
| | | | | | |____spring-web-context.xml
| | | | | | |____struts-config.xml
| | | | | | |____web.xml
| | | | | | |____weblogic.xml
| | | |____test
| | | | |____resources
| | | | | |____log4j.xml
| | | | |____tomcat
| | | | | |____context.xml
| |____project-webservices
| | |____pom.xml
| | |____src
| | | |____main
| | | | |____resources
| | | | | |____META-INF
| | | | | | |____spring
| | | | | | | |____module-context.xml
| | | |____test
| | | | |____java
| | | | | |____com
| | | | | | |____company
| | | | | | | |____archetype
| | | | | | | | |____ws
| | | | | | | | | |____AbstractWSTestCase.java
| | | | |____resources
| | | | | |____log4j.xml
+--

    This can be compared to the original tree of the project from which the
    archetype is originating.

+--
$ tree
.
|____archetype-application
| |____pom.xml
|____archetype-business
| |____pom.xml
| |____src
| | |____main
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____services
| | | | | | | | |____BusinessService.java
| | | |____resources
| | | | |____META-INF
| | | | | |____spring
| | | | | | |____module-context.xml
| | |____test
| | | |____java
| | | |____resources
| | | | |____log4j.xml
|____archetype-domain
| |____pom.xml
| |____src
| | |____main
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____domain
| | | | | | | | |____ConfigurableBean.java
| | | | | | | | |____User.java
| | | |____resources
| | | | |____META-INF
| | | | | |____orm.xml
| | |____test
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____domain
| | | | | | | | |____AbstractConfigurableBeanTest.java
| | | |____resources
| | | | |____log4j.xml
|____archetype-infrastructure
| |____pom.xml
| |____src
| | |____main
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____infrastructure
| | | | | | | | |____CompositeConfigurationFactoryBean.java
| | | | | | | | |____DefaultSecurityPolicy.java
| | | | | | | | |____Log4jMBean.java
| | | | | | | | |____SpringBeanConfigurer.java
| | | | | | | | |____SystemArchitecture.java
| | | |____resources
| | | | |____archetype.properties
| | | | |____META-INF
| | | | | |____aop.xml
| | | | | |____spring
| | | | | | |____module-context.xml
| | |____test
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____quickstart
| | | | | | | |____SanityTest.java
| | | |____resources
| | | | |____log4j.xml
|____archetype-persistence
| |____pom.xml
| |____src
| | |____main
| | | |____ddl
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____persistence
| | | |____resources
| | | | |____META-INF
| | | | | |____persistence.xml
| | | | | |____spring
| | | | | | |____module-context.xml
| | |____test
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____persistence
| | | | | | | | |____JpaClientRepositoryTest.java
| | | | | | | | |____MappingsTest.java
| | | |____resources
| | | | |____jdbc-tests.xml
| | | | |____log4j.xml
| | | | |____unitils.properties
|____archetype-remoting
| |____pom.xml
| |____src
| | |____main
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____remoting
| | | | | | | | |____Connector.java
| | | |____resources
| | | | |____META-INF
| | | | | |____spring
| | | | | | |____module-context.xml
| | |____test
| | | |____java
| | | |____resources
| | | | |____log4j.xml
|____archetype-webapp
| |____com.capgemini.archetype.gwt.Application.launch
| |____pom.xml
| |____src
| | |____main
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____web
| | | | | | | | |____struts
| | | | | | | | | |____AbstractPostBackAction.java
| | | | | | | | | |____GlobalExceptionHandler.java
| | | | | | | | | |____SafeActionMapping.java
| | | |____resources
| | | | |____META-INF
| | | | | |____log4j.xml
| | | | | |____spring
| | | | | | |____module-context.xml
| | | |____webapp
| | | | |____error.jsp
| | | | |____images
| | | | |____index.html
| | | | |____notFound.jsp
| | | | |____scripts
| | | | |____styles
| | | | |____WEB-INF
| | | | | |____jonas-web.xml
| | | | | |____spring-web-context.xml
| | | | | |____struts-config.xml
| | | | | |____web.xml
| | | | | |____weblogic.xml
| | |____test
| | | |____java
| | | |____resources
| | | | |____log4j.xml
| | | |____tomcat
| | | | |____context.xml
|____archetype-webservices
| |____pom.xml
| |____src
| | |____main
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____ws
| | | |____resources
| | | | |____META-INF
| | | | | |____spring
| | | | | | |____module-context.xml
| | |____test
| | | |____java
| | | | |____com
| | | | | |____capgemini
| | | | | | |____archetype
| | | | | | | |____ws
| | | | | | | | |____AbstractWSTestCase.java
| | | |____resources
| | | | |____log4j.xml
|____archetype.properties
|____pom.xml
|____src
| |____env
| | |____dev
+--