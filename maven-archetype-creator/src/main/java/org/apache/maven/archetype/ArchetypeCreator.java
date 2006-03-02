package org.apache.maven.archetype;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Properties;

/**
 * @author Jason van Zyl
 * @version $Revision:$
 */
public interface ArchetypeCreator
{
    static String ROLE = ArchetypeCreator.class.getName();

    File createArchetype( File basedir, ArtifactRepository localRepository, File targetDirectory, Properties properties )
        throws ArchetypeCreationException;

    File createArchetype( MavenProject project, ArtifactRepository localRepository, File targetDirectory, Properties properties )
        throws ArchetypeCreationException;
}
