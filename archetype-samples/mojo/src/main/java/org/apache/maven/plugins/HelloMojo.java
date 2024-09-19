package org.apache.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Says Hello to someone.
 * @goal hello
 */
public class HelloMojo extends AbstractMojo {

    /**
     * Someone to whom the plugin says hello.
     * @parameter expression="${toWhom}" default-value="World"
     */
    String toWhom;

    public void execute() throws MojoExecutionException {
        getLog().info("Hello " + toWhom + "!");
    }
}