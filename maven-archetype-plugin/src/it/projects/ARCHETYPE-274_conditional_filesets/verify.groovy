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

import java.io.File

def mainJava = new File(basedir, "/myArtifactId/src/main/java")
def includeFirstResource = new File(basedir, "/myArtifactId/src/main/resources/include-with-filter.txt")
def includeSecondResource = new File(basedir, "/myArtifactId/src/main/resources/include-without-filter.txt")
def excludeFirstResource = new File(basedir, "/myArtifactId/src/main/resources/exclude-with-filter.txt")
def excludeSecondResource = new File(basedir, "/myArtifactId/src/main/resources/exclude-without-filter.txt")

assert mainJava.exists() : "${mainJava} should be present."
assert includeFirstResource.exists() : "${includeFirstResource} should be present."
assert includeSecondResource.exists() : "${includeSecondResource} should be present."
assert !excludeFirstResource.exists() : "${excludeFirstResource} should not be present."
assert !excludeSecondResource.exists() : "${excludeSecondResource} should not be present."


