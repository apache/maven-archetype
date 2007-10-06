package org.apache.maven.archetype;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/** @author Jason van Zyl */
public class ArchetypeCreationRequest
{
    private ArtifactRepository localRepository;
    private List remoteRepositories;
    private MavenProject project;
    private File propertyFile;
    private List languages;
    private List filtereds;
    private String defaultEncoding;
    private boolean ignoreReplica;
    private boolean preserveCData;
    private boolean keepParent;
    private boolean partialArchetype;
    private File archetypeRegistryFile;

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public ArchetypeCreationRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;

        return this;
    }

    public List getRemoteRepositories()
    {
        return remoteRepositories;
    }

    public ArchetypeCreationRequest setRemoteRepositories( List remoteRepositories )
    {
        this.remoteRepositories = remoteRepositories;

        return this;
    }

    public MavenProject getProject()
    {
        return project;
    }

    public ArchetypeCreationRequest setProject( MavenProject project )
    {
        this.project = project;

        return this;
    }

    public File getPropertyFile()
    {
        return propertyFile;
    }

    public ArchetypeCreationRequest setPropertyFile( File propertyFile )
    {
        this.propertyFile = propertyFile;

        return this;
    }

    public List getLanguages()
    {
        return languages;
    }

    public ArchetypeCreationRequest setLanguages( List languages )
    {
        this.languages = languages;

        return this;
    }

    public List getFiltereds()
    {
        return filtereds;
    }

    public ArchetypeCreationRequest setFiltereds( List filtereds )
    {
        this.filtereds = filtereds;

        return this;
    }

    public String getDefaultEncoding()
    {
        return defaultEncoding;
    }

    public ArchetypeCreationRequest setDefaultEncoding( String defaultEncoding )
    {
        this.defaultEncoding = defaultEncoding;

        return this;
    }

    public boolean isIgnoreReplica()
    {
        return ignoreReplica;
    }

    public ArchetypeCreationRequest setIgnoreReplica( boolean ignoreReplica )
    {
        this.ignoreReplica = ignoreReplica;

        return this;
    }

    public boolean isPreserveCData()
    {
        return preserveCData;
    }

    public ArchetypeCreationRequest setPreserveCData( boolean preserveCData )
    {
        this.preserveCData = preserveCData;

        return this;
    }

    public boolean isKeepParent()
    {
        return keepParent;
    }

    public ArchetypeCreationRequest setKeepParent( boolean keepParent )
    {
        this.keepParent = keepParent;

        return this;
    }

    public boolean isPartialArchetype()
    {
        return partialArchetype;
    }

    public ArchetypeCreationRequest setPartialArchetype( boolean partialArchetype )
    {
        this.partialArchetype = partialArchetype;

        return this;
    }

    public File getArchetypeRegistryFile()
    {
        return archetypeRegistryFile;
    }

    public ArchetypeCreationRequest setArchetypeRegistryFile( File archetypeRegistryFile )
    {
        this.archetypeRegistryFile = archetypeRegistryFile;

        return this;
    }
}
