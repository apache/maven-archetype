package org.apache.maven.archetype.artifact
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.artifact.repository.ArtifactRepository

/**
 *
 * @author rafale
 */
interface ArchetypeArtifactManager {
    static String ROLE = 'org.apache.maven.archetype.artifact.ArchetypeArtifactManager'

    static String ARCHETYPE_DESCRIPTOR = 'META-INF/maven/archetype-metadata.xml'
    static String LEGACY_ARCHETYPE_DESCRIPTOR = 'META-INF/maven/archetype.xml'
    static String OLD_LEGACY_ARCHETYPE_DESCRIPTOR = 'META-INF/archetype.xml'

    def getFilesetArchetypeDescriptor( File archetypeFile )
    def getFilesetArchetypeResources( File archetypeFile )
    def getArchetypeZipFile( File archetypeFile )

    def getArchetypeFile( String groupId, String artifactId, String version,
        ArtifactRepository localRepository, List<ArtifactRepository> remoteRepository )
    def isLegacyArchetype( File archetypeFile )

    def getLegacyArchetypeDescriptor( File archetypeFile )
}

