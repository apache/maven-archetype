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

def myapp = new File(basedir, 'out/myapp')
assert new File(myapp,'myapp-api/.classpath').exists()
assert new File(myapp,'myapp-cli/.classpath').exists()
assert new File(myapp,'myapp-core/.classpath').exists()
assert new File(myapp,'myapp-model/.classpath').exists()
assert new File(myapp,'myapp-stores/myapp-store-memory/.classpath').exists()
assert new File(myapp,'myapp-stores/myapp-store-xstream/.classpath').exists()

assert new File(myapp,'myapp-api/.checkstyle').exists()
assert new File(myapp,'myapp-cli/.checkstyle').exists()
assert new File(myapp,'myapp-core/.checkstyle').exists()
assert new File(myapp,'myapp-model/.checkstyle').exists()
assert new File(myapp,'myapp-stores/myapp-store-memory/.checkstyle').exists()
assert new File(myapp,'myapp-stores/myapp-store-xstream/.checkstyle').exists()