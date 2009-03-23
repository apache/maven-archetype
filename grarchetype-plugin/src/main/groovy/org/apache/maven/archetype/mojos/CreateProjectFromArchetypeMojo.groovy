package org.apache.maven.archetype.mojos
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.ContextEnabled
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.archetype.Archetype
import org.apache.maven.archetype.ArchetypeGenerationRequest

import org.apache.maven.execution.MavenSession
import org.apache.maven.archetype.ui.ArchetypeSelector
import org.apache.maven.archetype.ui.ArchetypeGenerationConfigurator

/**
 *
 * @author rafale
 * @requiresProject false
 * @goal generate
 */
class CreateProjectFromArchetypeMojo
extends AbstractMojo
implements ContextEnabled {

    /** @component */
    private Archetype archetype
    /** @component */
    private ArchetypeSelector selector
    /** @component */
    private ArchetypeGenerationConfigurator configurator


    /**
     * The archetype's artifactId.
     *
     * @parameter expression="${archetypeArtifactId}"
     */
    private String archetypeArtifactId

    /**
     * The archetype's groupId.
     *
     * @parameter expression="${archetypeGroupId}"
     */
    private String archetypeGroupId

    /**
     * The archetype's version.
     *
     * @parameter expression="${archetypeVersion}"
     */
    private String archetypeVersion

    /**
     * The archetype's catalogs.
     * It is a comma separated list of catalogs.
     * Catalogs use scheme:
     * - 'file://...' with archetype-catalog.xml automatically appended when defining a directory
     * - 'http://...' with archetype-catalog.xml always appended
     * - 'local' which is the shortcut for 'file://~/.m2/archetype-catalog.xml'
     * - 'remote' which is the shortcut for 'http://repo1.maven.org/maven2'
     * - 'internal' which is an internal catalog
     *
     * @parameter expression="${archetypeCatalog}" default-value="internal,local"
     */
    private String archetypeCatalog

    /**
     * User settings use to check the interactiveMode.
     *
     * @parameter expression="${interactiveMode}" default-value="${settings.interactiveMode}"
     * @required
     */
    private Boolean interactiveMode

    /** @parameter expression="${basedir}" */
    private File basedir

    /**
     * @parameter expression="${session}"
     * @readonly
     */
    MavenSession session

    public void execute()
    throws MojoExecutionException, MojoFailureException {

        println"\n\n session ${session.dump()}"
        println"\n\n container ${session.container.dump()}"
        println"\n\n currentProject ${session.currentProject.dump()}"
        println"\n\n eventDispatcher ${session.eventDispatcher.dump()}"
        println"\n\n executionProperties ${session.executionProperties.dump()}"
        println"\n\n executionRootDir ${session.executionRootDir.dump()}"
        println"\n\n goals ${session.goals.dump()}"
        println"\n\n localRepository ${session.localRepository.dump()}"
        println"\n\n reactorManager ${session.reactorManager.dump()}"
        println"\n\n settings ${session.settings.dump()}"
        println"\n\n startTime ${session.startTime.dump()}"
        println"\n\n usingPOMsFromFilesystem ${session.usingPOMsFromFilesystem.dump()}"
//        println"session ${session.dump()}"
//        println"session ${session.dump()}"
//        println"session ${session.dump()}"
//        println"session ${session.dump()}"
//        println"session ${session.dump()}"


        def request = new ArchetypeGenerationRequest(
            groupId : archetypeGroupId,
            artifactId : archetypeArtifactId,
            version : archetypeVersion,
            outputDirectory : basedir.absolutePath,
            repositories : session.currentProject.remoteArtifactRepositories,
            localRepository : session.localRepository
        )
        selector.selectArchetype request, interactiveMode, archetypeCatalog
        configurator.configureArchetype request, interactiveMode, session.executionProperties

        def result = archetype.generateProjectFromArchetype( request )


        println "RESULT=${result?.dump()}"
        result?.cause?.printStackTrace()

    }

}

