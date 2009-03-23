package org.apache.maven.archetype.pom
import org.codehaus.plexus.logging.AbstractLogEnabled
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.Model
import org.jdom.input.SAXBuilder
import org.jdom.Document
import org.jdom.output.Format
import org.apache.maven.model.io.jdom.MavenJDOMWriter
import org.apache.maven.model.Build
import org.apache.maven.model.Reporting
import org.apache.maven.model.ModelBase
import org.apache.maven.model.DependencyManagement

/**
 *
 * @author rafale
 * @plexus.component
 */
class DefaultPomManager extends AbstractLogEnabled implements PomManager {

    def loadModel( File pomFile ) {
        def model
        pomFile.newReader( "UTF-8" ).withReader {
            model = new MavenXpp3Reader().read( it, false )
        }
        return model
    }

    def loadModel(String pomString){
        def model
        new StringReader( pomString ).withReader {
            model = new MavenXpp3Reader().read( it, false )
        }
        return model
    }

    void writeModel( File pomFile, Model newModel ) {
        def pomDocument
        pomFile.newInputStream().withStream {
            pomDocument = new SAXBuilder().build( it )
        }

        def encoding = newModel.modelEncoding?: 'UTF-8'
        pomFile.newWriter( encoding ).withWriter {
            new MavenJDOMWriter().write( newModel, pomDocument, it, Format.rawFormat.setEncoding( encoding ) )
        }
    }

    def mergeModel( Model initialModel, Model addedModel ) {

        println"merge "+initialModel?.dependencies.dump()
        println"mergeX "+initialModel?.properties.dump()


//        // Potential merging
//
//        initialModel.getModelEncoding (); // keep initial
//        initialModel.getModelVersion (); // keep initial
//
//        initialModel.getGroupId (); // keep initial
//        initialModel.getArtifactId (); // keep initial
//        initialModel.getVersion (); // keep initial
//        initialModel.getParent (); // keep initial
//
//        initialModel.getId (); // keep initial
//        initialModel.getName (); // keep initial
//        initialModel.getInceptionYear (); // keep initial
//        initialModel.getDescription (); // keep initial
//        initialModel.getUrl (); // keep initial
//        initialModel.getLicenses (); // keep initial
//        initialModel.getProperties (); // merged
        mergeProperties initialModel, addedModel
//
//        initialModel.getOrganization (); // keep initial
//        initialModel.getMailingLists (); // keep initial
//        initialModel.getContributors (); // keep initial
//        initialModel.getDevelopers (); // keep initial
//
//        initialModel.getScm (); // keep initial
//        initialModel.getCiManagement (); // keep initial
//        initialModel.getDistributionManagement (); // keep initial
//        initialModel.getIssueManagement (); // keep initial
//
//        initialModel.getPackaging (); // keep initial
//        initialModel.getDependencies (); // merged
        mergeDependencies initialModel, addedModel
//        initialModel.getDependencyManagement (); // merged
        mergeDependencyManagement initialModel, addedModel
//        initialModel.getPrerequisites ().getMaven (); // keep initial
//        initialModel.getPrerequisites ().getModelEncoding (); // keep initial
//
//        initialModel.getModules (); // keep initial
//        initialModel.getRepositories (); // merged
        mergeRepositories initialModel, addedModel
//        initialModel.getPluginRepositories (); // merged
        mergePluginRepositories initialModel, addedModel
//
//        initialModel.getBuild ().getDefaultGoal (); // keep initial
//        initialModel.getBuild ().getFinalName (); // keep initial
//        initialModel.getBuild ().getModelEncoding (); // keep initial
//        initialModel.getBuild ().getFilters ();
//        initialModel.getBuild ().getDirectory (); // keep initial
//        initialModel.getBuild ().getOutputDirectory (); // keep initial
//        initialModel.getBuild ().getSourceDirectory (); // keep initial
////////        initialModel.getBuild ().getResources ();
//        initialModel.getBuild ().getScriptSourceDirectory (); // keep initial
//        initialModel.getBuild ().getTestOutputDirectory (); // keep initial
////////        initialModel.getBuild ().getTestResources ();
//        initialModel.getBuild ().getTestSourceDirectory (); // keep initial
////////        initialModel.getBuild ().getExtensions ();
//        initialModel.getBuild ().getPluginsAsMap (); // merged
        mergeBuildPlugins initialModel, addedModel
//        initialModel.getBuild ().getPluginManagement (); // merged
        mergeBuildPluginManagement initialModel, addedModel
//
//        initialModel.getReporting ().getModelEncoding (); // keep initial
//        initialModel.getReporting ().getOutputDirectory (); // keep initial
//        initialModel.getReporting ().getReportPluginsAsMap (); // merged
        mergeReportPlugins initialModel, addedModel

////////        initialModel.getProfiles ();

        return initialModel
    }

    def mergeProperties( Model initialModel, Model addedModel ) {
        if( !addedModel.properties || !addedModel.properties.size() == 0 ) return
        if( !initialModel.properties ) initialModel.properties = addedModel.properties
        else {
            addedModel.properties.each { key, value ->
                if( initialModel.properties.containsKey( key ) ) {
                    logger.warn "property $key not over written"
                } else {
                    initialModel.properties[key] = value
                }
            }
        }
    }

    def mergeDependencies( Model initialModel, Model addedModel ) {
        if( !addedModel.dependencies || addedModel.dependencies.size() == 0 ) return
        if( !initialModel.dependencies ) initialModel.dependencies = addedModel.dependencies
        else {
            def initialDependencies = getDependencyMap( initialModel.dependencies )
            getDependencyMap( addedModel.dependencies ).each { key, dependency ->
                if( initialDependencies.containsKey( key ) ) {
                    logger.warn "dependency $key not over written"
                } else {
                    initialModel.dependencies.add dependency
                }
            }
        }
    }
    def getDependencyMap( dependencies ) {
        def map = [:]
        dependencies.each {
            map."${it.groupId}:${it.artifactId}" = it
        }
        return map
    }

    def mergeDependencyManagement( Model initialModel, Model addedModel ) {
        if( !addedModel.dependencyManagement || 
            !addedModel.dependencyManagement.dependencies ||
            addedModel.dependencyManagement.dependencies.size() == 0 ) return
        if( !initialModel.dependencyManagement ) initialModel.dependencyManagement = new DependencyManagement()
        if( !initialModel.dependencyManagement.dependencies ) initialModel.dependencyManagement.dependencies = addedModel.dependencyManagement.dependencies
        else {
            def initialDependencies = getDependencyMap( initialModel.dependencyManagement.dependencies )
            getDependencyMap( addedModel.dependencyManagement.dependencies ).each { key, dependency ->
                if( initialDependencies.containsKey( key ) ) {
                    logger.warn "dependency $key in dependencyManagement not over written"
                } else {
                    initialModel.dependencyManagement.dependencies.add dependency
                }
            }
        }
    }

    def mergeRepositories( Model initialModel, Model addedModel ) {
        if( !addedModel.repositories || addedModel.repositories.size() == 0 ) return
        if( !initialModel.repositories ) initialModel.repositories = addedModel.repositories
        else {
            def initialRepositories = getRepositoryMap( initialModel.repositories )
            getRepositoryMap( addedModel.repositories ).each { key, repository ->
                if( initialRepositories.containsKey( key ) ) {
                    logger.warn "repository $key not over written"
                } else {
                    initialModel.repositories.add repository
                }
            }
        }
    }
    def getRepositoryMap( repositories ) {
        def map = [:]
        repositories.each {
            map."${it.id}" = it
        }
        return map
    }

    def mergePluginRepositories( Model initialModel, Model addedModel ) {
        if( !addedModel.pluginRepositories || addedModel.pluginRepositories.size() == 0 ) return
        if( !initialModel.pluginRepositories ) initialModel.pluginRepositories = addedModel.pluginRepositories
        else {
            def initialPluginRepositories = getRepositoryMap( initialModel.pluginRepositories )
            getRepositoryMap( addedModel.pluginRepositories ).each { key, pluginRepository ->
                if( initialPluginRepositories.containsKey( key ) ) {
                    logger.warn "pluginRepository $key not over written"
                } else {
                    initialModel.pluginRepositories.add pluginRepository
                }
            }
        }
    }

    def mergeBuildPlugins( Model initialModel, Model addedModel ) {
        if( !addedModel.build ) return
        if( !addedModel.build.pluginsAsMap || addedModel.build.pluginsAsMap.size() == 0 ) return
        if( !initialModel.build ) initialModel.build = new Build()
        def initialPlugins = initialModel.build?.pluginsAsMap?:[:]        
        addedModel.build.pluginsAsMap.each { key, plugin ->
            if( initialPlugins.containsKey( key ) ) {
                logger.warn "plugin $key not over written"
            } else {
                initialModel.build.addPlugin plugin
            }
        }
    }

    def mergeBuildPluginManagement( Model initialModel, Model addedModel ) {
        if( !addedModel.build ) return
        if( !addedModel.build.pluginManagement ) return
        if( !addedModel.build.pluginManagement.plugins || addedModel.build.pluginManagement.plugins.size() == 0 ) return
        if( !initialModel.build ) initialModel.build = new Build()
        if( !initialModel.build.pluginManagement ) initialModel.build.pluginManagement = addedModel.build.pluginManagement
        else {
            def initialPlugins = getDependencyMap( initialModel.build.pluginManagement.plugins )
            getDependencyMap( addedModel.build.pluginManagement.plugins ).each { key, plugin ->
                if( initialPlugins.containsKey( key ) ) {
                    logger.warn "plugin $key in pluginManagement not over written"
                } else {
                    initialModel.build.pluginManagement.plugins.add plugin
                }
            }
        }

    }

    def mergeReportPlugins( Model initialModel, Model addedModel ) {
        if( !addedModel.reporting ) return
        if( !addedModel.reporting.reportPluginsAsMap || addedModel.reporting.reportPluginsAsMap.size() == 0 ) return
        if( !initialModel.reporting ) initialModel.reporting = new Reporting()
        def initialReportPlugins = initialModel.reporting.reportPluginsAsMap?:[:]

        addedModel.reporting.reportPluginsAsMap.each { key, plugin ->
            if( initialReportPlugins.containsKey( key ) ) {
                logger.warn "report plugin $key not over written"
            } else {
                initialModel.reporting.addPlugin plugin
            }
        }
    }
}

