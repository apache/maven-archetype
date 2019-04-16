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

 //ASSERT symbol_pound replacement (archetype-180 archetype-183)
def content = new File(basedir, 'out/myapp/src/main/java/com/mycompany/myapp/App.java').text
assert content.indexOf( '//A   #\\{some}' ) > 0
assert content.indexOf( '//B   #{some}' ) > 0
assert content.indexOf( '//C   #{some other}' ) > 0
assert content.indexOf( '//D   \\#{some other}' ) > 0
assert content.indexOf( '//E   #{}' ) > 0
assert content.indexOf( '//F   {some}' ) > 0
assert content.indexOf( '//G   ${someOtherProperty}' ) > 0
assert content.indexOf( '//H   ${someValue}' ) > 0
assert content.indexOf( '/*' ) > 0
assert content.indexOf( '  A   #\\{some}' ) > 0
assert content.indexOf( '  B   #{some}' ) > 0
assert content.indexOf( '  C   #{some other}' ) > 0
assert content.indexOf( '  D   \\#{some other}' ) > 0
assert content.indexOf( '  E   #{}' ) > 0
assert content.indexOf( '  F   {some}' ) > 0
assert content.indexOf( '  G   ${someOtherProperty}' ) > 0
assert content.indexOf( '  H   ${someValue}' ) > 0