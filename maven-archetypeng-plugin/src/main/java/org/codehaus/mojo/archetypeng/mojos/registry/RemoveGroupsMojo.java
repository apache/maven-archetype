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
 * @goal             remove-groups
 */
public class RemoveGroupsMojo
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
     * @parameter expression="${group}"
     */
    String  group;
    
    /**
     * @parameter expression="${groups}"
     */
    String groups;

    public void execute()
    throws MojoExecutionException, MojoFailureException
    {
        if (StringUtils.isEmpty(group)&&StringUtils.isEmpty(groups))
        {
            throw new MojoFailureException("-Dgroup or -Dgroups must be set");
        }
        else if (StringUtils.isNotEmpty(group)&& StringUtils.isNotEmpty(groups))
        {
            throw new MojoFailureException("Only one of -Dgroup or -Dgroups can be set");
        }
        
        
        try
        {
            List groupsToRemove = new ArrayList();
            if (StringUtils.isNotEmpty(group))
            {
                groupsToRemove.add(group);
            }
            else
            {
                groupsToRemove.addAll(Arrays.asList(StringUtils.split(groups, ",")));
            }
            
            ArchetypeRegistry registry = archetypeRegistryManager.readArchetypeRegistry(
                    archetypeRegistryFile
                );
            
            Iterator groupsToRemoveIterator = groupsToRemove.iterator();
            while(groupsToRemoveIterator.hasNext())
            {
                String groupToRemove = (String)groupsToRemoveIterator.next();
                if (registry.getArchetypeGroups().contains(groupToRemove))
                {
                    registry.removeArchetypeGroup(groupToRemove);
                    getLog().debug("Group "+ groupToRemove+" removed");
                }
                else
                {
                    getLog().debug("Group "+ groupToRemove+" doesn't exist");
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
