package org.apache.maven.archetype.creator;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.archetype.ArchetypeCreationRequest;
import org.apache.maven.archetype.ArchetypeCreationResult;
import org.apache.maven.archetype.common.ArchetypeFilesResolver;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.common.PomManager;
import org.apache.maven.archetype.common.util.FileCharsetDetector;
import org.apache.maven.archetype.common.util.ListScanner;
import org.apache.maven.archetype.common.util.PathUtils;
import org.apache.maven.archetype.exception.TemplateCreationException;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.FileSet;
import org.apache.maven.archetype.metadata.ModuleDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.archetype.metadata.io.xpp3.ArchetypeDescriptorXpp3Writer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Create a 2.0.x Archetype from a project.
 *
 *  @plexus.component role-hint="fileset"
 */
public class FilesetArchetypeCreator
    extends AbstractLogEnabled
    implements ArchetypeCreator
{
    /** @plexus.requirement */
    private ArchetypeFilesResolver archetypeFilesResolver;

    /** @plexus.requirement */
    private PomManager pomManager;

    /** @plexus.requirement */
    private MavenProjectBuilder projectBuilder;

    public void createArchetype( ArchetypeCreationRequest request, ArchetypeCreationResult result )
    {
        MavenProject project = request.getProject();
        List languages = request.getLanguages();
        List filtereds = request.getFiltereds();
        String defaultEncoding = request.getDefaultEncoding();
        boolean preserveCData = request.isPreserveCData();
        boolean keepParent = request.isKeepParent();
        boolean partialArchetype = request.isPartialArchetype();
        ArtifactRepository localRepository = request.getLocalRepository();

        Properties properties = new Properties();
        Properties configurationProperties = new Properties();
        if ( request.getProperties() != null )
        {
            properties.putAll( request.getProperties() );
            configurationProperties.putAll( request.getProperties() );
        }

        extractPropertiesFromProject( project, properties, configurationProperties, request.getPackageName() );

        File basedir = project.getBasedir();
        File generatedSourcesDirectory = FileUtils.resolveFile( basedir, getGeneratedSourcesDirectory() );
        generatedSourcesDirectory.mkdirs();

        getLogger().debug( "Creating archetype in " + generatedSourcesDirectory );

        try
        {
            File archetypePomFile =
                createArchetypeProjectPom( project, localRepository, configurationProperties, generatedSourcesDirectory );

            File archetypeResourcesDirectory = new File( generatedSourcesDirectory, getTemplateOutputDirectory() );

            File archetypeFilesDirectory = new File( archetypeResourcesDirectory, Constants.ARCHETYPE_RESOURCES );
            getLogger().debug( "Archetype's files output directory " + archetypeFilesDirectory );

            File archetypeDescriptorFile = new File( archetypeResourcesDirectory, Constants.ARCHETYPE_DESCRIPTOR );
            archetypeDescriptorFile.getParentFile().mkdirs();

            getLogger().debug( "Starting archetype's descriptor " + project.getArtifactId() );
            ArchetypeDescriptor archetypeDescriptor = new ArchetypeDescriptor();

            archetypeDescriptor.setName( project.getArtifactId() );
            archetypeDescriptor.setPartial( partialArchetype );

            addRequiredProperties( archetypeDescriptor, properties );

            // TODO ensure reverseProperties contains NO dotted properties
            Properties reverseProperties = getRequiredProperties( archetypeDescriptor, properties );
            // reverseProperties.remove( Constants.GROUP_ID );

            // TODO ensure pomReversedProperties contains NO dotted properties
            Properties pomReversedProperties = getRequiredProperties( archetypeDescriptor, properties );
            // pomReversedProperties.remove( Constants.PACKAGE );

            String packageName = configurationProperties.getProperty( Constants.PACKAGE );

            Model pom = pomManager.readPom( FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ) );

            List fileNames = resolveFileNames( pom, basedir );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Scanned for files " + fileNames.size() );

                for ( Iterator names = fileNames.iterator(); names.hasNext(); )
                {
                    getLogger().debug( "- " + names.next().toString() );
                }
            }

            List filesets = resolveFileSets( packageName, fileNames, languages, filtereds, defaultEncoding );
            getLogger().debug( "Resolved filesets for " + archetypeDescriptor.getName() );

            archetypeDescriptor.setFileSets( filesets );

            createArchetypeFiles( reverseProperties, filesets, packageName, basedir, archetypeFilesDirectory, defaultEncoding );
            getLogger().debug( "Created files for " + archetypeDescriptor.getName() );

            setParentArtifactId( reverseProperties, configurationProperties.getProperty( Constants.ARTIFACT_ID ) );

            for ( Iterator modules = pom.getModules().iterator(); modules.hasNext(); )
            {
                String moduleId = (String) modules.next();
                String rootArtifactId = configurationProperties.getProperty( Constants.ARTIFACT_ID );
                String moduleIdDirectory = moduleId;

                if ( moduleId.indexOf( rootArtifactId ) >= 0 )
                {
                    moduleIdDirectory = StringUtils.replace( moduleId, rootArtifactId, "__rootArtifactId__" );
                }

                getLogger().debug( "Creating module " + moduleId );

                ModuleDescriptor moduleDescriptor =
                    createModule( reverseProperties, rootArtifactId, moduleId, packageName,
                                  FileUtils.resolveFile( basedir, moduleId ),
                                  new File( archetypeFilesDirectory, moduleIdDirectory ), languages,
                                  filtereds, defaultEncoding, preserveCData, keepParent );

                archetypeDescriptor.addModule( moduleDescriptor );

                getLogger().debug(
                                   "Added module " + moduleDescriptor.getName() + " in "
                                       + archetypeDescriptor.getName() );
            }

            restoreParentArtifactId( reverseProperties, null );
            restoreArtifactId( reverseProperties, configurationProperties.getProperty( Constants.ARTIFACT_ID ) );

            createPoms( pom, configurationProperties.getProperty( Constants.ARTIFACT_ID ),
                        configurationProperties.getProperty( Constants.ARTIFACT_ID ), archetypeFilesDirectory, basedir,
                        pomReversedProperties, preserveCData, keepParent );
            getLogger().debug( "Created Archetype " + archetypeDescriptor.getName() + " template pom(s)" );

            Writer out = null;
            try
            {
                out = WriterFactory.newXmlWriter( archetypeDescriptorFile );

                ArchetypeDescriptorXpp3Writer writer = new ArchetypeDescriptorXpp3Writer();

                writer.write( out, archetypeDescriptor );

                getLogger().debug( "Archetype " + archetypeDescriptor.getName() + " descriptor written" );
            }
            finally
            {
                IOUtil.close( out );
            }

            createArchetypeBasicIt( archetypeDescriptor, generatedSourcesDirectory );

            InvocationRequest internalRequest = new DefaultInvocationRequest();
            internalRequest.setPomFile( archetypePomFile );
            internalRequest.setGoals( Collections.singletonList( request.getPostPhase() ) );

            Invoker invoker = new DefaultInvoker();
            invoker.execute( internalRequest );
        }
        catch ( Exception e )
        {
            result.setCause( e );
        }
    }

    /**
     * Create an archetype IT, ie goals.txt and archetype.properties in src/test/resources/projects/basic.
     *
     * @param archetypeDescriptor
     * @param generatedSourcesDirectory
     * @throws IOException
     */
    private void createArchetypeBasicIt( ArchetypeDescriptor archetypeDescriptor, File generatedSourcesDirectory )
        throws IOException
    {
        String basic = Constants.SRC + File.separator + Constants.TEST + File.separator + Constants.RESOURCES
            + File.separator + "projects" + File.separator + "basic";
        File basicItDirectory = new File( generatedSourcesDirectory, basic );
        basicItDirectory.mkdirs();

        InputStream in = null;
        OutputStream out = null;

        try
        {
            in = FilesetArchetypeCreator.class.getResourceAsStream( "archetype.properties" );

            Properties archetypeProperties = new Properties();
            archetypeProperties.load( in );

            for ( Iterator iter = archetypeDescriptor.getRequiredProperties().iterator(); iter.hasNext(); )
            {
                RequiredProperty req = (RequiredProperty) iter.next();

                archetypeProperties.put( req.getKey(), req.getDefaultValue() );
            }

            out = new FileOutputStream( new File( basicItDirectory, "archetype.properties" ) );
            archetypeProperties.store( out, null );
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }
        copyResource( "goal.txt", new File( basicItDirectory, "goal.txt" ) );

        getLogger().debug( "Added basic integration test" );
    }

    private void extractPropertiesFromProject( MavenProject project, Properties properties,
                                               Properties configurationProperties, String packageName )
    {
        if ( !properties.containsKey( Constants.GROUP_ID ) )
        {
            properties.setProperty( Constants.GROUP_ID, project.getGroupId() );
        }
        configurationProperties.setProperty( Constants.GROUP_ID, properties.getProperty( Constants.GROUP_ID ) );

        if ( !properties.containsKey( Constants.ARTIFACT_ID ) )
        {
            properties.setProperty( Constants.ARTIFACT_ID, project.getArtifactId() );
        }
        configurationProperties.setProperty( Constants.ARTIFACT_ID, properties.getProperty( Constants.ARTIFACT_ID ) );

        if ( !properties.containsKey( Constants.VERSION ) )
        {
            properties.setProperty( Constants.VERSION, project.getVersion() );
        }
        configurationProperties.setProperty( Constants.VERSION, properties.getProperty( Constants.VERSION ) );

        if ( packageName != null )
        {
            properties.setProperty( Constants.PACKAGE, packageName );
        }
        else if ( !properties.containsKey( Constants.PACKAGE ) )
        {
            properties.setProperty( Constants.PACKAGE, project.getGroupId() );
        }
        configurationProperties.setProperty( Constants.PACKAGE, properties.getProperty( Constants.PACKAGE ) );
    }

    /**
     * Create the archetype project pom.xml file, that will be used to build the archetype.
     */
    private File createArchetypeProjectPom( MavenProject project, ArtifactRepository localRepository,
                                            Properties configurationProperties, File projectDir )
        throws TemplateCreationException, IOException
    {
        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        // these values should be retrieved from the request with sensible defaults
        model.setGroupId( configurationProperties.getProperty( Constants.ARCHETYPE_GROUP_ID, project.getGroupId() ) );
        model.setArtifactId( configurationProperties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, project.getArtifactId() ) );
        model.setVersion( configurationProperties.getProperty( Constants.ARCHETYPE_VERSION, project.getVersion() ) );
        model.setPackaging( "maven-archetype" );
        model.setName( configurationProperties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, project.getArtifactId() ) );

        Build build = new Build();
        model.setBuild( build );

        // In many cases where we are behind a firewall making Archetypes for work mates we want
        // to simply be able to deploy the archetypes once we have created them. In order to do
        // this we want to utilize information from the project we are creating the archetype from.
        // This will be a fully working project that has been testing and inherits from a POM
        // that contains deployment information, along with any extensions required for deployment.
        // We don't want to create archetypes that cannot be deployed after we create them. People
        // might want to edit the archetype POM but they should not have too.

        if ( project.getParent() != null )
        {
            Artifact pa = project.getParentArtifact();

            try
            {
                MavenProject p = projectBuilder.buildFromRepository( pa, project.getRemoteArtifactRepositories(), localRepository );

                if ( p.getDistributionManagement() != null )
                {
                    model.setDistributionManagement( p.getDistributionManagement() );
                }

                if ( p.getBuildExtensions() != null )
                {
                    for ( Iterator i = p.getBuildExtensions().iterator(); i.hasNext(); )
                    {
                        Extension be = (Extension) i.next();

                        model.getBuild().addExtension( be );
                    }
                }
            }
            catch ( ProjectBuildingException e )
            {
                throw new TemplateCreationException( "Error reading parent POM of project: " + pa.getGroupId() + ":"
                    + pa.getArtifactId() + ":" + pa.getVersion() );
            }
        }

        Extension extension = new Extension();
        extension.setGroupId( "org.apache.maven.archetype" );
        extension.setArtifactId( "archetype-packaging" );
        extension.setVersion( getArchetypeVersion() );
        model.getBuild().addExtension( extension );

        Plugin plugin = new Plugin();
        plugin.setGroupId( "org.apache.maven.plugins" );
        plugin.setArtifactId( "maven-archetype-plugin" );
        plugin.setVersion( getArchetypeVersion() );
        plugin.setExtensions( true );

        PluginManagement pluginManagement = new PluginManagement();
        pluginManagement.addPlugin( plugin );
        model.getBuild().setPluginManagement( pluginManagement );

        getLogger().debug( "Creating archetype's pom" );

        File archetypePomFile = new File( projectDir, Constants.ARCHETYPE_POM );

        archetypePomFile.getParentFile().mkdirs();

        copyResource( "pom-prototype.xml", archetypePomFile );

        pomManager.writePom( model, archetypePomFile, archetypePomFile );

        return archetypePomFile;
    }

    private void copyResource( String name, File destination )
        throws IOException
    {
        InputStream in = null;
        OutputStream out = null;

        try
        {
            in = FilesetArchetypeCreator.class.getResourceAsStream( name );
            out = new FileOutputStream( destination );

            IOUtil.copy( in, out );
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }
    }

    private void addRequiredProperties( ArchetypeDescriptor archetypeDescriptor, Properties properties )
    {
        Properties requiredProperties = new Properties();
        requiredProperties.putAll( properties );
        requiredProperties.remove( Constants.ARCHETYPE_GROUP_ID );
        requiredProperties.remove( Constants.ARCHETYPE_ARTIFACT_ID );
        requiredProperties.remove( Constants.ARCHETYPE_VERSION );
        requiredProperties.remove( Constants.GROUP_ID );
        requiredProperties.remove( Constants.ARTIFACT_ID );
        requiredProperties.remove( Constants.VERSION );
        requiredProperties.remove( Constants.PACKAGE );

        for ( Iterator propertiesIterator = requiredProperties.keySet().iterator(); propertiesIterator.hasNext(); )
        {
            String propertyKey = (String) propertiesIterator.next();

            RequiredProperty requiredProperty = new RequiredProperty();
            requiredProperty.setKey( propertyKey );
            requiredProperty.setDefaultValue( requiredProperties.getProperty( propertyKey ) );

            archetypeDescriptor.addRequiredProperty( requiredProperty );

            getLogger().debug(
                               "Adding requiredProperty " + propertyKey + "="
                                   + requiredProperties.getProperty( propertyKey ) + " to archetype's descriptor" );
        }
    }

    private void createModulePoms( Properties pomReversedProperties, String rootArtifactId, String packageName,
                                   File basedir, File archetypeFilesDirectory, boolean preserveCData, boolean keepParent )
        throws FileNotFoundException, IOException, XmlPullParserException
    {
        Model pom = pomManager.readPom( FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ) );

        String parentArtifactId = pomReversedProperties.getProperty( Constants.PARENT_ARTIFACT_ID );
        String artifactId = pom.getArtifactId();
        setParentArtifactId( pomReversedProperties, pomReversedProperties.getProperty( Constants.ARTIFACT_ID ) );
        setArtifactId( pomReversedProperties, pom.getArtifactId() );

        for ( Iterator modules = pom.getModules().iterator(); modules.hasNext(); )
        {
            String subModuleId = (String) modules.next();

            String subModuleIdDirectory = subModuleId;

            if ( subModuleId.indexOf( rootArtifactId ) >= 0 )
            {
                subModuleIdDirectory = StringUtils.replace( subModuleId, rootArtifactId, "__rootArtifactId__" );
            }

            createModulePoms( pomReversedProperties, rootArtifactId, packageName,
                              FileUtils.resolveFile( basedir, subModuleId ),
                              FileUtils.resolveFile( archetypeFilesDirectory, subModuleIdDirectory ),
                              preserveCData, keepParent );
        }

        createModulePom( pom, rootArtifactId, archetypeFilesDirectory, pomReversedProperties,
                         FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ), preserveCData, keepParent );

        restoreParentArtifactId( pomReversedProperties, parentArtifactId );
        restoreArtifactId( pomReversedProperties, artifactId );
    }

    private void createPoms( Model pom, String rootArtifactId, String artifactId, File archetypeFilesDirectory,
                             File basedir, Properties pomReversedProperties, boolean preserveCData, boolean keepParent )
        throws IOException, FileNotFoundException, XmlPullParserException
    {
        setArtifactId( pomReversedProperties, pom.getArtifactId() );

        for ( Iterator modules = pom.getModules().iterator(); modules.hasNext(); )
        {
            String moduleId = (String) modules.next();

            String moduleIdDirectory = moduleId;

            if ( moduleId.indexOf( rootArtifactId ) >= 0 )
            {
                moduleIdDirectory = StringUtils.replace( moduleId, rootArtifactId, "__rootArtifactId__" );
            }

            createModulePoms( pomReversedProperties, rootArtifactId, moduleId,
                              FileUtils.resolveFile( basedir, moduleId ),
                              new File( archetypeFilesDirectory, moduleIdDirectory ),
                              preserveCData, keepParent );
        }

        restoreParentArtifactId( pomReversedProperties, null );
        restoreArtifactId( pomReversedProperties, artifactId );

        createArchetypePom( pom, archetypeFilesDirectory, pomReversedProperties,
                            FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ), preserveCData, keepParent );
    }

    private String getPackageInPathFormat( String aPackage )
    {
        return StringUtils.replace( aPackage, ".", "/" );
    }

    private void rewriteReferences( Model pom, String rootArtifactId, String groupId )
    {
        // rewrite Dependencies
        if ( pom.getDependencies() != null && !pom.getDependencies().isEmpty() )
        {
            for ( Iterator dependencies = pom.getDependencies().iterator(); dependencies.hasNext(); )
            {
                Dependency dependency = (Dependency) dependencies.next();

                if ( dependency.getArtifactId() != null && dependency.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( dependency.getGroupId() != null )
                    {
                        dependency.setGroupId( StringUtils.replace( dependency.getGroupId(), groupId,
                                                                    "${" + Constants.GROUP_ID + "}" ) );
                    }

                    dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(), rootArtifactId,
                                                                   "${rootArtifactId}" ) );

                    if ( dependency.getVersion() != null )
                    {
                        dependency.setVersion( "${" + Constants.VERSION + "}" );
                    }
                }
            }
        }

        // rewrite DependencyManagement
        if ( pom.getDependencyManagement() != null && pom.getDependencyManagement().getDependencies() != null
            && !pom.getDependencyManagement().getDependencies().isEmpty() )
        {
            for ( Iterator dependencies = pom.getDependencyManagement().getDependencies().iterator();
                dependencies.hasNext(); )
            {
                Dependency dependency = (Dependency) dependencies.next();

                if ( dependency.getArtifactId() != null && dependency.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( dependency.getGroupId() != null )
                    {
                        dependency.setGroupId( StringUtils.replace( dependency.getGroupId(), groupId,
                                                                    "${" + Constants.GROUP_ID + "}" ) );
                    }

                    dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(), rootArtifactId,
                                                                   "${rootArtifactId}" ) );

                    if ( dependency.getVersion() != null )
                    {
                        dependency.setVersion( "${" + Constants.VERSION + "}" );
                    }
                }
            }
        }

        // rewrite Plugins
        if ( pom.getBuild() != null && pom.getBuild().getPlugins() != null && !pom.getBuild().getPlugins().isEmpty() )
        {
            for ( Iterator plugins = pom.getBuild().getPlugins().iterator(); plugins.hasNext(); )
            {
                Plugin plugin = (Plugin) plugins.next();

                if ( plugin.getArtifactId() != null && plugin.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( plugin.getGroupId() != null )
                    {
                        plugin.setGroupId( StringUtils.replace( plugin.getGroupId(), groupId,
                                                                "${" + Constants.GROUP_ID + "}" ) );
                    }

                    plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(), rootArtifactId,
                                                               "${rootArtifactId}" ) );

                    if ( plugin.getVersion() != null )
                    {
                        plugin.setVersion( "${" + Constants.VERSION + "}" );
                    }
                }
            }
        }

        // rewrite PluginManagement
        if ( pom.getBuild() != null && pom.getBuild().getPluginManagement() != null
            && pom.getBuild().getPluginManagement().getPlugins() != null
            && !pom.getBuild().getPluginManagement().getPlugins().isEmpty() )
        {
            for ( Iterator plugins = pom.getBuild().getPluginManagement().getPlugins().iterator(); plugins.hasNext(); )
            {
                Plugin plugin = (Plugin) plugins.next();

                if ( plugin.getArtifactId() != null && plugin.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( plugin.getGroupId() != null )
                    {
                        plugin.setGroupId( StringUtils.replace( plugin.getGroupId(), groupId,
                                                                "${" + Constants.GROUP_ID + "}" ) );
                    }

                    plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(), rootArtifactId,
                                                               "${rootArtifactId}" ) );

                    if ( plugin.getVersion() != null )
                    {
                        plugin.setVersion( "${" + Constants.VERSION + "}" );
                    }
                }
            }
        }

        // rewrite Profiles
        if ( pom.getProfiles() != null )
        {
            for ( Iterator profiles = pom.getProfiles().iterator(); profiles.hasNext(); )
            {
                Profile profile = (Profile) profiles.next();

                // rewrite Dependencies
                if ( profile.getDependencies() != null && !profile.getDependencies().isEmpty() )
                {
                    for ( Iterator dependencies = profile.getDependencies().iterator(); dependencies.hasNext(); )
                    {
                        Dependency dependency = (Dependency) dependencies.next();

                        if ( dependency.getArtifactId() != null
                            && dependency.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                        {
                            if ( dependency.getGroupId() != null )
                            {
                                dependency.setGroupId( StringUtils.replace( dependency.getGroupId(), groupId,
                                                                            "${" + Constants.GROUP_ID + "}" ) );
                            }

                            dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(), rootArtifactId,
                                                                           "${rootArtifactId}" ) );

                            if ( dependency.getVersion() != null )
                            {
                                dependency.setVersion( "${" + Constants.VERSION + "}" );
                            }
                        }
                    }
                }

                // rewrite DependencyManagement
                if ( profile.getDependencyManagement() != null
                    && profile.getDependencyManagement().getDependencies() != null
                    && !profile.getDependencyManagement().getDependencies().isEmpty() )
                {
                    for ( Iterator dependencies = profile.getDependencyManagement().getDependencies().iterator();
                        dependencies.hasNext(); )
                    {
                        Dependency dependency = (Dependency) dependencies.next();

                        if ( dependency.getArtifactId() != null
                            && dependency.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                        {
                            if ( dependency.getGroupId() != null )
                            {
                                dependency.setGroupId( StringUtils.replace( dependency.getGroupId(), groupId,
                                                                            "${" + Constants.GROUP_ID + "}" ) );
                            }

                            dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(), rootArtifactId,
                                                                           "${rootArtifactId}" ) );

                            if ( dependency.getVersion() != null )
                            {
                                dependency.setVersion( "${" + Constants.VERSION + "}" );
                            }
                        }
                    }
                }

                // rewrite Plugins
                if ( profile.getBuild() != null && profile.getBuild().getPlugins() != null
                    && !profile.getBuild().getPlugins().isEmpty() )
                {
                    for ( Iterator plugins = profile.getBuild().getPlugins().iterator(); plugins.hasNext(); )
                    {
                        Plugin plugin = (Plugin) plugins.next();

                        if ( plugin.getArtifactId() != null && plugin.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                        {
                            if ( plugin.getGroupId() != null )
                            {
                                plugin.setGroupId( StringUtils.replace( plugin.getGroupId(), groupId,
                                                                        "${" + Constants.GROUP_ID + "}" ) );
                            }

                            plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(), rootArtifactId,
                                                                       "${rootArtifactId}" ) );

                            if ( plugin.getVersion() != null )
                            {
                                plugin.setVersion( "${" + Constants.VERSION + "}" );
                            }
                        }
                    }
                }

                // rewrite PluginManagement
                if ( profile.getBuild() != null && profile.getBuild().getPluginManagement() != null
                    && profile.getBuild().getPluginManagement().getPlugins() != null
                    && !profile.getBuild().getPluginManagement().getPlugins().isEmpty() )
                {
                    for ( Iterator plugins = profile.getBuild().getPluginManagement().getPlugins().iterator();
                        plugins.hasNext(); )
                    {
                        Plugin plugin = (Plugin) plugins.next();

                        if ( plugin.getArtifactId() != null && plugin.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                        {
                            if ( plugin.getGroupId() != null )
                            {
                                plugin.setGroupId( StringUtils.replace( plugin.getGroupId(), groupId,
                                                                        "${" + Constants.GROUP_ID + "}" ) );
                            }

                            plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(), rootArtifactId,
                                                                       "${rootArtifactId}" ) );

                            if ( plugin.getVersion() != null )
                            {
                                plugin.setVersion( "${" + Constants.VERSION + "}" );
                            }
                        }
                    }
                }
            }
        }
    }

    private void setArtifactId( Properties properties, String artifactId )
    {
        properties.setProperty( Constants.ARTIFACT_ID, artifactId );
    }

    private List concatenateToList( List toConcatenate, String with )
    {
        List result = new ArrayList( toConcatenate.size() );

        for ( Iterator iterator = toConcatenate.iterator(); iterator.hasNext(); )
        {
            String concatenate = (String) iterator.next();

            result.add( ( ( with.length() > 0 ) ? ( with + "/" + concatenate ) : concatenate ) );
        }

        return result;
    }

    private void copyFiles( File basedir, File archetypeFilesDirectory, String directory, List fileSetResources,
                            boolean packaged, String packageName )
        throws IOException
    {
        String packageAsDirectory = StringUtils.replace( packageName, ".", File.separator );

        getLogger().debug( "Package as Directory: Package:" + packageName + "->" + packageAsDirectory );

        for ( Iterator iterator = fileSetResources.iterator();  iterator.hasNext(); )
        {
            String inputFileName = (String) iterator.next();

            String outputFileName =
                packaged ? StringUtils.replace( inputFileName, packageAsDirectory + File.separator, "" )
                                : inputFileName;
            getLogger().debug( "InputFileName:" + inputFileName );
            getLogger().debug( "OutputFileName:" + outputFileName );

            File outputFile = new File( archetypeFilesDirectory, outputFileName );

            File inputFile = new File( basedir, inputFileName );

            outputFile.getParentFile().mkdirs();

            FileUtils.copyFile( inputFile, outputFile );
        }
    }

    private void createArchetypeFiles( Properties reverseProperties, List fileSets, String packageName, File basedir,
                                       File archetypeFilesDirectory, String defaultEncoding )
        throws IOException
    {
        getLogger().debug( "Creating Archetype/Module files from " + basedir + " to " + archetypeFilesDirectory );

        for ( Iterator iterator = fileSets.iterator(); iterator.hasNext(); )
        {
            FileSet fileSet = (FileSet) iterator.next();

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( basedir );
            scanner.setIncludes( (String[]) concatenateToList( fileSet.getIncludes(),
                                                               fileSet.getDirectory() ).toArray( new String[fileSet.getIncludes().size()] ) );
            scanner.setExcludes( (String[]) fileSet.getExcludes().toArray( new String[fileSet.getExcludes().size()] ) );
            scanner.addDefaultExcludes();
            getLogger().debug( "Using fileset " + fileSet );
            scanner.scan();

            List fileSetResources = Arrays.asList( scanner.getIncludedFiles() );
            getLogger().debug( "Scanned " + fileSetResources.size() + " resources" );

            if ( fileSet.isFiltered() )
            {
                processFileSet( basedir, archetypeFilesDirectory, fileSet.getDirectory(), fileSetResources,
                                fileSet.isPackaged(), packageName, reverseProperties, defaultEncoding );
                getLogger().debug( "Processed " + fileSet.getDirectory() + " files" );
            }
            else
            {
                copyFiles( basedir, archetypeFilesDirectory, fileSet.getDirectory(), fileSetResources,
                           fileSet.isPackaged(), packageName );
                getLogger().debug( "Copied " + fileSet.getDirectory() + " files" );
            }
        }
    }

    private void createArchetypePom( Model pom, File archetypeFilesDirectory, Properties pomReversedProperties,
                                     File initialPomFile, boolean preserveCData, boolean keepParent )
        throws IOException
    {
        File outputFile = FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        if ( preserveCData )
        {
            getLogger().debug( "Preserving CDATA parts of pom" );
            File inputFile = FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

            FileUtils.copyFile( initialPomFile, inputFile );

            Reader in = null;
            Writer out = null;
            try
            {
                in = ReaderFactory.newXmlReader( inputFile );

                String initialcontent = IOUtil.toString( in );

                String content = getReversedContent( initialcontent, pomReversedProperties );

                outputFile.getParentFile().mkdirs();

                out = WriterFactory.newXmlWriter( outputFile );

                IOUtil.copy( content, out );
            }
            finally
            {
                IOUtil.close( in );
                IOUtil.close( out );
            }

            inputFile.delete();
        }
        else
        {
            if ( !keepParent )
            {
                pom.setParent( null );
            }

            pom.setModules( null );
            pom.setGroupId( "${" + Constants.GROUP_ID + "}" );
            pom.setArtifactId( "${" + Constants.ARTIFACT_ID + "}" );
            pom.setVersion( "${" + Constants.VERSION + "}" );

            rewriteReferences( pom, pomReversedProperties.getProperty( Constants.ARTIFACT_ID ),
                               pomReversedProperties.getProperty( Constants.GROUP_ID ) );

            pomManager.writePom( pom, outputFile, initialPomFile );
        }

        Reader in = null;
        try
        {
            in = ReaderFactory.newXmlReader( initialPomFile );
            String initialcontent = IOUtil.toString( in );

            Iterator properties = pomReversedProperties.keySet().iterator();
            while ( properties.hasNext() )
            {
                String property = (String) properties.next();

                if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
                {
                    getLogger().warn(
                                      "Archetype uses ${" + property + "} for internal processing, but file "
                                          + initialPomFile + " contains this property already" );
                }
            }
        }
        finally
        {
            IOUtil.close( in );
        }
    }

    private FileSet createFileSet( final List excludes, final boolean packaged, final boolean filtered,
                                   final String group, final List includes, String defaultEncoding )
    {
        FileSet fileSet = new FileSet();

        fileSet.setDirectory( group );
        fileSet.setPackaged( packaged );
        fileSet.setFiltered( filtered );
        fileSet.setIncludes( includes );
        fileSet.setExcludes( excludes );
        fileSet.setEncoding( defaultEncoding );

        getLogger().debug( "Created Fileset " + fileSet );

        return fileSet;
    }

    private List createFileSets( List files, int level, boolean packaged, String packageName, boolean filtered,
                                 String defaultEncoding )
    {
        List fileSets = new ArrayList();

        if ( !files.isEmpty() )
        {
            getLogger().debug(
                               "Creating filesets" + ( packaged ? ( " packaged (" + packageName + ")" ) : "" )
                                   + ( filtered ? " filtered" : "" ) + " at level " + level );
            if ( level == 0 )
            {
                List includes = new ArrayList( files );
                List excludes = new ArrayList();

                if ( !includes.isEmpty() )
                {
                    fileSets.add( createFileSet( excludes, packaged, filtered, "", includes, defaultEncoding ) );
                }
            }
            else
            {
                Map groups = getGroupsMap( files, level );

                for ( Iterator groupIterator = groups.keySet().iterator(); groupIterator.hasNext(); )
                {
                    String group = (String) groupIterator.next();

                    getLogger().debug( "Creating filesets for group " + group );

                    if ( !packaged )
                    {
                        fileSets.add( getUnpackagedFileSet( filtered, group, (List) groups.get( group ),
                                                            defaultEncoding ) );
                    }
                    else
                    {
                        fileSets.addAll( getPackagedFileSets( filtered, group, (List) groups.get( group ), packageName,
                                                              defaultEncoding ) );
                    }
                }
            } // end if

            getLogger().debug( "Resolved fileSets " + fileSets );
        } // end if

        return fileSets;
    }

    private ModuleDescriptor createModule( Properties reverseProperties, String rootArtifactId, String moduleId,
                                           String packageName, File basedir, File archetypeFilesDirectory,
                                           List languages, List filtereds, String defaultEncoding,
                                           boolean preserveCData, boolean keepParent )
        throws IOException, XmlPullParserException
    {
        ModuleDescriptor archetypeDescriptor = new ModuleDescriptor();
        getLogger().debug( "Starting module's descriptor " + moduleId );

        archetypeFilesDirectory.mkdirs();
        getLogger().debug( "Module's files output directory " + archetypeFilesDirectory );

        Model pom = pomManager.readPom( FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ) );
        String replacementId = pom.getArtifactId();
        String moduleDirectory = pom.getArtifactId();

        if ( replacementId.indexOf( rootArtifactId ) >= 0 )
        {
            replacementId = StringUtils.replace( replacementId, rootArtifactId, "${rootArtifactId}" );
            moduleDirectory = StringUtils.replace( moduleId, rootArtifactId, "__rootArtifactId__" );
        }

        if ( moduleId.indexOf( rootArtifactId ) >= 0 )
        {
            moduleDirectory = StringUtils.replace( moduleId, rootArtifactId, "__rootArtifactId__" );
        }

        archetypeDescriptor.setName( replacementId );
        archetypeDescriptor.setId( replacementId );
        archetypeDescriptor.setDir( moduleDirectory );

        setArtifactId( reverseProperties, pom.getArtifactId() );

        List fileNames = resolveFileNames( pom, basedir );

        List filesets = resolveFileSets( packageName, fileNames, languages, filtereds, defaultEncoding );
        getLogger().debug( "Resolved filesets for module " + archetypeDescriptor.getName() );

        archetypeDescriptor.setFileSets( filesets );

        createArchetypeFiles( reverseProperties, filesets, packageName, basedir, archetypeFilesDirectory,
                              defaultEncoding );
        getLogger().debug( "Created files for module " + archetypeDescriptor.getName() );

        String parentArtifactId = reverseProperties.getProperty( Constants.PARENT_ARTIFACT_ID );
        setParentArtifactId( reverseProperties, pom.getArtifactId() );

        for ( Iterator modules = pom.getModules().iterator(); modules.hasNext(); )
        {
            String subModuleId = (String) modules.next();

            String subModuleIdDirectory = subModuleId;
            if ( subModuleId.indexOf( rootArtifactId ) >= 0 )
            {
                subModuleIdDirectory = StringUtils.replace( subModuleId, rootArtifactId, "__rootArtifactId__" );
            }

            getLogger().debug( "Creating module " + subModuleId );

            ModuleDescriptor moduleDescriptor =
                createModule( reverseProperties, rootArtifactId, subModuleId, packageName,
                              FileUtils.resolveFile( basedir, subModuleId ),
                              FileUtils.resolveFile( archetypeFilesDirectory, subModuleIdDirectory ), languages,
                              filtereds, defaultEncoding, preserveCData, keepParent );

            archetypeDescriptor.addModule( moduleDescriptor );

            getLogger().debug( "Added module " + moduleDescriptor.getName() + " in " + archetypeDescriptor.getName() );
        }

        restoreParentArtifactId( reverseProperties, parentArtifactId );
        restoreArtifactId( reverseProperties, pom.getArtifactId() );

        getLogger().debug( "Created Module " + archetypeDescriptor.getName() + " pom" );

        return archetypeDescriptor;
    }

    private void createModulePom( Model pom, String rootArtifactId, File archetypeFilesDirectory,
                                  Properties pomReversedProperties, File initialPomFile, boolean preserveCData,
                                  boolean keepParent )
        throws IOException
    {
        File outputFile = FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        if ( preserveCData )
        {
            getLogger().debug( "Preserving CDATA parts of pom" );
            File inputFile = FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

            FileUtils.copyFile( initialPomFile, inputFile );

            Reader in = null;
            Writer out = null;
            try
            {
                in = ReaderFactory.newXmlReader( inputFile );

                String initialcontent = IOUtil.toString( in );

                String content = getReversedContent( initialcontent, pomReversedProperties );

                outputFile.getParentFile().mkdirs();

                out = WriterFactory.newXmlWriter( outputFile );

                IOUtil.copy( content, out );
            }
            finally
            {
                IOUtil.close( in );
                IOUtil.close( out );
            }

            inputFile.delete();
        }
        else
        {
            if ( pom.getParent() != null )
            {
                pom.getParent().setGroupId(
                                            StringUtils.replace(
                                                                 pom.getParent().getGroupId(),
                                                                 pomReversedProperties.getProperty( Constants.GROUP_ID ),
                                                                 "${" + Constants.GROUP_ID + "}" ) );
                if ( pom.getParent().getArtifactId() != null
                    && pom.getParent().getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    pom.getParent().setArtifactId(
                                                   StringUtils.replace( pom.getParent().getArtifactId(),
                                                                        rootArtifactId, "${rootArtifactId}" ) );
                }
                if ( pom.getParent().getVersion() != null )
                {
                    pom.getParent().setVersion( "${" + Constants.VERSION + "}" );
                }
            }
            pom.setModules( null );

            if ( pom.getGroupId() != null )
            {
                pom.setGroupId( StringUtils.replace( pom.getGroupId(),
                                                     pomReversedProperties.getProperty( Constants.GROUP_ID ), "${"
                                                         + Constants.GROUP_ID + "}" ) );
            }

            pom.setArtifactId( "${" + Constants.ARTIFACT_ID + "}" );

            if ( pom.getVersion() != null )
            {
                pom.setVersion( "${" + Constants.VERSION + "}" );
            }

            rewriteReferences( pom, rootArtifactId, pomReversedProperties.getProperty( Constants.GROUP_ID ) );

            pomManager.writePom( pom, outputFile, initialPomFile );
        }

        Reader in = null;
        try
        {
            in = ReaderFactory.newXmlReader( initialPomFile );
            String initialcontent = IOUtil.toString( in );

            for ( Iterator properties = pomReversedProperties.keySet().iterator(); properties.hasNext(); )
            {
                String property = (String) properties.next();

                if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
                {
                    getLogger().warn(
                                      "OldArchetype uses ${" + property + "} for internal processing, but file "
                                          + initialPomFile + " contains this property already" );
                }
            }
        }
        finally
        {
            IOUtil.close( in );
        }
    }

    private Set getExtensions( List files )
    {
        Set extensions = new HashSet();

        for ( Iterator filesIterator = files.iterator(); filesIterator.hasNext(); )
        {
            String file = (String) filesIterator.next();

            extensions.add( FileUtils.extension( file ) );
        }

        return extensions;
    }

    private String getGeneratedSourcesDirectory()
    {
        return "target" + File.separator + "generated-sources" + File.separator + "archetype";
    }

    private Map getGroupsMap( final List files, final int level )
    {
        Map groups = new HashMap();

        for ( Iterator fileIterator = files.iterator(); fileIterator.hasNext(); )
        {
            String file = (String) fileIterator.next();

            String directory = PathUtils.getDirectory( file, level );
            // make all groups have unix style
            directory = StringUtils.replace( directory, File.separator, "/" );

            if ( !groups.containsKey( directory ) )
            {
                groups.put( directory, new ArrayList() );
            }

            List group = (List) groups.get( directory );

            String innerPath = file.substring( directory.length() + 1 );
            // make all groups have unix style
            innerPath = StringUtils.replace( innerPath, File.separator, "/" );

            group.add( innerPath );
        }

        getLogger().debug( "Sorted " + groups.size() + " groups in " + files.size() + " files" );
        getLogger().debug( "Sorted Files: " + files );

        return groups;
    }

    private FileSet getPackagedFileSet( final boolean filtered, final Set packagedExtensions, final String group,
                                        final Set unpackagedExtensions, final List unpackagedFiles,
                                        String defaultEncoding )
    {
        List includes = new ArrayList();
        List excludes = new ArrayList();

        for ( Iterator extensionsIterator = packagedExtensions.iterator(); extensionsIterator.hasNext(); )
        {
            String extension = (String) extensionsIterator.next();

            includes.add( "**/*." + extension );

            if ( unpackagedExtensions.contains( extension ) )
            {
                excludes.addAll( archetypeFilesResolver.getFilesWithExtension( unpackagedFiles, extension ) );
            }
        }

        return createFileSet( excludes, true, filtered, group, includes, defaultEncoding );
    }

    private List getPackagedFileSets( final boolean filtered, final String group, final List groupFiles,
                                      final String packageName, String defaultEncoding )
    {
        String packageAsDir = StringUtils.replace( packageName, ".", "/" );

        List packagedFileSets = new ArrayList();
        List packagedFiles = archetypeFilesResolver.getPackagedFiles( groupFiles, packageAsDir );
        getLogger().debug( "Found packaged Files:" + packagedFiles );

        List unpackagedFiles = archetypeFilesResolver.getUnpackagedFiles( groupFiles, packageAsDir );
        getLogger().debug( "Found unpackaged Files:" + unpackagedFiles );

        Set packagedExtensions = getExtensions( packagedFiles );
        getLogger().debug( "Found packaged extensions " + packagedExtensions );

        Set unpackagedExtensions = getExtensions( unpackagedFiles );

        if ( !packagedExtensions.isEmpty() )
        {
            packagedFileSets.add( getPackagedFileSet( filtered, packagedExtensions, group, unpackagedExtensions,
                                                      unpackagedFiles, defaultEncoding ) );
        }

        if ( !unpackagedExtensions.isEmpty() )
        {
            getLogger().debug( "Found unpackaged extensions " + unpackagedExtensions );

            packagedFileSets.add( getUnpackagedFileSet( filtered, unpackagedExtensions, unpackagedFiles, group,
                                                        packagedExtensions, defaultEncoding ) );
        }

        return packagedFileSets;
    }

    private void setParentArtifactId( Properties properties, String parentArtifactId )
    {
        properties.setProperty( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
    }

    private void processFileSet( File basedir, File archetypeFilesDirectory, String directory, List fileSetResources,
                                 boolean packaged, String packageName, Properties reverseProperties,
                                 String defaultEncoding )
        throws IOException
    {
        String packageAsDirectory = StringUtils.replace( packageName, ".", File.separator );

        getLogger().debug( "Package as Directory: Package:" + packageName + "->" + packageAsDirectory );

        for ( Iterator iterator = fileSetResources.iterator(); iterator.hasNext(); )
        {
            String inputFileName = (String) iterator.next();

            String outputFileName =
                packaged ? StringUtils.replace( inputFileName, packageAsDirectory + File.separator, "" )
                                : inputFileName;

            getLogger().debug( "InputFileName:" + inputFileName );
            getLogger().debug( "OutputFileName:" + outputFileName );

            File outputFile = new File( archetypeFilesDirectory, outputFileName );
            File inputFile = new File( basedir, inputFileName );

            FileCharsetDetector detector = new FileCharsetDetector( inputFile );

            String fileEncoding = detector.isFound() ? detector.getCharset() : defaultEncoding;

            String initialcontent = IOUtil.toString( new FileInputStream( inputFile ), fileEncoding );

            for ( Iterator properties = reverseProperties.keySet().iterator(); properties.hasNext(); )
            {
                String property = (String) properties.next();

                if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
                {
                    getLogger().warn(
                                      "Archetype uses ${" + property + "} for internal processing, but file "
                                          + inputFile + " contains this property already" );
                }
            }

            String content = getReversedContent( initialcontent, reverseProperties );

            outputFile.getParentFile().mkdirs();

            org.apache.commons.io.IOUtils.write( content, new FileOutputStream( outputFile ), fileEncoding );
        }
    }

    private List removePackage( List sources, String packageAsDirectory )
    {
        if ( sources == null )
        {
            return null;
        }

        List unpackagedSources = new ArrayList( sources.size() );

        for ( Iterator sourcesIterator = sources.iterator(); sourcesIterator.hasNext(); )
        {
            String source = (String) sourcesIterator.next();

            String unpackagedSource = StringUtils.replace( source, packageAsDirectory, "" );

            unpackagedSources.add( unpackagedSource );
        }

        return unpackagedSources;
    }

    private Properties getRequiredProperties( ArchetypeDescriptor archetypeDescriptor, Properties properties )
    {
        Properties reversedProperties = new Properties();

        reversedProperties.putAll( properties );
        reversedProperties.remove( Constants.ARCHETYPE_GROUP_ID );
        reversedProperties.remove( Constants.ARCHETYPE_ARTIFACT_ID );
        reversedProperties.remove( Constants.ARCHETYPE_VERSION );
        reversedProperties.setProperty( Constants.PACKAGE_IN_PATH_FORMAT,
                                        getPackageInPathFormat( properties.getProperty( Constants.PACKAGE ) ) );

        return reversedProperties;
    }

    private List resolveFileNames( final Model pom, final File basedir )
        throws IOException
    {
        getLogger().debug( "Resolving files for " + pom.getId() + " in " + basedir );

        StringBuffer buff = new StringBuffer( "pom.xml*,archetype.properties*,target/**," );
        for ( Iterator modules = pom.getModules().iterator(); modules.hasNext(); )
        {
            buff.append( ',' ).append( (String) modules.next() ).append( "/**" );
        }

        for ( Iterator defaultExcludes = Arrays.asList( ListScanner.DEFAULTEXCLUDES ).iterator();
            defaultExcludes.hasNext(); )
        {
            buff.append( ',' ).append( (String) defaultExcludes.next() ).append( "/**" );
        }

        String excludes = PathUtils.convertPathForOS( buff.toString() );

        List fileNames = FileUtils.getFileNames( basedir, "**,.*,**/.*", excludes, false );

        getLogger().debug( "Resolved " + fileNames.size() + " files" );
        getLogger().debug( "Resolved Files:" + fileNames );

        return fileNames;
    }

    private List resolveFileSets( String packageName, List fileNames, List languages, List filtereds,
                                  String defaultEncoding )
    {
        List resolvedFileSets = new ArrayList();
        getLogger().debug(
                           "Resolving filesets with package=" + packageName + ", languages=" + languages
                               + " and extentions=" + filtereds );

        List files = new ArrayList( fileNames );

        String languageIncludes = "";

        for ( Iterator languagesIterator = languages.iterator(); languagesIterator.hasNext(); )
        {
            String language = (String) languagesIterator.next();

            languageIncludes += ( ( languageIncludes.length() == 0 ) ? "" : "," ) + language + "/**";
        }

        getLogger().debug( "Using languages includes " + languageIncludes );

        String filteredIncludes = "";
        for ( Iterator filteredsIterator = filtereds.iterator(); filteredsIterator.hasNext(); )
        {
            String filtered = (String) filteredsIterator.next();

            filteredIncludes +=
                ( ( filteredIncludes.length() == 0 ) ? "" : "," ) + "**/" + ( filtered.startsWith( "." ) ? "" : "*." )
                    + filtered;
        }

        getLogger().debug( "Using filtered includes " + filteredIncludes );

        /* sourcesMainFiles */
        List sourcesMainFiles = archetypeFilesResolver.findSourcesMainFiles( files, languageIncludes );
        if ( !sourcesMainFiles.isEmpty() )
        {
            files.removeAll( sourcesMainFiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( sourcesMainFiles, filteredIncludes );
            sourcesMainFiles.removeAll( filteredFiles );

            List unfilteredFiles = sourcesMainFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding ) );
            }

            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding ) );
            }
        }

        /* resourcesMainFiles */
        List resourcesMainFiles = archetypeFilesResolver.findResourcesMainFiles( files, languageIncludes );
        if ( !resourcesMainFiles.isEmpty() )
        {
            files.removeAll( resourcesMainFiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( resourcesMainFiles, filteredIncludes );
            resourcesMainFiles.removeAll( filteredFiles );

            List unfilteredFiles = resourcesMainFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 3, false, packageName, false, defaultEncoding ) );
            }
        }

        /* sourcesTestFiles */
        List sourcesTestFiles = archetypeFilesResolver.findSourcesTestFiles( files, languageIncludes );
        if ( !sourcesTestFiles.isEmpty() )
        {
            files.removeAll( sourcesTestFiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( sourcesTestFiles, filteredIncludes );
            sourcesTestFiles.removeAll( filteredFiles );

            List unfilteredFiles = sourcesTestFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding ) );
            }
        }

        /* ressourcesTestFiles */
        List resourcesTestFiles = archetypeFilesResolver.findResourcesTestFiles( files, languageIncludes );
        if ( !resourcesTestFiles.isEmpty() )
        {
            files.removeAll( resourcesTestFiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( resourcesTestFiles, filteredIncludes );
            resourcesTestFiles.removeAll( filteredFiles );

            List unfilteredFiles = resourcesTestFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 3, false, packageName, false, defaultEncoding ) );
            }
        }

        /* siteFiles */
        List siteFiles = archetypeFilesResolver.findSiteFiles( files, languageIncludes );
        if ( !siteFiles.isEmpty() )
        {
            files.removeAll( siteFiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( siteFiles, filteredIncludes );
            siteFiles.removeAll( filteredFiles );

            List unfilteredFiles = siteFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 2, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 2, false, packageName, false, defaultEncoding ) );
            }
        }

        /* thirdLevelSourcesfiles */
        List thirdLevelSourcesfiles = archetypeFilesResolver.findOtherSources( 3, files, languageIncludes );
        if ( !thirdLevelSourcesfiles.isEmpty() )
        {
            files.removeAll( thirdLevelSourcesfiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( thirdLevelSourcesfiles, filteredIncludes );
            thirdLevelSourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = thirdLevelSourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding ) );
            }

            /* thirdLevelResourcesfiles */
            List thirdLevelResourcesfiles =
                archetypeFilesResolver.findOtherResources( 3, files, thirdLevelSourcesfiles, languageIncludes );
            if ( !thirdLevelResourcesfiles.isEmpty() )
            {
                files.removeAll( thirdLevelResourcesfiles );
                filteredFiles = archetypeFilesResolver.getFilteredFiles( thirdLevelResourcesfiles, filteredIncludes );
                thirdLevelResourcesfiles.removeAll( filteredFiles );
                unfilteredFiles = thirdLevelResourcesfiles;
                if ( !filteredFiles.isEmpty() )
                {
                    resolvedFileSets.addAll( createFileSets( filteredFiles, 3, false, packageName, true,
                                                             defaultEncoding ) );
                }
                if ( !unfilteredFiles.isEmpty() )
                {
                    resolvedFileSets.addAll( createFileSets( unfilteredFiles, 3, false, packageName, false,
                                                             defaultEncoding ) );
                }
            }
        } // end if

        /* secondLevelSourcesfiles */
        List secondLevelSourcesfiles = archetypeFilesResolver.findOtherSources( 2, files, languageIncludes );
        if ( !secondLevelSourcesfiles.isEmpty() )
        {
            files.removeAll( secondLevelSourcesfiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( secondLevelSourcesfiles, filteredIncludes );
            secondLevelSourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = secondLevelSourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 2, true, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 2, true, packageName, false, defaultEncoding ) );
            }
        }

        /* secondLevelResourcesfiles */
        List secondLevelResourcesfiles = archetypeFilesResolver.findOtherResources( 2, files, languageIncludes );
        if ( !secondLevelResourcesfiles.isEmpty() )
        {
            files.removeAll( secondLevelResourcesfiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( secondLevelResourcesfiles, filteredIncludes );
            secondLevelResourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = secondLevelResourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 2, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 2, false, packageName, false, defaultEncoding ) );
            }
        }

        /* rootResourcesfiles */
        List rootResourcesfiles = archetypeFilesResolver.findOtherResources( 0, files, languageIncludes );
        if ( !rootResourcesfiles.isEmpty() )
        {
            files.removeAll( rootResourcesfiles );

            List filteredFiles = archetypeFilesResolver.getFilteredFiles( rootResourcesfiles, filteredIncludes );
            rootResourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = rootResourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 0, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( unfilteredFiles, 0, false, packageName, false, defaultEncoding ) );
            }
        }

        /**/
        if ( !files.isEmpty() )
        {
            getLogger().info( "Ignored files: " + files );
        }

        return resolvedFileSets;
    }

    private void restoreArtifactId( Properties properties, String artifactId )
    {
        if ( StringUtils.isEmpty( artifactId ) )
        {
            properties.remove( Constants.ARTIFACT_ID );
        }
        else
        {
            properties.setProperty( Constants.ARTIFACT_ID, artifactId );
        }
    }

    private void restoreParentArtifactId( Properties properties, String parentArtifactId )
    {
        if ( StringUtils.isEmpty( parentArtifactId ) )
        {
            properties.remove( Constants.PARENT_ARTIFACT_ID );
        }
        else
        {
            properties.setProperty( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
        }
    }

    private String getReversedContent( String content, Properties properties )
    {
        String result =
            StringUtils.replace( StringUtils.replace( content, "$", "${symbol_dollar}" ), "\\", "${symbol_escape}" );

        for ( Iterator propertyIterator = properties.keySet().iterator(); propertyIterator.hasNext(); )
        {
            String propertyKey = (String) propertyIterator.next();

            result = StringUtils.replace( result, properties.getProperty( propertyKey ), "${" + propertyKey + "}" );
        }
        // TODO: Replace velocity to a better engine...
        return "#set( $symbol_pound = '#' )\n" + "#set( $symbol_dollar = '$' )\n" + "#set( $symbol_escape = '\\' )\n"
            + StringUtils.replace( result, "#", "${symbol_pound}" );
    }

    private String getTemplateOutputDirectory()
    {
        return Constants.SRC + File.separator + Constants.MAIN + File.separator + Constants.RESOURCES;
    }

    private FileSet getUnpackagedFileSet( final boolean filtered, final String group, final List groupFiles,
                                          String defaultEncoding )
    {
        Set extensions = getExtensions( groupFiles );

        List includes = new ArrayList();
        List excludes = new ArrayList();

        for ( Iterator extensionsIterator = extensions.iterator(); extensionsIterator.hasNext(); )
        {
            String extension = (String) extensionsIterator.next();

            includes.add( "**/*." + extension );
        }

        return createFileSet( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private FileSet getUnpackagedFileSet( final boolean filtered, final Set unpackagedExtensions,
                                          final List unpackagedFiles, final String group, final Set packagedExtensions,
                                          String defaultEncoding )
    {
        List includes = new ArrayList();
        List excludes = new ArrayList();

        for ( Iterator extensionsIterator = unpackagedExtensions.iterator(); extensionsIterator.hasNext(); )
        {
            String extension = (String) extensionsIterator.next();

            if ( packagedExtensions.contains( extension ) )
            {
                includes.addAll( archetypeFilesResolver.getFilesWithExtension( unpackagedFiles, extension ) );
            }
            else
            {
                includes.add( "**/*." + extension );
            }
        }

        return createFileSet( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private static final String MAVEN_PROPERTIES = "META-INF/maven/org.apache.maven.archetype/archetype-common/pom.properties";

    public String getArchetypeVersion()
    {
        InputStream is = null;

        // This should actually come from the pom.properties at testing but it's not generated and put into the JAR, it happens
        // as part of the JAR plugin which is crap as it makes testing inconsistent.
        String version = "version";

        try
        {
            Properties properties = new Properties();

            is = getClass().getClassLoader().getResourceAsStream( MAVEN_PROPERTIES );

            if ( is != null )
            {
                properties.load( is );

                String property = properties.getProperty( "version" );

                if ( property != null )
                {
                    return property;
                }
            }

            return version;
        }
        catch ( IOException e )
        {
            return version;
        }
        finally
        {
            IOUtil.close( is );
        }
    }
}
