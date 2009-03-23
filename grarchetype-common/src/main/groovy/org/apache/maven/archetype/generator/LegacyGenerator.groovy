package org.apache.maven.archetype.generator
import groovy.xml.MarkupBuilder

/**
 *
 * @author raphaelpieroni
 * @plexus.component role-hint="legacy"
 */
class LegacyGenerator
extends AbstractFilesetArchetypeGenerator {
    def loadArchetypeDescriptor( File archetypeFile ) {println"LegacyGenerator ${archetypeFile}"
        def oldDescriptor = new XmlSlurper().parse( archetypeArtifactManager.getLegacyArchetypeDescriptor( archetypeFile ) )

        def filesetDescriptor = new StringWriter()
        def filesetDescriptorBuilder = new MarkupBuilder( filesetDescriptor )

        filesetDescriptorBuilder.'archetype-descriptor'( name:oldDescriptor.id.toString(), partial:getPartial(oldDescriptor) ) {
            fileSets {
                oldDescriptor?.sources?.source.each { source ->
                    fileSet( engine:'velocity', packaged:true, filtered:true ) {
                        directory getSourceDirectory( source.toString() )
                        includes {
                            include getSourcePath( source.toString() )
                        }
                    }
                }
                oldDescriptor?.testSources?.source.each { source ->
                    fileSet( engine:'velocity', packaged:true, filtered:true ) {
                        directory getSourceDirectory( source.toString() )
                        includes {
                            include getSourcePath( source.toString() )
                        }
                    }
                }
                oldDescriptor?.resources?.resource.each { resource ->
                    fileSet( engine:'velocity', packaged:false, filtered:getFiltered( resource?.@filtered.toString() ) ) {
                        directory getResourceDirectory( resource.toString() )
                        includes {
                            include getResourcePath( resource.toString() )
                        }
                    }
                }
                oldDescriptor?.siteResources?.resource.each { resource ->
                    fileSet( engine:'velocity', packaged:false, filtered:getFiltered( resource?.@filtered.toString() ) ) {
                        directory getResourceDirectory( resource.toString() )
                        includes {
                            include getResourcePath( resource.toString() )
                        }
                    }
                }
            }
        }

//        filesetDescriptor = filesetDescriptor.toString()
        logger.error"Using translated descriptor ${filesetDescriptor.toString()}"

        new XmlSlurper().parseText( filesetDescriptor.toString() )
    }

    def getPartial( oldDescriptor ) {
        if(oldDescriptor.allowPartial.toString()) return oldDescriptor.allowPartial.toString()
        return false
    }

    def getSourceDirectory( source ) {
        source.substring 0, source.lastIndexOf( 'java/' ) + 4
    }

    def getResourceDirectory( source ) {
        source.substring 0, source.lastIndexOf( '/' )
    }

    def getSourcePath( source ) {
        source.substring source.lastIndexOf( 'java/' ) + 5
    }

    def getResourcePath( source ) {
        source.substring source.lastIndexOf( '/' ) + 1
    }

    def getFiltered( filtered ) {
        filtered ? filtered : true
    }
}

