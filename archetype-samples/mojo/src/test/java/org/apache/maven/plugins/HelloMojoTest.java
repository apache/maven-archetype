package org.apache.maven.plugins;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 *
 * @author raphaelpieroni
 */
public class HelloMojoTest extends AbstractMojoTestCase
{

    public void testExecute()
    throws Exception
    {
        HelloMojo mojo = (HelloMojo) lookupMojo(
            "hello",
            getTestFile( "src/test/resources/unit/project-to-test/pom.xml" ) );
        assertNotNull(mojo);
        mojo.execute();
    }
}
