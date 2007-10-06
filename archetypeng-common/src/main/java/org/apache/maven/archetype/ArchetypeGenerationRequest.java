package org.apache.maven.archetype;

import java.util.Properties;
import org.apache.maven.artifact.repository.ArtifactRepository;

/** @author Jason van Zyl */
public class ArchetypeGenerationRequest
{
    private String outputDirectory;

    private ArtifactRepository localRepository;

    // Archetype definition
    private String archetypeName;

    private String archetypeGroupId;

    private String archetypeArtifactId;

    private String archetypeVersion;

    private String archetypeGoals;

    private String archetypeRepository;

    // Archetype configuration
    private String groupId;

    private String artifactId;

    private String version;

    private String packageName;

    private Properties additionalProperties;

    public String getArchetypeGroupId()
    {
        return archetypeGroupId;
    }

    public ArchetypeGenerationRequest setArchetypeGroupId( String archetypeGroupId )
    {
        this.archetypeGroupId = archetypeGroupId;

        return this;
    }

    public String getArchetypeArtifactId()
    {
        return archetypeArtifactId;
    }

    public ArchetypeGenerationRequest setArchetypeArtifactId( String archetypeArtifactId )
    {
        this.archetypeArtifactId = archetypeArtifactId;

        return this;
    }

    public String getArchetypeVersion()
    {
        return archetypeVersion;
    }

    public ArchetypeGenerationRequest setArchetypeVersion( String archetypeVersion )
    {
        this.archetypeVersion = archetypeVersion;

        return this;
    }

    public String getArchetypeGoals()
    {
        return archetypeGoals;
    }

    public ArchetypeGenerationRequest setArchetypeGoals( String archetypeGoals )
    {
        this.archetypeGoals = archetypeGoals;

        return this;
    }

    public String getArchetypeName()
    {
        return archetypeName;
    }

    public ArchetypeGenerationRequest setArchetypeName( String archetypeName )
    {
        this.archetypeName = archetypeName;

        return this;
    }

    public String getArchetypeRepository()
    {
        return archetypeRepository;
    }

    public ArchetypeGenerationRequest setArchetypeRepository( String archetypeRepository )
    {
        this.archetypeRepository = archetypeRepository;

        return this;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public ArchetypeGenerationRequest setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;

        return this;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getVersion()
    {
        return version;
    }

    public ArchetypeGenerationRequest setVersion( String version )
    {
        this.version = version;

        return this;
    }

    public String getPackage()
    {
        return packageName;
    }

    public ArchetypeGenerationRequest setPackage( String packageName )
    {
        this.packageName = packageName;

        return this;
    }

    public Properties getAdditionalProperties()
    {
        return additionalProperties;
    }

    public ArchetypeGenerationRequest setAdditionalProperties( Properties additionalProperties )
    {
        this.additionalProperties = additionalProperties;

        return this;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public ArchetypeGenerationRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;

        return this;
    }

    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public ArchetypeGenerationRequest setOutputDirectory( String outputDirectory )
    {
        this.outputDirectory = outputDirectory;

        return this;
    }
}
