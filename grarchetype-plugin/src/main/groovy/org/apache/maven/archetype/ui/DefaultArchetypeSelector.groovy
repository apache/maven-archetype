package org.apache.maven.archetype.ui
import org.apache.maven.archetype.ArchetypeGenerationRequest
import org.apache.maven.archetype.catalog.ArchetypeCataloger
import org.codehaus.plexus.logging.AbstractLogEnabled
import org.codehaus.plexus.components.interactivity.Prompter
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
import org.apache.maven.artifact.repository.DefaultArtifactRepository
import org.apache.maven.archetype.util.RepositoryCreator

/**
 *
 * @author raphaelpieroni
 * @plexus.component
 */
class DefaultArchetypeSelector
extends AbstractLogEnabled
implements ArchetypeSelector {
    static DEFAULT_ARCHETYPE = [groupId:"org.apache.maven.archetypes", artifactId:"maven-archetype-quickstart"]

    /** @plexus.requirement */
    private RepositoryCreator repositoryCreator

    /** @plexus.requirement */
    private ArchetypeCataloger archetypeCataloger

    /** @plexus.requirement role-hint="archetype" */
    private Prompter prompter
    
    void selectArchetype( ArchetypeGenerationRequest request, Boolean interactiveMode, String catalogs ) {
        def definition = [groupId:request.groupId, artifactId:request.artifactId, version:request.version]

        def archetypeCatalogs = archetypeCataloger.getArchetypesByCatalog( catalogs )
        def catalogArchetype
        def repositories = request.repositories.clone()

        if( archetypeDefined( definition ) ) {
            catalogArchetype = archetypeCataloger.searchArchetype( definition, archetypeCatalogs )
            if( catalogArchetype ) repositories.add repositoryCreator.createRepository( catalogArchetype.repository )
            if( archetypeCataloger.archetypeRecursivelyExists( catalogArchetype, archetypeCatalogs, request.localRepository, repositories ) ) {
                request.repositories = repositories
                logger.info "Using defined archetype $definition"
                return
            } else {
                logger.error "Defined archetype $definition not found"
                throw new RuntimeException( "Defined archetype $definition not found" )
            }
        } else if( !interactiveMode ) {
            catalogArchetype = archetypeCataloger.searchArchetype( DEFAULT_ARCHETYPE, archetypeCatalogs )
            if( catalogArchetype ) repositories.add repositoryCreator.createRepository( catalogArchetype.repository )
            if( archetypeCataloger.archetypeRecursivelyExists( catalogArchetype, archetypeCatalogs, request.localRepository, request.repositories ) ) {
                request.repositories = repositories
                def archetype = archetypeCataloger.searchArchetype( catalogArchetype, archetypeCatalogs )
                request.groupId = archetype.groupId
                request.artifactId = archetype.artifactId
                request.version = archetype.version
                logger.info "Using default archetype $DEFAULT_ARCHETYPE"
                return
            } else {
                logger.error "Default archetype $DEFAULT_ARCHETYPE not found"
                throw new RuntimeException( "Default archetype $DEFAULT_ARCHETYPE not found" )
            }
        } else {
            catalogArchetype = askForDefinition( archetypeCatalogs )
            if( catalogArchetype ) repositories.add repositoryCreator.createRepository( catalogArchetype.repository )
            if( archetypeCataloger.archetypeRecursivelyExists( catalogArchetype, archetypeCatalogs, request.localRepository, request.repositories ) ) {
                request.repositories = repositories
                def archetype = archetypeCataloger.searchArchetype( catalogArchetype, archetypeCatalogs )
                request.groupId = archetype.groupId
                request.artifactId = archetype.artifactId
                request.version = archetype.version
                logger.info "Using selected archetype $catalogArchetype"
                return
            } else {
                logger.error "Selected archetype $catalogArchetype not found"
                throw new RuntimeException( "Selected archetype $catalogArchetype not found" )
            }
        }
    }

    def archetypeDefined( definition ) {
        return definition?.groupId && definition?.artifactId && definition?.version
    }
    def askForDefinition( archetypeCatalogs ) {
        def chooseArchetypeQuery = "Choose archetype:\n"
        def chooseArchetypeList =[]
        def archetypeAnswerMap = [:]
        def archetypeReverseAnswerMap = [:]
        def archetypeVersions
        def counter = 1
        def defaultArchetype = 0
        archetypeCatalogs.each { key, archetype ->
            def archetypeKey = "${archetype.groupId}:${archetype.artifactId}".toString()
            def answerKey = "$counter".toString()
            if( archetypeReverseAnswerMap.containsKey( archetypeKey ) ) {
                archetypeVersions = archetypeAnswerMap[ archetypeReverseAnswerMap[archetypeKey] ]
            } else {
                archetypeVersions = []
                archetypeAnswerMap[answerKey] = archetypeVersions
                chooseArchetypeQuery += "$answerKey:${archetype.catalog} -> ${archetype.artifactId} (${archetype.description})\n"
                chooseArchetypeList.add answerKey
                if( archetypeKey == DEFAULT_ARCHETYPE ) {
                    defaultArchetype = answerKey
                }
                archetypeReverseAnswerMap[archetypeKey] = answerKey
                counter ++
            }
            archetypeVersions.add archetype
        }
        chooseArchetypeQuery += "choose a number: "

        def chooseArchetypeAnswer
        if( !defaultArchetype ) {
            chooseArchetypeAnswer = prompter.prompt( chooseArchetypeQuery, chooseArchetypeList )
        } else {
            chooseArchetypeAnswer = prompter.prompt( chooseArchetypeQuery, chooseArchetypeList, defaultArchetype )
        }

        archetypeVersions = archetypeAnswerMap[chooseArchetypeAnswer]
        if( archetypeVersions.size() == 1 ) {
            return archetypeVersions[0]
        } else {
            return askForVersion( archetypeVersions )
        }
    }
    def askForVersion( archetypes )
    {
        def chooseVersionQuery = "Choose version: \n"
        List chooseVersionList = []
        def versionAnswerMap = [:]

        def counter = 1
        archetypes.each { archetype ->
            def answerKey = "$counter".toString()
            versionAnswerMap.put answerKey, archetype
            chooseVersionList.add answerKey
            chooseVersionQuery += "$counter: ${archetype.version}\n"
            counter++
        }
        chooseVersionQuery += "Choose a number: "

        return versionAnswerMap[prompter.prompt( chooseVersionQuery, chooseVersionList )]
    }
}

