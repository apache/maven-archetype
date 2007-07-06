package org.codehaus.mojo.archetypeng.mojos.registry;

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
 * @goal             show-extensions
 */
public class ShowExtensionsMojo
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

    public void execute()
    throws MojoExecutionException, MojoFailureException
    {
        try
        {
            Iterator extensions =
                archetypeRegistryManager.readArchetypeRegistry(
                    archetypeRegistryFile
                ).getFilteredExtensions().iterator();
            
            getLog().info("Filtered extensions defined in " + archetypeRegistryFile);
            while(extensions.hasNext())
            {
                getLog().info(" - "+extensions.next());
            }

        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }

}
