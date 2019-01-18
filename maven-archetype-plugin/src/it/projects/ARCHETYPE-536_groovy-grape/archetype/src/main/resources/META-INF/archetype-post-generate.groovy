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

def rootDir = new File(request.getOutputDirectory() + "/" + request.getArtifactId())
def textFile = new File(rootDir, "test.txt")

// use commons-code as example to include an external library
@Grab(group='commons-codec', module='commons-codec', version='1.11')
def test = "Test Text"
def textHexString = org.apache.commons.codec.binary.Hex.encodeHexString(test.getBytes("UTF-8"))

textFile.newWriter("UTF-8").withWriter { w ->
  w << textHexString
}
