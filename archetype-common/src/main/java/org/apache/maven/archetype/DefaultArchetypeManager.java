package org.apache.maven.archetype;

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
import org.apache.maven.archetype.creator.ArchetypeCreator;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Jason van Zyl
 */
@Component( role = ArchetypeManager.class )
public class DefaultArchetypeManager
    extends AbstractLogEnabled
    implements ArchetypeManager
{
    @Requirement( hint = "fileset" )
    private ArchetypeCreator creator;

    @Requirement
    private ArchetypeGenerator generator;

    @Requirement( role = ArchetypeDataSource.class )
    private Map<String, ArchetypeDataSource> archetypeSources;

    @Override
    public ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request )
    {
        ArchetypeCreationResult result = new ArchetypeCreationResult();

        creator.createArchetype( request, result );

        return result;
    }

    @Override
    public ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request )
    {
        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        generator.generateArchetype( request, result );

        return result;
    }

    @Override
    public File archiveArchetype( File archetypeDirectory, File outputDirectory, String finalName )
        throws DependencyResolutionRequiredException, IOException
    {
        File jarFile = new File( outputDirectory, finalName + ".jar" );

        zip( archetypeDirectory, jarFile );

        return jarFile;
    }

    public void zip( File sourceDirectory, File archive )
        throws IOException
    {
        if ( !archive.getParentFile().exists() )
        {
            archive.getParentFile().mkdirs();
        }


        if ( !archive.exists() && !archive.createNewFile() )
        {
            getLogger().warn( "Could not create new file \"" + archive.getPath() + "\" or the file already exists." );
        }

        try ( ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( archive ) ) )
        {
            zos.setLevel( 9 );

            zipper( zos, sourceDirectory.getAbsolutePath().length(), sourceDirectory );
        }
    }

    private void zipper( ZipOutputStream zos, int offset, File currentSourceDirectory )
        throws IOException
    {
        File[] files = currentSourceDirectory.listFiles();

        if ( files.length == 0 )
        {
            // add an empty directory
            String dirName = currentSourceDirectory.getAbsolutePath().substring( offset + 1 );

            if ( File.separatorChar != '/' )
            {
                dirName = dirName.replace( '\\', '/' );
            }

            zos.putNextEntry( new ZipEntry( dirName + '/' ) );
        }

        for ( int i = 0; i < files.length; i++ )
        {
            if ( files[i].isDirectory() )
            {
                zipper( zos, offset, files[i] );
            }
            else
            {
                String fileName = files[i].getAbsolutePath().substring( offset + 1 );

                if ( File.separatorChar != '/' )
                {
                    fileName = fileName.replace( '\\', '/' );
                }

                ZipEntry e = new ZipEntry( fileName );

                zos.putNextEntry( e );

                try ( FileInputStream is = new FileInputStream( files[i] ) )
                {
                    IOUtil.copy( is, zos );
                }

                zos.closeEntry();
            }
        }
    }

    @Override
    public ArchetypeCatalog getInternalCatalog()
    {
        try
        {
            ArchetypeDataSource source = archetypeSources.get( "internal-catalog" );

            return source.getArchetypeCatalog( null );
        }
        catch ( ArchetypeDataSourceException e )
        {
            return new ArchetypeCatalog();
        }
    }

    @Override
    public ArchetypeCatalog getLocalCatalog( ProjectBuildingRequest buildingRequest )
    {
        try
        {
            ArchetypeDataSource source = archetypeSources.get( "catalog" );

            return source.getArchetypeCatalog( buildingRequest );
        }
        catch ( ArchetypeDataSourceException e )
        {
            return new ArchetypeCatalog();
        }
    }

    @Override
    public ArchetypeCatalog getRemoteCatalog( ProjectBuildingRequest buildingRequest )
    {
        try
        {
            ArchetypeDataSource source = archetypeSources.get( "remote-catalog" );

            return source.getArchetypeCatalog( buildingRequest );
        }
        catch ( ArchetypeDataSourceException e )
        {
            return new ArchetypeCatalog();
        }
    }

    @Override
    public void updateLocalCatalog( ProjectBuildingRequest buildingRequest, Archetype archetype )
    {
        try
        {
            ArchetypeDataSource source = archetypeSources.get( "catalog" );

            source.updateCatalog( buildingRequest, archetype );
        }
        catch ( ArchetypeDataSourceException e )
        {
        }
    }
}