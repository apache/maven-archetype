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

def templateDir = new File(basedir, 'target/generated-sources/archetype/src/main/resources/archetype-resources')

def root = new File(templateDir, 'pom.xml')
assert root.exists()
assert root.text.contains('<artifactId>${artifactId}</artifactId>')
assert root.text.contains('Maven archetype Test create-2')
assert root.text.contains('<packaging>pom</packaging>')

def subModule = new File(templateDir, 'subModule/pom.xml')
assert subModule.exists()
assert subModule.text.contains('<artifactId>${rootArtifactId}</artifactId>')
assert subModule.text.contains('<parent>')
assert subModule.text.contains('Maven archetype Test create-2-subModule')

// the non-standard source directory src/it-test is part of the archetype
def itApp = new File(templateDir, 'src/it-test/java/subfolder1/App.java')
assert itApp.exists()
assert itApp.text.contains('// ${someProperty}')

def descriptor = new File(basedir, 'target/generated-sources/archetype/src/main/resources/META-INF/maven/archetype-metadata.xml')
assert descriptor.text.contains('<module id="${rootArtifactId}-subModule" dir="subModule"')
assert descriptor.text.contains('<directory>src/it-test/java</directory>')
