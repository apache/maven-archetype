package org.apache.maven.archetype.scripting;

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
 
import java.io.Reader;

import java.util.Properties;
 
/**
 * @author Lucien Weller
 */
public class ArchetypePropertyScripterRequest {
  private Properties properties;
  private String     scriptFileName;
  private Reader     scriptFileReader;
  
  public Properties getProperties() {
    return properties;
  }

  public ArchetypePropertyScripterRequest setProperties(Properties properties) {
    this.properties = properties;
    
    return this;
  }

  public String getScriptFileName() {
    return scriptFileName;
  }

  public ArchetypePropertyScripterRequest setScriptFileName(String scriptFileName) {
    this.scriptFileName = scriptFileName;
    
    return this;
  }

  public Reader getScriptFileReader() {
    return scriptFileReader;
  }

  public ArchetypePropertyScripterRequest setScriptFileReader(Reader scriptFileReader) {
    this.scriptFileReader = scriptFileReader;
    
    return this;
  }
}
