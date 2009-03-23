package org.apache.maven.archetype.catalog

/**
 *
 * @author raphaelpieroni
 */
interface ArchetypeCataloger {
    static String ROLE = 'org.apache.maven.archetype.catalog.ArchetypeCataloger'

    def loadCatalog( String catalog )
    
    def archetypeRecursivelyExists( archetype, archetypeDefinitions, localRepository, repositories )

    def getArchetypesByCatalog( catalogs )

    def searchArchetype( definition, archetypeCatalogs )
}

