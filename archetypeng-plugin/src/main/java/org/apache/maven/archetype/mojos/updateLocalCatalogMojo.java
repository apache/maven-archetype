package org.apache.maven.archetype.mojos;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;


/**
 * Updates the local catalog
 *
 * @phase install
 * @goal update-local-catalog
 *
 * @author rafale
 */
public class updateLocalCatalogMojo
    extends AbstractMojo
    implements ContextEnabled
{
    /** @plexus.requirement role="org.apache.maven.archetype.source.ArchetypeDataSource" */
    private Map archetypeSources;

    /**
     * The project artifact, which should have the LATEST metadata added to it.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The project artifact, which should have the LATEST metadata added to it.
     *
     * @parameter expression="${settings.localRepository}"
     * @required
     * @readonly
     */
    private File localRepository;

    public void execute( )
        throws MojoExecutionException
    {
        try
        {
            Archetype archetype = new Archetype(  );
            archetype.setGroupId( project.getGroupId(  ) );
            archetype.setArtifactId( project.getArtifactId(  ) );
            archetype.setVersion( project.getVersion(  ) );
            archetype.setDescription( project.getName(  ) );
            archetype.setRepository( localRepository.toString(  ) );
//            archetype.setGoals(project.get);
//            archetype.setProperties(project.get);
            File archetypeCatalogPropertiesFile = new File( System.getProperty( "user.home" ), ".m2/archetype-catalog.properties" );

            if ( archetypeCatalogPropertiesFile.exists(  ) )
            {
                Properties archetypeCatalogProperties = PropertyUtils.loadProperties( archetypeCatalogPropertiesFile );

                getLog(  ).debug( "Updating catalogs " + archetypeCatalogProperties );

                String[] sources = StringUtils.split( archetypeCatalogProperties.getProperty( "sources" ), "," );

                for ( int i = 0; i < sources.length; i++ )
                {
                    String sourceRoleHint = sources[i];

                    getLog(  ).debug( "Updating catalog " + sourceRoleHint );

                    ArchetypeDataSource source = (ArchetypeDataSource) archetypeSources.get( sourceRoleHint );

                    source.updateCatalog( getArchetypeSourceProperties( sourceRoleHint, archetypeCatalogProperties ), archetype );
                }
            }
            else
            {
                getLog(  ).debug( "Not updating wiki catalog" );
            }
        }
        catch ( ArchetypeDataSourceException ex )
        {
            throw new MojoExecutionException( ex.getMessage(  ), ex );
        }
    }

    private Properties getArchetypeSourceProperties( String sourceRoleHint, Properties archetypeCatalogProperties )
    {
        Properties p = new Properties(  );

        for ( Iterator i = archetypeCatalogProperties.keySet(  ).iterator(  ); i.hasNext(  ); )
        {
            String key = (String) i.next();

            if ( key.startsWith( sourceRoleHint ) )
            {
                String k = key.substring( sourceRoleHint.length(  ) + 1 );

                p.setProperty( k, archetypeCatalogProperties.getProperty( key ) );
            }
        }

        return p;
    }
}