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

package org.apache.maven.archetype.creator;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualTreeBidiMap;
import org.apache.maven.archetype.common.ArchetypeConfiguration;
import org.apache.maven.archetype.common.ArchetypeDefinition;
import org.apache.maven.archetype.common.ArchetypeFactory;
import org.apache.maven.archetype.common.ArchetypeFilesResolver;
import org.apache.maven.archetype.common.ArchetypePropertiesManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.common.PomManager;
import org.apache.maven.archetype.common.util.FileCharsetDetector;
import org.apache.maven.archetype.common.util.ListScanner;
import org.apache.maven.archetype.common.util.PathUtils;
import org.apache.maven.archetype.creator.olddescriptor.OldArchetypeDescriptor;
import org.apache.maven.archetype.creator.olddescriptor.OldArchetypeDescriptorXpp3Writer;
import org.apache.maven.archetype.exception.ArchetypeNotConfigured;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
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
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/** @plexus.component role-hint="fileset" */
public class FilesetArchetypeCreator
    extends AbstractLogEnabled
    implements ArchetypeCreator
{
    /** @plexus.requirement */
    private ArchetypeFactory archetypeFactory;

    /** @plexus.requirement */
    private ArchetypeFilesResolver archetypeFilesResolver;

    /** @plexus.requirement */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /** @plexus.requirement */
    private PomManager pomManager;

    /** @plexus.requirement */
    private ArchetypeRegistryManager archetypeRegistryManager;

    public void createArchetype(
        MavenProject project,
        File propertyFile,
        List languages,
        List filtereds,
        String defaultEncoding,
        boolean ignoreReplica,
        boolean preserveCData,
        boolean keepParent,
        boolean partialArchetype,
        File archetypeRegistryFile
    )
        throws
        IOException,
        ArchetypeNotDefined,
        ArchetypeNotConfigured,
        TemplateCreationException,
        XmlPullParserException
    {
        Properties properties = initialiseArchetypeProperties( propertyFile );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition( properties );
        if ( !archetypeDefinition.isDefined() )
        {
            throw new ArchetypeNotDefined( "The archetype is not defined" );
        }

        ArchetypeConfiguration archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration( archetypeDefinition, properties );
        if ( !archetypeConfiguration.isConfigured() )
        {
            throw new ArchetypeNotConfigured( "The archetype is not configured" );
        }

        File basedir = project.getBasedir();
        File generatedSourcesDirectory =
            FileUtils.resolveFile( basedir, getGeneratedSourcesDirectory() );
        generatedSourcesDirectory.mkdirs();
        getLogger().debug( "Creating archetype in " + generatedSourcesDirectory );

        Model model = new Model();
        model.setModelVersion( "4.0.0" );

        // In many cases where we are behind a firewall making Archetypes for work mates we want
        // to simply be able to deploy the archetypes once we have created them. In order to do
        // this we want to utilize information from the project we are creating the archetype from.
        // This will be a fully working project that has been testing and inherits from a POM
        // that contains deployment information, along with any extensions required for deployment.
        // We don't want to create archetypes that cannot be deployed after we create them. People
        // might want to edit the archetype POM but they should not have too.

        if ( project.getParent() != null )
        {
            Parent parent = new Parent();
            parent.setGroupId( project.getParent().getGroupId() );
            parent.setArtifactId( project.getParent().getArtifactId() );
            parent.setVersion( project.getParent().getVersion() );
            model.setParent( parent );
        }

        model.setGroupId( archetypeDefinition.getGroupId() );
        model.setArtifactId( archetypeDefinition.getArtifactId() );
        model.setVersion( archetypeDefinition.getVersion() );
        model.setPackaging( "maven-archetype" );
        model.setName( archetypeDefinition.getArtifactId() );

        Build build = new Build();
        model.setBuild( build );

        Extension extension = new Extension();
        extension.setGroupId( "org.apache.maven.archetype" );
        extension.setArtifactId( "archetypeng-packaging" );
        extension.setVersion( "2.0-SNAPSHOT" );
        model.getBuild().addExtension( extension );

        Plugin plugin = new Plugin();
        plugin.setGroupId( "org.apache.maven.plugins" );
        plugin.setArtifactId( "maven-archetypeng-plugin" );
        plugin.setVersion( "2.0-SNAPSHOT" );
        plugin.setExtensions( true );
        model.getBuild().addPlugin( plugin );
        getLogger().debug( "Creating archetype's pom" );

        File archetypePomFile = FileUtils.resolveFile( basedir, getArchetypePom() );
        archetypePomFile.getParentFile().mkdirs();
        pomManager.writePom( model, archetypePomFile, archetypePomFile );

        File archetypeResourcesDirectory =
            FileUtils.resolveFile( generatedSourcesDirectory, getTemplateOutputDirectory() );
        archetypeResourcesDirectory.mkdirs();

        File archetypeFilesDirectory =
            FileUtils.resolveFile( archetypeResourcesDirectory, Constants.ARCHETYPE_RESOURCES );
        archetypeFilesDirectory.mkdirs();
        getLogger().debug( "Archetype's files output directory " + archetypeFilesDirectory );

        File replicaMainDirectory =
            FileUtils.resolveFile(
                generatedSourcesDirectory,
                getReplicaOutputDirectory() + File.separator
                    + archetypeDefinition.getArtifactId()
            );
        if ( !ignoreReplica )
        {
            replicaMainDirectory.mkdirs();
        }

        File replicaFilesDirectory = FileUtils.resolveFile( replicaMainDirectory, "reference" );
        if ( !ignoreReplica )
        {
            replicaFilesDirectory.mkdirs();
        }

        File archetypeDescriptorFile =
            FileUtils.resolveFile( archetypeResourcesDirectory, Constants.ARCHETYPE_DESCRIPTOR );
        archetypeDescriptorFile.getParentFile().mkdirs();

        ArchetypeDescriptor archetypeDescriptor = new ArchetypeDescriptor();
        archetypeDescriptor.setName( archetypeDefinition.getArtifactId() );
        getLogger().debug(
            "Starting archetype's descriptor " + archetypeDefinition.getArtifactId()
        );
        archetypeDescriptor.setPartial( partialArchetype );

        addRequiredProperties( archetypeDescriptor, properties );

        // TODO ensure reversedproperties contains NO dotted properties
        Properties reverseProperties = getRequiredProperties( archetypeDescriptor, properties );
        reverseProperties.remove( Constants.GROUP_ID );

        // TODO ensure pomReversedProperties contains NO dotted properties
        Properties pomReversedProperties =
            getRequiredProperties( archetypeDescriptor, properties );
        pomReversedProperties.remove( Constants.PACKAGE );

        String packageName = archetypeConfiguration.getProperty( Constants.PACKAGE );

        Model pom =
            pomManager.readPom( FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ) );

        registerProject( pom );

        List fileNames = resolveFileNames( pom, basedir );
        getLogger().debug( "Scanned for files " + fileNames.size() );

        Iterator names = fileNames.iterator();
        while ( names.hasNext() )
        {
            getLogger().debug( "- " + names.next().toString() );
        }

        List filesets =
            resolveFileSets( packageName, fileNames, languages, filtereds, defaultEncoding );
        getLogger().debug( "Resolved filesets for " + archetypeDescriptor.getName() );

        archetypeDescriptor.setFileSets( filesets );

        createArchetypeFiles(
            reverseProperties,
            filesets,
            packageName,
            basedir,
            archetypeFilesDirectory,
            defaultEncoding
        );
        getLogger().debug( "Created files for " + archetypeDescriptor.getName() );

        if ( !ignoreReplica )
        {
            createReplicaFiles( filesets, basedir, replicaFilesDirectory );
            getLogger().debug( "Created replica files for " + archetypeDescriptor.getName() );

            FileUtils.copyFile(
                propertyFile,
                new File( replicaMainDirectory, "archetype.properties" )
            );
            new File( replicaMainDirectory, "goal.txt" ).createNewFile();
        }

        setParentArtifactId(
            reverseProperties,
            archetypeConfiguration.getProperty( Constants.ARTIFACT_ID )
        );

        Iterator modules = pom.getModules().iterator();
        while ( modules.hasNext() )
        {
            String moduleId = (String) modules.next();

            getLogger().debug( "Creating module " + moduleId );

            ModuleDescriptor moduleDescriptor =
                createModule(
                    reverseProperties,
                    archetypeConfiguration.getProperty( Constants.ARTIFACT_ID ),
                    moduleId,
                    packageName,
                    FileUtils.resolveFile( basedir, moduleId ),
                    FileUtils.resolveFile( archetypeFilesDirectory, moduleId ),
                    FileUtils.resolveFile( replicaFilesDirectory, moduleId ),
                    languages,
                    filtereds,
                    defaultEncoding,
                    ignoreReplica,
                    preserveCData,
                    keepParent
                );

            archetypeDescriptor.addModule( moduleDescriptor );
            getLogger().debug(
                "Added module " + moduleDescriptor.getName() + " in "
                    + archetypeDescriptor.getName()
            );
        }
        restoreParentArtifactId( reverseProperties, null );
        restoreArtifactId(
            reverseProperties,
            archetypeConfiguration.getProperty( Constants.ARTIFACT_ID )
        );

        createPoms( pom, archetypeConfiguration.getProperty( Constants.ARTIFACT_ID ),
            archetypeConfiguration.getProperty( Constants.ARTIFACT_ID ),
            archetypeFilesDirectory, basedir,
            pomReversedProperties, preserveCData, keepParent );

        getLogger().debug( "Created Archetype " + archetypeDescriptor.getName() + " pom" );

        ArchetypeDescriptorXpp3Writer writer = new ArchetypeDescriptorXpp3Writer();
        writer.write( new FileWriter( archetypeDescriptorFile ), archetypeDescriptor );
        getLogger().debug( "Archetype " + archetypeDescriptor.getName() + " descriptor written" );

        OldArchetypeDescriptor oldDescriptor =
            convertToOldDescriptor( archetypeDescriptor.getName(), packageName, basedir );
        File oldDescriptorFile =
            FileUtils.resolveFile(
                archetypeResourcesDirectory,
                Constants.OLD_ARCHETYPE_DESCRIPTOR
            );
        archetypeDescriptorFile.getParentFile().mkdirs();
        writeOldDescriptor( oldDescriptor, oldDescriptorFile );
        getLogger().debug(
            "Archetype " + archetypeDescriptor.getName() + " old descriptor written"
        );
    }

    private void addRequiredProperties(
        ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
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

        Iterator propertiesIterator = requiredProperties.keySet().iterator();
        while ( propertiesIterator.hasNext() )
        {
            String propertyKey = (String) propertiesIterator.next();
            RequiredProperty requiredProperty = new RequiredProperty();
            requiredProperty.setKey( propertyKey );
            requiredProperty.setDefaultValue( requiredProperties.getProperty( propertyKey ) );
            archetypeDescriptor.addRequiredProperty( requiredProperty );

            getLogger().debug(
                "Adding requiredProperty " + propertyKey + "="
                    + requiredProperties.getProperty( propertyKey ) + " to archetype's descriptor"
            );
        }
    }

    private void createModulePoms(
        Properties pomReversedProperties,
        String rootArtifactId,
        String packageName,
        File basedir,
        File archetypeFilesDirectory,
        boolean preserveCData,
        boolean keepParent )
        throws
        FileNotFoundException,
        IOException,
        XmlPullParserException
    {
        Model pom =
            pomManager.readPom( FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ) );

        String parentArtifactId = pomReversedProperties.getProperty( Constants.PARENT_ARTIFACT_ID );
        String artifactId = pom.getArtifactId();
        setParentArtifactId( pomReversedProperties, pomReversedProperties.getProperty( Constants.ARTIFACT_ID ) );
        setArtifactId( pomReversedProperties, pom.getArtifactId() );

        Iterator modules = pom.getModules().iterator();
        while ( modules.hasNext() )
        {
            String subModuleId = (String) modules.next();

            createModulePoms(
                pomReversedProperties,
                rootArtifactId,
                packageName,
                FileUtils.resolveFile( basedir, subModuleId ),
                FileUtils.resolveFile( archetypeFilesDirectory, subModuleId ),
                preserveCData,
                keepParent
            );
        }
        createModulePom(
            pom,
            rootArtifactId,
            archetypeFilesDirectory,
            pomReversedProperties,
            FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ),
            preserveCData,
            keepParent
        );
        restoreParentArtifactId( pomReversedProperties, parentArtifactId );
        restoreArtifactId( pomReversedProperties, artifactId );
    }

    private void createPoms( Model pom,
                             String rootArtifactId,
                             String artifactId,
                             File archetypeFilesDirectory,
                             File basedir,
                             Properties pomReversedProperties,
                             boolean preserveCData,
                             boolean keepParent )
        throws
        IOException,
        FileNotFoundException,
        XmlPullParserException
    {
        setArtifactId( pomReversedProperties, pom.getArtifactId() );

        Iterator modules = pom.getModules().iterator();
        while ( modules.hasNext() )
        {
            String moduleId = (String) modules.next();

            createModulePoms(
                pomReversedProperties,
                rootArtifactId,
                moduleId,
                FileUtils.resolveFile( basedir, moduleId ),
                FileUtils.resolveFile( archetypeFilesDirectory, moduleId ),
                preserveCData,
                keepParent
            );
        }
        restoreParentArtifactId( pomReversedProperties, null );
        restoreArtifactId( pomReversedProperties, artifactId );

        createArchetypePom(
            pom,
            archetypeFilesDirectory,
            pomReversedProperties,
            FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ),
            preserveCData,
            keepParent
        );
    }

    private String getArchetypePom()
    {
        return getGeneratedSourcesDirectory() + File.separator + Constants.ARCHETYPE_POM;
    }

    private int id = 0;
    private BidiMap registeredProjects = new DualTreeBidiMap();

    private void registerProject( Model pom )
    {
        registeredProjects.put( new Integer( id++ ), pom.getId() );
    }

    private void rewriteReferences( Model pom,
                                    String rootArtifactId,
                                    String groupId )
    {
        // rewrite Dependencies
        if ( pom.getDependencies() != null && !pom.getDependencies().
            isEmpty() )
        {
            Iterator dependencies = pom.getDependencies().iterator();
            while ( dependencies.hasNext() )
            {
                Dependency dependency =
                    (Dependency) dependencies.next();

                if ( dependency.getArtifactId() != null &&
                    dependency.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( dependency.getGroupId() != null )
                    {
                        dependency.setGroupId( StringUtils.replace( dependency.getGroupId(),
                            groupId,
                            "${" +
                                Constants.GROUP_ID + "}" ) );
                    }
                    dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(),
                        rootArtifactId, "${rootArtifactId}" ) );
                    if ( dependency.getVersion() != null )
                    {
                        dependency.setVersion( "${" +
                            Constants.VERSION + "}" );
                    }
                }
            }
        }

        // rewrite DependencyManagement
        if ( pom.getDependencyManagement() != null &&
            pom.getDependencyManagement().getDependencies() != null &&
            !pom.getDependencyManagement().getDependencies().isEmpty() )
        {
            Iterator dependencies =
                pom.getDependencyManagement().getDependencies().iterator();
            while ( dependencies.hasNext() )
            {
                Dependency dependency =
                    (Dependency) dependencies.next();

                if ( dependency.getArtifactId() != null &&
                    dependency.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( dependency.getGroupId() != null )
                    {
                        dependency.setGroupId( StringUtils.replace( dependency.getGroupId(),
                            groupId,
                            "${" +
                                Constants.GROUP_ID + "}" ) );
                    }
                    dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(),
                        rootArtifactId, "${rootArtifactId}" ) );
                    if ( dependency.getVersion() != null )
                    {
                        dependency.setVersion( "${" +
                            Constants.VERSION + "}" );
                    }
                }
            }
        }

        // rewrite Plugins
        if ( pom.getBuild() != null && pom.getBuild().getPlugins() !=
            null && !pom.getBuild().getPlugins().isEmpty() )
        {
            Iterator plugins = pom.getBuild().getPlugins().iterator();
            while ( plugins.hasNext() )
            {
                Plugin plugin = (Plugin) plugins.next();

                if ( plugin.getArtifactId() != null &&
                    plugin.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( plugin.getGroupId() != null )
                    {
                        plugin.setGroupId( StringUtils.replace( plugin.getGroupId(),
                            groupId,
                            "${" +
                                Constants.GROUP_ID + "}" ) );
                    }
                    plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(),
                        rootArtifactId, "${rootArtifactId}" ) );
                    if ( plugin.getVersion() != null )
                    {
                        plugin.setVersion( "${" +
                            Constants.VERSION + "}" );
                    }
                }
            }
        }

        // rewrite PluginManagement
        if ( pom.getBuild() != null &&
            pom.getBuild().getPluginManagement() != null &&
            pom.getBuild().getPluginManagement().getPlugins() != null &&
            !pom.getBuild().getPluginManagement().getPlugins().
                isEmpty() )
        {
            Iterator plugins =
                pom.getBuild().getPluginManagement().getPlugins().
                    iterator();
            while ( plugins.hasNext() )
            {
                Plugin plugin = (Plugin) plugins.next();

                if ( plugin.getArtifactId() != null &&
                    plugin.getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    if ( plugin.getGroupId() != null )
                    {
                        plugin.setGroupId( StringUtils.replace( plugin.getGroupId(),
                            groupId,
                            "${" +
                                Constants.GROUP_ID + "}" ) );
                    }
                    plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(),
                        rootArtifactId, "${rootArtifactId}" ) );
                    if ( plugin.getVersion() != null )
                    {
                        plugin.setVersion( "${" +
                            Constants.VERSION + "}" );
                    }
                }
            }
        }
        // rewrite Profiles
        if ( pom.getProfiles() != null )
        {
            Iterator profiles = pom.getProfiles().iterator();
            while ( profiles.hasNext() )
            {
                Profile profile = (Profile) profiles.next();

                // rewrite Dependencies
                if ( profile.getDependencies() != null &&
                    !profile.getDependencies().isEmpty() )
                {
                    Iterator dependencies = profile.getDependencies().
                        iterator();
                    while ( dependencies.hasNext() )
                    {
                        Dependency dependency =
                            (Dependency) dependencies.next();

                        if ( dependency.getArtifactId() != null &&
                            dependency.getArtifactId().
                                indexOf( rootArtifactId ) >= 0 )
                        {
                            if ( dependency.getGroupId() != null )
                            {
                                dependency.setGroupId( StringUtils.replace( dependency.getGroupId(),
                                    groupId,
                                    "${" +
                                        Constants.GROUP_ID + "}" ) );
                            }
                            dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(),
                                rootArtifactId, "${rootArtifactId}" ) );
                            if ( dependency.getVersion() != null )
                            {
                                dependency.setVersion( "${" +
                                    Constants.VERSION + "}" );
                            }
                        }
                    }
                }

                // rewrite DependencyManagement
                if ( profile.getDependencyManagement() != null &&
                    profile.getDependencyManagement().getDependencies() !=
                        null &&
                    !profile.getDependencyManagement().getDependencies().
                        isEmpty() )
                {
                    Iterator dependencies =
                        profile.getDependencyManagement().getDependencies().
                            iterator();
                    while ( dependencies.hasNext() )
                    {
                        Dependency dependency =
                            (Dependency) dependencies.next();

                        if ( dependency.getArtifactId() != null &&
                            dependency.getArtifactId().
                                indexOf( rootArtifactId ) >= 0 )
                        {
                            if ( dependency.getGroupId() != null )
                            {
                                dependency.setGroupId( StringUtils.replace( dependency.getGroupId(),
                                    groupId,
                                    "${" +
                                        Constants.GROUP_ID + "}" ) );
                            }
                            dependency.setArtifactId( StringUtils.replace( dependency.getArtifactId(),
                                rootArtifactId, "${rootArtifactId}" ) );
                            if ( dependency.getVersion() != null )
                            {
                                dependency.setVersion( "${" +
                                    Constants.VERSION + "}" );
                            }
                        }
                    }
                }

                // rewrite Plugins
                if ( profile.getBuild() != null &&
                    profile.getBuild().getPlugins() != null &&
                    !profile.getBuild().getPlugins().isEmpty() )
                {
                    Iterator plugins = profile.getBuild().getPlugins().
                        iterator();
                    while ( plugins.hasNext() )
                    {
                        Plugin plugin =
                            (Plugin) plugins.next();

                        if ( plugin.getArtifactId() != null &&
                            plugin.getArtifactId().indexOf( rootArtifactId ) >=
                                0 )
                        {
                            if ( plugin.getGroupId() != null )
                            {
                                plugin.setGroupId( StringUtils.replace( plugin.getGroupId(),
                                    groupId,
                                    "${" +
                                        Constants.GROUP_ID + "}" ) );
                            }
                            plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(),
                                rootArtifactId, "${rootArtifactId}" ) );
                            if ( plugin.getVersion() != null )
                            {
                                plugin.setVersion( "${" +
                                    Constants.VERSION + "}" );
                            }
                        }
                    }
                }

                // rewrite PluginManagement
                if ( profile.getBuild() != null &&
                    profile.getBuild().getPluginManagement() != null &&
                    profile.getBuild().getPluginManagement().getPlugins() !=
                        null &&
                    !profile.getBuild().getPluginManagement().getPlugins().
                        isEmpty() )
                {
                    Iterator plugins =
                        profile.getBuild().getPluginManagement().
                            getPlugins().iterator();
                    while ( plugins.hasNext() )
                    {
                        Plugin plugin =
                            (Plugin) plugins.next();

                        if ( plugin.getArtifactId() != null &&
                            plugin.getArtifactId().indexOf( rootArtifactId ) >=
                                0 )
                        {
                            if ( plugin.getGroupId() != null )
                            {
                                plugin.setGroupId( StringUtils.replace( plugin.getGroupId(),
                                    groupId,
                                    "${" +
                                        Constants.GROUP_ID + "}" ) );
                            }
                            plugin.setArtifactId( StringUtils.replace( plugin.getArtifactId(),
                                rootArtifactId, "${rootArtifactId}" ) );
                            if ( plugin.getVersion() != null )
                            {
                                plugin.setVersion( "${" +
                                    Constants.VERSION + "}" );
                            }
                        }
                    }
                }
            }
        }
    }

    private void setArtifactId(
        Properties properties,
        String artifactId
    )
    {
        properties.setProperty( Constants.ARTIFACT_ID, artifactId );
    }

    private List concatenateToList( List toConcatenate,
                                    String with )
    {
        List result = new ArrayList( toConcatenate.size() );
        Iterator iterator = toConcatenate.iterator();
        while ( iterator.hasNext() )
        {
            String concatenate = (String) iterator.next();
            result.add( ( ( with.length() > 0 ) ? ( with + "/" + concatenate ) : concatenate ) );
        }
        return result;
    }

    private OldArchetypeDescriptor convertToOldDescriptor(
        String id,
        String packageName,
        File basedir
    )
        throws
        IOException
    {
        getLogger().debug( "Resolving OldArchetypeDescriptor files in " + basedir );

        String excludes = "pom.xml,archetype.properties*,**/target/**";

        Iterator defaultExcludes = Arrays.asList( ListScanner.DEFAULTEXCLUDES ).iterator();
        while ( defaultExcludes.hasNext() )
        {
            excludes += "," + (String) defaultExcludes.next() + "/**";
        }

        List fileNames = FileUtils.getFileNames( basedir, "**", excludes, false );

        getLogger().debug( "Resolved " + fileNames.size() + " files" );

        String packageAsDirectory = StringUtils.replace( packageName, '.', '/' ) + "/";

        List sources = archetypeFilesResolver.findSourcesMainFiles( fileNames, "java/**" );
        fileNames.removeAll( sources );
        sources = removePackage( sources, packageAsDirectory );

        List testSources = archetypeFilesResolver.findSourcesTestFiles( fileNames, "java/**" );
        fileNames.removeAll( testSources );
        testSources = removePackage( testSources, packageAsDirectory );

        List resources = archetypeFilesResolver.findResourcesMainFiles( fileNames, "java/**" );
        fileNames.removeAll( resources );

        List testResources = archetypeFilesResolver.findResourcesTestFiles( fileNames, "java/**" );
        fileNames.removeAll( testResources );

        List siteResources = archetypeFilesResolver.findSiteFiles( fileNames, null );
        fileNames.removeAll( siteResources );

        resources.addAll( fileNames );

        OldArchetypeDescriptor descriptor = new OldArchetypeDescriptor();
        descriptor.setId( id );
        descriptor.setSources( sources );
        descriptor.setTestSources( testSources );
        descriptor.setResources( resources );
        descriptor.setTestResources( testResources );
        descriptor.setSiteResources( siteResources );

        return descriptor;
    }

    private void copyFiles(
        File basedir,
        File archetypeFilesDirectory,
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName
    )
        throws
        IOException
    {
        String packageAsDirectory = StringUtils.replace( packageName, ".", File.separator );
        getLogger().debug(
            "Package as Directory: Package:" + packageName + "->" + packageAsDirectory
        );

        Iterator iterator = fileSetResources.iterator();

        while ( iterator.hasNext() )
        {
            String inputFileName = (String) iterator.next();

            String outputFileName =
                packaged
                    ? StringUtils.replace( inputFileName, packageAsDirectory + File.separator, "" )
                    : inputFileName;
            getLogger().debug( "InputFileName:" + inputFileName );
            getLogger().debug( "OutputFileName:" + outputFileName );

            File outputFile = new File( archetypeFilesDirectory, outputFileName );

            File inputFile = new File( basedir, inputFileName );

            outputFile.getParentFile().mkdirs();

            FileUtils.copyFile( inputFile, outputFile );
        } // end while
    }

    private void copyPom( File basedir,
                          File replicaFilesDirectory )
        throws
        IOException
    {
        FileUtils.copyFileToDirectory(
            new File( basedir, Constants.ARCHETYPE_POM ),
            replicaFilesDirectory
        );
    }

    private void createArchetypeFiles(
        Properties reverseProperties,
        List fileSets,
        String packageName,
        File basedir,
        File archetypeFilesDirectory,
        String defaultEncoding
    )
        throws
        IOException
    {
        getLogger().debug(
            "Creating Archetype/Module files from " + basedir + " to " + archetypeFilesDirectory
        );

        Iterator iterator = fileSets.iterator();

        while ( iterator.hasNext() )
        {
            FileSet fileSet = (FileSet) iterator.next();

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( basedir );
            scanner.setIncludes(
                (String[]) concatenateToList( fileSet.getIncludes(), fileSet.getDirectory() )
                    .toArray( new String[fileSet.getIncludes().size()] )
            );
            scanner.setExcludes(
                (String[]) fileSet.getExcludes().toArray(
                    new String[fileSet.getExcludes().size()]
                )
            );
            scanner.addDefaultExcludes();
            getLogger().debug( "Using fileset " + fileSet );
            scanner.scan();

            List fileSetResources = Arrays.asList( scanner.getIncludedFiles() );
            getLogger().debug( "Scanned " + fileSetResources.size() + " resources" );

            if ( fileSet.isFiltered() )
            {
                processFileSet(
                    basedir,
                    archetypeFilesDirectory,
                    fileSet.getDirectory(),
                    fileSetResources,
                    fileSet.isPackaged(),
                    packageName,
                    reverseProperties,
                    defaultEncoding
                );
                getLogger().debug( "Processed " + fileSet.getDirectory() + " files" );
            }
            else
            {
                copyFiles(
                    basedir,
                    archetypeFilesDirectory,
                    fileSet.getDirectory(),
                    fileSetResources,
                    fileSet.isPackaged(),
                    packageName
                );
                getLogger().debug( "Copied " + fileSet.getDirectory() + " files" );
            }
        } // end while
    }

    private void createArchetypePom(
        Model pom,
        File archetypeFilesDirectory,
        Properties pomReversedProperties,
        File initialPomFile,
        boolean preserveCData,
        boolean keepParent
    )
        throws
        IOException
    {
        File outputFile =
            FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        if ( preserveCData )
        {
            getLogger().debug( "Preserving CDATA parts of pom" );
            File inputFile =
                FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

            FileUtils.copyFile( initialPomFile, inputFile );

            String initialcontent = FileUtils.fileRead( inputFile );

            String content = getReversedContent( initialcontent, pomReversedProperties );

            outputFile.getParentFile().mkdirs();

            FileUtils.fileWrite( outputFile.getAbsolutePath(), content );

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
            if ( pom.getVersion() != null )
            {
                pom.setVersion( "${" + Constants.VERSION + "}" );
            }

            rewriteReferences( pom, pomReversedProperties.getProperty( Constants.ARTIFACT_ID ),
                pomReversedProperties.getProperty( Constants.GROUP_ID ) );

            pomManager.writePom( pom, outputFile, initialPomFile );
        }

        String initialcontent = FileUtils.fileRead( initialPomFile );
        Iterator properties = pomReversedProperties.keySet().iterator();
        while ( properties.hasNext() )
        {
            String property = (String) properties.next();

            if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
            {
                getLogger().warn( "Archetype uses ${" + property +
                    "} for internal processing, but file " + initialPomFile +
                    " contains this property already" );
            }
        }
    }

    private FileSet createFileSet(
        final List excludes,
        final boolean packaged,
        final boolean filtered,
        final String group,
        final List includes,
        String defaultEncoding
    )
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

    private List createFileSets(
        List files,
        int level,
        boolean packaged,
        String packageName,
        boolean filtered,
        String defaultEncoding
    )
    {
        List fileSets = new ArrayList();

        if ( !files.isEmpty() )
        {
            getLogger().debug(
                "Creating filesets" + ( packaged ? ( " packaged (" + packageName + ")" ) : "" )
                    + ( filtered ? " filtered" : "" ) + " at level " + level
            );
            if ( level == 0 )
            {
                List includes = new ArrayList();
                List excludes = new ArrayList();

                Iterator filesIterator = files.iterator();
                while ( filesIterator.hasNext() )
                {
                    String file = (String) filesIterator.next();

                    includes.add( file );
                }

                if ( !includes.isEmpty() )
                {
                    fileSets.add(
                        createFileSet(
                            excludes,
                            packaged,
                            filtered,
                            "",
                            includes,
                            defaultEncoding
                        )
                    );
                }
            }
            else
            {
                Map groups = getGroupsMap( files, level );

                Iterator groupIterator = groups.keySet().iterator();
                while ( groupIterator.hasNext() )
                {
                    String group = (String) groupIterator.next();

                    getLogger().debug( "Creating filesets for group " + group );

                    if ( !packaged )
                    {
                        fileSets.add(
                            getUnpackagedFileSet(
                                filtered,
                                group,
                                (List) groups.get( group ),
                                defaultEncoding
                            )
                        );
                    }
                    else
                    {
                        fileSets.addAll(
                            getPackagedFileSets(
                                filtered,
                                group,
                                (List) groups.get( group ),
                                packageName,
                                defaultEncoding
                            )
                        );
                    }
                }
            } // end if

            getLogger().debug( "Resolved fileSets " + fileSets );
        } // end if
        return fileSets;
    }

    private ModuleDescriptor createModule(
        Properties reverseProperties,
        String rootArtifactId,
        String moduleId,
        String packageName,
        File basedir,
        File archetypeFilesDirectory,
        File replicaFilesDirectory,
        List languages,
        List filtereds,
        String defaultEncoding,
        boolean ignoreReplica,
        boolean preserveCData,
        boolean keepParent
    )
        throws
        IOException,
        XmlPullParserException
    {
        ModuleDescriptor archetypeDescriptor = new ModuleDescriptor();
        getLogger().debug( "Starting module's descriptor " + moduleId );

        archetypeFilesDirectory.mkdirs();
        getLogger().debug( "Module's files output directory " + archetypeFilesDirectory );

        Model pom =
            pomManager.readPom( FileUtils.resolveFile( basedir, Constants.ARCHETYPE_POM ) );
        String replacementId = pom.getArtifactId();
        if ( replacementId.indexOf( rootArtifactId ) >= 0 )
        {
            replacementId = StringUtils.replace( replacementId, rootArtifactId, "${rootArtifactId}" );
            moduleId = StringUtils.replace( moduleId, rootArtifactId, "${rootArtifactId}" );
        }
        if ( moduleId.indexOf( rootArtifactId ) >= 0 )
        {
            moduleId = StringUtils.replace( moduleId, rootArtifactId, "${rootArtifactId}" );
        }
        archetypeDescriptor.setName( replacementId );
        archetypeDescriptor.setId( replacementId );
        archetypeDescriptor.setDir( moduleId );

        registerProject( pom );

        setArtifactId( reverseProperties, pom.getArtifactId() );

        List fileNames = resolveFileNames( pom, basedir );

        List filesets =
            resolveFileSets( packageName, fileNames, languages, filtereds, defaultEncoding );
        getLogger().debug( "Resolved filesets for module " + archetypeDescriptor.getName() );

        archetypeDescriptor.setFileSets( filesets );

        createArchetypeFiles(
            reverseProperties,
            filesets,
            packageName,
            basedir,
            archetypeFilesDirectory,
            defaultEncoding
        );
        getLogger().debug( "Created files for module " + archetypeDescriptor.getName() );

        if ( !ignoreReplica )
        {
            createReplicaFiles( filesets, basedir, replicaFilesDirectory );
            getLogger().debug( "Created replica files for " + archetypeDescriptor.getName() );
        }

        String parentArtifactId = reverseProperties.getProperty( Constants.PARENT_ARTIFACT_ID );
        setParentArtifactId( reverseProperties, pom.getArtifactId() );

        Iterator modules = pom.getModules().iterator();
        while ( modules.hasNext() )
        {
            String subModuleId = (String) modules.next();

            getLogger().debug( "Creating module " + subModuleId );

            ModuleDescriptor moduleDescriptor =
                createModule(
                    reverseProperties,
                    rootArtifactId,
                    subModuleId,
                    packageName,
                    FileUtils.resolveFile( basedir, subModuleId ),
                    FileUtils.resolveFile( archetypeFilesDirectory, subModuleId ),
                    FileUtils.resolveFile( replicaFilesDirectory, subModuleId ),
                    languages,
                    filtereds,
                    defaultEncoding,
                    ignoreReplica,
                    preserveCData,
                    keepParent
                );

            archetypeDescriptor.addModule( moduleDescriptor );
            getLogger().debug(
                "Added module " + moduleDescriptor.getName() + " in "
                    + archetypeDescriptor.getName()
            );
        }
        restoreParentArtifactId( reverseProperties, parentArtifactId );
        restoreArtifactId( reverseProperties, pom.getArtifactId() );

        getLogger().debug( "Created Module " + archetypeDescriptor.getName() + " pom" );

        return archetypeDescriptor;
    }

    private void createModulePom(
        Model pom,
        String rootArtifactId,
        File archetypeFilesDirectory,
        Properties pomReversedProperties,
        File initialPomFile,
        boolean preserveCData,
        boolean keepParent
    )
        throws
        IOException
    {
        File outputFile =
            FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        if ( preserveCData )
        {
            getLogger().debug( "Preserving CDATA parts of pom" );
            File inputFile =
                FileUtils.resolveFile( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

            FileUtils.copyFile( initialPomFile, inputFile );
            String initialcontent = FileUtils.fileRead( inputFile );

            String content = getReversedContent( initialcontent, pomReversedProperties );

            outputFile.getParentFile().mkdirs();

            FileUtils.fileWrite( outputFile.getAbsolutePath(), content );

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
                        "${" + Constants.GROUP_ID + "}" )
                );
                if ( pom.getParent().getArtifactId() != null &&
                    pom.getParent().getArtifactId().indexOf( rootArtifactId ) >= 0 )
                {
                    pom.getParent().setArtifactId( StringUtils.replace( pom.getParent().getArtifactId(), rootArtifactId, "${rootArtifactId}" ) );
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
                    StringUtils.replace(
                        pom.getGroupId(),
                        pomReversedProperties.getProperty( Constants.GROUP_ID ),
                        "${" + Constants.GROUP_ID + "}" )
                );
            }
            pom.setArtifactId( "${" + Constants.ARTIFACT_ID + "}" );
            if ( pom.getVersion() != null )
            {
                pom.setVersion( "${" + Constants.VERSION + "}" );
            }

            rewriteReferences( pom, rootArtifactId, pomReversedProperties.getProperty( Constants.GROUP_ID ) );

            pomManager.writePom( pom, outputFile, initialPomFile );
        }

        String initialcontent = FileUtils.fileRead( initialPomFile );
        Iterator properties = pomReversedProperties.keySet().iterator();
        while ( properties.hasNext() )
        {
            String property = (String) properties.next();

            if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
            {
                getLogger().warn( "Archetype uses ${" + property +
                    "} for internal processing, but file " + initialPomFile +
                    " contains this property already" );
            }
        }
    }

    private void createReplicaFiles( List filesets,
                                     File basedir,
                                     File replicaFilesDirectory )
        throws
        IOException
    {
        getLogger().debug(
            "Creating Archetype/Module replica files from " + basedir + " to "
                + replicaFilesDirectory
        );

        copyPom( basedir, replicaFilesDirectory );

        Iterator iterator = filesets.iterator();

        while ( iterator.hasNext() )
        {
            FileSet fileset = (FileSet) iterator.next();

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( basedir );
            scanner.setIncludes(
                (String[]) concatenateToList( fileset.getIncludes(), fileset.getDirectory() )
                    .toArray( new String[fileset.getIncludes().size()] )
            );
            scanner.setExcludes(
                (String[]) fileset.getExcludes().toArray(
                    new String[fileset.getExcludes().size()]
                )
            );
            scanner.addDefaultExcludes();
            getLogger().debug( "Using fileset " + fileset );
            scanner.scan();

            List fileSetResources = Arrays.asList( scanner.getIncludedFiles() );

            copyFiles(
                basedir,
                replicaFilesDirectory,
                fileset.getDirectory(),
                fileSetResources,
                false,
                null
            );
            getLogger().debug( "Copied " + fileset.getDirectory() + " files" );
        }
    }

    private Set getExtensions( List files )
    {
        Set extensions = new HashSet();
        Iterator filesIterator = files.iterator();
        while ( filesIterator.hasNext() )
        {
            String file = (String) filesIterator.next();

            extensions.add( FileUtils.extension( file ) );
        }

        return extensions;
    }

    private String getGeneratedSourcesDirectory()
    {
        return "target" + File.separator + "generated-sources" + File.separator + "archetypeng";
    }

    private Map getGroupsMap( final List files,
                              final int level )
    {
        Map groups = new HashMap();
        Iterator fileIterator = files.iterator();
        while ( fileIterator.hasNext() )
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
        getLogger().debug(
            "Sorted " + groups.size() + " groups in " + files.size() + " files"
        );
        getLogger().debug( "Sorted Files:" + files );
        return groups;
    }

    private Properties initialiseArchetypeProperties( File propertyFile )
        throws
        IOException
    {
        Properties properties = new Properties();
        archetypePropertiesManager.readProperties( properties, propertyFile );
        return properties;
    }

    private FileSet getPackagedFileSet(
        final boolean filtered,
        final Set packagedExtensions,
        final String group,
        final Set unpackagedExtensions,
        final List unpackagedFiles,
        String defaultEncoding
    )
    {
        List includes = new ArrayList();
        List excludes = new ArrayList();

        Iterator extensionsIterator = packagedExtensions.iterator();
        while ( extensionsIterator.hasNext() )
        {
            String extension = (String) extensionsIterator.next();

            includes.add( "**/*." + extension );

            if ( unpackagedExtensions.contains( extension ) )
            {
                excludes.addAll(
                    archetypeFilesResolver.getFilesWithExtension( unpackagedFiles, extension )
                );
            }
        }

        FileSet fileset =
            createFileSet( excludes, true, filtered, group, includes, defaultEncoding );
        return fileset;
    }

    private List getPackagedFileSets(
        final boolean filtered,
        final String group,
        final List groupFiles,
        final String packageName,
        String defaultEncoding
    )
    {
        String packageAsDir = StringUtils.replace( packageName, ".", "/" );
        List packagedFileSets = new ArrayList();
        List packagedFiles = archetypeFilesResolver.getPackagedFiles( groupFiles, packageAsDir );
        getLogger().debug( "Found packaged Files:" + packagedFiles );

        List unpackagedFiles =
            archetypeFilesResolver.getUnpackagedFiles( groupFiles, packageAsDir );
        getLogger().debug( "Found unpackaged Files:" + unpackagedFiles );

        Set packagedExtensions = getExtensions( packagedFiles );
        getLogger().debug( "Found packaged extensions " + packagedExtensions );

        Set unpackagedExtensions = getExtensions( unpackagedFiles );

        if ( !packagedExtensions.isEmpty() )
        {
            packagedFileSets.add(
                getPackagedFileSet(
                    filtered,
                    packagedExtensions,
                    group,
                    unpackagedExtensions,
                    unpackagedFiles,
                    defaultEncoding
                )
            );
        }

        if ( !unpackagedExtensions.isEmpty() )
        {
            getLogger().debug( "Found unpackaged extensions " + unpackagedExtensions );
            packagedFileSets.add(
                getUnpackagedFileSet(
                    filtered,
                    unpackagedExtensions,
                    unpackagedFiles,
                    group,
                    packagedExtensions,
                    defaultEncoding
                )
            );
        }
        return packagedFileSets;
    }

    private void setParentArtifactId(
        Properties properties,
        String parentArtifactId
    )
    {
        properties.setProperty( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
    }

    private void processFileSet(
        File basedir,
        File archetypeFilesDirectory,
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName,
        Properties reverseProperties,
        String defaultEncoding
    )
        throws
        IOException
    {
        String packageAsDirectory = StringUtils.replace( packageName, ".", File.separator );
        getLogger().debug(
            "Package as Directory: Package:" + packageName + "->" + packageAsDirectory
        );

        Iterator iterator = fileSetResources.iterator();

        while ( iterator.hasNext() )
        {
            String inputFileName = (String) iterator.next();
            String outputFileName =
                packaged
                    ? StringUtils.replace( inputFileName, packageAsDirectory + File.separator, "" )
                    : inputFileName;
            getLogger().debug( "InputFileName:" + inputFileName );
            getLogger().debug( "OutputFileName:" + outputFileName );

            File outputFile = new File( archetypeFilesDirectory, outputFileName );
            File inputFile = new File( basedir, inputFileName );

            FileCharsetDetector detector = new FileCharsetDetector( inputFile );

            String fileEncoding = detector.isFound() ? detector.getCharset() : defaultEncoding;

            String initialcontent =
                org.apache.commons.io.IOUtils.toString(
                    new FileInputStream( inputFile ),
                    fileEncoding
                );

            Iterator properties = reverseProperties.keySet().iterator();
            while ( properties.hasNext() )
            {
                String property = (String) properties.next();

                if ( initialcontent.indexOf( "${" + property + "}" ) > 0 )
                {
                    getLogger().warn( "Archetype uses ${" + property +
                        "} for internal processing, but file " + inputFile +
                        " contains this property already" );
                }
            }

            String content = getReversedContent( initialcontent, reverseProperties );
            outputFile.getParentFile().mkdirs();
            org.apache.commons.io.IOUtils.write(
                content,
                new FileOutputStream( outputFile ),
                fileEncoding
            );
//            FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );
        } // end while
    }

    private List removePackage( List sources,
                                String packageAsDirectory )
    {
        if ( sources == null )
        {
            return null;
        }

        List unpackagedSources = new ArrayList( sources.size() );
        Iterator sourcesIterator = sources.iterator();
        while ( sourcesIterator.hasNext() )
        {
            String source = (String) sourcesIterator.next();
            String unpackagedSource = StringUtils.replace( source, packageAsDirectory, "" );
            unpackagedSources.add( unpackagedSource );
        }

        return unpackagedSources;
    }

    private String getReplicaOutputDirectory()
    {
        return
            Constants.SRC + File.separator + Constants.TEST + File.separator + Constants.RESOURCES
                + File.separator + "projects";
    }

    private Properties getRequiredProperties(
        ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        Properties reversedProperties = new Properties();

        reversedProperties.putAll( properties );
        reversedProperties.remove( Constants.ARCHETYPE_GROUP_ID );
        reversedProperties.remove( Constants.ARCHETYPE_ARTIFACT_ID );
        reversedProperties.remove( Constants.ARCHETYPE_VERSION );

        return reversedProperties;
    }

    private List resolveFileNames( final Model pom,
                                   final File basedir )
        throws
        IOException
    {
        getLogger().debug( "Resolving files for " + pom.getId() + " in " + basedir );

        Iterator modules = pom.getModules().iterator();
        String excludes = "pom.xml*,archetype.properties*,target/**,";
        while ( modules.hasNext() )
        {
            excludes += "," + (String) modules.next() + "/**";
        }

        Iterator defaultExcludes = Arrays.asList( ListScanner.DEFAULTEXCLUDES ).iterator();
        while ( defaultExcludes.hasNext() )
        {
            excludes += "," + (String) defaultExcludes.next() + "/**";
        }

        excludes = PathUtils.convertPathForOS( excludes );

        List fileNames = FileUtils.getFileNames( basedir, "**", excludes, false );

        getLogger().debug( "Resolved " + fileNames.size() + " files" );
        getLogger().debug( "Resolved Files:" + fileNames );

        return fileNames;
    }

    private List resolveFileSets(
        String packageName,
        List fileNames,
        List languages,
        List filtereds,
        String defaultEncoding
    )
    {
        List resolvedFileSets = new ArrayList();
        getLogger().debug(
            "Resolving filesets with package=" + packageName + ", languages=" + languages
                + " and extentions=" + filtereds
        );

        List files = new ArrayList( fileNames );

        String languageIncludes = "";
        Iterator languagesIterator = languages.iterator();
        while ( languagesIterator.hasNext() )
        {
            String language = (String) languagesIterator.next();

            languageIncludes +=
                ( ( languageIncludes.length() == 0 ) ? "" : "," ) + language + "/**";
        }

        getLogger().debug( "Using languages includes " + languageIncludes );

        String filteredIncludes = "";
        Iterator filteredsIterator = filtereds.iterator();
        while ( filteredsIterator.hasNext() )
        {
            String filtered = (String) filteredsIterator.next();

            filteredIncludes +=
                ( ( filteredIncludes.length() == 0 ) ? "" : "," ) + "**/"
                    + ( filtered.startsWith( "." ) ? "" : "*." ) + filtered;
        }

        getLogger().debug( "Using filtered includes " + filteredIncludes );

        /*sourcesMainFiles*/
        List sourcesMainFiles =
            archetypeFilesResolver.findSourcesMainFiles( files, languageIncludes );
        if ( !sourcesMainFiles.isEmpty() )
        {
            files.removeAll( sourcesMainFiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles( sourcesMainFiles, filteredIncludes );
            sourcesMainFiles.removeAll( filteredFiles );

            List unfilteredFiles = sourcesMainFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding )
                );
            }

            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding )
                );
            }
        }

        /*resourcesMainFiles*/
        List resourcesMainFiles =
            archetypeFilesResolver.findResourcesMainFiles( files, languageIncludes );
        if ( !resourcesMainFiles.isEmpty() )
        {
            files.removeAll( resourcesMainFiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles( resourcesMainFiles, filteredIncludes );
            resourcesMainFiles.removeAll( filteredFiles );

            List unfilteredFiles = resourcesMainFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 3, false, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets(
                        unfilteredFiles,
                        3,
                        false,
                        packageName,
                        false,
                        defaultEncoding
                    )
                );
            }
        }

        /*sourcesTestFiles*/
        List sourcesTestFiles =
            archetypeFilesResolver.findSourcesTestFiles( files, languageIncludes );
        if ( !sourcesTestFiles.isEmpty() )
        {
            files.removeAll( sourcesTestFiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles( sourcesTestFiles, filteredIncludes );
            sourcesTestFiles.removeAll( filteredFiles );

            List unfilteredFiles = sourcesTestFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding )
                );
            }
        }

        /*ressourcesTestFiles*/
        List resourcesTestFiles =
            archetypeFilesResolver.findResourcesTestFiles( files, languageIncludes );
        if ( !resourcesTestFiles.isEmpty() )
        {
            files.removeAll( resourcesTestFiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles( resourcesTestFiles, filteredIncludes );
            resourcesTestFiles.removeAll( filteredFiles );

            List unfilteredFiles = resourcesTestFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 3, false, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets(
                        unfilteredFiles,
                        3,
                        false,
                        packageName,
                        false,
                        defaultEncoding
                    )
                );
            }
        }

        /*siteFiles*/
        List siteFiles = archetypeFilesResolver.findSiteFiles( files, languageIncludes );
        if ( !siteFiles.isEmpty() )
        {
            files.removeAll( siteFiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles( siteFiles, filteredIncludes );
            siteFiles.removeAll( filteredFiles );

            List unfilteredFiles = siteFiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 2, false, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets(
                        unfilteredFiles,
                        2,
                        false,
                        packageName,
                        false,
                        defaultEncoding
                    )
                );
            }
        }

        /*thirdLevelSourcesfiles*/
        List thirdLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources( 3, files, languageIncludes );
        if ( !thirdLevelSourcesfiles.isEmpty() )
        {
            files.removeAll( thirdLevelSourcesfiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles(
                    thirdLevelSourcesfiles,
                    filteredIncludes
                );
            thirdLevelSourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = thirdLevelSourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 3, true, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 3, true, packageName, false, defaultEncoding )
                );
            }

            /*thirdLevelResourcesfiles*/
            List thirdLevelResourcesfiles =
                archetypeFilesResolver.findOtherResources(
                    3,
                    files,
                    thirdLevelSourcesfiles,
                    languageIncludes
                );
            if ( !thirdLevelResourcesfiles.isEmpty() )
            {
                files.removeAll( thirdLevelResourcesfiles );
                filteredFiles =
                    archetypeFilesResolver.getFilteredFiles(
                        thirdLevelResourcesfiles,
                        filteredIncludes
                    );
                thirdLevelResourcesfiles.removeAll( filteredFiles );
                unfilteredFiles = thirdLevelResourcesfiles;
                if ( !filteredFiles.isEmpty() )
                {
                    resolvedFileSets.addAll(
                        createFileSets(
                            filteredFiles,
                            3,
                            false,
                            packageName,
                            true,
                            defaultEncoding
                        )
                    );
                }
                if ( !unfilteredFiles.isEmpty() )
                {
                    resolvedFileSets.addAll(
                        createFileSets(
                            unfilteredFiles,
                            3,
                            false,
                            packageName,
                            false,
                            defaultEncoding
                        )
                    );
                }
            }
        } // end if

        /*secondLevelSourcesfiles*/
        List secondLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources( 2, files, languageIncludes );
        if ( !secondLevelSourcesfiles.isEmpty() )
        {
            files.removeAll( secondLevelSourcesfiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles(
                    secondLevelSourcesfiles,
                    filteredIncludes
                );
            secondLevelSourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = secondLevelSourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 2, true, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( unfilteredFiles, 2, true, packageName, false, defaultEncoding )
                );
            }
        }

        /*secondLevelResourcesfiles*/
        List secondLevelResourcesfiles =
            archetypeFilesResolver.findOtherResources( 2, files, languageIncludes );
        if ( !secondLevelResourcesfiles.isEmpty() )
        {
            files.removeAll( secondLevelResourcesfiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles(
                    secondLevelResourcesfiles,
                    filteredIncludes
                );
            secondLevelResourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = secondLevelResourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 2, false, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets(
                        unfilteredFiles,
                        2,
                        false,
                        packageName,
                        false,
                        defaultEncoding
                    )
                );
            }
        }

        /*rootResourcesfiles*/
        List rootResourcesfiles =
            archetypeFilesResolver.findOtherResources( 0, files, languageIncludes );
        if ( !rootResourcesfiles.isEmpty() )
        {
            files.removeAll( rootResourcesfiles );

            List filteredFiles =
                archetypeFilesResolver.getFilteredFiles( rootResourcesfiles, filteredIncludes );
            rootResourcesfiles.removeAll( filteredFiles );

            List unfilteredFiles = rootResourcesfiles;
            if ( !filteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets( filteredFiles, 0, false, packageName, true, defaultEncoding )
                );
            }
            if ( !unfilteredFiles.isEmpty() )
            {
                resolvedFileSets.addAll(
                    createFileSets(
                        unfilteredFiles,
                        0,
                        false,
                        packageName,
                        false,
                        defaultEncoding
                    )
                );
            }
        }

         /**/
        if ( !files.isEmpty() )
        {
            getLogger().info( "Ignored files: " + files );
        }

        return resolvedFileSets;
    }

    private void restoreArtifactId(
        Properties properties,
        String artifactId
    )
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

    private void restoreParentArtifactId(
        Properties properties,
        String parentArtifactId
    )
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

    private String getReversedContent( String content,
                                       final Properties properties )
    {
        String result = content;
        Iterator propertyIterator = properties.keySet().iterator();
        while ( propertyIterator.hasNext() )
        {
            String propertyKey = (String) propertyIterator.next();
            result =
                StringUtils.replace(
                    result,
                    properties.getProperty( propertyKey ),
                    "${" + propertyKey + "}"
                );
        }
        return result;
    }

    private String getTemplateOutputDirectory()
    {
        return
            Constants.SRC + File.separator + Constants.MAIN + File.separator + Constants.RESOURCES;
    }

    private FileSet getUnpackagedFileSet(
        final boolean filtered,
        final String group,
        final List groupFiles,
        String defaultEncoding
    )
    {
        Set extensions = getExtensions( groupFiles );

        List includes = new ArrayList();
        List excludes = new ArrayList();

        Iterator extensionsIterator = extensions.iterator();
        while ( extensionsIterator.hasNext() )
        {
            String extension = (String) extensionsIterator.next();

            includes.add( "**/*." + extension );
        }

        return createFileSet( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private FileSet getUnpackagedFileSet(
        final boolean filtered,
        final Set unpackagedExtensions,
        final List unpackagedFiles,
        final String group,
        final Set packagedExtensions,
        String defaultEncoding
    )
    {
        List includes = new ArrayList();
        List excludes = new ArrayList();

        Iterator extensionsIterator = unpackagedExtensions.iterator();
        while ( extensionsIterator.hasNext() )
        {
            String extension = (String) extensionsIterator.next();
            if ( packagedExtensions.contains( extension ) )
            {
                includes.addAll(
                    archetypeFilesResolver.getFilesWithExtension( unpackagedFiles, extension )
                );
            }
            else
            {
                includes.add( "**/*." + extension );
            }
        }

        return createFileSet( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private void writeOldDescriptor( OldArchetypeDescriptor oldDescriptor,
                                     File oldDescriptorFile )
        throws
        IOException
    {
        OldArchetypeDescriptorXpp3Writer writer = new OldArchetypeDescriptorXpp3Writer();
        writer.write( new FileWriter( oldDescriptorFile ), oldDescriptor );
    }
}
