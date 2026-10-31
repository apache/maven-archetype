
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

def archetype = new File(basedir, "target/generated-sources/archetype/src/main/resources/archetype-resources/")

// ARCHETYPE-513
for (exclude in ["/", "src/", "src/main/", "src/main/resources/"]) {
    def excluded = new File(archetype, exclude + "toexclude")
    assert !excluded.exists() : "${excluded} folder exists when it should have been excluded."
}

for (include in ["file.txt", "file.xml"]) {
    def included = new File(archetype, "src/main/resources/" + include)
    assert included.isFile() : "${included} file does not exist when it should have been included."
}
