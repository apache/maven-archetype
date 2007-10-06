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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

/** @plexus.component */
public class DefaultArchetypePropertiesManager
    extends AbstractLogEnabled
    implements ArchetypePropertiesManager
{
    public void readProperties( Properties properties,
                                File propertyFile )
        throws
        IOException
    {
        getLogger().debug( "Reading property file " + propertyFile );

        InputStream is = new FileInputStream( propertyFile );

        try
        {
            properties.load( is );

            getLogger().debug( "Read " + properties.size() + " properties" );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    public void writeProperties( Properties properties,
                                 File propertyFile )
        throws
        IOException
    {
        Properties storedProperties = new Properties();
        try
        {
            readProperties( storedProperties, propertyFile );
        }
        catch ( FileNotFoundException ex )
        {
            getLogger().debug( "Property file not found. Creating a new one" );
        }

        getLogger().debug( "Adding " + properties.size() + " properties" );

        Iterator propertiesIterator = properties.keySet().iterator();
        while ( propertiesIterator.hasNext() )
        {
            String propertyKey = (String) propertiesIterator.next();
            storedProperties.setProperty( propertyKey, properties.getProperty( propertyKey ) );
        }

        OutputStream os = new FileOutputStream( propertyFile );

        try
        {
            storedProperties.store( os, "" );

            getLogger().debug( "Stored " + storedProperties.size() + " properties" );
        }
        finally
        {
            IOUtil.close( os );
        }
    }
}
