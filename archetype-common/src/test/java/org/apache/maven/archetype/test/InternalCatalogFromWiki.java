package org.apache.maven.archetype.test;

/*
 *  Copyright 2008 rafale.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.ArchetypeManager;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.apache.maven.archetype.source.WikiArchetypeDataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Generate catalog content from Wiki to replace internal catalog.
 *
 * @author rafale
 */
public class InternalCatalogFromWiki
    extends PlexusTestCase
{
    private static final String CENTRAL = "http://repo1.maven.org/maven2";

    private ArtifactRepository localRepository;

    private ArchetypeArtifactManager aam;

    private ArchetypeRegistryManager arm;

    private ArchetypeManager plexusarchetype;

    private File outputDirectory;

    private ArchetypeCatalog fetchArchetypeCatalogFromWiki()
        throws ArchetypeDataSourceException
    {
        // ArchetypeDataSource ads =  (ArchetypeDataSource) lookup( ArchetypeDataSource.ROLE, "wiki" );
        ArchetypeDataSource ads = new WikiArchetypeDataSource();

        // fetch and parse Wiki page content
        ArchetypeCatalog ac = ads.getArchetypeCatalog( new Properties() );

        for ( Iterator archetypes = ac.getArchetypes().iterator(); archetypes.hasNext(); )
        {
            Archetype archetype = (Archetype) archetypes.next();

            if ( "".equals( archetype.getRepository() )
                || ( archetype.getRepository() != null && archetype.getRepository().indexOf( CENTRAL.substring( 7 ) ) >= 0 ) )
            {
                archetype.setRepository( null );
            }
        }

        System.out.println( "found " + ac.getArchetypes().size() + " archetypes on http://docs.codehaus.org/display/MAVENUSER/Archetypes+List" );

        for ( Iterator archetypes = ac.getArchetypes().iterator(); archetypes.hasNext(); )
        {
            Archetype archetype = (Archetype) archetypes.next();
            System.out.println( "  " + archetype );
        }

        return ac;
    }

    private ArchetypeGenerationResult testArchetype( int count, Archetype archetype )
        throws UnknownArchetype
    {
        System.out.println( "\n\nTesting archetype #" + count + ": " + archetype );

        ArchetypeGenerationRequest request =
            new ArchetypeGenerationRequest( archetype )
            .setGroupId( "groupId" + count )
            .setArtifactId( "artifactId" + count + "-" + archetype.getArtifactId() )
            .setVersion( "version" + count )
            .setPackage( "package" + count )
            .setOutputDirectory( outputDirectory.getPath() )
            .setLocalRepository( localRepository );

        Properties properties = new Properties();

        ArtifactRepository repository =
            arm.createRepository( archetype.getRepository(), archetype.getRepository() + "-repo" );

        if ( aam.isFileSetArchetype( archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion(),
                                     repository, localRepository, new ArrayList( /* repositories */) ) )
        {
            ArchetypeDescriptor descriptor =
                aam.getFileSetArchetypeDescriptor( archetype.getGroupId(), archetype.getArtifactId(),
                                                   archetype.getVersion(), repository, localRepository,
                                                   new ArrayList( /* repositories */) );

            for ( Iterator required = descriptor.getRequiredProperties().iterator(); required.hasNext(); )
            {
                RequiredProperty prop = (RequiredProperty) required.next();

                properties.setProperty( prop.getKey(), prop.getDefaultValue() != null
                    && !"".equals( prop.getDefaultValue() ) ? prop.getDefaultValue() : "test-value" );
            }

        }
        request.setProperties( properties );

        return plexusarchetype.generateProjectFromArchetype( request );
    }

    public void testInternalCatalog()
        throws Exception
    {
        ArchetypeCatalog ac = fetchArchetypeCatalogFromWiki();

        System.out.println( "Testing archetypes to " + outputDirectory.getPath() );

        List validArchetypes = new ArrayList();

        int count = 1;

        List errors = new ArrayList();
        List warnings = new ArrayList();

        for ( Iterator archetypes = ac.getArchetypes().iterator(); archetypes.hasNext(); )
        {
            Archetype a = (Archetype) archetypes.next();
            Archetype ar = new Archetype();

            ar.setGroupId( a.getGroupId() );
            ar.setArtifactId( a.getArtifactId() );
            ar.setVersion( "RELEASE" );
            ar.setDescription( a.getDescription() );
            ar.setGoals( a.getGoals() );
            ar.setProperties( a.getProperties() );
            ar.setRepository( a.getRepository() );

            if ( ar.getRepository() == null )
            {
                ar.setRepository( CENTRAL );
            }

            ArchetypeGenerationResult releaseGenerationResult = testArchetype( count, ar );

            if ( releaseGenerationResult.getCause() != null )
            {
                // RELEASE version failed: try with the version specified in the Wiki
                ar.setVersion( a.getVersion() );

                ArchetypeGenerationResult generationResult = testArchetype( count, ar );

                if ( generationResult.getCause() == null )
                {
                    if ( !( ar.getVersion().indexOf( "SNAPSHOT" ) > 0 )
                        && !( ar.getVersion().indexOf( "snapshot" ) > 0 ) )
                    {
                        validArchetypes.add( ar );

                        warnings.add( "#" + count + ' ' + ar + ": error for RELEASE - " + releaseGenerationResult.getCause().getMessage() );
                    }
                }
                else
                {
                    errors.add( "#" + count + ' ' + ar + ' ' + generationResult.getCause().getMessage() );
                }
            }
            else
            {
                validArchetypes.add( ar );

                if ( !"RELEASE".equals( a.getVersion() ) )
                {
                    warnings.add( "#" + count + ' ' + ar + ": Wiki page mentions " + a.getVersion() +", should be empty since RELEASE is ok." );
                }
            }

            if ( CENTRAL.equals( ar.getRepository() ) )
            {
                ar.setRepository( null );
            }

            count++;

            System.out.println( "\n\n" );
        }

        ArchetypeCatalog fac = new ArchetypeCatalog();
        fac.setArchetypes( validArchetypes );

        File catalog = new File( getBasedir(), "target/archetype-catalog.xml" );
        Writer writer = null;

        try
        {
            writer = WriterFactory.newXmlWriter( catalog );

            ArchetypeCatalogXpp3Writer acxw = new ArchetypeCatalogXpp3Writer();

            acxw.write( writer, fac );
        }
        finally
        {
            IOUtil.close( writer );
        }

        if ( warnings.size() > 0 )
        {
            System.err.println();
            System.err.println( "Warnings: " );
            for ( Iterator iterator = warnings.iterator(); iterator.hasNext(); )
            {
                System.err.println( "  " + iterator.next() );
            }
        }

        System.err.println();
        System.err.println( "Resulting catalog file: " + catalog );

        System.err.println( "This catalog contains " + fac.getArchetypes().size() + " archetypes." );

        if ( ac.getArchetypes().size() > fac.getArchetypes().size() )
        {
            System.err.println();
            System.err.println( "Removed " + ( ac.getArchetypes().size() - fac.getArchetypes().size() )
                + " archetype(s) from Wiki page:" );

            List removedArchetypes = new ArrayList( ac.getArchetypes() );
            removedArchetypes.removeAll( validArchetypes );

            for ( Iterator archetypes = removedArchetypes.iterator(); archetypes.hasNext(); )
            {
                Archetype archetype = (Archetype) archetypes.next();
                System.err.println( "  " + archetype );
            }

            System.err.println();
            System.err.println( "Got " + errors.size() + " error message(s): " );
            for ( Iterator iterator = errors.iterator(); iterator.hasNext(); )
            {
                System.err.println( "  " + iterator.next() );
            }
        }
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        ArchetypeRegistryManager registryManager = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.ROLE );

        File local = new File( getBasedir(), "target/test-classes/repositories/local" );
        localRepository = registryManager.createRepository( local.toURI().toURL().toExternalForm(), "local-repo");

        aam = (ArchetypeArtifactManager) lookup( ArchetypeArtifactManager.class.getName() );
        arm = (ArchetypeRegistryManager) lookup( ArchetypeRegistryManager.class.getName() );

        plexusarchetype =
            (org.apache.maven.archetype.ArchetypeManager) lookup( org.apache.maven.archetype.ArchetypeManager.class.getName() );

        outputDirectory = new File( getBasedir(), "target/internal-archetypes-projects" );
        outputDirectory.mkdirs();

        FileUtils.cleanDirectory( outputDirectory );
    }
}
