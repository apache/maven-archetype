package org.apache.maven.archetype.file

/**
 *
 * @author rafale
 */
interface ArchetypeFileResolver {
    static String ROLE = 'org.apache.maven.archetype.file.ArchetypeFileResolver'
    def getFiles( moduleOffset, fileset, archetypeResources )
    def getFile( moduleOffset, filesetDirectory, resource, archetypeZipFile )
    def getResourcePath( moduleOffset, filesetDirectory, resource )
}

