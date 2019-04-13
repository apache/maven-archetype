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
assert template.text.contains('Maven archetype Test create-4 ${someProperty}')
assert template.text.contains('<packaging>pom</packaging>')

earTemplate = new File(templateDir,'subModuleEAR/pom.xml')
assert earTemplate.exists()
assert earTemplate.text.contains('${groupId}')
assert earTemplate.text.contains('${artifactId}')
assert earTemplate.text.contains('${version}')
assert earTemplate.text.contains('Maven archetype Test create-4-subModuleEAR')
assert earTemplate.text.contains('<packaging>ear</packaging>')
assert earTemplate.text.contains('<parent>')

warTemplate = new File(templateDir,'subModuleWar/pom.xml')
assert warTemplate.exists()
assert warTemplate.text.contains('${groupId}')
assert warTemplate.text.contains('${artifactId}')
assert warTemplate.text.contains('${version}')
assert warTemplate.text.contains('Maven archetype Test create-4-subModuleWar ${someProperty}')
assert warTemplate.text.contains('<packaging>war</packaging>')
assert warTemplate.text.contains('<parent>')