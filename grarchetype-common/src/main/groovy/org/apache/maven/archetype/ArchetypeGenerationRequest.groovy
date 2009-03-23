package org.apache.maven.archetype

/**
 *
 * @author rafale
 */
class ArchetypeGenerationRequest {
    //- input properties
    def repositories
    def localRepository

    def groupId
    def artifactId
    def version
    def outputDirectory
    def generationFilterProperties

    //- calculed properties
    def archetypeFile
    def archetypes
}

