package org.apache.maven.archetype.mojos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Properties;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Reader;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.archetype.source.ArchetypeDataSourceException;
import org.apache.maven.archetype.source.CatalogArchetypeDataSource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;


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

    private ArchetypeCatalogXpp3Reader catalogReader =
        new ArchetypeCatalogXpp3Reader (  );

    private ArchetypeCatalogXpp3Writer catalogWriter =
        new ArchetypeCatalogXpp3Writer (  );

    public void execute ( )
        throws MojoExecutionException
    {
        try
        {
            File catalogFile = getCatalogFile (  );
            ArchetypeCatalog catalog = readLocalCatalog ( catalogFile );
            updateCatalog ( project, catalog );
            writeLocalCatalog ( catalog, catalogFile );
        }
        catch ( ArchetypeDataSourceException ex )
        {
            throw new MojoExecutionException ( ex.getMessage (  ), ex );
        }
    }

    private File getCatalogFile ( )
    {
        File archetypeCatalogPropertiesFile =
            new File ( System.getProperty ( "user.home" ),
            ".m2/archetype-catalog.properties" );

        getLog (  ).
            debug ( "Using catalog properties " + archetypeCatalogPropertiesFile );

        Properties archetypeCatalogProperties;

        if ( archetypeCatalogPropertiesFile.exists (  ) )
        {
            archetypeCatalogProperties =
                PropertyUtils.loadProperties ( archetypeCatalogPropertiesFile );
        }
        else
        {
            archetypeCatalogProperties = new Properties (  );
            archetypeCatalogProperties.setProperty ( "catalog." +
                CatalogArchetypeDataSource.ARCHETYPE_CATALOG_PROPERTY,
                CatalogArchetypeDataSource.ARCHETYPE_CATALOG_FILENAME );
        }


        String s =
            archetypeCatalogProperties.getProperty ( "catalog." +
            CatalogArchetypeDataSource.ARCHETYPE_CATALOG_PROPERTY );

        s = StringUtils.replace ( s, "${user.home}",
            System.getProperty ( "user.home" ) );

        getLog (  ).debug ( "Using catalog file " + s );

        return new File ( s );
    }

    private ArchetypeCatalog readLocalCatalog ( File catalogFile )
        throws ArchetypeDataSourceException
    {
        ArchetypeCatalog catalog;
        if ( catalogFile.exists (  ) )
        {
            try
            {
                getLog (  ).debug ( "Reading the catalog " + catalogFile );
                catalog = readCatalog ( new FileReader ( catalogFile ) );
            }
            catch ( FileNotFoundException ex )
            {
                getLog (  ).debug ( "Catalog file don't exist" );
                catalog =
                    new ArchetypeCatalog (  );
            }
        }
        else
        {
            getLog (  ).debug ( "Catalog file don't exist" );
            catalog = new ArchetypeCatalog (  );
        }

        return catalog;
    }

    protected ArchetypeCatalog readCatalog ( Reader reader )
        throws ArchetypeDataSourceException
    {
        try
        {
            return catalogReader.read ( reader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException ( "Error reading archetype catalog.",
                e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeDataSourceException ( "Error parsing archetype catalog.",
                e );
        }
        finally
        {
            IOUtil.close ( reader );
        }
    }

    private void updateCatalog ( MavenProject project,
        ArchetypeCatalog catalog )
    {
        Iterator archetypes = catalog.getArchetypes (  ).iterator (  );
        boolean found = false;
        Archetype archetype = null;
        while ( !found && archetypes.hasNext (  ) )
        {
            Archetype a =
                (Archetype) archetypes.next();
            if ( a.getGroupId (  ).equals ( project.getGroupId (  ) ) &&
                a.getArtifactId (  ).equals ( project.getArtifactId (  ) ) )
            {
                archetype = a;
                found = true;
            }
        }
        if ( !found )
        {
            archetype = new Archetype (  );
            archetype.setGroupId ( project.getGroupId (  ) );
            archetype.setArtifactId ( project.getArtifactId (  ) );
            catalog.addArchetype ( archetype );
        }
        //TODO: find correct values
        archetype.setVersion ( project.getVersion (  ) );
        archetype.setRepository ( "file://" + localRepository.getAbsolutePath (  ) );
        archetype.setDescription ( project.getDescription (  ) );
//        archetype.setProperties(null);
//        archetype.setGoals(null);
    }

    private void writeLocalCatalog ( ArchetypeCatalog catalog,
        File catalogFile )
        throws ArchetypeDataSourceException
    {
        FileWriter writer = null;
        try
        {
            writer = new FileWriter ( catalogFile );
            catalogWriter.write ( writer, catalog );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDataSourceException ( "Error writing archetype catalog.",
                e );
        }
        finally
        {
            IOUtil.close ( writer );
        }
    }
}