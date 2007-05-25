/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.plugin.archetype;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.archetype.Archetype;
import org.apache.maven.archetype.ArchetypeNotFoundException;
import org.apache.maven.archetype.ArchetypeDescriptorException;
import org.apache.maven.archetype.ArchetypeTemplateProcessingException;
import org.apache.maven.archetype.ArchetypeCreator;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

/**
 * Builds archetype containers based from an existing Maven project (currently
 * under development).
 *
 * @goal create-from-project
 * @description Create an archetype from an existing Maven project.
 * @requiresProject true
 */
public class CreateArchetypeFromProjectMojo
    extends AbstractMojo
{
    /**
     * The Maven Project to be used as the basis for the creating of the archetype.
     * 
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Maven's local repository.
     * 
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * Output build directory.
     *  
     * @parameter expression="${targetDirectory}" default-value="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * Maven ArchetypeCreator
     * 
     * @component
     */
    private ArchetypeCreator archetypeCreator;

    /**
     * Maven ProjectHelper
     *
     * @component
     */
    private MavenProjectHelper projectHelper;


    /**
     * Contains Archetype Properties.
     * 
     * @parameter expression="${archetypeProperties}" default-value="${basedir}/src/main/archetype/archetype.properties"
     * @required
     */
    private File archetypeProperties;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            Properties p = new Properties();

            p.load( new FileInputStream( archetypeProperties ) );

            File archetypeJar = archetypeCreator.createArchetype( project, localRepository, targetDirectory, p );

            projectHelper.attachArtifact( project, "jar", "archetype", archetypeJar );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Cannot create archetype from this project.", e );
        }
    }
}
