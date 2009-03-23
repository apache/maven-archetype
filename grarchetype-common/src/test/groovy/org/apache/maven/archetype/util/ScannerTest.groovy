package org.apache.maven.archetype.util

import org.junit.Test
import static org.junit.Assert.*


/**
 *
 * @author rafale
 */
class ScannerTest {
//	def scanner = new Scanner( fileset:fileset )
	@Test
    void test_includeAll() {
        def scanner = new Scanner( fileset: new XmlSlurper().parseText(
"""
<fileset>
  <includes>
    <include>**/*</include>
  </includes>
</fileset>
""" ) )
        assertTrue scanner.match( 'App.java' )
        assertTrue scanner.match( 'foo/bar/baz/App.java' )
        assertTrue scanner.match( 'App.properties' )
        assertTrue scanner.match( 'foo/bar/baz/App.properties' )
    }

	@Test
    void test_includeJavaBar() {
        def scanner = new Scanner( fileset: new XmlSlurper().parseText(
"""
<fileset>
  <includes>
    <include>**/*.java</include>
    <include>**/bar/**/*</include>
  </includes>
</fileset>
""" ) )
        assertTrue scanner.match( 'App.java' )
        assertTrue scanner.match( 'foo/bar/baz/App.java' )
        assertFalse scanner.match( 'App.properties' )
        assertTrue scanner.match( 'foo/bar/baz/App.properties' )
    }

	@Test
    void test_includeJavaExcludeBar() {
        def scanner = new Scanner( fileset: new XmlSlurper().parseText(
"""
<fileset>
  <includes>
    <include>**/*.java</include>
  </includes>
  <excludes>
    <exclude>**/bar/**/*</exclude>
  </excludes>
</fileset>
""" ) )
        assertTrue scanner.match( 'App.java' )
        assertFalse scanner.match( 'foo/bar/baz/App.java' )
        assertFalse scanner.match( 'App.properties' )
        assertFalse scanner.match( 'foo/bar/baz/App.properties' )
    }
}

