package org.apache.maven.archetype;

import org.apache.maven.archetype.creator.ArchetypeCreator;
import org.apache.maven.archetype.generator.ArchetypeGenerator;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultArchetyper
    implements Archetyper
{
    /** @plexus.requirement role-hint="fileset" */
    private ArchetypeCreator creator;

    /** @plexus.requirement */
    private ArchetypeGenerator generator;

    /** @plexus.requirement role="org.apache.maven.archetype.source.ArchetypeDataSource" */
    private Map archetypeSources;

    public ArchetypeCreationResult createArchetypeFromProject( ArchetypeCreationRequest request )
    {
        ArchetypeCreationResult result = new ArchetypeCreationResult();

        creator.createArchetype( request, result );

        return result;
    }

    public ArchetypeGenerationResult generateProjectFromArchetype( ArchetypeGenerationRequest request )
    {
        ArchetypeGenerationResult result = new ArchetypeGenerationResult();

        generator.generateArchetype( request, result );

        return result;
    }

    public Collection getArchetypes( ArchetypeDataSource source,
                                     Properties sourceConfiguration )
        throws ArchetypeDataSourceException
    {
        return source.getArchetypes( sourceConfiguration );
    }

    public Collection getArchetypeDataSources()
    {
        return archetypeSources.values();
    }

    public Collection getAvailableArchetypes()
    {
        File archetypeCatalogPropertiesFile = new File( System.getProperty( "user.home" ), ".m2/archetype-catalog.properties" );

        return getAvailableArchetypes( archetypeCatalogPropertiesFile );
    }

    public Collection getAvailableArchetypes( File archetypeCatalogPropertiesFile )
    {
        List archetypes = new ArrayList();

        if ( archetypeCatalogPropertiesFile.exists() )
        {
            Properties archetypeCatalogProperties = PropertyUtils.loadProperties( archetypeCatalogPropertiesFile );

            String[] sources = StringUtils.split( archetypeCatalogProperties.getProperty( "sources" ), "," );

            for ( int i = 0; i < sources.length; i++ )
            {
                String sourceRoleHint = sources[i];

                try
                {
                    ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( sourceRoleHint );

                    archetypes.addAll(
                        source.getArchetypes( getArchetypeDataSourceProperties( sourceRoleHint, archetypeCatalogProperties ) ) );
                }
                catch ( ArchetypeDataSourceException e )
                {
                    // do nothing, gracefully move on
                }
            }
        }

        // If we haven't found any Archetypes then we will currently attempt to use the Wiki source.
        // Eventually we will use a more reliable remote catalog from the central repository.

        if ( archetypes.size() == 0 )
        {
            try
            {
                ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( "wiki" );

                archetypes.addAll( source.getArchetypes( new Properties() ) );
            }
            catch ( ArchetypeDataSourceException e )
            {
                // do nothing, gracefully move on
            }
        }

        return archetypes;
    }

    public Properties getArchetypeDataSourceProperties( String sourceRoleHint,
                                                        Properties archetypeCatalogProperties )
    {
        Properties p = new Properties();

        for ( Iterator i = archetypeCatalogProperties.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();

            if ( key.startsWith( sourceRoleHint ) )
            {
                String k = key.substring( sourceRoleHint.length() + 1 );

                p.setProperty( k, archetypeCatalogProperties.getProperty( key ) );
            }
        }

        return p;
    }

}
