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

package org.apache.maven.archetype.common;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class TestPomManager {

  //ref: https://www.baeldung.com/java-pretty-print-xml
  //https://bugs.openjdk.java.net/browse/JDK-8262285?attachmentViewMode=list
  @Test
  public void testAddModule() throws Exception {
    PomManager pomManager = new DefaultPomManager();

    URL pom = getClass().getResource("/projects/generate-9/pom.xml.sample");
    File pomFileSrc = new File(pom.toURI());
    File pomFile = new File(pomFileSrc.getAbsolutePath() + "-copied.xml");
    FileUtils.copyFile(pomFileSrc, pomFile);
    final int moduleNumber = 4;
    for (int i = 0; i < moduleNumber; i++ ) {
      pomManager.addModule(pomFile, "test" + i);
    }
    String fileText = FileUtils.readFileToString( pomFile, "UTF-8" );
    Pattern pattern = Pattern.compile("(^[ ]+[\\r\\n]+){"+moduleNumber + "}", Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(fileText);
    Assert.assertFalse(matcher.find());
  }

}
