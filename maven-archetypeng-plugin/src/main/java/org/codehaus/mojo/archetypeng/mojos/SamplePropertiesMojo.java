package org.codehaus.mojo.archetypeng.mojos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.archetypeng.ArchetypeArtifactManager;
import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.ArchetypeRegistryManager;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.exception.ArchetypeGenerationConfigurationFailure;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.UnknownArchetype;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author rafale

 * @requiresProject  false
 * @goal             sample-properties
 */
public class SamplePropertiesMojo
extends AbstractMojo
{
    /**
     * @parameter  expression="${user.home}/.m2/archetype.xml"
     */
    private File archetypeRegistryFile;

    /**
     * @component
     */
    ArchetypeRegistryManager archetypeRegistryManager;

    /**
     * @component
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

    /**
     * @component
     */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /**
     * Local maven repository.
     *
     * @parameter  expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @parameter  expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List pomRemoteRepositories;

    /**
     * @parameter  default-value="archetype.properties" expression="${archetype.properties}"
     */
    private File propertyFile = null;

    /**
     * Other remote repositories available for discovering dependencies and extensions.
     *
     * @parameter  expression="${remoteRepositories}"
     */
    private String remoteRepositories;

    public void execute()
    throws MojoExecutionException, MojoFailureException
    {
        try
        {
            List repositories =
                archetypeRegistryManager.getRepositories (
                    pomRemoteRepositories,
                    remoteRepositories,
                    archetypeRegistryFile
                );

            Properties properties =
                initialiseArchetypeProperties ( System.getProperties(), propertyFile );

            ArchetypeDefinition archetypeDefinition =
                archetypeFactory.createArchetypeDefinition ( properties );
            if ( !archetypeDefinition.isDefined () )
            {
                throw new ArchetypeNotDefined ( "The archetype is not defined" );
            }

            if ( !archetypeArtifactManager.exists (
                    archetypeDefinition.getGroupId (),
                    archetypeDefinition.getArtifactId (),
                    archetypeDefinition.getVersion (),
                    localRepository,
                    repositories
                )
            )
            {
                throw new UnknownArchetype (
                    "The desired archetype does not exist (" + archetypeDefinition.getGroupId () + ":"
                    + archetypeDefinition.getArtifactId () + ":" + archetypeDefinition.getVersion ()
                    + ")"
                );
            }

            ArchetypeConfiguration archetypeConfiguration;

            if ( archetypeArtifactManager.isOldArchetype (
                    archetypeDefinition.getGroupId (),
                    archetypeDefinition.getArtifactId (),
                    archetypeDefinition.getVersion (),
                    localRepository,
                    repositories
                )
            )
            {
                org.apache.maven.archetype.descriptor.ArchetypeDescriptor archetypeDescriptor =
                    archetypeArtifactManager.getOldArchetypeDescriptor (
                        archetypeDefinition.getGroupId (),
                        archetypeDefinition.getArtifactId (),
                        archetypeDefinition.getVersion (),
                        localRepository,
                        repositories
                    );
                archetypeConfiguration =
                    archetypeFactory.createArchetypeConfiguration ( archetypeDescriptor, properties );
            }
            else if (
                archetypeArtifactManager.isFileSetArchetype (
                    archetypeDefinition.getGroupId (),
                    archetypeDefinition.getArtifactId (),
                    archetypeDefinition.getVersion (),
                    localRepository,
                    repositories
                )
            )
            {
                org.codehaus.mojo.archetypeng.archetype.filesets.ArchetypeDescriptor archetypeDescriptor =
                    archetypeArtifactManager.getFileSetArchetypeDescriptor (
                        archetypeDefinition.getGroupId (),
                        archetypeDefinition.getArtifactId (),
                        archetypeDefinition.getVersion (),
                        localRepository,
                        repositories
                    );
                archetypeConfiguration =
                    archetypeFactory.createArchetypeConfiguration ( archetypeDescriptor, properties );
            }
            else
            {
                throw new ArchetypeGenerationConfigurationFailure (
                    "The defined artifact is not an archetype"
                );
            }
            
            archetypeConfiguration.setProperty(Constants.GROUP_ID, "com.company");
            archetypeConfiguration.setProperty(Constants.ARTIFACT_ID, "project");
            archetypeConfiguration.setProperty(Constants.VERSION, "1.0-SNAPSHOT");
            archetypeConfiguration.setProperty(Constants.PACKAGE, "com.company.project");
            
            Iterator requiredProperties = archetypeConfiguration.getRequiredProperties().iterator();
            while(requiredProperties.hasNext())
            {
                String requiredProperty = (String) requiredProperties.next();
                
                if (StringUtils.isEmpty(archetypeConfiguration.getProperty(requiredProperty)))
                {
                    archetypeConfiguration.setProperty(
                        requiredProperty, 
                        StringUtils.isEmpty(archetypeConfiguration.getDefaultValue(requiredProperty)) ?
                            "To be defined": 
                            archetypeConfiguration.getDefaultValue(requiredProperty)
                        );
                }
            }

            archetypePropertiesManager.writeProperties (
                archetypeConfiguration.toProperties (),
                propertyFile
            );
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }

    private Properties initialiseArchetypeProperties (
        Properties commandLineProperties,
        File propertyFile
    )
    throws FileNotFoundException, IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );

        Iterator commandLinePropertiesIterator =
            new ArrayList ( commandLineProperties.keySet () ).iterator ();
        while ( commandLinePropertiesIterator.hasNext () )
        {
            String propertyKey = (String) commandLinePropertiesIterator.next ();
            properties.setProperty (
                propertyKey,
                commandLineProperties.getProperty ( propertyKey )
            );
        }
        return properties;
    }
}
