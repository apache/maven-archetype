package org.apache.maven.archetype.pom
import org.apache.maven.model.Model

/**
 *
 * @author rafale
 */
interface PomManager {
    String ROLE = 'org.apache.maven.archetype.pom.PomManager'

    def loadModel( File pomFile )
    def loadModel( String pomString )
    void writeModel( File pomFile, Model newModel )
    def mergeModel( Model initialModel, Model addedModel )
}

