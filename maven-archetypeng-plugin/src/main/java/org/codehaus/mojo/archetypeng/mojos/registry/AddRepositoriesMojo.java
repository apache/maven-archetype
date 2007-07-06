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
 * @goal             add-repositories
 */
public class AddRepositoriesMojo
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
     * @parameter expression="${repositoryUrl}"
     */
    String  repositoryUrl;
    
    /**
     * @parameter expression="${repositories}"
     */
    String repositories;

    public void execute()
    throws MojoExecutionException, MojoFailureException
    {
        if (StringUtils.isEmpty(repositoryId)&&StringUtils.isEmpty(repositories))
        {
            throw new MojoFailureException(" (-DrepositoryId and -DrepositoryUrl) or -Drepositories must be set");
        }
        else if (StringUtils.isNotEmpty(repositoryId)&& StringUtils.isNotEmpty(repositories))
        {
            throw new MojoFailureException("Only one of (-DrepositoryId and -DrepositoryUrl) or -Drepositories can be set");
        }
        
        
        try
        {
            List repositoriesToAdd = new ArrayList();
            if (StringUtils.isNotEmpty(repositoryId) && StringUtils.isNotEmpty(repositoryUrl))
            {
                ArchetypeRepository repository = new ArchetypeRepository();
                
                repository.setId(repositoryId);
                repository.setUrl(repositoryUrl);
                
                repositoriesToAdd.add(repository);
            }
            else
            {
                Iterator repositoriesDefinitions =  Arrays.asList(StringUtils.split(repositories, ",")).iterator();
                while(repositoriesDefinitions.hasNext())
                {
                    String repositoryDefinition = (String) repositoriesDefinitions.next();
                    
                    String[] repositoryDefinitionParts = StringUtils.split(repositoryDefinition, "=");
                    
                    ArchetypeRepository repository = new ArchetypeRepository();

                    repository.setId(repositoryDefinitionParts[0]);
                    repository.setUrl(repositoryDefinitionParts[1]);

                    repositoriesToAdd.add(repository);
                }
                
            }
            
            ArchetypeRegistry registry = archetypeRegistryManager.readArchetypeRegistry(
                    archetypeRegistryFile
                );
            
            Iterator repositoriesToAddIterator = repositoriesToAdd.iterator();
            while(repositoriesToAddIterator.hasNext())
            {
                ArchetypeRepository repositoryToAdd = (ArchetypeRepository)repositoriesToAddIterator.next();
                if (registry.getArchetypeRepositories().contains(repositoryToAdd))
                {
                    getLog().debug("Repository "+ repositoryToAdd+" already exists");
                }
                else
                {
                    registry.addArchetypeRepository(repositoryToAdd);
                    getLog().debug("Repository "+ repositoryToAdd+" added");
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
