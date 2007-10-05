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

    public void setArchetypeGroupId( String archetypeGroupId )
    {
        this.archetypeGroupId = archetypeGroupId;
    }

    public String getArchetypeArtifactId()
    {
        return archetypeArtifactId;
    }

    public void setArchetypeArtifactId( String archetypeArtifactId )
    {
        this.archetypeArtifactId = archetypeArtifactId;
    }

    public String getArchetypeVersion()
    {
        return archetypeVersion;
    }

    public void setArchetypeVersion( String archetypeVersion )
    {
        this.archetypeVersion = archetypeVersion;
    }

    public String getArchetypeGoals()
    {
        return archetypeGoals;
    }

    public void setArchetypeGoals( String archetypeGoals )
    {
        this.archetypeGoals = archetypeGoals;
    }

    public String getArchetypeName()
    {
        return archetypeName;
    }

    public void setArchetypeName( String archetypeName )
    {
        this.archetypeName = archetypeName;
    }

    public String getArchetypeRepository()
    {
        return archetypeRepository;
    }

    public void setArchetypeRepository( String archetypeRepository )
    {
        this.archetypeRepository = archetypeRepository;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
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

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getPackage()
    {
        return packageName;
    }

    public void setPackage( String packageName )
    {
        this.packageName = packageName;
    }

    public Properties getAdditionalProperties()
    {
        return additionalProperties;
    }

    public void setAdditionalProperties( Properties additionalProperties )
    {
        this.additionalProperties = additionalProperties;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public void setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( String outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }
}
