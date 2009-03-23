package org.apache.maven.archetype.util
import org.codehaus.plexus.util.SelectorUtils

/**
 *
 * @author rafale
 */
class Scanner {
	def fileset

    def match( path ) {
        included( path ) && !excluded( path )
    }
    def included( path ) {
        matchPatterns( path, fileset.includes.include )
    }
    def excluded( path ) {
        matchPatterns( path, fileset.excludes.exclude )
    }
    def matchPatterns( path, patterns ) {
        def found = false
        patterns.each{ pattern ->
            found = found || matchPattern( path, pattern.text().toString() )
        }
        return found
    }
    def matchPattern( path, pattern ) {
        SelectorUtils.matchPath pattern, path, true
    }
}

