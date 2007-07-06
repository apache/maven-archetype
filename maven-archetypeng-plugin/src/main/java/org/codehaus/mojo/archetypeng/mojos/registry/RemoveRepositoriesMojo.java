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
import org.codehaus.mojo.archetypeng.registry.ArchetypeRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author rafale

 * @requiresProject  false
 * @goal             remove-repositories
 */
public class RemoveRepositoriesMojo
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
     * @parameter expression="${repositoryId}"
     */
    String  repositoryId;
    
    /**
     * @parameter expression="${repositories}"
     */
    String repositories;

    public void execute()
    throws MojoExecutionException, MojoFailureException
    {
        if (StringUtils.isEmpty(repositoryId)&&StringUtils.isEmpty(repositories))
        {
            throw new MojoFailureException(" -DrepositoryId or -Drepositories must be set");
        }
        else if (StringUtils.isNotEmpty(repositoryId)&& StringUtils.isNotEmpty(repositories))
        {
            throw new MojoFailureException("Only one of -DrepositoryId or -Drepositories can be set");
        }
        
        
        try
        {
            List repositoriesToRemove = new ArrayList();
            if (StringUtils.isNotEmpty(repositoryId))
            {
                ArchetypeRepository repository = new ArchetypeRepository();
                
                repository.setId(repositoryId);
                repository.setUrl("EMPTY");
                
                repositoriesToRemove.add(repository);
            }
            else
            {
                Iterator repositoriesDefinitions =  Arrays.asList(StringUtils.split(repositories, ",")).iterator();
                while(repositoriesDefinitions.hasNext())
                {
                    String repositoryDefinition = (String) repositoriesDefinitions.next();
                    
                    ArchetypeRepository repository = new ArchetypeRepository();

                    repository.setId(repositoryDefinition);
                    repository.setUrl("EMPTY");

                    repositoriesToRemove.add(repository);
                }
                
            }
            
            ArchetypeRegistry registry = archetypeRegistryManager.readArchetypeRegistry(
                    archetypeRegistryFile
                );
            
            Iterator repositoriesToRemoveIterator = repositoriesToRemove.iterator();
            while(repositoriesToRemoveIterator.hasNext())
            {
                ArchetypeRepository repositoryToRemove = (ArchetypeRepository)repositoriesToRemoveIterator.next();
                if (registry.getArchetypeRepositories().contains(repositoryToRemove))
                {
                    registry.removeArchetypeRepository(repositoryToRemove);
                    getLog().debug("Repository "+ repositoryToRemove.getId()+" removed");
                }
                else
                {
                    getLog().debug("Repository "+ repositoryToRemove.getId()+" doesn't exist");
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
