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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.archetype.old.ArchetypeTemplateProcessingException;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.xml.sax.SAXException;

public interface PomManager {
    String ROLE = PomManager.class.getName();

    void addModule(File basedirPom, String artifactId)
            throws IOException, ParserConfigurationException, TransformerException, SAXException, InvalidPackaging,
                    ArchetypeTemplateProcessingException;

    void addParent(File pom, File basedirPom) throws IOException, XmlPullParserException;

    void mergePoms(File pom, File temporaryPom) throws IOException, XmlPullParserException;

    Model readPom(File pomFile) throws IOException, XmlPullParserException;

    Model readPom(InputStream pomStream) throws IOException, XmlPullParserException;

    void writePom(Model model, File pomFile, File initialPomFile) throws IOException;
}
