package org.apache.maven.archetype.generator

import org.junit.Test
import static org.junit.Assert.*
import org.apache.maven.archetype.ArchetypeGenerationRequest
import org.apache.maven.archetype.ArchetypeGenerationResult
import org.apache.maven.archetype.generator.DefaultFilesetArchetypeGenerator
import org.apache.maven.archetype.pom.PomManager
import org.apache.maven.artifact.repository.DefaultArtifactRepository
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout

import org.codehaus.plexus.PlexusTestCase
import org.apache.maven.plugin.testing.AbstractMojoTestCase
import org.codehaus.plexus.util.FileUtils


/**
 *
 * @author rafale
 */
class DefaultArchetypeFilesetGeneratorTest extends AbstractMojoTestCase {
    def project
    def archetypeFile
    def outputDirectory
    def projectDirectory
    def generator
    def generationRequest
    def pomManager

    void setUp() {
        super.setUp()
        generator = lookup(FilesetArchetypeGenerator.ROLE, 'fileset')
        pomManager = lookup(PomManager.ROLE)
    }

    void tearDown() {super.tearDown()}

    @Test void test_generateMonoModuleProjectWithNoParent() {
        println 'generateMonoModuleProjectWithNoParent'
        prepareTest project: 'generateMonoModuleProjectWithNoParent', artifactId: 'file-value', archetype: 'basic'
        generationRequest = new ArchetypeGenerationRequest(
                archetypeFile: archetypeFile,
                outputDirectory: outputDirectory,
                generationFilterProperties: [
                        groupId: 'file-value',
                        artifactId: 'file-value',
                        version: 'file-value',
                        packageName: 'file.value.package',
                        propertyWithDefault: 'file-value',
                        propertyWithoutDefault: 'file-value'] as Properties
        )
        generator.generateProject generationRequest

        assertTemplateContent 'src/main/java/file/value/package/App.java'
        assertCopyContent 'src/main/resources/images/foo.png'
        def pom = pomManager.loadModel(new File(projectDirectory, 'pom.xml'))
        assertEquals 'file-value', pom.groupId
        assertEquals 'file-value', pom.artifactId
        assertEquals 'file-value', pom.version
    }

    @Test void test_generateMonoModuleProjectParentFromArchetype() {
        println 'generateMonoModuleProjectParentFromArchetype'
        prepareTest project: 'generateMonoModuleProjectParentFromArchetype', artifactId: 'file-value', archetype: 'basic-with-parent'
        generationRequest = new ArchetypeGenerationRequest(
                archetypeFile: archetypeFile,
                outputDirectory: outputDirectory,
                generationFilterProperties: [
                        groupId: 'file-value',
                        artifactId: 'file-value',
                        version: 'file-value',
                        packageName: 'file.value.package',
                        propertyWithDefault: 'file-value',
                        propertyWithoutDefault: 'file-value'] as Properties
        )
        generator.generateProject generationRequest

        assertTemplateContent 'src/main/java/file/value/package/App.java'
        assertCopyContent 'src/main/resources/images/foo.png'
        def pom = pomManager.loadModel(new File(projectDirectory, 'pom.xml'))
        assertNotNull pom.parent
        assertEquals 'parent-doesnt-exist', pom.parent.artifactId
        assertEquals 'file-value', pom.groupId
        assertEquals 'file-value', pom.artifactId
        assertEquals 'file-value', pom.version
        def parentPom = pomManager.loadModel(new File(outputDirectory, 'pom.xml'))
        assertEquals 1, parentPom.modules.size()
    }

    @Test void test_generateMonoModuleProjectParentFromDirectory() {
        println 'generateMonoModuleProjectParentFromDirectory'
        prepareTest project: 'generateMonoModuleProjectParentFromDirectory', artifactId: 'file-value', archetype: 'basic'
        generationRequest = new ArchetypeGenerationRequest(
                archetypeFile: archetypeFile,
                outputDirectory: outputDirectory,
                generationFilterProperties: [
                        groupId: 'file-value',
                        artifactId: 'file-value',
                        version: 'file-value',
                        packageName: 'file.value.package',
                        propertyWithDefault: 'file-value',
                        propertyWithoutDefault: 'file-value'] as Properties
        )
        generator.generateProject generationRequest

        assertTemplateContent 'src/main/java/file/value/package/App.java'
        assertCopyContent 'src/main/resources/images/foo.png'
        def pom = pomManager.loadModel(new File(projectDirectory, 'pom.xml'))
        assertNotNull pom.parent
        assertEquals 'generateMonoModuleProjectParentFromDirectory', pom.parent.artifactId
        assertEquals 'file-value', pom.groupId
        assertEquals 'file-value', pom.artifactId
        assertEquals 'file-value', pom.version
        def parentPom = pomManager.loadModel(new File(outputDirectory, 'pom.xml'))
        assertEquals 1, parentPom.modules.size()
    }

    @Test void test_generateMultiModuleProjectWithNoParent() {
        println 'generateMultiModuleProjectWithNoParent'
        prepareTest project: 'generateMultiModuleProjectWithNoParent', artifactId: 'foobar', archetype: 'multi'
        generationRequest = new ArchetypeGenerationRequest(
                archetypeFile: archetypeFile,
                outputDirectory: outputDirectory,
                generationFilterProperties: [
                        groupId: 'file-value',
                        artifactId: 'foobar',
                        version: 'file-value',
                        packageName: 'file.value.package',
                        propertyWithDefault: 'file-value',
                        propertyWithoutDefault: 'file-value'] as Properties
        )
        generator.generateProject generationRequest

        def pom = pomManager.loadModel(new File(projectDirectory, 'pom.xml'))
        assertEquals 'file-value', pom.groupId
        assertEquals 'foobar', pom.artifactId
        assertEquals 'file-value', pom.version

        def innestPom = pomManager.loadModel(new File(projectDirectory, "foobar-c/foobar-e/foobar-c-h/pom.xml"))
        assertEquals 'file-value', innestPom.parent.groupId
        assertEquals 'foobar-e', innestPom.parent.artifactId
        assertEquals 'foobar-c-h', innestPom.artifactId
        assertEquals 'file-value', innestPom.version
    }

    @Test void test_generateMultiModuleProjectWithParentFromArchetype() {
        println 'generateMultiModuleProjectWithParentFromArchetype'
        prepareTest project: 'generateMultiModuleProjectWithParentFromArchetype', artifactId: 'foobar', archetype: 'multi-with-parent'
        generationRequest = new ArchetypeGenerationRequest(
                archetypeFile: archetypeFile,
                outputDirectory: outputDirectory,
                generationFilterProperties: [
                        groupId: 'file-value',
                        artifactId: 'foobar',
                        version: 'file-value',
                        packageName: 'file.value.package',
                        propertyWithDefault: 'file-value',
                        propertyWithoutDefault: 'file-value'] as Properties
        )
        generator.generateProject generationRequest

        def pom = pomManager.loadModel(new File(projectDirectory, 'pom.xml'))
        assertNotNull pom.parent
        assertEquals 'parent-doesnt-exist', pom.parent.artifactId
        assertEquals 'file-value', pom.groupId
        assertEquals 'foobar', pom.artifactId
        assertEquals 'file-value', pom.version
        def parentPom = pomManager.loadModel(new File(outputDirectory, 'pom.xml'))
        assertEquals 1, parentPom.modules.size

        def innestPom = pomManager.loadModel(new File(projectDirectory, "foobar-c/foobar-e/foobar-c-h/pom.xml"))
        assertEquals 'file-value', innestPom.parent.groupId
        assertEquals 'foobar-e', innestPom.parent.artifactId
        assertEquals 'foobar-c-h', innestPom.artifactId
        assertEquals 'file-value', innestPom.version
    }

    @Test void test_generateMultiModuleProjectWithParentFromDirectory() {
        println 'generateMultiModuleProjectWithParentFromDirectory'
        prepareTest project: 'generateMultiModuleProjectWithParentFromDirectory', artifactId: 'foobar', archetype: 'multi'
        generationRequest = new ArchetypeGenerationRequest(
                archetypeFile: archetypeFile,
                outputDirectory: outputDirectory,
                generationFilterProperties: [
                        groupId: 'file-value',
                        artifactId: 'foobar',
                        version: 'file-value',
                        packageName: 'file.value.package',
                        propertyWithDefault: 'file-value',
                        propertyWithoutDefault: 'file-value'] as Properties
        )
        generator.generateProject generationRequest

        def pom = pomManager.loadModel(new File(projectDirectory, 'pom.xml'))
        assertNotNull pom.parent
        assertEquals 'generateMultiModuleProjectWithParentFromDirectory', pom.parent.artifactId
        assertEquals 'file-value', pom.groupId
        assertEquals 'foobar', pom.artifactId
        assertEquals 'file-value', pom.version
        def parentPom = pomManager.loadModel(new File(outputDirectory, 'pom.xml'))
        assertEquals 1, parentPom.modules.size()

        def innestPom = pomManager.loadModel(new File(projectDirectory, "foobar-c/foobar-e/foobar-c-h/pom.xml"))
        assertEquals 'file-value', innestPom.parent.groupId
        assertEquals 'foobar-e', innestPom.parent.artifactId
        assertEquals 'foobar-c-h', innestPom.artifactId
        assertEquals 'file-value', innestPom.version
    }

    private void prepareTest(args) {
        project = args.project
        outputDirectory = "${basedir}/target/projects/$project"
        projectDirectory = new File(outputDirectory, args.artifactId)
        assertDeleted projectDirectory
        archetypeFile = new File("${basedir}/target/repositories/central/archetypes/${args.archetype}/1.0/${args.archetype}-1.0.jar")
    }

    private void assertDeleted(file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                try {
                    FileUtils.deleteDirectory file
                } catch (IOException e) {
                    fail "Unable to delete directory:$file:${e.getLocalizedMessage()}"
                }
            }
        } else {
            try {
                FileUtils.forceDelete file
            } catch (IOException e) {
                fail "Unable to delete file:$file:${e.getLocalizedMessage()}"
                e.printStackTrace()
            }
        }
        if (file.exists()) {
            fail "File not deleted:$file"
        }
    }

    private def loadProperties(template) {
        def templateFile = new File(projectDirectory, template)
        if (!templateFile.exists()) fail "Missing File:$templateFile"

        Properties properties = new Properties()
        properties.load new FileInputStream(templateFile)
        return properties
    }

    private void assertTemplateContent(template) {
        Properties properties = loadProperties(template)
        assertEquals "file-value", properties["groupId"]
        assertEquals "file-value", properties["artifactId"]
        assertEquals "file-value", properties["version"]
        assertEquals "file.value.package", properties["package"]
        assertEquals "file-value", properties["propertyWithDefault"]
        assertEquals "file-value", properties["propertyWithoutDefault"]

        assertEquals "file/value/package", properties["packageReplaced"]
        assertEquals "file/value/package", properties["packageReplaced2"]
        assertEquals '${packageName.replace(\'.\', \'/\')}', properties["ignoredDollar"]
        assertEquals "<%=packageName.replace('.', '/')%>", properties["ignoredInferior"]
    }

    private void assertCopyContent(template) {
        Properties properties = loadProperties(template)
        assertEquals '${groupId}', properties["groupId"]
        assertEquals '${artifactId}', properties["artifactId"]
        assertEquals '${version}', properties["version"]
        assertEquals '${packageName}', properties["package"]
        assertEquals '${propertyWithDefault}', properties["propertyWithDefault"]
        assertEquals '${propertyWithoutDefault}', properties["propertyWithoutDefault"]

        assertEquals '${packageName.replace(\'.\', \'/\')}', properties["packageReplaced"]
        assertEquals "<%=packageName.replace('.', '/')%>", properties["packageReplaced2"]
        assertEquals '${packageName.replace(\'.\', \'/\')}', properties["ignoredDollar"]
        assertEquals '${\'<\'}%=packageName.replace(\'.\', \'/\')%>', properties["ignoredInferior"]
    }
}

