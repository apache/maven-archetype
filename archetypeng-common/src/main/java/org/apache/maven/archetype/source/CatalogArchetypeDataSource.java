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
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author Jason van Zyl
 * @plexus.component role-hint="catalog"
 */
public class CatalogArchetypeDataSource
    extends AbstractLogEnabled
    implements ArchetypeDataSource
{
    public static final String ARCHETYPE_CATALOG_PROPERTY = "file";

    public static final String ARCHETYPE_CATALOG_FILENAME = "archetype-catalog.xml";

    private ArchetypeCatalogXpp3Reader catalogReader = new ArchetypeCatalogXpp3Reader();

    private ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer();

    public static final File USER_HOME = new File( System.getProperty( "user.home" ) );

    public static final File MAVEN_CONFIGURATION = new File( USER_HOME, ".m2" );

    public static final File DEFAULT_ARCHETYPE_CATALOG = new File( MAVEN_CONFIGURATION, ARCHETYPE_CATALOG_FILENAME );

    public List getArchetypes( Properties properties )
        throws ArchetypeDataSourceException
    {
        String s = properties.getProperty( ARCHETYPE_CATALOG_PROPERTY );

        s = StringUtils.replace( s, "${user.home}", System.getProperty( "user.home" ) );

        getLogger().debug( "Using catalog " + s );

        File catalogFile = new File( s );

        if ( catalogFile.exists() )
        {

            try
            {
                ArchetypeCatalog catalog = readCatalog( new FileReader( catalogFile ) );

                return createArchetypeMap( catalog );
            }
            catch ( FileNotFoundException e )
            {
                throw new ArchetypeDataSourceException( "The specific archetype catalog does not exist.", e );
            }
        }
        else
        {
            return new ArrayList();
        }
    }

    public void updateCatalog( Properties properties,
                               Archetype archetype,
                               Settings settings )
        throws ArchetypeDataSourceException
    {
        String s = properties.getProperty( ARCHETYPE_CATALOG_PROPERTY );

        s = StringUtils.replace( s, "${user.home}", System.getProperty( "user.home" ) );

        getLogger().debug( "Using catalog " + s );

        File catalogFile = new File( s );

        ArchetypeCatalog catalog;
        if ( catalogFile.exists() )
        {
            try
            {
                getLogger().debug( "Reading the catalog " + catalogFile );
                catalog = readCatalog( new FileReader( catalogFile ) );
            }
            catch ( FileNotFoundException ex )
            {
                getLogger().debug( "Catalog file don't exist" );
                catalog = new ArchetypeCatalog();
            }
        }
        else
        {
            getLogger().debug( "Catalog file don't exist" );
            catalog = new ArchetypeCatalog();
        }

        Iterator archetypes = catalog.getArchetypes().iterator();
        boolean found = false;
        Archetype newArchetype = archetype;
        while ( !found && archetypes.hasNext() )
        {
            Archetype a = (Archetype) archetypes.next();
            if ( a.getGroupId().equals( archetype.getGroupId() ) && a.getArtifactId().
                equals( archetype.getArtifactId() ) )
            {
                newArchetype = a;
                found = true;
            }
        }
        if ( !found )
        {
            catalog.addArchetype( newArchetype );
        }

        newArchetype.setVersion( archetype.getVersion() );
        newArchetype.setRepository( archetype.getRepository() );
        newArchetype.setDescription( archetype.getDescription() );
        newArchetype.setProperties( archetype.getProperties() );
        newArchetype.setGoals( archetype.getGoals() );

        writeLocalCatalog( catalog, catalogFile );
    }

    protected void writeLocalCatalog( ArchetypeCatalog catalog,
                                      File catalogFile )
        throws ArchetypeDataSourceException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter( catalogFile );
            catalogWriter.write( writer, catalog );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error writing archetype catalog.", e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    protected List createArchetypeMap( ArchetypeCatalog archetypeCatalog )
        throws ArchetypeDataSourceException
    {
        List archetypes = new ArrayList();

        for ( Iterator i = archetypeCatalog.getArchetypes().iterator(); i.hasNext(); )
        {
            Archetype archetype = (Archetype) i.next();

            archetypes.add( archetype );
        }

        return archetypes;
    }

    protected ArchetypeCatalog readCatalog( Reader reader )
        throws ArchetypeDataSourceException
    {
        try
        {
            return catalogReader.read( reader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException( "Error reading archetype catalog.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeDataSourceException( "Error parsing archetype catalog.", e );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    public ArchetypeCatalog getArchetypeCatalog( Properties properties )
        throws ArchetypeDataSourceException
    {
        String s = properties.getProperty( ARCHETYPE_CATALOG_PROPERTY );

        s = StringUtils.replace( s, "${user.home}", System.getProperty( "user.home" ) );

        File catalogFile = new File( s );
        if ( catalogFile.exists() && catalogFile.isDirectory() )
        {
            catalogFile = new File( catalogFile, ARCHETYPE_CATALOG_FILENAME );
        }
        getLogger().debug( "Using catalog " + catalogFile );

        if ( catalogFile.exists() )
        {

            try
            {
                return readCatalog( new FileReader( catalogFile ) );

            }
            catch ( FileNotFoundException e )
            {
                throw new ArchetypeDataSourceException( "The specific archetype catalog does not exist.",
                    e );
            }
        }
        else
        {
            return new ArchetypeCatalog();
        }
    }

//    public ArchetypeDataSourceDescriptor getDescriptor()
//    {
//        ArchetypeDataSourceDescriptor d = new ArchetypeDataSourceDescriptor();
//
//        d.addParameter( ARCHETYPE_CATALOG_PROPERTY, String.class, DEFAULT_ARCHETYPE_CATALOG.getAbsolutePath(),
//            "The repository URL where the archetype catalog resides." );
//
//        return d;
//    }
}