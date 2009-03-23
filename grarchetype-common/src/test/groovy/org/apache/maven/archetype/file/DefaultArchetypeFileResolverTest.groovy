package org.apache.maven.archetype.file
import org.apache.maven.plugin.testing.AbstractMojoTestCase

import org.junit.Test
import static org.junit.Assert.*
/**
 *
 * @author rafale
 */
class DefaultArchetypeFileResolverTest extends AbstractMojoTestCase {

    void setUp(){
        super.setUp()
        archetypeResources = [
            'pom.xml',
            'sub/pom.xml',
            'sub/inner/deep/pom.xml',
            'src/main/java/App.java',
            'src/main/java/foo/Foo.java',
            'src/test/resources/images/logo.png',
            'src/test/resources/images/logo-big.png',
            'sub/src/main/webapp/WEB-INF/web.xml',
            'sub/inner/deep/src/main/groovy/bar/Bar.groovy']
        resolver = lookup( DefaultArchetypeFileResolver.ROLE )
    }
    void tearDown(){super.tearDown()}

    def resolver
    def archetypeResources

	@Test
    void test_resolveRootModule(){
        assertEquals 'RootModule', ['foo/Foo.java'],
            resolver.getFiles( '', new XmlSlurper().parseText(
"""
<fileset>
  <directory>src/main/java</directory>
  <includes>
    <include>**/*.java</include>
  </includes>
  <excludes>
    <exclude>*.java</exclude>
  </excludes>
</fileset>
""" ), archetypeResources )
    }
    
	@Test
    void test_resolveSubModule(){
        assertEquals 'SubModule', ['WEB-INF/web.xml'],
            resolver.getFiles( 'sub', new XmlSlurper().parseText(
"""
<fileset>
  <directory>src/main/webapp</directory>
  <includes>
    <include>**/*.xml</include>
  </includes>
</fileset>
""" ), archetypeResources )
    }

	@Test
    void test_resolveDeepModule(){
        assertEquals 'DeepModule', ['bar/Bar.groovy'],
            resolver.getFiles( 'sub/inner/deep', new XmlSlurper().parseText(
"""
<fileset>
  <directory>src/main/groovy</directory>
  <includes>
    <include>**/*.groovy</include>
  </includes>
</fileset>
""" ), archetypeResources )
    }
}

