package org.apache.maven.archetype.artifact
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import org.codehaus.plexus.logging.AbstractLogEnabled
import org.apache.maven.artifact.factory.ArtifactFactory
import org.apache.maven.artifact.resolver.ArtifactResolver
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.artifact.Artifact

/**
 *
 * @author raphaelpieroni
 * @plexus.component
 */
class DefaultArchetypeArtifactManager
extends AbstractLogEnabled
implements ArchetypeArtifactManager {

    /**
     * @plexus.requirement
     */
    ArtifactResolver artifactResolver

    /**
     * @plexus.requirement
     */
    ArtifactFactory artifactFactory
    
	def getFilesetArchetypeDescriptor( File archetypeFile ) {
        ZipFile zipFile = getArchetypeZipFile( archetypeFile )
        ZipEntry entry = zipFile.getEntry( ARCHETYPE_DESCRIPTOR.replaceAll( File.separator, "/" ) )
        if( !entry ) {
            logger.debug "No found ${ARCHETYPE_DESCRIPTOR} retrying with windows path"
            entry = zipFile.getEntry( ARCHETYPE_DESCRIPTOR.replaceAll( "/", File.separator ) )
        }
        if( !entry ) throw new IOException( "The ${ARCHETYPE_DESCRIPTOR} descriptor cannot be found." )

        zipFile.getInputStream entry
    }

    def getFilesetArchetypeResources( File archetypeFile ) {
        ZipFile zipFile = getArchetypeZipFile( archetypeFile )
        zipFile.entries().findAll { entry ->
            !entry.isDirectory() && entry.name.startsWith ( 'archetype-resources/' )
        }.collect { entry ->
            entry.name.replace 'archetype-resources/', ''
        }
    }

    def getArchetypeZipFile( File archetypeFile ) {
        logger.debug "Loading zip file ${archetypeFile.exists()} ${archetypeFile}"
        new ZipFile( archetypeFile )
    }


    def getArchetypeFile( String groupId, String artifactId, String version, 
        ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories ) {
        
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version,
            Artifact.SCOPE_RUNTIME, 'jar' )
        artifactResolver.resolve( artifact, remoteRepositories, localRepository )
        artifact.file
    }

    def isLegacyArchetype( File archetypeFile ) {
        ZipFile zipFile = getArchetypeZipFile( archetypeFile )

        ZipEntry regularDescriptor = zipFile.getEntry( ARCHETYPE_DESCRIPTOR.replaceAll( File.separator, "/" ) )
        if( !regularDescriptor ) {
            regularDescriptor = zipFile.getEntry( ARCHETYPE_DESCRIPTOR.replaceAll( "/", File.separator ) )
        }
        if( regularDescriptor ) return false

        ZipEntry legacyDescriptor = zipFile.getEntry( LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( File.separator, "/" ) )
        if( !legacyDescriptor ) {
            legacyDescriptor = zipFile.getEntry( LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( "/", File.separator ) )
        }
        if( !legacyDescriptor ) {
            legacyDescriptor = zipFile.getEntry( OLD_LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( File.separator, "/" ) )
        }
        if( !legacyDescriptor ) {
            legacyDescriptor = zipFile.getEntry( OLD_LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( "/", File.separator ) )
        }
        if( legacyDescriptor ) return true

        return false
    }

	def getLegacyArchetypeDescriptor( File archetypeFile ) {
        ZipFile zipFile = getArchetypeZipFile( archetypeFile )
        ZipEntry entry = zipFile.getEntry( LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( File.separator, "/" ) )
        if( !entry ) {
            logger.debug "No found ${LEGACY_ARCHETYPE_DESCRIPTOR} retrying with windows path"
            entry = zipFile.getEntry( LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( "/", File.separator ) )
        }
        if( !entry ) {
            logger.debug "No found ${LEGACY_ARCHETYPE_DESCRIPTOR} retrying with ${OLD_LEGACY_ARCHETYPE_DESCRIPTOR}"
            entry = zipFile.getEntry( OLD_LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( File.separator, "/" ) )
        }
        if( !entry ) {
            logger.debug "No found ${OLD_LEGACY_ARCHETYPE_DESCRIPTOR} retrying with windows path"
            entry = zipFile.getEntry( OLD_LEGACY_ARCHETYPE_DESCRIPTOR.replaceAll( "/", File.separator ) )
        }

        zipFile.getInputStream entry
    }
    
    def getNestedArchetypeFiles( archetype, localRepository, repositories, archetypeFileMap = [:] ) {
        def archetypeFile = getArchetypeFile(
            archetype.groupId, archetype.artifactId, archetype.version,
            localRepository, repositories )
        archetypeFileMap[(archetype)] = archetypeFile
        if( !isLegacyArchetype( archetypeFile ) ) {
            getNestedArchetypes( archetypeFile, localRepository, repositories ).each { nestedArchetype ->
                getNestedArchetypeFiles( nestedArchetype, localRepository, repositories, archetypeFileMap )
            }
        }
        return archetypeFileMap
    }
    def getNestedArchetypes( archetypeFile, localRepository, repositories ) {
        logger.debug "Searching for nested archetype of ${archetypeFile}"
        def descriptor = new XmlSlurper().parse( getFilesetArchetypeDescriptor( archetypeFile ) )
        def nestedArchetypes = []
        descriptor.modules.module.each {
            if( it.@nested.toBoolean() ) {
                logger.debug "Module ${it.@artifactId.toString()} is a nested archetype"
                nestedArchetypes.add toDefinition( it )
            } else {
                logger.debug "Module ${it.@artifactId.toString()} is a regular module"
                getInnerArchetypes it, nestedArchetypes, localRepository, repositories
            }

        }
        return nestedArchetypes
    }
    void getInnerArchetypes( module, nestedArchetypes, localRepository, repositories ) {
        module.modules.module.each {
            if( it.@nested.toBoolean() ) {
                logger.debug "Module ${it.@artifactId.toString()} is a nested archetype"
                nestedArchetypes.add toDefinition( it )
            } else {
                logger.debug "Module ${it.@artifactId.toString()} is a regular module"
                getInnerArchetypes it, nestedArchetypes, localRepository, repositories
            }
        }
    }
    def toDefinition( module ) {
        [groupId:module.@archetypeGroupId.text().toString(),
        artifactId:module.@archetypeArtifactId.text().toString(),
        version:module.@archetypeVersion.text().toString()]
    }
}

