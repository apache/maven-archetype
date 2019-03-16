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


import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Jason van Zyl
 */
public abstract  class CatalogArchetypeDataSource
    extends AbstractLogEnabled
    implements ArchetypeDataSource
{
    public static final String ARCHETYPE_CATALOG_PROPERTY = "file";

    protected void writeLocalCatalog( ArchetypeCatalog catalog, File catalogFile )
        throws ArchetypeDataSourceException
    {
        try ( Writer writer = WriterFactory.newXmlWriter( catalogFile ) )
        {
            ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();

            catalogWriter.write( writer, catalog );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error writing archetype catalog.", e );
        }
    }

    protected ArchetypeCatalog readCatalog( Reader reader )
        throws ArchetypeDataSourceException
    {
        try ( Reader catReader = reader )
        {
            ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

            return catalogReader.read( catReader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error reading archetype catalog.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeDataSourceException( "Error parsing archetype catalog.", e );
        }
    }
}