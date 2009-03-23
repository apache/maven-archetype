package org.apache.maven.archetype.generator
import org.apache.maven.archetype.ArchetypeGenerationRequest
import org.apache.maven.archetype.artifact.ArchetypeArtifactManager
import org.codehaus.plexus.logging.AbstractLogEnabled
import groovy.text.SimpleTemplateEngine
import org.apache.maven.archetype.file.ArchetypeFileResolver
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.StreamingSAXBuilder
import groovy.xml.MarkupBuilder
import org.apache.maven.archetype.pom.PomManager
import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.gparallelizer.Asynchronizer
import org.apache.velocity.runtime.log.LogChute
import org.apache.velocity.runtime.RuntimeServices
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.VelocityContext

/**
 *
 * @author raphaelpieroni
 */
abstract class AbstractFilesetArchetypeGenerator
extends AbstractLogEnabled
implements FilesetArchetypeGenerator, LogChute {

    /** @plexus.requirement */
    ArchetypeArtifactManager archetypeArtifactManager
    /** @plexus.requirement */
    ArchetypeFileResolver archetypeFileResolver
    /** @plexus.requirement */
    PomManager pomManager

    def templateEngine = new SimpleTemplateEngine()

    void generateProject( ArchetypeGenerationRequest request ){
        def archetypeDescriptor = loadArchetypeDescriptor( request.archetypeFile )
        def archetypeResources = archetypeArtifactManager.getFilesetArchetypeResources( request.archetypeFile )
        def archetypeZipFile = archetypeArtifactManager.getArchetypeZipFile( request.archetypeFile )
        def velocity = getVelocityEngine( request.archetypeFile )
        def basedirPom = new File( request.outputDirectory, 'pom.xml' )
        def filterProperties = prepareFilterProperties( request )

logger.error"RESOURCES${archetypeResources}"
logger.error"this=${this.dump()}"
        if( !archetypeDescriptor.@partial.toBoolean() ) {
            logger.info "Processing complete archetype: ${archetypeDescriptor.@name}"
            def projectOutputDirectory = new File( request.outputDirectory, filterProperties.artifactId )

            if( new File( projectOutputDirectory, 'pom.xml' ).exists() ) throw new Exception( "A project already exists in ${projectOutputDirectory}" )

            def processRequest = [moduleOffset:'',
                zipModuleOffset:'',
                archetypeDescriptor:archetypeDescriptor,
                filterProperties:filterProperties,
                archetypeResources:archetypeResources,
                archetypeZipFile:archetypeZipFile,
                outputDirectory:projectOutputDirectory,
                updirPom:basedirPom,
                archetypes:request.archetypes,
                velocity:velocity
            ]
            processProject processRequest
        } else {
logger.error "Processing partial archetype: ${archetypeDescriptor.@name} in ${request.outputDirectory}"

            if( !basedirPom.exists() ) throw new Exception( "No project to modify in  ${request.outputDirectory}" )

            def processRequest = [moduleOffset:'',
                zipModuleOffset:'',
                archetypeDescriptor:archetypeDescriptor,
                filterProperties:filterProperties,
                archetypeResources:archetypeResources,
                archetypeZipFile:archetypeZipFile,
                outputDirectory:request.outputDirectory,
                archetypes:request.archetypes,
                velocity:velocity
            ]
            processPartialProject processRequest

        }
    }

    abstract def loadArchetypeDescriptor( File archetypeFile )

    def getVelocityEngine( File jarFile ){
        def engine = new VelocityEngine()
        engine.setProperty VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this
        engine.setProperty VelocityEngine.RESOURCE_LOADER, 'jar'
        engine.setProperty 'jar.resource.loader.class', 'org.apache.velocity.runtime.resource.loader.JarResourceLoader'
        engine.setProperty 'jar.resource.loader.path', "jar:file:${jarFile.path}".toString()
        engine.init()
        return engine
    }
    void init(RuntimeServices rsvc) {/*println"init ${rsvc}"*/}
    void log(int level, String message) {/*println"log ${level} ${message}"*/}
    void log(int level, String message, Throwable t) {/*println "log ${level} ${message} ${t}"*/}
    boolean isLevelEnabled(int level) {/*println"isLevelEnabled ${level}";*/ return false }


    def prepareFilterProperties( request ) {
logger.error"props ${request.generationFilterProperties}"
        def filters = request.generationFilterProperties
        if( !filters.rootArtifactId ) {
            filters.rootArtifactId = filters.artifactId
        }
        filters.upArtifactId = filters.artifactId
        filters.'package' = filters.packageName

        return filters
    }

    def processPartialProject( processRequest ) {
        def pom = mergePom( processRequest )
        processFilesets processRequest
        processModules processRequest, pom
    }

    def processProject( processRequest ) {
        processRequest.outputDirectory.mkdirs()
        def pom = processPom( processRequest )
        processFilesets processRequest
        processModules processRequest, pom
    }
    def processModules( processRequest, pom ) {
        def moduleOffset = processRequest.moduleOffset
        def zipModuleOffset = processRequest.zipModuleOffset
        def outputDirectory = processRequest.outputDirectory
        def filterProperties = processRequest.filterProperties
        def upArtifactId = processRequest.filterProperties.artifactId
        processRequest.archetypeDescriptor.modules.module.each { module ->
//            println"${Thread.currentThread().name} ${module?.dump()}"
            def artifactId = getOutputName( module.@artifactId.text(), processRequest.filterProperties )
            def moduleRequest = processRequest.clone()
            moduleRequest.moduleOffset = "${moduleOffset ? moduleOffset + '/' : ''}${artifactId}"
            moduleRequest.zipModuleOffset = "${zipModuleOffset ? zipModuleOffset + '/' : ''}${module.@artifactId.text()}"
            moduleRequest.outputDirectory = new File( outputDirectory, artifactId )
            moduleRequest.archetypeDescriptor = module
            moduleRequest.updirPom = pom
//            println"${Thread.currentThread().name} artifactId ${artifactId}"
//            println"${Thread.currentThread().name} moduleRequest.moduleOffset ${moduleRequest.moduleOffset}"
//            println"${Thread.currentThread().name} moduleRequest.outputDirectory ${moduleRequest.outputDirectory}"

            def moduleFilter = filterProperties.clone()
            moduleFilter.upArtifactId = upArtifactId
            moduleFilter.artifactId = artifactId
            moduleRequest.filterProperties = moduleFilter

            if(!module.@nested.toBoolean()){
                logger.warn "Processing module ${module.@artifactId.text()} in ${moduleRequest.outputDirectory}"
                processProject moduleRequest
            } else {
                def archetype = processRequest.archetypes."${module.@archetypeGroupId.toString()}:${module.@archetypeArtifactId.toString()}:${module.@archetypeVersion.toString()}"

                logger.warn"Processing nested archetype ${module.@archetypeArtifactId.toString()} in ${outputDirectory}"
                def request = new ArchetypeGenerationRequest()
                request.archetypes = moduleRequest.archetypes
                request.archetypeFile = archetype.file
                request.outputDirectory = outputDirectory
                moduleRequest.filterProperties.remove 'upArtifactId'
                moduleRequest.filterProperties.remove 'rootArtifactId'
                request.generationFilterProperties = moduleRequest.filterProperties
//                request.archetypes = moduleRequest.archetypes
//                request.archetypes = moduleRequest.archetypes
//                request.archetypes = moduleRequest.archetypes
                archetype.generator.generateProject request
            }
        }
    }

    def mergePom( processRequest ) {
        def pomFile = new File( processRequest.outputDirectory, 'pom.xml' )
        Model initialModel = pomManager.loadModel( pomFile )

        StringWriter writer = new StringWriter()
        processRequest.velocity.mergeTemplate( archetypeFileResolver.getResourcePath( processRequest.zipModuleOffset, '', 'pom.xml' ), new VelocityContext( processRequest.filterProperties ), writer )
        def slurpedPom = new XmlSlurper().parseText( writer.toString() )
//logger.error "${Thread.currentThread().name} INITIAL POM ${pomFile}"
        def pomBuilder = new StreamingMarkupBuilder().bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace '':'http://maven.apache.org/POM/4.0.0'
            mkp.yield slurpedPom
        }
        Model newModel = pomManager.loadModel( pomBuilder.toString() )

        pomManager.writeModel pomFile, pomManager.mergeModel( initialModel, newModel )

        return pomFile
    }

    def processPom( processRequest ) {

        def pomFile = new File( processRequest.outputDirectory, 'pom.xml' )
//logger.error "${Thread.currentThread().name} Processing POM ${pomFile}"
        pomFile.parentFile.mkdirs()
        pomFile.withWriter {
            it.write archetypeFileResolver.getFile(
                processRequest.zipModuleOffset, '', 'pom.xml', processRequest.archetypeZipFile
            ).newReader().text
        }

//        def initialPomFile = archetypeFileResolver.getFile(
//            processRequest.zipModuleOffset, '', 'pom.xml', processRequest.archetypeZipFile
//        ).newReader()
//        def filteredPom = templateEngine.createTemplate( initialPomFile ).
//            make( processRequest.filterProperties )
        StringWriter writer = new StringWriter()
        processRequest.velocity.mergeTemplate( archetypeFileResolver.getResourcePath( processRequest.zipModuleOffset, '', 'pom.xml' ), new VelocityContext( processRequest.filterProperties ), writer )
        def slurpedPom = new XmlSlurper().parseText( writer.toString() )
//logger.error "${Thread.currentThread().name} INITIAL POM ${pomFile}"
        def pomBuilder = new StreamingMarkupBuilder().bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace '':'http://maven.apache.org/POM/4.0.0'
            mkp.yield slurpedPom
        }
        Model newModel = pomManager.loadModel( pomBuilder.toString() )

        if( processRequest.updirPom?.exists() ) {
            Model parentModel = pomManager.loadModel( processRequest.updirPom )
            if( !newModel.parent ) {
                newModel.parent = new Parent()
                newModel.parent.groupId = parentModel.groupId
                newModel.parent.artifactId = parentModel.artifactId
                newModel.parent.version = parentModel.version
            }
//logger.error "MODULES=${parentModel.modules}"
            if( !parentModel.modules.contains(newModel.artifactId) ) {
                parentModel.modules << newModel.artifactId
                pomManager.writeModel processRequest.updirPom, parentModel
            }
        }

        pomManager.writeModel pomFile, newModel

        return pomFile
    }


    def processFilesets( processRequest ) {

//logger.error "archetypeDescriptor ${processRequest.archetypeDescriptor.fileSets.dump()}"
//logger.error "archetypeDescriptor ${processRequest.archetypeDescriptor.fileSets.fileSet.dump()}"

//        Asynchronizer.withAsynchronizer {
            processRequest.archetypeDescriptor.fileSets.fileSet.each/*Async*/ { fileset ->
//println"A"
//logger.error"Thread ${Thread.currentThread().name}"
//    logger.error"A${fileset.@filtered.toBoolean()}"
//    logger.error"B${fileset.@packaged.toBoolean()}"
//    logger.error"C${fileset.directory}"
//    logger.error"D${fileset.includes.include.dump()}"

                def filesetResources = archetypeFileResolver.getFiles(
                    processRequest.zipModuleOffset, fileset, processRequest.archetypeResources )
//println"B"
//logger.error "FILESET== ${filesetResources}"

                def filesetRequest = processRequest.clone()
                filesetRequest.filesetResources = filesetResources
                filesetRequest.packaged = fileset.@packaged.toBoolean()
                filesetRequest.filesetDirectory = fileset.directory.text().toString()
                filesetRequest.engine = fileset.@engine?.toString()

//println"C"
                if( fileset.@filtered.toBoolean() )
                    processFileset filesetRequest
                else
                    copyFileset filesetRequest
            }
//        }
    }

    def processFileset( filesetRequest ) {

println"\n\n\n\n\nD"
logger.error "Processing Fileset ${filesetRequest.filesetDirectory}"
//        Asynchronizer.withAsynchronizer {
            VelocityContext context
            if( 'velocity' == filesetRequest.engine ) context = new VelocityContext( filesetRequest.filterProperties )

            filesetRequest.filesetResources.each { resource ->
println"E  ${resource}"
                def resourceFile = archetypeFileResolver.getFile(
                    filesetRequest.zipModuleOffset, filesetRequest.filesetDirectory, resource, filesetRequest.archetypeZipFile
                ).newReader()
                def text
                if( 'groovy' == filesetRequest.engine ) {
                    text = templateEngine.createTemplate( resourceFile ).make( filesetRequest.filterProperties )
println"F"
                } else {
                    StringWriter writer = new StringWriter()
                    filesetRequest.velocity.mergeTemplate( archetypeFileResolver.getResourcePath( filesetRequest.zipModuleOffset, filesetRequest.filesetDirectory, resource ), context, writer )
                    text = writer.toString()
println"G"
                }
    logger.error "RESULT=\n${text}"
                def path = getPath( filesetRequest.moduleOffset,
                                    filesetRequest.filesetDirectory,
                                    filesetRequest.packaged,
                                    filesetRequest.filterProperties,
                                    resource )
                def outName = getOutputName( path, filesetRequest.filterProperties )
                //templateEngine.createTemplate( path ).make( filesetRequest.filterProperties )
                def outputFile = new File( filesetRequest.outputDirectory, outName.toString() )
    logger.error "outputFile=${outputFile}"
                outputFile.parentFile.mkdirs()
                outputFile.withWriter {
                    it.write text
                }
//            }
        }
println"I"
    }

    def copyFileset( filesetRequest ) {

//logger.error "Processing Fileset ${filesetRequest.filesetDirectory}"
//        Asynchronizer.withAsynchronizer {
            filesetRequest.filesetResources.each { resource ->
                def resourceFile = archetypeFileResolver.getFile(
                    filesetRequest.zipModuleOffset, filesetRequest.filesetDirectory, resource, filesetRequest.archetypeZipFile )
                def path = getPath( filesetRequest.moduleOffset,
                                    filesetRequest.filesetDirectory,
                                    filesetRequest.packaged,
                                    filesetRequest.filterProperties,
                                    resource )
                def outName = getOutputName( path, filesetRequest.filterProperties )
                //templateEngine.createTemplate( path ).make( filesetRequest.filterProperties )
                def outputFile = new File( filesetRequest.outputDirectory, outName.toString() )
//    logger.error "outputFile=${outputFile}"
                outputFile.parentFile.mkdirs()
                outputFile.withOutputStream {
                    it << resourceFile
                }
            }
//        }
    }

    def getPath( moduleOffset, filesetDirectory, packaged, filterProperties, resource ) {
        def offset = moduleOffset ? moduleOffset + '/' : ''
        def packageName = packaged ? filterProperties.packageName.replace( '.', '/' ) + '/' : ''
        return "${offset}${filesetDirectory}/${packageName}${resource}"
    }


    def getOutputName( path, filterProperties ) {
        def outputName = path.toString()
        def log = "${Thread.currentThread().name} ${outputName}"
        filterProperties.each { key, value ->
            outputName = outputName.replaceAll( /\$\{${key}}/, value.toString() )
            log+= " (${key}=${value.toString()})-> ${outputName}"
        }
//        println "${log} = ${outputName}"
        return outputName
    }


}

