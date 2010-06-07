package org.apache.maven.archetype.source;

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

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * @author Jason van Zyl
 * @plexus.component role-hint="internal-catalog"
 */
public class InternalCatalogArchetypeDataSource
    extends CatalogArchetypeDataSource
{

    public ArchetypeCatalog getArchetypeCatalog( Properties properties )
        throws ArchetypeDataSourceException
    {
        try
        {
            InputStream in = getClass().getClassLoader().getResourceAsStream( ARCHETYPE_CATALOG_FILENAME );
            Reader reader = ReaderFactory.newXmlReader( in );

            return readCatalog( reader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error reading archetype catalog.", e );
        }
    }
}