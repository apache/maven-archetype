package org.apache.maven.archetype.file
import org.codehaus.plexus.logging.AbstractLogEnabled
import java.util.zip.ZipEntry
import org.apache.maven.archetype.util.Scanner

/**
 *
 * @author rafale
 * @plexus.component
 */
class DefaultArchetypeFileResolver
extends AbstractLogEnabled 
implements ArchetypeFileResolver {

    def getFiles( moduleOffset, fileset, archetypeResources ) {
        def resourceStart = "${moduleOffset?moduleOffset+'/':''}${fileset.directory.text().toString()?fileset.directory.text().toString()+'/':''}"
        def scanner = new Scanner( fileset:fileset )
//        logger.error"START=${resourceStart}"
        archetypeResources.findAll{ resource ->
            resource.startsWith resourceStart
        }.collect{ resource ->
            resource.replace resourceStart, ''
        }.findAll{resource ->
            scanner.match resource
        }
    }

    def getFile( moduleOffset, filesetDirectory, resource, archetypeZipFile ) {
        def resourceZipPath = getResourcePath( moduleOffset, filesetDirectory, resource )
//       logger.error"RES${resourceZipPath}}"
        try {
            ZipEntry entry = archetypeZipFile.getEntry( resourceZipPath )
            return archetypeZipFile.getInputStream( entry )
        } catch (e) {
            logger.error "Can not find resource ${resourceZipPath} in archetype sip file"
        }
    }

    def getResourcePath( moduleOffset, filesetDirectory, resource ) {
        "archetype-resources/${moduleOffset?moduleOffset+'/':''}${filesetDirectory?filesetDirectory+'/':''}${resource}".toString()
    }
}

