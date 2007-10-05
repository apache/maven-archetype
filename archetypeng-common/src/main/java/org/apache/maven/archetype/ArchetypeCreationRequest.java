package org.apache.maven.archetype;

import org.apache.maven.artifact.repository.ArtifactRepository;

import java.util.List;

/** @author Jason van Zyl */
public class ArchetypeCreationRequest
{
    private ArtifactRepository localRepository;

    private List remoteRepositories;     
}
