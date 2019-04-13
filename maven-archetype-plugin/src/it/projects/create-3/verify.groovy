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

def templateDir = new File(basedir,'target/generated-sources/archetype/src/main/resources/archetype-resources')

template = new File(templateDir,'pom.xml')
assert template.exists()
assert template.text.contains('${groupId}')
assert template.text.contains('${artifactId}')
assert template.text.contains('${version}')
assert template.text.contains('Maven archetype Test create-3')
assert template.text.contains('<packaging>pom</packaging>')
assert !template.text.contains('<parent>')

template = new File(templateDir,'src/site/site.xml')
assert template.exists()
assert template.text.contains('<!-- ${packageInPathFormat}/test')
assert template.text.contains('${someProperty} -->')

template = new File(templateDir,'src/site/resources/site.png')
assert template.exists()
assert !template.text.contains('${someProperty}')

template = new File(templateDir,'libs/pom.xml')
assert template.exists()
assert template.text.contains('${groupId}')
assert template.text.contains('${artifactId}')
assert template.text.contains('${version}')
assert template.text.contains('Maven archetype Test create-3-libraries')
assert template.text.contains('<packaging>pom</packaging>')
assert template.text.contains('<parent>')

template = new File(templateDir,'libs/prj-a/pom.xml')
assert template.exists()
assert template.text.contains('${groupId}')
assert template.text.contains('${artifactId}')
assert template.text.contains('${version}')
assert template.text.contains('Maven archetype Test create-3-libraries-project-a')
assert !template.text.contains('<packaging>pom</packaging>')
assert template.text.contains('<parent>')

template = new File(templateDir,'libs/prj-a/src/main/mdo/descriptor.xml')
assert template.exists()
assert template.text.contains('<!-- ${packageInPathFormat}/test')
assert template.text.contains('${someProperty} -->')

template = new File(templateDir,'libs/prj-b/pom.xml')
assert template.exists()
assert template.text.contains('${groupId}')
assert template.text.contains('${artifactId}')
assert template.text.contains('${version}')
assert template.text.contains('Maven archetype Test create-3-libraries-project-b')
assert !template.text.contains('<packaging>pom</packaging>')
assert template.text.contains('<parent>')

template = new File(templateDir,'libs/prj-b/src/main/java/test/com/Component.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}')

template = new File(templateDir,'libs/prj-b/src/main/java/test/com/package.html')
assert template.exists()
assert template.text.contains('<!-- ${packageInPathFormat}/test')
assert template.text.contains('${someProperty} -->')

template = new File(templateDir,'libs/prj-b/src/test/java/test/common/ComponentTest.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}')

template = new File(templateDir,'application/pom.xml')
assert template.exists()
assert template.text.contains('${groupId}')
assert template.text.contains('${artifactId}')
assert template.text.contains('${version}')
assert template.text.contains('Maven archetype Test create-3-application')
assert !template.text.contains('<packaging>pom</packaging>')
assert template.text.contains('<parent>')

template = new File(templateDir,'application/src/main/java/Main.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert !template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}/test')

template = new File(templateDir,'application/src/main/java/test/application/Application.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}')

template = new File(templateDir,'application/src/main/java/test/application/audios/Application.ogg')
assert template.exists()
assert !template.text.contains('${someProperty}')

template = new File(templateDir,'application/src/main/java/test/application/images/Application.png')
assert template.exists()
assert !template.text.contains('${someProperty}')

template = new File(templateDir,'application/src/main/resources/log4j.properties')
assert template.exists()
assert template.text.contains('${someProperty}')
assert !template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}/test')

template = new File(templateDir,'application/src/main/resources/META-INF/MANIFEST.MF')
assert template.exists()
assert template.text.contains('${someProperty}')
assert !template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}/test')

template = new File(templateDir,'application/src/main/resources/test/application/some/Gro.groovy')
assert template.exists()
assert template.text.contains('${someProperty}')
assert !template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}/test')

template = new File(templateDir,'application/src/main/resources/splash.png')
assert template.exists()
assert !template.text.contains('${someProperty}')

template = new File(templateDir,'application/src/test/java/TestAll.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert !template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}/test')

template = new File(templateDir,'application/src/test/java/test/application/ApplicationTest.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert template.text.contains('package ${package}.test.application;')
assert template.text.contains('${packageInPathFormat}/test/application')

template = new File(templateDir,'application/src/it-test/java/test/ItTest1.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert template.text.contains('package ${package}.test;')
assert template.text.contains('${packageInPathFormat}/test')

template = new File(templateDir,'application/src/it-test/java/ItTestAll.java')
assert template.exists()
assert template.text.contains('${someProperty}')
assert !template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}/test')

template = new File(templateDir,'application/src/it-test/resources/ItTest1Result.txt')
assert template.exists()
assert template.text.contains('${someProperty}')
assert !template.text.contains('${package}')
assert template.text.contains('${packageInPathFormat}/test')