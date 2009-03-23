package org.apache.maven.archetype.catalog
import org.codehaus.plexus.logging.AbstractLogEnabled
import org.apache.maven.archetype.artifact.ArchetypeArtifactManager

/**
 *
 * @author raphaelpieroni
 * @plexus.component
 */
class DefaultArchetypeCataloger
extends AbstractLogEnabled
implements ArchetypeCataloger {

    /** @plexus.requirement */
    private ArchetypeArtifactManager artifactManager

    def loadCatalog( String catalogName ) {
        def catalog
        switch( catalogName ) {
            case "internal":
                logger.info "Using internal catalog"
                catalog = this.class.classLoader.getResourceAsStream( "archetype-catalog.xml" )
                break

            case "local":
                logger.info "Using default local catalog"
                catalog = new File( System.getProperty( "user.home" ), ".m2/archetype-catalog.xml" )
                break

            case "remote":
                logger.info "Using default remote catalog"
                catalog = "http://repo1.maven.org/maven2/archetype-catalog.xml"
                break

            case ~"file://.*":
                logger.info "Using local catalog $catalogName"
                break

            case ~"https?://.*":
                logger.info "Using remote catalog $catalogName"
                catalog = catalogName
                break
                
            default: return null
        }
        if( !catalog ) {
            logger.warn "Catalog $catalogName not found"
            return null
        }
        try {
            return new XmlSlurper().parse( catalog )
        } catch( e ) {
            logger.warn "Can not read catalog $catalogName: $e.message"
            return
        }
    }


    def archetypeRecursivelyExists( archetype, archetypeDefinitions, localRepository, repositories ) {
        def archetypeFile
        try {
            archetypeFile = artifactManager.getArchetypeFile(
            archetype.groupId, archetype.artifactId, archetype.version,
            localRepository, repositories )
        } catch( e ) {
            logger.warn "Archetype ${archetype} not found", e
            return false
        }
        if( artifactManager.isLegacyArchetype( archetypeFile ) ) {
            return true
        } else {
            def recursivelyExists = true
            getNestedArchetypes( archetypeFile, archetypeDefinitions, localRepository, repositories ).each { nestedArchetype ->
                recursivelyExists = recursivelyExists &&
                    archetypeRecursivelyExists( nestedArchetype, archetypeDefinitions, localRepository, repositories )
            }
            return recursivelyExists
        }
    }
    def getNestedArchetypes( archetypeFile, archetypeDefinitions, localRepository, repositories ) {
        logger.debug "Searching for nested archetype of ${archetypeFile}"
        def descriptor = new XmlSlurper().parse( artifactManager.getFilesetArchetypeDescriptor( archetypeFile ) )
        def nestedArchetypes = []
        descriptor.modules.module.each {
            if( it.@nested.toBoolean() ) {
                logger.debug "Module ${it.@artifactId.toString()} is a nested archetype"
                nestedArchetypes.add toDefinition( it )
            } else {
                logger.debug "Module ${it.@artifactId.toString()} is a regular module"
                getInnerArchetypes it, archetypeDefinitions, nestedArchetypes, localRepository, repositories
            }

        }
        return nestedArchetypes
    }
    void getInnerArchetypes( module, archetypes, nestedArchetypes, localRepository, repositories ) {
        module.modules.module.each {
            if( it.@nested.toBoolean() ) {
                logger.debug "Module ${it.@artifactId.toString()} is a nested archetype"
                nestedArchetypes.add toDefinition( it )
            } else {
                logger.debug "Module ${it.@artifactId.toString()} is a regular module"
                getInnerArchetypes it, archetypes, nestedArchetypes, localRepository, repositories
            }
        }
    }
    def getArchetypesByCatalog( catalogs ) {
        if( !catalogs ) return null
        def archetypes = [:]
        catalogs.split( "," ).each { catalogName ->
            def catalog = loadCatalog( catalogName )
            if( !catalog ) {
                logger.error "Unknown catalog $catalog"
                return null
            }
            catalog.archetypes.archetype.each{
                def archetype = [
                    catalog:catalogName,
                    groupId:it.groupId.text().toString(),
                    artifactId:it.artifactId.text().toString(),
                    version:it.version.text().toString(),
                    description:it.description.text().toString(),
                    repository:it.repository.text().toString()]
                if( !archetype.repository && (catalogName =~ "(file|https?)://.*" ) ) archetype.repository = catalogName - "archetype-catalog.xml"

                archetypes["${archetype.groupId}:${archetype.artifactId}:${archetype.version}".toString()] = archetype
            }
        }
        return archetypes
    }
    def toDefinition( module ) {
        [groupId:module.@archetypeGroupId.text().toString(),
        artifactId:module.@archetypeArtifactId.text().toString(),
        version:module.@archetypeVersion.text().toString()]
    }
    def searchArchetype( definition, archetypeCatalogs ) {
        def archetype
        archetypeCatalogs.each { key, value ->
            if( !archetype &&
                definition['groupId'] == value['groupId'] &&
                definition['artifactId'] == value['artifactId'] &&
                (definition['version'] ? definition['version'] == value['version'] : true) ) {
                archetype = value
            }
        }
        if( !archetype ) {
            log.warn "Archetype ${definition} not found in any catalog"
        }
        return archetype
    }
}

