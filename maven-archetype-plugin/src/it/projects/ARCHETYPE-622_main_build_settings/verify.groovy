
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

import java.io.*
import org.codehaus.plexus.util.*

def basedirTargetProject = new File(basedir, "target/test-classes/projects/basic/project/basic")
assert basedirTargetProject.exists() : "${basedirTargetProject} is missing."

def main = new File(basedirTargetProject, "src/main")
def app = new File(main, "java/build/archetype/App.java")
// check <fileset packaged="true">
assert app.isFile() : "${app} file is missing or not a file."

def buildLog = new File(basedirTargetProject, "build.log").text

assert buildLog.contains("Archetype tests executed!") :
 "build.log missing System.out.println from verify.groovy"
// we expect the archetype:integration-test to use the settings.xml from the main Maven build - so downloading should happen
// also from local.central specified in the test-settings.xml
assert buildLog.contains("local.central (file://") :
 "test-settings.xml wasn't passed from the main Maven build!: 'local.central (file://' was NOT found in the output! The output was:\n${content}"

def settingsXmlPath = new File("maven-archetype-plugin/target/it/projects/ARCHETYPE-622_main_build_settings/target/archetype-it", "archetype-settings.xml").toPath().toString().replace("\\", "\\\\")
assert buildLog.matches("(?s).*\\[DEBUG\\] Reading user settings from .*" + settingsXmlPath + ".*") : "test-settings.xml wasn't passed from the main Maven build!: 'Reading user settings from ... archetype-settings.xml' was NOT found in the output! The output was: ${buildLog}"


def mainBuildLog = new File( basedir, "../../logs/ARCHETYPE-622_main_build_settings/build.log" ).text
assert mainBuildLog.contains('[INFO] Invoking post-archetype-generation goals: compile') : 'post-archetype-generation invocation not recorder in log'
assert !mainBuildLog.contains('[INFO] [INFO]') : 'output of post-archetype-generation should not be present in log'