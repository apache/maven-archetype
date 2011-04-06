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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.codehaus.groovy.control.CompilationFailedException;

import java.io.IOException;
import java.io.Reader;
import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Lucien Weller
 * @plexus.component
 */
public class GroovyArchetypePropertiesScripter implements ArchetypePropertiesScripter {
  public ArchetypePropertyScripterResult executeScript(ArchetypePropertyScripterRequest request) {
    Binding binding = new Binding();

    List<String> errors = new ArrayList<String>();
    binding.setVariable("errors", errors);

    Properties properties = new Properties(request.getProperties());
    binding.setVariable("properties", properties);

    GroovyShell shell = new GroovyShell(binding);

    try {
      if(request.getScriptFileName() == null) {
        shell.evaluate(request.getScriptFileReader());
      } else {
        String fileName = new File(request.getScriptFileName()).getName().replaceAll("\\.groovy$", "").replaceAll("[^A-Za-z0-9]", "_");
        shell.evaluate(request.getScriptFileReader(), fileName);
      }
    } catch (CompilationFailedException e) {
      throw new RuntimeException("Script Error:" + e.getMessage());
    }

    return new ArchetypePropertyScripterResult(properties, errors);
  }
}
