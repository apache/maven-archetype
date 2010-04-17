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

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/** @author Jason van Zyl */
public class CatalogArchetypeDataSink
    extends AbstractLogEnabled
    implements ArchetypeDataSink
{
    private ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();

    public void putArchetypes( List archetypes,
                               Writer writer )
        throws ArchetypeDataSinkException
    {
        ArchetypeCatalog catalog = new ArchetypeCatalog();

        for ( Iterator i = archetypes.iterator(); i.hasNext(); )
        {
            Archetype archetype = (Archetype) i.next();

            catalog.addArchetype( archetype );
        }

        try
        {
            catalogWriter.write( writer, catalog );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSinkException( "Error writing archetype catalog.", e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    public void putArchetypes( ArchetypeDataSource source,
                               Properties properties,
                               Writer writer )
        throws ArchetypeDataSourceException, ArchetypeDataSinkException
    {
        List archetypes = source.getArchetypeCatalog( properties ).getArchetypes();

        putArchetypes( archetypes, writer );
    }
}
