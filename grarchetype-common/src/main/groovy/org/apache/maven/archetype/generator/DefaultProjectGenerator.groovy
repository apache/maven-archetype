package org.apache.maven.archetype.generator
import org.codehaus.plexus.logging.AbstractLogEnabled
import org.apache.maven.archetype.ArchetypeGenerationRequest
import org.apache.maven.archetype.artifact.ArchetypeArtifactManager

/**
 *
 * @author raphaelpieroni
 * @plexus.component
 */
class DefaultProjectGenerator 
extends AbstractLogEnabled
implements ProjectGenerator {

    /** @plexus.requirement */
    ArchetypeArtifactManager artifactManager

    /** @plexus.requirement role-hint="fileset" */
    FilesetArchetypeGenerator filesetGenerator

    /** @plexus.requirement role-hint="legacy" */
    FilesetArchetypeGenerator oldGenerator

    void generateProject( ArchetypeGenerationRequest request ) {
        
        def mainArchetypeName = "${request.groupId}:${request.artifactId}:${request.version}"
        logger.info "Generating project from archetype ${mainArchetypeName} into directory ${request.outputDirectory}"

        def mainArchetypeFile = artifactManager.getArchetypeFile(
            request.groupId, request.artifactId, request.version,
            request.localRepository, request.repositories )
println"generateProject ${artifactManager.isLegacyArchetype( mainArchetypeFile )} ${mainArchetypeFile}"
        if( artifactManager.isLegacyArchetype( mainArchetypeFile ) ) {
            logger.info "Archetype ${mainArchetypeName} is a legacy archetype"
            request.archetypeFile = mainArchetypeFile
            
            oldGenerator.generateProject request
            
        } else {
            logger.info "Archetype ${mainArchetypeName} is a fileset archetype"
            request.archetypeFile = mainArchetypeFile
            
            def archetypes = [mainArchetypeName:[file:mainArchetypeFile, generator:filesetGenerator]]
            getNestedArchetypes mainArchetypeFile, archetypes, request.localRepository, request.repositories
            request.archetypes = archetypes

            logger.info "Found ${archetypes.size()} archetypes to generate the project with"
            filesetGenerator.generateProject request
        }
    }

    void getNestedArchetypes( archetypeFile, archetypes, localRepository, repositories ) {
        logger.info "Searching for nested archetype of ${archetypeFile}"
        def descriptor = new XmlSlurper().parse( artifactManager.getFilesetArchetypeDescriptor( archetypeFile ) )
        def nestedArchetypes = []
        descriptor.modules.module.each {
            if( it.@nested.toBoolean() ) {
                logger.info "Module ${it.@artifactId.toString()} is a nested archetype"
                addArchetype it, archetypes, nestedArchetypes, localRepository, repositories
            } else {
                logger.info "Module ${it.@artifactId.toString()} is a regular module"
                getInnerArchetypes it, archetypes, nestedArchetypes, localRepository, repositories
            }
        }
        nestedArchetypes.each { nestedFile ->
            getNestedArchetypes nestedFile, archetypes, localRepository, repositories
        }
    }

    void getInnerArchetypes( module, archetypes, nestedArchetypes, localRepository, repositories ) {
        module.modules.module.each {
            if( it.@nested.toBoolean() ) {
                logger.info "Module ${it.@artifactId.toString()} is a nested archetype"
                addArchetype it, archetypes, nestedArchetypes, localRepository, repositories
            } else {
                logger.info "Module ${it.@artifactId.toString()} is a regular module"
                getInnerArchetypes it, archetypes, nestedArchetypes, localRepository, repositories
            }
        }
    }
    
    void addArchetype( module, archetypes, nestedArchetypes, localRepository, repositories ) {
        def moduleFile = artifactManager.getArchetypeFile(
            module.@archetypeGroupId.toString(), 
            module.@archetypeArtifactId.toString(),
            module.@archetypeVersion.toString(),
            localRepository, repositories )
        def archetypeName = "${module.@archetypeGroupId.toString()}:${module.@archetypeArtifactId.toString()}:${module.@archetypeVersion.toString()}"

        if( artifactManager.isLegacyArchetype( moduleFile ) ) {
            logger.info "Nested Archetype ${archetypeName} is a legacy archetype"
            archetypes."${archetypeName}" = [file:moduleFile, generator:oldGenerator]
        } else {
            logger.info "Nested Archetype ${archetypeName} is a fileset archetype"
            archetypes."${archetypeName}" = [file:moduleFile, generator:filesetGenerator]
            nestedArchetypes << moduleFile
        }
    }
}

