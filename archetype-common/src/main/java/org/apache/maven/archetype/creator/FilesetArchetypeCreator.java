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

import org.apache.commons.collections.CollectionUtils;
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
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static org.apache.commons.io.IOUtils.write;

/**
 * Create a 2.x Archetype project from a project. Since 2.0-alpha-5, an integration-test named "basic" is created along
 * the archetype itself to provide immediate test when building the archetype.
 */
@Component( role = ArchetypeCreator.class, hint = "fileset" )
public class FilesetArchetypeCreator
    extends AbstractLogEnabled
    implements ArchetypeCreator
{
    private static final String DEFAULT_OUTPUT_DIRECTORY =
        "target" + File.separator + "generated-sources" + File.separator + "archetype";

    @Requirement
    private ArchetypeFilesResolver archetypeFilesResolver;

    @Requirement
    private PomManager pomManager;
    
    @Requirement
    private Invoker invoker;

    @Override
    public void createArchetype( ArchetypeCreationRequest request, ArchetypeCreationResult result )
    {
        MavenProject project = request.getProject();
        List<String> languages = request.getLanguages();
        List<String> filtereds = request.getFiltereds();
        String defaultEncoding = request.getDefaultEncoding();
        boolean preserveCData = request.isPreserveCData();
        boolean keepParent = request.isKeepParent();
        boolean partialArchetype = request.isPartialArchetype();
        File outputDirectory = request.getOutputDirectory();
        File basedir = project.getBasedir();

        Properties properties = new Properties();
        Properties configurationProperties = new Properties();
        if ( request.getProperties() != null )
        {
            properties.putAll( request.getProperties() );
            configurationProperties.putAll( request.getProperties() );
        }

        extractPropertiesFromProject( project, properties, configurationProperties, request.getPackageName() );

        if ( outputDirectory == null )
        {
            getLogger().debug( "No output directory defined, using default: " + DEFAULT_OUTPUT_DIRECTORY );
            outputDirectory = FileUtils.resolveFile( basedir, DEFAULT_OUTPUT_DIRECTORY );
        }
        outputDirectory.mkdirs();

        getLogger().debug( "Creating archetype in " + outputDirectory );

        try
        {
            File archetypePomFile = createArchetypeProjectPom( project, request.getProjectBuildingRequest(),
                                                               configurationProperties, outputDirectory );

            File archetypeResourcesDirectory = new File( outputDirectory, getTemplateOutputDirectory() );

            File archetypeFilesDirectory = new File( archetypeResourcesDirectory, Constants.ARCHETYPE_RESOURCES );
            getLogger().debug( "Archetype's files output directory " + archetypeFilesDirectory );

            File archetypeDescriptorFile = new File( archetypeResourcesDirectory, Constants.ARCHETYPE_DESCRIPTOR );
            archetypeDescriptorFile.getParentFile().mkdirs();

            File archetypePostGenerationScript =
                new File( archetypeResourcesDirectory, Constants.ARCHETYPE_POST_GENERATION_SCRIPT );
            archetypePostGenerationScript.getParentFile().mkdirs();

            if ( request.getProject().getBuild() != null && CollectionUtils.isNotEmpty(
                request.getProject().getBuild().getResources() ) )
            {
                for ( Resource resource : request.getProject().getBuild().getResources() )
                {
                    File inputFile = new File(
                        resource.getDirectory() + File.separator + Constants.ARCHETYPE_POST_GENERATION_SCRIPT );
                    if ( inputFile.exists() )
                    {
                        FileUtils.copyFile( inputFile, archetypePostGenerationScript );
                    }
                }
            }

            getLogger().debug( "Starting archetype's descriptor " + project.getArtifactId() );
            ArchetypeDescriptor archetypeDescriptor = new ArchetypeDescriptor();

            archetypeDescriptor.setName( project.getArtifactId() );
            archetypeDescriptor.setPartial( partialArchetype );

            addRequiredProperties( archetypeDescriptor, properties );

            // TODO ensure reverseProperties contains NO dotted properties
            Properties reverseProperties = getReversedProperties( archetypeDescriptor, properties );
            // reverseProperties.remove( Constants.GROUP_ID );

            // TODO ensure pomReversedProperties contains NO dotted properties
            Properties pomReversedProperties = getReversedProperties( archetypeDescriptor, properties );
            // pomReversedProperties.remove( Constants.PACKAGE );

            String packageName = configurationProperties.getProperty( Constants.PACKAGE );

            Model pom = pomManager.readPom( project.getFile() );

            List<String> excludePatterns =
                configurationProperties.getProperty( Constants.EXCLUDE_PATTERNS ) != null
                    ? Arrays.asList(
                    StringUtils.split( configurationProperties.getProperty( Constants.EXCLUDE_PATTERNS ), "," ) )
                    : Collections.<String>emptyList();

            List<String> fileNames = resolveFileNames( pom, basedir, excludePatterns );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Scanned for files " + fileNames.size() );

                for ( String name : fileNames )
                {
                    getLogger().debug( "- " + name );
                }
            }

            List<FileSet> filesets = resolveFileSets( packageName, fileNames, languages, filtereds, defaultEncoding );
            getLogger().debug( "Resolved filesets for " + archetypeDescriptor.getName() );

            archetypeDescriptor.setFileSets( filesets );

            createArchetypeFiles( reverseProperties, filesets, packageName, basedir, archetypeFilesDirectory,
                                  defaultEncoding, excludePatterns );
            getLogger().debug( "Created files for " + archetypeDescriptor.getName() );

            setParentArtifactId( reverseProperties, configurationProperties.getProperty( Constants.ARTIFACT_ID ) );

            for ( String moduleId : pom.getModules() )
            {
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
                                  new File( archetypeFilesDirectory, moduleIdDirectory ), languages, filtereds,
                                  defaultEncoding, preserveCData, keepParent );

                archetypeDescriptor.addModule( moduleDescriptor );

                getLogger().debug(
                    "Added module " + moduleDescriptor.getName() + " in " + archetypeDescriptor.getName() );
            }

            restoreParentArtifactId( reverseProperties, null );
            restoreArtifactId( reverseProperties, configurationProperties.getProperty( Constants.ARTIFACT_ID ) );

            createPoms( pom, configurationProperties.getProperty( Constants.ARTIFACT_ID ),
                        configurationProperties.getProperty( Constants.ARTIFACT_ID ), archetypeFilesDirectory, basedir,
                        project.getFile(), pomReversedProperties, preserveCData, keepParent );
            getLogger().debug( "Created Archetype " + archetypeDescriptor.getName() + " template pom(s)" );

            
            try ( Writer out = WriterFactory.newXmlWriter( archetypeDescriptorFile ) )
            {
                ArchetypeDescriptorXpp3Writer writer = new ArchetypeDescriptorXpp3Writer();

                writer.write( out, archetypeDescriptor );

                getLogger().debug( "Archetype " + archetypeDescriptor.getName() + " descriptor written" );
            }

            createArchetypeBasicIt( archetypeDescriptor, outputDirectory );

            // Copy archetype integration tests.
            File archetypeIntegrationTestInputFolder =
                new File( basedir, Constants.SRC + File.separator + "it" + File.separator + "projects" );
            File archetypeIntegrationTestOutputFolder = new File( outputDirectory,
                                                                  Constants.SRC + File.separator + Constants.TEST
                                                                      + File.separator + Constants.RESOURCES
                                                                      + File.separator + "projects" );

            if ( archetypeIntegrationTestInputFolder.exists() )
            {
                getLogger().info( "Copying: " + archetypeIntegrationTestInputFolder.getAbsolutePath() + " into "
                                      + archetypeIntegrationTestOutputFolder.getAbsolutePath() );

                FileUtils.copyDirectoryStructure( archetypeIntegrationTestInputFolder,
                                                  archetypeIntegrationTestOutputFolder );

            }
            InvocationRequest internalRequest = new DefaultInvocationRequest();
            internalRequest.setPomFile( archetypePomFile );
            internalRequest.setUserSettingsFile( request.getSettingsFile() );
            internalRequest.setGoals( Collections.singletonList( request.getPostPhase() ) );
            if ( request.getLocalRepository() != null )
            {
                internalRequest.setLocalRepositoryDirectory( new File( request.getLocalRepository().getBasedir() ) );
            }
            
            String httpsProtocols = System.getProperty( "https.protocols" );
            if ( httpsProtocols != null )
            {
                Properties userProperties = new Properties();
                userProperties.setProperty( "https.protocols", httpsProtocols );
                internalRequest.setProperties( userProperties );
            }

            InvocationResult invokerResult = invoker.execute( internalRequest );
            if ( invokerResult.getExitCode() != 0 )
            {
                if ( invokerResult.getExecutionException() != null )
                {
                    throw invokerResult.getExecutionException();
                }
                else
                {
                    throw new Exception( "Invoker process ended with result different than 0!" );
                }
            }

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
     * @since 2.0-alpha-5
     */
    private void createArchetypeBasicIt( ArchetypeDescriptor archetypeDescriptor, File generatedSourcesDirectory )
        throws IOException
    {
        String basic =
            Constants.SRC + File.separator + Constants.TEST + File.separator + Constants.RESOURCES + File.separator
                + "projects" + File.separator + "basic";
        File basicItDirectory = new File( generatedSourcesDirectory, basic );
        basicItDirectory.mkdirs();

        File archetypePropertiesFile = new File( basicItDirectory, "archetype.properties" );
        if ( !archetypePropertiesFile.exists() && !archetypePropertiesFile.createNewFile() )
        {
            getLogger().warn( "Could not create new file \"" + archetypePropertiesFile.getPath()
                    + "\" or the file already exists." );
        }

        try ( InputStream in = FilesetArchetypeCreator.class.getResourceAsStream( "archetype.properties" );
              OutputStream out = new FileOutputStream( archetypePropertiesFile ) )
        {
            Properties archetypeProperties = new Properties();
            archetypeProperties.load( in );

            for ( RequiredProperty req : archetypeDescriptor.getRequiredProperties() )
            {
                archetypeProperties.put( req.getKey(), req.getDefaultValue() );
            }

            archetypeProperties.store( out, null );
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
    private File createArchetypeProjectPom( MavenProject project, ProjectBuildingRequest buildingRequest,
                                            Properties configurationProperties, File projectDir )
        throws TemplateCreationException, IOException
    {
        Model model = new Model();
        model.setModelVersion( "4.0.0" );
        // these values should be retrieved from the request with sensible defaults
        model.setGroupId( configurationProperties.getProperty( Constants.ARCHETYPE_GROUP_ID, project.getGroupId() ) );
        model.setArtifactId(
            configurationProperties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, project.getArtifactId() ) );
        model.setVersion( configurationProperties.getProperty( Constants.ARCHETYPE_VERSION, project.getVersion() ) );
        model.setPackaging( "maven-archetype" );
        model.setName(
            configurationProperties.getProperty( Constants.ARCHETYPE_ARTIFACT_ID, project.getArtifactId() ) );
        model.setUrl( configurationProperties.getProperty( Constants.ARCHETYPE_URL, project.getUrl() ) );
        model.setDescription(
            configurationProperties.getProperty( Constants.ARCHETYPE_DESCRIPTION, project.getDescription() ) );
        model.setLicenses( project.getLicenses() );
        model.setDevelopers( project.getDevelopers() );
        model.setScm( project.getScm() );
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
            MavenProject p = project.getParent();

            if ( p.getDistributionManagement() != null )
            {
                model.setDistributionManagement( p.getDistributionManagement() );
            }

            if ( p.getBuildExtensions() != null )
            {
                for ( Extension be : p.getBuildExtensions() )
                {
                    model.getBuild().addExtension( be );
                }
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
        if ( !destination.exists() && !destination.createNewFile() )
        {
            getLogger().warn( "Could not create new file \"" + destination.getPath()
                    + "\" or the file already exists." );
        }

        try ( InputStream in = FilesetArchetypeCreator.class.getResourceAsStream( name );
              OutputStream out = new FileOutputStream( destination ) )
        {
            IOUtil.copy( in, out );
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
        requiredProperties.remove( Constants.EXCLUDE_PATTERNS );

        for ( Iterator<?> propertiesIterator = requiredProperties.keySet().iterator(); propertiesIterator.hasNext(); )
        {
            String propertyKey = (String) propertiesIterator.next();

            RequiredProperty requiredProperty = new RequiredProperty();
            requiredProperty.setKey( propertyKey );
            requiredProperty.setDefaultValue( requiredProperties.getProperty( propertyKey ) );

            archetypeDescriptor.addRequiredProperty( requiredProperty );

            getLogger().debug(
                "Adding requiredProperty " + propertyKey + "=" + requiredProperties.getProperty( propertyKey )
                    + " to archetype's descriptor" );
        }
    }

    private void createModulePoms( Properties pomReversedProperties, String rootArtifactId, String packageName,
                                   File basedir, File archetypeFilesDirectory, boolean preserveCData,
                                   boolean keepParent )
        throws FileNotFoundException, IOException, XmlPullParserException
    {
        Model pom = pomManager.readPom( FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ) );

        String parentArtifactId = pomReversedProperties.getProperty( Constants.PARENT_ARTIFACT_ID );
        String artifactId = pom.getArtifactId();
        setParentArtifactId( pomReversedProperties, pomReversedProperties.getProperty( Constants.ARTIFACT_ID ) );
        setArtifactId( pomReversedProperties, pom.getArtifactId() );

        for ( String subModuleId : pom.getModules() )
        {
            String subModuleIdDirectory = subModuleId;

            if ( subModuleId.indexOf( rootArtifactId ) >= 0 )
            {
                subModuleIdDirectory = StringUtils.replace( subModuleId, rootArtifactId, "__rootArtifactId__" );
            }

            createModulePoms( pomReversedProperties, rootArtifactId, packageName,
                              FileUtils.resolveFile( basedir, subModuleId ),
                              FileUtils.resolveFile( archetypeFilesDirectory, subModuleIdDirectory ), preserveCData,
                              keepParent );
        }

        createModulePom( pom, rootArtifactId, archetypeFilesDirectory, pomReversedProperties,
                         FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ), preserveCData, keepParent );

        restoreParentArtifactId( pomReversedProperties, parentArtifactId );
        restoreArtifactId( pomReversedProperties, artifactId );
    }

    private void createPoms( Model pom, String rootArtifactId, String artifactId, File archetypeFilesDirectory,
                             File basedir, File rootPom, Properties pomReversedProperties, boolean preserveCData,
                             boolean keepParent )
        throws IOException, FileNotFoundException, XmlPullParserException
    {
        setArtifactId( pomReversedProperties, pom.getArtifactId() );

        for ( String moduleId : pom.getModules() )
        {
            String moduleIdDirectory = moduleId;

            if ( moduleId.indexOf( rootArtifactId ) >= 0 )
            {
                moduleIdDirectory = StringUtils.replace( moduleId, rootArtifactId, "__rootArtifactId__" );
            }

            createModulePoms( pomReversedProperties, rootArtifactId, moduleId,
                              FileUtils.resolveFile( basedir, moduleId ),
                              new File( archetypeFilesDirectory, moduleIdDirectory ), preserveCData, keepParent );
        }

        restoreParentArtifactId( pomReversedProperties, null );
        restoreArtifactId( pomReversedProperties, artifactId );

        createArchetypePom( pom, archetypeFilesDirectory, pomReversedProperties, rootPom, preserveCData, keepParent );
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
            for ( Dependency dependency : pom.getDependencies() )
            {
                rewriteDependencyReferences( dependency, rootArtifactId, groupId );
            }
        }

        // rewrite DependencyManagement
        if ( pom.getDependencyManagement() != null && pom.getDependencyManagement().getDependencies() != null
            && !pom.getDependencyManagement().getDependencies().isEmpty() )
        {
            for ( Dependency dependency : pom.getDependencyManagement().getDependencies() )
            {
                rewriteDependencyReferences( dependency, rootArtifactId, groupId );
            }
        }

        // rewrite Plugins
        if ( pom.getBuild() != null && pom.getBuild().getPlugins() != null && !pom.getBuild().getPlugins().isEmpty() )
        {
            for ( Plugin plugin : pom.getBuild().getPlugins() )
            {
                rewritePluginReferences( plugin, rootArtifactId, groupId );
            }
        }

        // rewrite PluginManagement
        if ( pom.getBuild() != null && pom.getBuild().getPluginManagement() != null
            && pom.getBuild().getPluginManagement().getPlugins() != null
            && !pom.getBuild().getPluginManagement().getPlugins().isEmpty() )
        {
            for ( Plugin plugin : pom.getBuild().getPluginManagement().getPlugins() )
            {
                rewritePluginReferences( plugin, rootArtifactId, groupId );
            }
        }

        // rewrite Profiles
        if ( pom.getProfiles() != null )
        {
            for ( Profile profile : pom.getProfiles() )
            {
                // rewrite Dependencies
                if ( profile.getDependencies() != null && !profile.getDependencies().isEmpty() )
                {
                    for ( Dependency dependency : profile.getDependencies() )
                    {
                        rewriteDependencyReferences( dependency, rootArtifactId, groupId );
                    }
                }

                // rewrite DependencyManagement
                if ( profile.getDependencyManagement() != null
                    && profile.getDependencyManagement().getDependencies() != null
                    && !profile.getDependencyManagement().getDependencies().isEmpty() )
                {
                    for ( Dependency dependency : profile.getDependencyManagement().getDependencies() )
                    {
                        rewriteDependencyReferences( dependency, rootArtifactId, groupId );
                    }
                }

                // rewrite Plugins
                if ( profile.getBuild() != null && profile.getBuild().getPlugins() != null
                    && !profile.getBuild().getPlugins().isEmpty() )
                {
                    for ( Plugin plugin : profile.getBuild().getPlugins() )
                    {
                        rewritePluginReferences( plugin, rootArtifactId, groupId );
                    }
                }

                // rewrite PluginManagement
                if ( profile.getBuild() != null && profile.getBuild().getPluginManagement() != null
                    && profile.getBuild().getPluginManagement().getPlugins() != null
                    && !profile.getBuild().getPluginManagement().getPlugins().isEmpty() )
                {
                    for ( Plugin plugin : profile.getBuild().getPluginManagement().getPlugins() )
                    {
                        rewritePluginReferences( plugin, rootArtifactId, groupId );
                    }
                }
            }
        }
    }

    private void rewriteDependencyReferences( Dependency dependency, String rootArtifactId, String groupId )
    {
        if ( dependency.getArtifactId() != null && dependency.getArtifactId().indexOf( rootArtifactId ) >= 0 )
        {
            if ( dependency.getGroupId() != null )
            {
                dependency.setGroupId(
                    StringUtils.replace( dependency.getGroupId(), groupId, "${" + Constants.GROUP_ID + "}" ) );
            }

            dependency.setArtifactId(
                StringUtils.replace( dependency.getArtifactId(), rootArtifactId, "${rootArtifactId}" ) );

            if ( dependency.getVersion() != null )
            {
                dependency.setVersion( "${" + Constants.VERSION + "}" );
            }
        }
    }

    private void rewritePluginReferences( Plugin plugin, String rootArtifactId, String groupId )
    {
        if ( plugin.getArtifactId() != null && plugin.getArtifactId().indexOf( rootArtifactId ) >= 0 )
        {
            if ( plugin.getGroupId() != null )
            {
                String g = StringUtils.replace( plugin.getGroupId(), groupId, "${" + Constants.GROUP_ID + "}" );
                plugin.setGroupId( g );
            }

            plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(), rootArtifactId, "${rootArtifactId}" ) );

            if ( plugin.getVersion() != null )
            {
                plugin.setVersion( "${" + Constants.VERSION + "}" );
            }
        }

        if ( plugin.getArtifactId() != null && "maven-ear-plugin".equals( plugin.getArtifactId() ) )
        {
            rewriteEARPluginReferences( plugin, rootArtifactId, groupId );
        }
    }

    private void rewriteEARPluginReferences( Plugin plugin, String rootArtifactId, String groupId )
    {
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        if ( configuration != null )
        {
            Xpp3Dom[] modules = configuration.getChild( "modules" ).getChildren();
            for ( int i = 0; i < modules.length; i++ )
            {
                Xpp3Dom module = modules[i];
                Xpp3Dom moduleGroupId = module.getChild( "groupId" );
                Xpp3Dom moduleArtifactId = module.getChild( "artifactId" );
                Xpp3Dom moduleBundleFileName = module.getChild( "bundleFileName" );
                Xpp3Dom moduleModuleId = module.getChild( "moduleId" );
                Xpp3Dom moduleContextRoot = module.getChild( "contextRoot" );

                if ( moduleGroupId != null )
                {
                    moduleGroupId.setValue(
                        StringUtils.replace( moduleGroupId.getValue(), groupId, "${" + Constants.GROUP_ID + "}" ) );
                }

                if ( moduleArtifactId != null )
                {
                    moduleArtifactId.setValue(
                        StringUtils.replace( moduleArtifactId.getValue(), rootArtifactId, "${rootArtifactId}" ) );
                }

                if ( moduleBundleFileName != null )
                {
                    moduleBundleFileName.setValue(
                        StringUtils.replace( moduleBundleFileName.getValue(), rootArtifactId, "${rootArtifactId}" ) );
                }

                if ( moduleModuleId != null )
                {
                    moduleModuleId.setValue(
                        StringUtils.replace( moduleModuleId.getValue(), rootArtifactId, "${rootArtifactId}" ) );
                }

                if ( moduleContextRoot != null )
                {
                    moduleContextRoot.setValue(
                        StringUtils.replace( moduleContextRoot.getValue(), rootArtifactId, "${rootArtifactId}" ) );
                }
            }
        }
    }

    private void setArtifactId( Properties properties, String artifactId )
    {
        properties.setProperty( Constants.ARTIFACT_ID, artifactId );
    }

    private List<String> concatenateToList( List<String> toConcatenate, String with )
    {
        List<String> result = new ArrayList<>( toConcatenate.size() );

        for ( String concatenate : toConcatenate )
        {
            result.add( ( ( with.length() > 0 ) ? ( with + "/" + concatenate ) : concatenate ) );
        }

        return result;
    }

    private List<String> addLists( List<String> list, List<String> other )
    {
        List<String> result = new ArrayList<>( list.size() + other.size() );
        result.addAll( list );
        result.addAll( other );
        return result;
    }

    private void copyFiles( File basedir, File archetypeFilesDirectory, String directory, List<String> fileSetResources,
                            boolean packaged, String packageName, Properties reverseProperties )
        throws IOException
    {
        String packageAsDirectory = StringUtils.replace( packageName, ".", File.separator );

        getLogger().debug( "Package as Directory: Package:" + packageName + "->" + packageAsDirectory );

        for ( String inputFileName : fileSetResources )
        {
            String outputFileName = packaged
                ? StringUtils.replace( inputFileName, packageAsDirectory + File.separator, "" )
                : inputFileName;
            getLogger().debug( "InputFileName:" + inputFileName );
            getLogger().debug( "OutputFileName:" + outputFileName );

            reverseProperties.remove( "archetype.languages" );

            String reversedOutputFilename = getReversedFilename( outputFileName, reverseProperties );

            File outputFile = new File( archetypeFilesDirectory, reversedOutputFilename );

            File inputFile = new File( basedir, inputFileName );

            outputFile.getParentFile().mkdirs();

            FileUtils.copyFile( inputFile, outputFile );
        }
    }

    private void createArchetypeFiles( Properties reverseProperties, List<FileSet> fileSets, String packageName,
                                       File basedir, File archetypeFilesDirectory, String defaultEncoding,
                                       List<String> excludePatterns )
        throws IOException
    {
        getLogger().debug( "Creating Archetype/Module files from " + basedir + " to " + archetypeFilesDirectory );

        for ( FileSet fileSet : fileSets )
        {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( basedir );
            scanner.setIncludes( concatenateToList( fileSet.getIncludes(), fileSet.getDirectory() ).toArray(
                new String[fileSet.getIncludes().size()] ) );
            scanner.setExcludes( addLists( fileSet.getExcludes(), excludePatterns ).toArray(
                new String[fileSet.getExcludes().size()] ) );
            scanner.addDefaultExcludes();
            getLogger().debug( "Using fileset " + fileSet );
            scanner.scan();

            List<String> fileSetResources = Arrays.asList( scanner.getIncludedFiles() );
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
                           fileSet.isPackaged(), packageName, reverseProperties );
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

            outputFile.getParentFile().mkdirs();

            try ( Reader in = ReaderFactory.newXmlReader( inputFile );
                  Writer out = WriterFactory.newXmlWriter( outputFile ) )
            {
                String initialcontent = IOUtil.toString( in );

                String content = getReversedContent( initialcontent, pomReversedProperties );

                IOUtil.copy( content, out );
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
            pom.setName( getReversedPlainContent( pom.getName(), pomReversedProperties ) );
            pom.setDescription( getReversedPlainContent( pom.getDescription(), pomReversedProperties ) );
            pom.setUrl( getReversedPlainContent( pom.getUrl(), pomReversedProperties ) );

            rewriteReferences( pom, pomReversedProperties.getProperty( Constants.ARTIFACT_ID ),
                               pomReversedProperties.getProperty( Constants.GROUP_ID ) );

            pomManager.writePom( pom, outputFile, initialPomFile );
        }

        try ( Reader in = ReaderFactory.newXmlReader( initialPomFile ) )
        {
            String initialcontent = IOUtil.toString( in );

            Iterator<?> properties = pomReversedProperties.keySet().iterator();
            while ( properties.hasNext() )
            {
                String property = (String) properties.next();

                if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
                {
                    getLogger().warn(
                        "Archetype uses ${" + property + "} for internal processing, but file " + initialPomFile
                            + " contains this property already" );
                }
            }
        }
    }

    private FileSet createFileSet( final List<String> excludes, final boolean packaged, final boolean filtered,
                                   final String group, final List<String> includes, String defaultEncoding )
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

    private List<FileSet> createFileSets( List<String> files, int level, boolean packaged, String packageName,
                                          boolean filtered, String defaultEncoding )
    {
        List<FileSet> fileSets = new ArrayList<>();

        if ( !files.isEmpty() )
        {
            getLogger().debug( "Creating filesets" + ( packaged ? ( " packaged (" + packageName + ")" ) : "" ) + (
                filtered
                    ? " filtered"
                    : "" ) + " at level " + level );
            if ( level == 0 )
            {
                List<String> includes = new ArrayList<>( files );
                List<String> excludes = new ArrayList<>();

                if ( !includes.isEmpty() )
                {
                    fileSets.add( createFileSet( excludes, packaged, filtered, "", includes, defaultEncoding ) );
                }
            }
            else
            {
                Map<String, List<String>> groups = getGroupsMap( files, level );

                for ( String group : groups.keySet() )
                {
                    getLogger().debug( "Creating filesets for group " + group );

                    if ( !packaged )
                    {
                        fileSets.add( getUnpackagedFileSet( filtered, group, groups.get( group ), defaultEncoding ) );
                    }
                    else
                    {
                        fileSets.addAll(
                            getPackagedFileSets( filtered, group, groups.get( group ), packageName, defaultEncoding ) );
                    }
                }
            } // end if

            getLogger().debug( "Resolved fileSets " + fileSets );
        } // end if

        return fileSets;
    }

    private ModuleDescriptor createModule( Properties reverseProperties, String rootArtifactId, String moduleId,
                                           String packageName, File basedir, File archetypeFilesDirectory,
                                           List<String> languages, List<String> filtereds, String defaultEncoding,
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

        List<String> excludePatterns =
            reverseProperties.getProperty( Constants.EXCLUDE_PATTERNS ) != null
                ? Arrays.asList( StringUtils.split( reverseProperties.getProperty( Constants.EXCLUDE_PATTERNS ), "," ) )
                : Collections.<String>emptyList();

        List<String> fileNames = resolveFileNames( pom, basedir, excludePatterns );

        List<FileSet> filesets = resolveFileSets( packageName, fileNames, languages, filtereds, defaultEncoding );
        getLogger().debug( "Resolved filesets for module " + archetypeDescriptor.getName() );

        archetypeDescriptor.setFileSets( filesets );

        createArchetypeFiles( reverseProperties, filesets, packageName, basedir, archetypeFilesDirectory,
                              defaultEncoding, excludePatterns );
        getLogger().debug( "Created files for module " + archetypeDescriptor.getName() );

        String parentArtifactId = reverseProperties.getProperty( Constants.PARENT_ARTIFACT_ID );
        setParentArtifactId( reverseProperties, pom.getArtifactId() );

        for ( String subModuleId : pom.getModules() )
        {
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

            outputFile.getParentFile().mkdirs();

            try ( Reader in = ReaderFactory.newXmlReader( inputFile );
                  Writer out = WriterFactory.newXmlWriter( outputFile ) )
            {
                String initialcontent = IOUtil.toString( in );

                String content = getReversedContent( initialcontent, pomReversedProperties );

                IOUtil.copy( content, out );
            }

            inputFile.delete();
        }
        else
        {
            if ( pom.getParent() != null )
            {
                pom.getParent().setGroupId( StringUtils.replace( pom.getParent().getGroupId(),
                                                                 pomReversedProperties.getProperty(
                                                                     Constants.GROUP_ID ),
                                                                 "${" + Constants.GROUP_ID + "}" ) );
                if ( pom.getParent().getArtifactId() != null
                    && pom.getParent().getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    pom.getParent().setArtifactId(
                        StringUtils.replace( pom.getParent().getArtifactId(), rootArtifactId, "${rootArtifactId}" ) );
                }
                if ( pom.getParent().getVersion() != null )
                {
                    pom.getParent().setVersion( "${" + Constants.VERSION + "}" );
                }
            }
            pom.setModules( null );

            if ( pom.getGroupId() != null )
            {
                pom.setGroupId(
                    StringUtils.replace( pom.getGroupId(), pomReversedProperties.getProperty( Constants.GROUP_ID ),
                                         "${" + Constants.GROUP_ID + "}" ) );
            }

            pom.setArtifactId( "${" + Constants.ARTIFACT_ID + "}" );

            if ( pom.getVersion() != null )
            {
                pom.setVersion( "${" + Constants.VERSION + "}" );
            }

            pom.setName( getReversedPlainContent( pom.getName(), pomReversedProperties ) );
            pom.setDescription( getReversedPlainContent( pom.getDescription(), pomReversedProperties ) );
            pom.setUrl( getReversedPlainContent( pom.getUrl(), pomReversedProperties ) );

            rewriteReferences( pom, rootArtifactId, pomReversedProperties.getProperty( Constants.GROUP_ID ) );

            pomManager.writePom( pom, outputFile, initialPomFile );
        }

        try ( Reader in = ReaderFactory.newXmlReader( initialPomFile ) )
        {
            String initialcontent = IOUtil.toString( in );

            for ( Iterator<?> properties = pomReversedProperties.keySet().iterator(); properties.hasNext(); )
            {
                String property = (String) properties.next();

                if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
                {
                    getLogger().warn(
                        "OldArchetype uses ${" + property + "} for internal processing, but file " + initialPomFile
                            + " contains this property already" );
                }
            }
        }
    }

    private Set<String> getExtensions( List<String> files )
    {
        Set<String> extensions = new HashSet<>();

        for ( String file : files )
        {
            extensions.add( FileUtils.extension( file ) );
        }

        return extensions;
    }

    private Map<String, List<String>> getGroupsMap( final List<String> files, final int level )
    {
        Map<String, List<String>> groups = new HashMap<>();

        for ( String file : files )
        {
            String directory = PathUtils.getDirectory( file, level );
            // make all groups have unix style
            directory = StringUtils.replace( directory, File.separator, "/" );

            if ( !groups.containsKey( directory ) )
            {
                groups.put( directory, new ArrayList<String>() );
            }

            List<String> group = groups.get( directory );

            String innerPath = file.substring( directory.length() + 1 );
            // make all groups have unix style
            innerPath = StringUtils.replace( innerPath, File.separator, "/" );

            group.add( innerPath );
        }

        getLogger().debug( "Sorted " + groups.size() + " groups in " + files.size() + " files" );
        getLogger().debug( "Sorted Files: " + files );

        return groups;
    }

    private FileSet getPackagedFileSet( final boolean filtered, final Set<String> packagedExtensions,
                                        final String group, final Set<String> unpackagedExtensions,
                                        final List<String> unpackagedFiles, String defaultEncoding )
    {
        List<String> includes = new ArrayList<>();
        List<String> excludes = new ArrayList<>();

        for ( String extension : packagedExtensions )
        {
            includes.add( "**/*." + extension );

            if ( unpackagedExtensions.contains( extension ) )
            {
                excludes.addAll( archetypeFilesResolver.getFilesWithExtension( unpackagedFiles, extension ) );
            }
        }

        return createFileSet( excludes, true, filtered, group, includes, defaultEncoding );
    }

    private List<FileSet> getPackagedFileSets( final boolean filtered, final String group,
                                               final List<String> groupFiles, final String packageName,
                                               String defaultEncoding )
    {
        String packageAsDir = StringUtils.replace( packageName, ".", "/" );

        List<FileSet> packagedFileSets = new ArrayList<>();
        List<String> packagedFiles = archetypeFilesResolver.getPackagedFiles( groupFiles, packageAsDir );
        getLogger().debug( "Found packaged Files:" + packagedFiles );

        List<String> unpackagedFiles = archetypeFilesResolver.getUnpackagedFiles( groupFiles, packageAsDir );
        getLogger().debug( "Found unpackaged Files:" + unpackagedFiles );

        Set<String> packagedExtensions = getExtensions( packagedFiles );
        getLogger().debug( "Found packaged extensions " + packagedExtensions );

        Set<String> unpackagedExtensions = getExtensions( unpackagedFiles );

        if ( !packagedExtensions.isEmpty() )
        {
            packagedFileSets.add(
                getPackagedFileSet( filtered, packagedExtensions, group, unpackagedExtensions, unpackagedFiles,
                                    defaultEncoding ) );
        }

        if ( !unpackagedExtensions.isEmpty() )
        {
            getLogger().debug( "Found unpackaged extensions " + unpackagedExtensions );

            packagedFileSets.add(
                getUnpackagedFileSet( filtered, unpackagedExtensions, unpackagedFiles, group, packagedExtensions,
                                      defaultEncoding ) );
        }

        return packagedFileSets;
    }

    private void setParentArtifactId( Properties properties, String parentArtifactId )
    {
        properties.setProperty( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
    }

    private void processFileSet( File basedir, File archetypeFilesDirectory, String directory,
                                 List<String> fileSetResources, boolean packaged, String packageName,
                                 Properties reverseProperties, String defaultEncoding )
        throws IOException
    {
        String packageAsDirectory = StringUtils.replace( packageName, ".", File.separator );

        getLogger().debug( "Package as Directory: Package:" + packageName + "->" + packageAsDirectory );

        for ( String inputFileName : fileSetResources )
        {
            String initialFilename = packaged
                ? StringUtils.replace( inputFileName, packageAsDirectory + File.separator, "" )
                : inputFileName;

            getLogger().debug( "InputFileName:" + inputFileName );

            File inputFile = new File( basedir, inputFileName );

            FileCharsetDetector detector = new FileCharsetDetector( inputFile );

            String fileEncoding = detector.isFound() ? detector.getCharset() : defaultEncoding;

            String initialcontent = IOUtil.toString( new FileInputStream( inputFile ), fileEncoding );

            for ( Iterator<?> properties = reverseProperties.keySet().iterator(); properties.hasNext(); )
            {
                String property = (String) properties.next();

                if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
                {
                    getLogger().warn(
                        "Archetype uses ${" + property + "} for internal processing, but file " + inputFile
                            + " contains this property already" );
                }
            }

            String content = getReversedContent( initialcontent, reverseProperties );
            String outputFilename = getReversedFilename( initialFilename, reverseProperties );

            getLogger().debug( "OutputFileName:" + outputFilename );

            File outputFile = new File( archetypeFilesDirectory, outputFilename );
            outputFile.getParentFile().mkdirs();

            if ( !outputFile.exists() && !outputFile.createNewFile() )
            {
                getLogger().warn( "Could not create new file \"" + outputFile.getPath()
                        + "\" or the file already exists." );
            }

            try ( OutputStream os = new FileOutputStream( outputFile ) )
            {
                write( content, os, fileEncoding );
            }
        }
    }

    private Properties getReversedProperties( ArchetypeDescriptor archetypeDescriptor, Properties properties )
    {
        Properties reversedProperties = new Properties();

        reversedProperties.putAll( properties );
        reversedProperties.remove( Constants.ARCHETYPE_GROUP_ID );
        reversedProperties.remove( Constants.ARCHETYPE_ARTIFACT_ID );
        reversedProperties.remove( Constants.ARCHETYPE_VERSION );

        String packageName = properties.getProperty( Constants.PACKAGE );
        String packageInPathFormat = getPackageInPathFormat( packageName );
        if ( !packageInPathFormat.equals( packageName ) )
        {
            reversedProperties.setProperty( Constants.PACKAGE_IN_PATH_FORMAT, packageInPathFormat );
        }

        // TODO check that reversed properties are all different and no one is a substring of another?
        // to avoid wrong variable replacements

        return reversedProperties;
    }

    private List<String> resolveFileNames( final Model pom, final File basedir, List<String> excludePatterns )
        throws IOException
    {
        getLogger().debug( "Resolving files for " + pom.getId() + " in " + basedir );

        StringBuilder buff = new StringBuilder( "pom.xml*,archetype.properties*,target/**," );
        for ( String module : pom.getModules() )
        {
            buff.append( ',' ).append( module ).append( "/**" );
        }

        for ( String defaultExclude : ListScanner.DEFAULTEXCLUDES )
        {
            buff.append( ',' ).append( defaultExclude ).append( "/**" );
        }

        for ( String excludePattern : excludePatterns )
        {
            buff.append( ',' ).append( excludePattern );
        }

        String excludes = PathUtils.convertPathForOS( buff.toString() );

        List<String> fileNames = FileUtils.getFileNames( basedir, "**,.*,**/.*", excludes, false );

        getLogger().debug( "Resolved " + fileNames.size() + " files" );
        getLogger().debug( "Resolved Files:" + fileNames );

        return fileNames;
    }

    private List<FileSet> resolveFileSets( String packageName, List<String> fileNames, List<String> languages,
                                           List<String> filtereds, String defaultEncoding )
    {
        List<FileSet> resolvedFileSets = new ArrayList<>();
        getLogger().debug(
            "Resolving filesets with package=" + packageName + ", languages=" + languages + " and extentions="
                + filtereds );

        List<String> files = new ArrayList<>( fileNames );

        StringBuilder languageIncludes = new StringBuilder();

        for ( String language : languages )
        {
            languageIncludes.append( ( ( languageIncludes.length() == 0 ) ? "" : "," ) + language + "/**" );
        }

        getLogger().debug( "Using languages includes " + languageIncludes );

        StringBuilder filteredIncludes = new StringBuilder();
        for ( String filtered : filtereds )
        {
            filteredIncludes.append(
                ( ( filteredIncludes.length() == 0 ) ? "" : "," ) + "**/" + ( filtered.startsWith( "." ) ? "" : "*." )
                    + filtered );
        }

        getLogger().debug( "Using filtered includes " + filteredIncludes );

        /* sourcesMainFiles */
        List<String> sourcesMainFiles =
            archetypeFilesResolver.findSourcesMainFiles( files, languageIncludes.toString() );
        if ( !sourcesMainFiles.isEmpty() )
        {
            files.removeAll( sourcesMainFiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( sourcesMainFiles, filteredIncludes.toString() );
            sourcesMainFiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = sourcesMainFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding ) );
            }

            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding ) );
            }
        }

        /* resourcesMainFiles */
        List<String> resourcesMainFiles =
            archetypeFilesResolver.findResourcesMainFiles( files, languageIncludes.toString() );
        if ( !resourcesMainFiles.isEmpty() )
        {
            files.removeAll( resourcesMainFiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( resourcesMainFiles, filteredIncludes.toString() );
            resourcesMainFiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = resourcesMainFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 3, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, false, packageName, false, defaultEncoding ) );
            }
        }

        /* sourcesTestFiles */
        List<String> sourcesTestFiles =
            archetypeFilesResolver.findSourcesTestFiles( files, languageIncludes.toString() );
        if ( !sourcesTestFiles.isEmpty() )
        {
            files.removeAll( sourcesTestFiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( sourcesTestFiles, filteredIncludes.toString() );
            sourcesTestFiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = sourcesTestFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding ) );
            }
        }

        /* ressourcesTestFiles */
        List<String> resourcesTestFiles =
            archetypeFilesResolver.findResourcesTestFiles( files, languageIncludes.toString() );
        if ( !resourcesTestFiles.isEmpty() )
        {
            files.removeAll( resourcesTestFiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( resourcesTestFiles, filteredIncludes.toString() );
            resourcesTestFiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = resourcesTestFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 3, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, false, packageName, false, defaultEncoding ) );
            }
        }

        /* siteFiles */
        List<String> siteFiles = archetypeFilesResolver.findSiteFiles( files, languageIncludes.toString() );
        if ( !siteFiles.isEmpty() )
        {
            files.removeAll( siteFiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( siteFiles, filteredIncludes.toString() );
            siteFiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = siteFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 2, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 2, false, packageName, false, defaultEncoding ) );
            }
        }

        /* thirdLevelSourcesfiles */
        List<String> thirdLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources( 3, files, languageIncludes.toString() );
        if ( !thirdLevelSourcesfiles.isEmpty() )
        {
            files.removeAll( thirdLevelSourcesfiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( thirdLevelSourcesfiles, filteredIncludes.toString() );
            thirdLevelSourcesfiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = thirdLevelSourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding ) );
            }

            /* thirdLevelResourcesfiles */
            List<String> thirdLevelResourcesfiles =
                archetypeFilesResolver.findOtherResources( 3, files, thirdLevelSourcesfiles,
                                                           languageIncludes.toString() );
            if ( !thirdLevelResourcesfiles.isEmpty() )
            {
                files.removeAll( thirdLevelResourcesfiles );
                filteredFiles =
                    archetypeFilesResolver.getFilteredFiles( thirdLevelResourcesfiles, filteredIncludes.toString() );
                thirdLevelResourcesfiles.removeAll( filteredFiles );
                unfilteredFiles = thirdLevelResourcesfiles;
                if ( !filteredFiles.isEmpty() )
                {
                    resolvedFileSets.addAll(
                        createFileSets( filteredFiles, 3, false, packageName, true, defaultEncoding ) );
                }
                if ( !unfilteredFiles.isEmpty() )
                {
                    resolvedFileSets.addAll(
                        createFileSets( unfilteredFiles, 3, false, packageName, false, defaultEncoding ) );
                }
            }
        } // end if

        /* secondLevelSourcesfiles */
        List<String> secondLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources( 2, files, languageIncludes.toString() );
        if ( !secondLevelSourcesfiles.isEmpty() )
        {
            files.removeAll( secondLevelSourcesfiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( secondLevelSourcesfiles, filteredIncludes.toString() );
            secondLevelSourcesfiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = secondLevelSourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll( createFileSets( filteredFiles, 2, true, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 2, true, packageName, false, defaultEncoding ) );
            }
        }

        /* secondLevelResourcesfiles */
        List<String> secondLevelResourcesfiles =
            archetypeFilesResolver.findOtherResources( 2, files, languageIncludes.toString() );
        if ( !secondLevelResourcesfiles.isEmpty() )
        {
            files.removeAll( secondLevelResourcesfiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( secondLevelResourcesfiles, filteredIncludes.toString() );
            secondLevelResourcesfiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = secondLevelResourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 2, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 2, false, packageName, false, defaultEncoding ) );
            }
        }

        /* rootResourcesfiles */
        List<String> rootResourcesfiles =
            archetypeFilesResolver.findOtherResources( 0, files, languageIncludes.toString() );
        if ( !rootResourcesfiles.isEmpty() )
        {
            files.removeAll( rootResourcesfiles );

            List<String> filteredFiles =
                archetypeFilesResolver.getFilteredFiles( rootResourcesfiles, filteredIncludes.toString() );
            rootResourcesfiles.removeAll( filteredFiles );

            List<String> unfilteredFiles = rootResourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 0, false, packageName, true, defaultEncoding ) );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 0, false, packageName, false, defaultEncoding ) );
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
        result = getReversedPlainContent( result, properties );

        // TODO: Replace velocity to a better engine...
        return "#set( $symbol_pound = '#' )\n" + "#set( $symbol_dollar = '$' )\n" + "#set( $symbol_escape = '\\' )\n"
            + StringUtils.replace( result, "#", "${symbol_pound}" );
    }

    private String getReversedPlainContent( String content, Properties properties )
    {
        String result = content;

        for ( Iterator<?> propertyIterator = properties.keySet().iterator(); propertyIterator.hasNext(); )
        {
            String propertyKey = (String) propertyIterator.next();

            result = StringUtils.replace( result, properties.getProperty( propertyKey ), "${" + propertyKey + "}" );
        }
        return result;
    }

    private String getReversedFilename( String filename, Properties properties )
    {
        String result = filename;

        for ( Iterator<?> propertyIterator = properties.keySet().iterator(); propertyIterator.hasNext(); )
        {
            String propertyKey = (String) propertyIterator.next();

            result = StringUtils.replace( result, properties.getProperty( propertyKey ), "__" + propertyKey + "__" );
        }

        return result;
    }

    private String getTemplateOutputDirectory()
    {
        return Constants.SRC + File.separator + Constants.MAIN + File.separator + Constants.RESOURCES;
    }

    private FileSet getUnpackagedFileSet( final boolean filtered, final String group, final List<String> groupFiles,
                                          String defaultEncoding )
    {
        Set<String> extensions = getExtensions( groupFiles );

        List<String> includes = new ArrayList<>();
        List<String> excludes = new ArrayList<>();

        for ( String extension : extensions )
        {
            includes.add( "**/*." + extension );
        }

        return createFileSet( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private FileSet getUnpackagedFileSet( final boolean filtered, final Set<String> unpackagedExtensions,
                                          final List<String> unpackagedFiles, final String group,
                                          final Set<String> packagedExtensions, String defaultEncoding )
    {
        List<String> includes = new ArrayList<>();
        List<String> excludes = new ArrayList<>();

        for ( String extension : unpackagedExtensions )
        {
            if ( packagedExtensions.contains( extension ) )
            {
                includes.addAll( archetypeFilesResolver.getFilesWithExtension( unpackagedFiles, extension ) );
            }
            else
            {
                if ( StringUtils.isEmpty( extension ) )
                {
                    includes.add( "**/*" );
                }
                else
                {
                    includes.add( "**/*." + extension );
                }
            }
        }

        return createFileSet( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private static final String MAVEN_PROPERTIES =
        "META-INF/maven/org.apache.maven.archetype/archetype-common/pom.properties";

    public String getArchetypeVersion()
    {
        // This should actually come from the pom.properties at testing but it's not generated and put into the JAR, it
        // happens as part of the JAR plugin which is crap as it makes testing inconsistent.
        String version = "version";

        try ( InputStream is = getClass().getClassLoader().getResourceAsStream( MAVEN_PROPERTIES ) )
        {
            Properties properties = new Properties();

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
    }
}
