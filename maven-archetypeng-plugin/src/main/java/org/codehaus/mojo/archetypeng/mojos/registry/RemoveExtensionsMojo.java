package org.codehaus.mojo.archetypeng.mojos.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.codehaus.mojo.archetypeng.registry.ArchetypeRegistry;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author rafale

 * @requiresProject  false
 * @goal             remove-extensions
 */
public class RemoveExtensionsMojo
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
     * @parameter expression="${extension}"
     */
    String extension;
    
    /**
     * @parameter expression="${extensions}"
     */
    String extensions;

    public void execute()
    throws MojoExecutionException, MojoFailureException
    {
        if (StringUtils.isEmpty(extension)&&StringUtils.isEmpty(extensions))
        {
            throw new MojoFailureException("-Dextension or -Dextensions must be set");
        }
        else if (StringUtils.isNotEmpty(extension)&& StringUtils.isNotEmpty(extensions))
        {
            throw new MojoFailureException("Only one of -Dextension or -Dextensions can be set");
        }
        
        
        try
        {
            List extensionsToRemove = new ArrayList();
            if (StringUtils.isNotEmpty(extension))
            {
                extensionsToRemove.add(extension);
            }
            else
            {
                extensionsToRemove.addAll(Arrays.asList(StringUtils.split(extensions, ",")));
            }
            
            ArchetypeRegistry registry = archetypeRegistryManager.readArchetypeRegistry(
                    archetypeRegistryFile
                );
            
            Iterator extensionsToRemoveIterator = extensionsToRemove.iterator();
            while(extensionsToRemoveIterator.hasNext())
            {
                String extensionToRemove = (String)extensionsToRemoveIterator.next();
                if (registry.getFilteredExtensions().contains(extensionToRemove))
                {
                    registry.removeFilteredExtension(extensionToRemove);
                    getLog().debug("Extension "+ extensionToRemove+" removed");
                }
                else
                {
                    getLog().debug("Extension "+ extensionToRemove+" doesn't exist");
                }
            }
            archetypeRegistryManager.writeArchetypeRegistry(archetypeRegistryFile, registry);
        }
        catch ( Exception ex )
        {
            throw new MojoExecutionException ( ex.getMessage (), ex );
        }
    }

}
