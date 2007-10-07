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

package org.apache.maven.archetype.source;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.settings.Settings;

import java.util.List;
import java.util.Properties;

/**
 * Sources we can get Archetypes from. This may be the local registry, a Wiki, or,
 * a Maven Repository application. We might also want to get all the Archetypes based
 * on some predetermined criteria and that could be anything given the source. A simple
 * use-case might be to grab all Archetypes for a particular groupId, or Archetypes for
 * webapps, or who knows what.
 *
 * @author Jason van Zyl
 */
public interface ArchetypeDataSource
{
    String ROLE = ArchetypeDataSource.class.getName();

    List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException;

    void updateCatalog( Properties properties, Archetype archetype, Settings settings )
        throws ArchetypeDataSourceException;

    ArchetypeDataSourceDescriptor getDescriptor();
}