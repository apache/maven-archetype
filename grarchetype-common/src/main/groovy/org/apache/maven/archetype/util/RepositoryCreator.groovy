package org.apache.maven.archetype.util

/**
 *
 * @author raphaelpieroni
 */
interface RepositoryCreator {
	static String ROLE = 'org.apache.maven.archetype.util.RepositoryCreator'

	def createRepository( String repository )
}

