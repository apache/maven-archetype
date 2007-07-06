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

package org.codehaus.mojo.archetypeng.creator;

import org.apache.maven.model.Build;
import org.apache.maven.model.Extension;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import org.codehaus.mojo.archetypeng.ArchetypeConfiguration;
import org.codehaus.mojo.archetypeng.ArchetypeDefinition;
import org.codehaus.mojo.archetypeng.ArchetypeFactory;
import org.codehaus.mojo.archetypeng.ArchetypeFilesResolver;
import org.codehaus.mojo.archetypeng.ArchetypePropertiesManager;
import org.codehaus.mojo.archetypeng.Constants;
import org.codehaus.mojo.archetypeng.FileCharsetDetector;
import org.codehaus.mojo.archetypeng.ListScanner;
import org.codehaus.mojo.archetypeng.PathUtils;
import org.codehaus.mojo.archetypeng.PomManager;
import org.codehaus.mojo.archetypeng.archetype.filesets.ArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.archetype.filesets.FileSet;
import org.codehaus.mojo.archetypeng.archetype.filesets.ModuleDescriptor;
import org.codehaus.mojo.archetypeng.archetype.filesets.RequiredProperty;
import org.codehaus.mojo.archetypeng.archetype.filesets.io.xpp3.ArchetypeDescriptorXpp3Writer;
import org.codehaus.mojo.archetypeng.creator.olddescriptor.OldArchetypeDescriptor;
import org.codehaus.mojo.archetypeng.creator.olddescriptor.OldArchetypeDescriptorXpp3Writer;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotConfigured;
import org.codehaus.mojo.archetypeng.exception.ArchetypeNotDefined;
import org.codehaus.mojo.archetypeng.exception.TemplateCreationException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
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

/**
 * @plexus.component  role-hint="fileset"
 */
public class FilesetArchetypeCreator
extends AbstractLogEnabled
implements ArchetypeCreator
{
    /**
     * @plexus.requirement
     */
    private ArchetypeFactory archetypeFactory;

    /**
     * @plexus.requirement
     */
    private ArchetypeFilesResolver archetypeFilesResolver;

    /**
     * @plexus.requirement
     */
    private ArchetypePropertiesManager archetypePropertiesManager;

    /**
     * @plexus.requirement
     */
    private PomManager pomManager;

    public void createArchetype (
        MavenProject project,
        File propertyFile,
        List languages,
        List filtereds,
        String defaultEncoding
    )
    throws IOException,
        ArchetypeNotDefined,
        ArchetypeNotConfigured,
        TemplateCreationException,
        XmlPullParserException
    {
        Properties properties = initialiseArchetypeProperties ( propertyFile );

        ArchetypeDefinition archetypeDefinition =
            archetypeFactory.createArchetypeDefinition ( properties );
        if ( !archetypeDefinition.isDefined () )
        {
            throw new ArchetypeNotDefined ( "The archetype is not defined" );
        }

        ArchetypeConfiguration archetypeConfiguration =
            archetypeFactory.createArchetypeConfiguration ( archetypeDefinition, properties );
        if ( !archetypeConfiguration.isConfigured () )
        {
            throw new ArchetypeNotConfigured ( "The archetype is not configured" );
        }

        File basedir = project.getBasedir ();
        File generatedSourcesDirectory =
            FileUtils.resolveFile ( basedir, getGeneratedSourcesDirectory () );
        generatedSourcesDirectory.mkdirs ();
        getLogger ().debug ( "Creating archetype in " + generatedSourcesDirectory );

        Model model = new Model ();
        model.setModelVersion ( "4.0.0" );
        model.setGroupId ( archetypeDefinition.getGroupId () );
        model.setArtifactId ( archetypeDefinition.getArtifactId () );
        model.setVersion ( archetypeDefinition.getVersion () );
        model.setPackaging ( "maven-archetype" );

        Build build = new Build ();
        model.setBuild ( build );

        Extension extension = new Extension ();
        extension.setGroupId ( "org.codehaus.mojo" );
        extension.setArtifactId ( "maven-archetypeng-plugin" );
        extension.setVersion ( "1.0-SNAPSHOT" );
        model.getBuild ().addExtension ( extension );

        Plugin plugin = new Plugin ();
        plugin.setGroupId ( "org.codehaus.mojo" );
        plugin.setArtifactId ( "maven-archetypeng-plugin" );
        plugin.setVersion ( "1.0-SNAPSHOT" );
        plugin.setExtensions ( true );
        model.getBuild ().addPlugin ( plugin );
        getLogger ().debug ( "Creating archetype's pom" );

        File archetypePomFile = FileUtils.resolveFile ( basedir, getArchetypePom () );
        archetypePomFile.getParentFile ().mkdirs ();
        pomManager.writePom ( model, archetypePomFile );

        File archetypeResourcesDirectory =
            FileUtils.resolveFile ( generatedSourcesDirectory, getTemplateOutputDirectory () );
        archetypeResourcesDirectory.mkdirs ();

        File archetypeFilesDirectory =
            FileUtils.resolveFile ( archetypeResourcesDirectory, Constants.ARCHETYPE_RESOURCES );
        archetypeFilesDirectory.mkdirs ();
        getLogger ().debug ( "Archetype's files output directory " + archetypeFilesDirectory );

        File replicaMainDirectory =
            FileUtils.resolveFile (
                generatedSourcesDirectory,
                getReplicaOutputDirectory () + File.separator
                + archetypeDefinition.getArtifactId ()
            );
        replicaMainDirectory.mkdirs ();

        File replicaFilesDirectory = FileUtils.resolveFile ( replicaMainDirectory, "reference" );
        replicaFilesDirectory.mkdirs ();

        File archetypeDescriptorFile =
            FileUtils.resolveFile ( archetypeResourcesDirectory, Constants.ARCHETYPE_DESCRIPTOR );
        archetypeDescriptorFile.getParentFile ().mkdirs ();

        ArchetypeDescriptor archetypeDescriptor = new ArchetypeDescriptor ();
        archetypeDescriptor.setId ( archetypeDefinition.getArtifactId () );
        getLogger ().debug (
            "Starting archetype's descriptor " + archetypeDefinition.getArtifactId ()
        );
        archetypeDescriptor.setPartial ( false );

        addRequiredProperties ( archetypeDescriptor, properties );

        // TODO ensure reversedproperties contains NO dotted properties
        Properties reverseProperties = getRequiredProperties ( archetypeDescriptor, properties );
        reverseProperties.remove ( Constants.GROUP_ID );

        // TODO ensure pomReversedProperties contains NO dotted properties
        Properties pomReversedProperties =
            getRequiredProperties ( archetypeDescriptor, properties );
        pomReversedProperties.remove ( Constants.PACKAGE );

        String packageName = archetypeConfiguration.getProperty ( Constants.PACKAGE );

        Model pom =
            pomManager.readPom ( FileUtils.resolveFile ( basedir, Constants.ARCHETYPE_POM ) );

        List fileNames = resolveFileNames ( pom, basedir );

        List filesets =
            resolveFileSets ( packageName, fileNames, languages, filtereds, defaultEncoding );
        getLogger ().debug ( "Resolved filesets for " + archetypeDescriptor.getId () );

        archetypeDescriptor.setFileSets ( filesets );

        createArchetypeFiles (
            reverseProperties,
            filesets,
            packageName,
            basedir,
            archetypeFilesDirectory,
            defaultEncoding
        );
        getLogger ().debug ( "Created files for " + archetypeDescriptor.getId () );

        createReplicaFiles ( filesets, basedir, replicaFilesDirectory );
        getLogger ().debug ( "Created replica files for " + archetypeDescriptor.getId () );

        FileUtils.copyFile (
            propertyFile,
            new File ( replicaMainDirectory, "archetype.properties" )
        );
        new File ( replicaMainDirectory, "goal.txt" ).createNewFile ();

        setParentArtifactId (
            reverseProperties,
            pomReversedProperties,
            archetypeConfiguration.getProperty ( Constants.ARTIFACT_ID )
        );

        Iterator modules = pom.getModules ().iterator ();
        while ( modules.hasNext () )
        {
            String moduleId = (String) modules.next ();

            setArtifactId ( reverseProperties, pomReversedProperties, moduleId );

            getLogger ().debug ( "Creating module " + moduleId );

            ModuleDescriptor moduleDescriptor =
                createModule (
                    reverseProperties,
                    pomReversedProperties,
                    moduleId,
                    packageName,
                    FileUtils.resolveFile ( basedir, moduleId ),
                    FileUtils.resolveFile ( archetypeFilesDirectory, moduleId ),
                    FileUtils.resolveFile ( replicaFilesDirectory, moduleId ),
                    languages,
                    filtereds,
                    defaultEncoding
                );

            archetypeDescriptor.addModule ( moduleDescriptor );
            getLogger ().debug (
                "Added module " + moduleDescriptor.getId () + " in "
                + archetypeDescriptor.getId ()
            );
        }
        restoreParentArtifactId ( reverseProperties, pomReversedProperties, null );
        restoreArtifactId (
            reverseProperties,
            pomReversedProperties,
            archetypeConfiguration.getProperty ( Constants.ARTIFACT_ID )
        );

        createArchetypePom (
            pom,
            archetypeFilesDirectory,
            pomReversedProperties,
            FileUtils.resolveFile ( basedir, Constants.ARCHETYPE_POM )
        );
        getLogger ().debug ( "Created Archetype " + archetypeDescriptor.getId () + " pom" );

        ArchetypeDescriptorXpp3Writer writer = new ArchetypeDescriptorXpp3Writer ();
        writer.write ( new FileWriter ( archetypeDescriptorFile ), archetypeDescriptor );
        getLogger ().debug ( "Archetype " + archetypeDescriptor.getId () + " descriptor written" );

        OldArchetypeDescriptor oldDescriptor = convertToOldDescriptor (archetypeDescriptor.getId (), packageName, basedir);
        File oldDescriptorFile =
            FileUtils.resolveFile ( archetypeResourcesDirectory, Constants.OLD_ARCHETYPE_DESCRIPTOR );
        archetypeDescriptorFile.getParentFile ().mkdirs ();
        writeOldDescriptor(oldDescriptor, oldDescriptorFile);
        getLogger ().debug ( "Archetype " + archetypeDescriptor.getId () + " old descriptor written" );

    }

    private void addRequiredProperties (
        ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        Properties requiredProperties = new Properties ();
        requiredProperties.putAll ( properties );
        requiredProperties.remove ( Constants.ARCHETYPE_GROUP_ID );
        requiredProperties.remove ( Constants.ARCHETYPE_ARTIFACT_ID );
        requiredProperties.remove ( Constants.ARCHETYPE_VERSION );
        requiredProperties.remove ( Constants.GROUP_ID );
        requiredProperties.remove ( Constants.ARTIFACT_ID );
        requiredProperties.remove ( Constants.VERSION );
        requiredProperties.remove ( Constants.PACKAGE );

        Iterator propertiesIterator = requiredProperties.keySet ().iterator ();
        while ( propertiesIterator.hasNext () )
        {
            String propertyKey = (String) propertiesIterator.next ();
            RequiredProperty requiredProperty = new RequiredProperty ();
            requiredProperty.setKey ( propertyKey );
            requiredProperty.setDefaultValue ( requiredProperties.getProperty ( propertyKey ) );
            archetypeDescriptor.addRequiredProperty ( requiredProperty );

            getLogger ().debug (
                "Adding requiredProperty " + propertyKey + "="
                + requiredProperties.getProperty ( propertyKey ) + "to archetype's descriptor"
            );
        }
    }

    private String getArchetypePom ()
    {
        return getGeneratedSourcesDirectory () + File.separator + Constants.ARCHETYPE_POM;
    }

    private void setArtifactId (
        Properties reverseProperties,
        Properties pomReversedProperties,
        String artifactId
    )
    {
        reverseProperties.setProperty ( Constants.ARTIFACT_ID, artifactId );
        pomReversedProperties.setProperty ( Constants.ARTIFACT_ID, artifactId );
    }

    private List concatenateToList ( List toConcatenate, String with )
    {
        List result = new ArrayList ( toConcatenate.size () );
        Iterator iterator = toConcatenate.iterator ();
        while ( iterator.hasNext () )
        {
            String concatenate = (String) iterator.next ();
            result.add ( ( ( with.length () > 0 ) ? ( with + "/" + concatenate ) : concatenate ) );
        }
        return result;
    }

    private void copyFiles (
        File basedir,
        File archetypeFilesDirectory,
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName
    )
    throws IOException
    {
        Iterator iterator = fileSetResources.iterator ();

        while ( iterator.hasNext () )
        {
            String inputFileName = (String) iterator.next ();

            String packageAsDirectory = StringUtils.replace ( packageName, ".", "/" );

            String outputFileName =
                packaged ? StringUtils.replace ( inputFileName, packageAsDirectory + "/", "" )
                         : inputFileName;

            File outputFile = new File ( archetypeFilesDirectory, outputFileName );

            File inputFile = new File ( basedir, inputFileName );

            outputFile.getParentFile ().mkdirs ();

            FileUtils.copyFile ( inputFile, outputFile );
        } // end while
    }

    private void copyPom ( File basedir, File replicaFilesDirectory )
    throws IOException
    {
        FileUtils.copyFileToDirectory (
            new File ( basedir, Constants.ARCHETYPE_POM ),
            replicaFilesDirectory
        );
    }

    private void createArchetypeFiles (
        Properties reverseProperties,
        List fileSets,
        String packageName,
        File basedir,
        File archetypeFilesDirectory,
        String defaultEncoding
    )
    throws IOException
    {
        getLogger ().debug (
            "Creating Archetype/Module files from " + basedir + " to " + archetypeFilesDirectory
        );

        Iterator iterator = fileSets.iterator ();

        while ( iterator.hasNext () )
        {
            FileSet fileSet = (FileSet) iterator.next ();

            DirectoryScanner scanner = new DirectoryScanner ();
            scanner.setBasedir ( basedir );
            scanner.setIncludes (
                (String[]) concatenateToList ( fileSet.getIncludes (), fileSet.getDirectory () )
                .toArray ( new String[fileSet.getIncludes ().size ()] )
            );
            scanner.setExcludes (
                (String[]) fileSet.getExcludes ().toArray (
                    new String[fileSet.getExcludes ().size ()]
                )
            );
            scanner.addDefaultExcludes ();
            getLogger ().debug ( "Using fileset " + fileSet );
            scanner.scan ();

            List fileSetResources = Arrays.asList ( scanner.getIncludedFiles () );

            if ( fileSet.isFiltered () )
            {
                processFileSet (
                    basedir,
                    archetypeFilesDirectory,
                    fileSet.getDirectory (),
                    fileSetResources,
                    fileSet.isPackaged (),
                    packageName,
                    reverseProperties,
                    defaultEncoding
                );
                getLogger ().debug ( "Processed " + fileSet.getDirectory () + " files" );
            }
            else
            {
                copyFiles (
                    basedir,
                    archetypeFilesDirectory,
                    fileSet.getDirectory (),
                    fileSetResources,
                    fileSet.isPackaged (),
                    packageName
                );
                getLogger ().debug ( "Copied " + fileSet.getDirectory () + " files" );
            }
        } // end while
    }

    private void createArchetypePom (
        Model pom,
        File archetypeFilesDirectory,
        Properties pomReversedProperties,
        File initialPomFile
    )
    throws IOException
    {
//        pom.setParent ( null );
//        pom.setModules ( null );
//        pom.setGroupId ( "${" + Constants.GROUP_ID + "}" );
//        pom.setArtifactId ( "${" + Constants.ARTIFACT_ID + "}" );
//        pom.setVersion ( "${" + Constants.VERSION + "}" );

        File outputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        File inputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

        FileUtils.copyFile ( initialPomFile, inputFile );

//        pomManager.writePom ( pom, inputFile );

        String initialcontent = FileUtils.fileRead ( inputFile );

        String content = getReversedContent ( initialcontent, pomReversedProperties );

        outputFile.getParentFile ().mkdirs ();

        FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );

        inputFile.delete ();
    }

    private FileSet createFileSet (
        final List excludes,
        final boolean packaged,
        final boolean filtered,
        final String group,
        final List includes,
        String defaultEncoding
    )
    {
        FileSet fileSet = new FileSet ();

        fileSet.setDirectory ( group );
        fileSet.setPackaged ( packaged );
        fileSet.setFiltered ( filtered );
        fileSet.setIncludes ( includes );
        fileSet.setExcludes ( excludes );
        fileSet.setEncoding ( defaultEncoding );

        getLogger ().debug ( "Created Fileset " + fileSet );

        return fileSet;
    }

    private List createFileSets (
        List files,
        int level,
        boolean packaged,
        String packageName,
        boolean filtered,
        String defaultEncoding
    )
    {
        List fileSets = new ArrayList ();

        if ( !files.isEmpty () )
        {
            getLogger ().debug (
                "Creating filesets" + ( packaged ? ( " packaged (" + packageName + ")" ) : "" )
                + ( filtered ? " filtered" : "" ) + " at level " + level
            );
            if ( level == 0 )
            {
                List includes = new ArrayList ();
                List excludes = new ArrayList ();

                Iterator filesIterator = files.iterator ();
                while ( filesIterator.hasNext () )
                {
                    String file = (String) filesIterator.next ();

                    includes.add ( file );
                }

                if ( !includes.isEmpty () )
                {
                    fileSets.add (
                        createFileSet (
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
                Map groups = getGroupsMap ( files, level );

                Iterator groupIterator = groups.keySet ().iterator ();
                while ( groupIterator.hasNext () )
                {
                    String group = (String) groupIterator.next ();

                    getLogger ().debug ( "Creating filesets for group " + group );

                    if ( !packaged )
                    {
                        fileSets.add (
                            getUnpackagedFileSet (
                                filtered,
                                group,
                                (List) groups.get ( group ),
                                defaultEncoding
                            )
                        );
                    }
                    else
                    {
                        fileSets.addAll (
                            getPackagedFileSets (
                                filtered,
                                group,
                                (List) groups.get ( group ),
                                packageName,
                                defaultEncoding
                            )
                        );
                    }
                }
            } // end if

            getLogger ().debug ( "Resolved " + fileSets.size () + " filesets" );
        } // end if
        return fileSets;
    }

    private ModuleDescriptor createModule (
        Properties reverseProperties,
        Properties pomReversedProperties,
        String moduleId,
        String packageName,
        File basedir,
        File archetypeFilesDirectory,
        File replicaFilesDirectory,
        List languages,
        List filtereds,
        String defaultEncoding
    )
    throws IOException, XmlPullParserException
    {
        ModuleDescriptor archetypeDescriptor = new ModuleDescriptor ();
        archetypeDescriptor.setId ( moduleId );
        getLogger ().debug ( "Starting module's descriptor " + moduleId );

        archetypeFilesDirectory.mkdirs ();
        getLogger ().debug ( "Module's files output directory " + archetypeFilesDirectory );

        Model pom =
            pomManager.readPom ( FileUtils.resolveFile ( basedir, Constants.ARCHETYPE_POM ) );

        List fileNames = resolveFileNames ( pom, basedir );

        List filesets =
            resolveFileSets ( packageName, fileNames, languages, filtereds, defaultEncoding );
        getLogger ().debug ( "Resolved filesets for module " + archetypeDescriptor.getId () );

        archetypeDescriptor.setFileSets ( filesets );

        createArchetypeFiles (
            reverseProperties,
            filesets,
            packageName,
            basedir,
            archetypeFilesDirectory,
            defaultEncoding
        );
        getLogger ().debug ( "Created files for module " + archetypeDescriptor.getId () );

        createReplicaFiles ( filesets, basedir, replicaFilesDirectory );
        getLogger ().debug ( "Created replica files for " + archetypeDescriptor.getId () );

        String parentArtifactId = reverseProperties.getProperty ( Constants.PARENT_ARTIFACT_ID );
        setParentArtifactId ( reverseProperties, pomReversedProperties, moduleId );

        Iterator modules = pom.getModules ().iterator ();
        while ( modules.hasNext () )
        {
            String subModuleId = (String) modules.next ();

            setArtifactId ( reverseProperties, pomReversedProperties, subModuleId );

            getLogger ().debug ( "Creating module " + subModuleId );

            ModuleDescriptor moduleDescriptor =
                createModule (
                    reverseProperties,
                    pomReversedProperties,
                    subModuleId,
                    packageName,
                    FileUtils.resolveFile ( basedir, subModuleId ),
                    FileUtils.resolveFile ( archetypeFilesDirectory, subModuleId ),
                    FileUtils.resolveFile ( replicaFilesDirectory, subModuleId ),
                    languages,
                    filtereds,
                    defaultEncoding
                );

            archetypeDescriptor.addModule ( moduleDescriptor );
            getLogger ().debug (
                "Added module " + moduleDescriptor.getId () + " in "
                + archetypeDescriptor.getId ()
            );
        }
        restoreParentArtifactId ( reverseProperties, pomReversedProperties, parentArtifactId );
        restoreArtifactId ( reverseProperties, pomReversedProperties, moduleId );

        createModulePom (
            pom,
            archetypeFilesDirectory,
            pomReversedProperties,
            FileUtils.resolveFile ( basedir, Constants.ARCHETYPE_POM )
        );
        getLogger ().debug ( "Created Module " + archetypeDescriptor.getId () + " pom" );

        return archetypeDescriptor;
    }

    private void createModulePom (
        Model pom,
        File archetypeFilesDirectory,
        Properties pomReversedProperties,
        File initialPomFile
    )
    throws IOException
    {
//        pom.setParent ( null );
//        pom.setModules ( null );
//        pom.setGroupId ( "${" + Constants.GROUP_ID + "}" );
//        pom.setArtifactId ( "${" + Constants.ARTIFACT_ID + "}" );
//        pom.setVersion ( "${" + Constants.VERSION + "}" );

        File outputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM );

        File inputFile =
            FileUtils.resolveFile ( archetypeFilesDirectory, Constants.ARCHETYPE_POM + ".tmp" );

        FileUtils.copyFile ( initialPomFile, inputFile );

//        pomManager.writePom ( pom, inputFile );

        String initialcontent = FileUtils.fileRead ( inputFile );

        String content = getReversedContent ( initialcontent, pomReversedProperties );

        outputFile.getParentFile ().mkdirs ();

        FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );

        inputFile.delete ();
    }

    private void createReplicaFiles ( List filesets, File basedir, File replicaFilesDirectory )
    throws IOException
    {
        getLogger ().debug (
            "Creating Archetype/Module replica files from " + basedir + " to "
            + replicaFilesDirectory
        );

        copyPom ( basedir, replicaFilesDirectory );

        Iterator iterator = filesets.iterator ();

        while ( iterator.hasNext () )
        {
            FileSet fileset = (FileSet) iterator.next ();

            DirectoryScanner scanner = new DirectoryScanner ();
            scanner.setBasedir ( basedir );
            scanner.setIncludes (
                (String[]) concatenateToList ( fileset.getIncludes (), fileset.getDirectory () )
                .toArray ( new String[fileset.getIncludes ().size ()] )
            );
            scanner.setExcludes (
                (String[]) fileset.getExcludes ().toArray (
                    new String[fileset.getExcludes ().size ()]
                )
            );
            scanner.addDefaultExcludes ();
            getLogger ().debug ( "Using fileset " + fileset );
            scanner.scan ();

            List fileSetResources = Arrays.asList ( scanner.getIncludedFiles () );

            copyFiles (
                basedir,
                replicaFilesDirectory,
                fileset.getDirectory (),
                fileSetResources,
                false,
                null
            );
            getLogger ().debug ( "Copied " + fileset.getDirectory () + " files" );
        }
    }

    private Set getExtensions ( List files )
    {
        Set extensions = new HashSet ();
        Iterator filesIterator = files.iterator ();
        while ( filesIterator.hasNext () )
        {
            String file = (String) filesIterator.next ();

            extensions.add ( FileUtils.extension ( file ) );
        }

        return extensions;
    }

    private String getGeneratedSourcesDirectory ()
    {
        return "target" + File.separator + "generated-sources" + File.separator + "archetypeng";
    }

    private Map getGroupsMap ( final List files, final int level )
    {
        Map groups = new HashMap ();
        Iterator fileIterator = files.iterator ();
        while ( fileIterator.hasNext () )
        {
            String file = (String) fileIterator.next ();

            String directory = PathUtils.getDirectory ( file, level );

            if ( !groups.containsKey ( directory ) )
            {
                groups.put ( directory, new ArrayList () );
            }

            List group = (List) groups.get ( directory );

            String innerPath = file.substring ( directory.length () + 1 );

            group.add ( innerPath );
        }
        getLogger ().debug (
            "Sorted " + groups.size () + " groups in " + files.size () + " files"
        );
        return groups;
    }

    private Properties initialiseArchetypeProperties ( File propertyFile )
    throws IOException
    {
        Properties properties = new Properties ();
        archetypePropertiesManager.readProperties ( properties, propertyFile );
        return properties;
    }

    private FileSet getPackagedFileSet (
        final boolean filtered,
        final Set packagedExtensions,
        final String group,
        final Set unpackagedExtensions,
        final List unpackagedFiles,
        String defaultEncoding
    )
    {
        List includes = new ArrayList ();
        List excludes = new ArrayList ();

        Iterator extensionsIterator = packagedExtensions.iterator ();
        while ( extensionsIterator.hasNext () )
        {
            String extension = (String) extensionsIterator.next ();

            includes.add ( "**/*." + extension );

            if ( unpackagedExtensions.contains ( extension ) )
            {
                excludes.addAll (
                    archetypeFilesResolver.getFilesWithExtension ( unpackagedFiles, extension )
                );
            }
        }

        FileSet fileset =
            createFileSet ( excludes, true, filtered, group, includes, defaultEncoding );
        return fileset;
    }

    private List getPackagedFileSets (
        final boolean filtered,
        final String group,
        final List groupFiles,
        final String packageName,
        String defaultEncoding
    )
    {
        String packageAsDir = StringUtils.replace ( packageName, ".", "/" );
        List packagedFileSets = new ArrayList ();
        List packagedFiles = archetypeFilesResolver.getPackagedFiles ( groupFiles, packageAsDir );

        List unpackagedFiles =
            archetypeFilesResolver.getUnpackagedFiles ( groupFiles, packageAsDir );

        Set packagedExtensions = getExtensions ( packagedFiles );
        getLogger ().debug ( "Found packaged extensions " + packagedExtensions );

        Set unpackagedExtensions = getExtensions ( unpackagedFiles );

        packagedFileSets.add (
            getPackagedFileSet (
                filtered,
                packagedExtensions,
                group,
                unpackagedExtensions,
                unpackagedFiles,
                defaultEncoding
            )
        );

        if ( !unpackagedExtensions.isEmpty () )
        {
            getLogger ().debug ( "Found unpackaged extensions " + unpackagedExtensions );
            packagedFileSets.add (
                getUnpackagedFileSet (
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

    private void setParentArtifactId (
        Properties reverseProperties,
        Properties pomReversedProperties,
        String parentArtifactId
    )
    {
        reverseProperties.setProperty ( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
        pomReversedProperties.setProperty ( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
    }

    private void processFileSet (
        File basedir,
        File archetypeFilesDirectory,
        String directory,
        List fileSetResources,
        boolean packaged,
        String packageName,
        Properties reverseProperties,
        String defaultEncoding
    )
    throws IOException
    {
        Iterator iterator = fileSetResources.iterator ();

        while ( iterator.hasNext () )
        {
            String inputFileName = (String) iterator.next ();

            String packageAsDirectory = StringUtils.replace ( packageName, ".", "/" );

            String outputFileName =
                packaged ? StringUtils.replace ( inputFileName, packageAsDirectory + "/", "" )
                         : inputFileName;

            File outputFile = new File ( archetypeFilesDirectory, outputFileName );
            File inputFile = new File ( basedir, inputFileName );

            FileCharsetDetector detector = new FileCharsetDetector ( inputFile );

            String fileEncoding = detector.isFound () ? detector.getCharset () : defaultEncoding;

            String initialcontent =
                org.apache.commons.io.IOUtils.toString (
                    new FileInputStream ( inputFile ),
                    fileEncoding
                );
//            String initialcontent = FileUtils.fileRead ( inputFile );
            String content = getReversedContent ( initialcontent, reverseProperties );
            outputFile.getParentFile ().mkdirs ();
            org.apache.commons.io.IOUtils.write (
                content,
                new FileOutputStream ( outputFile ),
                fileEncoding
            );
//            FileUtils.fileWrite ( outputFile.getAbsolutePath (), content );
        } // end while
    }

    private String getReplicaOutputDirectory ()
    {
        return
            Constants.SRC + File.separator + Constants.TEST + File.separator + Constants.RESOURCES
            + File.separator + "projects";
    }

    private Properties getRequiredProperties (
        ArchetypeDescriptor archetypeDescriptor,
        Properties properties
    )
    {
        Properties reversedProperties = new Properties ();

        reversedProperties.putAll ( properties );
        reversedProperties.remove ( Constants.ARCHETYPE_GROUP_ID );
        reversedProperties.remove ( Constants.ARCHETYPE_ARTIFACT_ID );
        reversedProperties.remove ( Constants.ARCHETYPE_VERSION );
//        reversedProperties.remove ( Constants.GROUP_ID );
//        reversedProperties.remove ( Constants.ARTIFACT_ID );
//        reversedProperties.remove ( Constants.VERSION );

        return reversedProperties;
    }

    private List resolveFileNames ( final Model pom, final File basedir )
    throws IOException
    {
        getLogger ().debug ( "Resolving files for " + pom.getId () + " in " + basedir );

        Iterator modules = pom.getModules ().iterator ();
        String excludes = "pom.xml*,archetype.properties*,target/**,";
        while ( modules.hasNext () )
        {
            excludes += "," + (String) modules.next () + "/**";
        }

        Iterator defaultExcludes = Arrays.asList ( ListScanner.DEFAULTEXCLUDES ).iterator ();
        while ( defaultExcludes.hasNext () )
        {
            excludes += "," + (String) defaultExcludes.next () + "/**";
        }

        List fileNames = FileUtils.getFileNames ( basedir, "**", excludes, false );

        getLogger ().debug ( "Resolved " + fileNames.size () + " files" );

        return fileNames;
    }

    private List resolveFileSets (
        String packageName,
        List fileNames,
        List languages,
        List filtereds,
        String defaultEncoding
    )
    {
        List resolvedFileSets = new ArrayList ();
        getLogger ().debug (
            "Resolving filesets with package=" + packageName + ", languages=" + languages
            + " and extentions=" + filtereds
        );

        List files = new ArrayList ( fileNames );

        String languageIncludes = "";
        Iterator languagesIterator = languages.iterator ();
        while ( languagesIterator.hasNext () )
        {
            String language = (String) languagesIterator.next ();

            languageIncludes +=
                ( ( languageIncludes.length () == 0 ) ? "" : "," ) + language + "/**";
        }

        String filteredIncludes = "";
        Iterator filteredsIterator = filtereds.iterator ();
        while ( filteredsIterator.hasNext () )
        {
            String filtered = (String) filteredsIterator.next ();

            filteredIncludes +=
                ( ( filteredIncludes.length () == 0 ) ? "" : "," ) + "**/"
                + ( filtered.startsWith ( "." ) ? "" : "*." ) + filtered;
        }

        /*sourcesMainFiles*/
        List sourcesMainFiles =
            archetypeFilesResolver.findSourcesMainFiles ( files, languageIncludes );
        if ( !sourcesMainFiles.isEmpty () )
        {
            files.removeAll ( sourcesMainFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( sourcesMainFiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        sourcesMainFiles,
                        filteredIncludes
                    ),
                    3,
                    true,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /*resourcesMainFiles*/
        List resourcesMainFiles =
            archetypeFilesResolver.findResourcesMainFiles ( files, languageIncludes );
        if ( !resourcesMainFiles.isEmpty () )
        {
            files.removeAll ( resourcesMainFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles (
                        resourcesMainFiles,
                        filteredIncludes
                    ),
                    3,
                    false,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        resourcesMainFiles,
                        filteredIncludes
                    ),
                    3,
                    false,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /*sourcesTestFiles*/
        List sourcesTestFiles =
            archetypeFilesResolver.findSourcesTestFiles ( files, languageIncludes );
        if ( !sourcesTestFiles.isEmpty () )
        {
            files.removeAll ( sourcesTestFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( sourcesTestFiles, filteredIncludes ),
                    3,
                    true,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        sourcesTestFiles,
                        filteredIncludes
                    ),
                    3,
                    true,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /*ressourcesTestFiles*/
        List resourcesTestFiles =
            archetypeFilesResolver.findResourcesTestFiles ( files, languageIncludes );
        if ( !resourcesTestFiles.isEmpty () )
        {
            files.removeAll ( resourcesTestFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles (
                        resourcesTestFiles,
                        filteredIncludes
                    ),
                    3,
                    false,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        resourcesTestFiles,
                        filteredIncludes
                    ),
                    3,
                    false,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /*siteFiles*/
        List siteFiles = archetypeFilesResolver.findSiteFiles ( files, languageIncludes );
        if ( !siteFiles.isEmpty () )
        {
            files.removeAll ( siteFiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles ( siteFiles, filteredIncludes ),
                    2,
                    false,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles ( siteFiles, filteredIncludes ),
                    2,
                    false,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /*thirdLevelSourcesfiles*/
        List thirdLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources ( 3, files, languageIncludes );
        if ( !thirdLevelSourcesfiles.isEmpty () )
        {
            files.removeAll ( thirdLevelSourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles (
                        thirdLevelSourcesfiles,
                        filteredIncludes
                    ),
                    3,
                    true,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        thirdLevelSourcesfiles,
                        filteredIncludes
                    ),
                    3,
                    true,
                    packageName,
                    false,
                    defaultEncoding
                )
            );

            /*thirdLevelResourcesfiles*/
            List thirdLevelResourcesfiles =
                archetypeFilesResolver.findOtherResources (
                    3,
                    files,
                    thirdLevelSourcesfiles,
                    languageIncludes
                );
            if ( !thirdLevelResourcesfiles.isEmpty () )
            {
                files.removeAll ( thirdLevelResourcesfiles );
                resolvedFileSets.addAll (
                    createFileSets (
                        archetypeFilesResolver.getFilteredFiles (
                            thirdLevelResourcesfiles,
                            filteredIncludes
                        ),
                        3,
                        false,
                        packageName,
                        true,
                        defaultEncoding
                    )
                );
                resolvedFileSets.addAll (
                    createFileSets (
                        archetypeFilesResolver.getUnfilteredFiles (
                            thirdLevelResourcesfiles,
                            filteredIncludes
                        ),
                        3,
                        false,
                        packageName,
                        false,
                        defaultEncoding
                    )
                );
            }
        } // end if

        /*secondLevelSourcesfiles*/
        List secondLevelSourcesfiles =
            archetypeFilesResolver.findOtherSources ( 2, files, languageIncludes );
        if ( !secondLevelSourcesfiles.isEmpty () )
        {
            files.removeAll ( secondLevelSourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles (
                        secondLevelSourcesfiles,
                        filteredIncludes
                    ),
                    2,
                    true,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        secondLevelSourcesfiles,
                        filteredIncludes
                    ),
                    2,
                    true,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /*secondLevelResourcesfiles*/
        List secondLevelResourcesfiles =
            archetypeFilesResolver.findOtherResources ( 2, files, languageIncludes );
        if ( !secondLevelResourcesfiles.isEmpty () )
        {
            files.removeAll ( secondLevelResourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles (
                        secondLevelResourcesfiles,
                        filteredIncludes
                    ),
                    2,
                    false,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        secondLevelResourcesfiles,
                        filteredIncludes
                    ),
                    2,
                    false,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /*rootResourcesfiles*/
        List rootResourcesfiles =
            archetypeFilesResolver.findOtherResources ( 0, files, languageIncludes );
        if ( !rootResourcesfiles.isEmpty () )
        {
            files.removeAll ( rootResourcesfiles );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getFilteredFiles (
                        rootResourcesfiles,
                        filteredIncludes
                    ),
                    0,
                    false,
                    packageName,
                    true,
                    defaultEncoding
                )
            );
            resolvedFileSets.addAll (
                createFileSets (
                    archetypeFilesResolver.getUnfilteredFiles (
                        rootResourcesfiles,
                        filteredIncludes
                    ),
                    0,
                    false,
                    packageName,
                    false,
                    defaultEncoding
                )
            );
        }

        /**/
        if ( !files.isEmpty () )
        {
            getLogger ().info ( "Ignored files: " + files );
        }

        return resolvedFileSets;
    }

    private void restoreArtifactId (
        Properties reverseProperties,
        Properties pomReversedProperties,
        String artifactId
    )
    {
        if ( StringUtils.isEmpty ( artifactId ) )
        {
            reverseProperties.remove ( Constants.ARTIFACT_ID );
            pomReversedProperties.remove ( Constants.ARTIFACT_ID );
        }
        else
        {
            reverseProperties.setProperty ( Constants.ARTIFACT_ID, artifactId );
            pomReversedProperties.setProperty ( Constants.ARTIFACT_ID, artifactId );
        }
    }

    private void restoreParentArtifactId (
        Properties reverseProperties,
        Properties pomReversedProperties,
        String parentArtifactId
    )
    {
        if ( StringUtils.isEmpty ( parentArtifactId ) )
        {
            reverseProperties.remove ( Constants.PARENT_ARTIFACT_ID );
            pomReversedProperties.remove ( Constants.PARENT_ARTIFACT_ID );
        }
        else
        {
            reverseProperties.setProperty ( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
            pomReversedProperties.setProperty ( Constants.PARENT_ARTIFACT_ID, parentArtifactId );
        }
    }

    private String getReversedContent ( String content, final Properties properties )
    {
        String result = content;
        Iterator propertyIterator = properties.keySet ().iterator ();
        while ( propertyIterator.hasNext () )
        {
            String propertyKey = (String) propertyIterator.next ();
            result =
                StringUtils.replace (
                    result,
                    properties.getProperty ( propertyKey ),
                    "${" + propertyKey + "}"
                );
        }
        return result;
    }

    private String getTemplateOutputDirectory ()
    {
        return
            Constants.SRC + File.separator + Constants.MAIN + File.separator + Constants.RESOURCES;
    }

    private FileSet getUnpackagedFileSet (
        final boolean filtered,
        final String group,
        final List groupFiles,
        String defaultEncoding
    )
    {
        Set extensions = getExtensions ( groupFiles );

        List includes = new ArrayList ();
        List excludes = new ArrayList ();

        Iterator extensionsIterator = extensions.iterator ();
        while ( extensionsIterator.hasNext () )
        {
            String extension = (String) extensionsIterator.next ();

            includes.add ( "**/*." + extension );
        }

        return createFileSet ( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private FileSet getUnpackagedFileSet (
        final boolean filtered,
        final Set unpackagedExtensions,
        final List unpackagedFiles,
        final String group,
        final Set packagedExtensions,
        String defaultEncoding
    )
    {
        List includes = new ArrayList ();
        List excludes = new ArrayList ();

        Iterator extensionsIterator = unpackagedExtensions.iterator ();
        while ( extensionsIterator.hasNext () )
        {
            String extension = (String) extensionsIterator.next ();
            if ( packagedExtensions.contains ( extension ) )
            {
                includes.addAll (
                    archetypeFilesResolver.getFilesWithExtension ( unpackagedFiles, extension )
                );
            }
            else
            {
                includes.add ( "**/*." + extension );
            }
        }

        return createFileSet ( excludes, false, filtered, group, includes, defaultEncoding );
    }

    private OldArchetypeDescriptor convertToOldDescriptor (String id, String packageName, File basedir) throws IOException
    {
        getLogger ().debug ( "Resolving OldArchetypeDescriptor files in " + basedir );

        String excludes = "pom.xml,archetype.properties*,**/target/**";

        Iterator defaultExcludes = Arrays.asList ( ListScanner.DEFAULTEXCLUDES ).iterator ();
        while ( defaultExcludes.hasNext () )
        {
            excludes += "," + (String) defaultExcludes.next () + "/**";
        }

        List fileNames = FileUtils.getFileNames ( basedir, "**", excludes, false );

        getLogger ().debug ( "Resolved " + fileNames.size () + " files" );

        String packageAsDirectory = StringUtils.replace (packageName, '.', '/') +"/";

        List sources = archetypeFilesResolver.findSourcesMainFiles (fileNames, "java/**");
        fileNames.removeAll (sources);
         sources = removePackage(sources, packageAsDirectory);

        List testSources = archetypeFilesResolver.findSourcesTestFiles (fileNames, "java/**");
        fileNames.removeAll (testSources);
         testSources = removePackage(testSources, packageAsDirectory);

        List resources = archetypeFilesResolver.findResourcesMainFiles (fileNames, "java/**");
        fileNames.removeAll (resources);

        List testResources = archetypeFilesResolver.findResourcesTestFiles (fileNames, "java/**");
        fileNames.removeAll (testResources);

        List siteResources = archetypeFilesResolver.findSiteFiles (fileNames, null);
        fileNames.removeAll (siteResources);

        resources.addAll (fileNames);

        OldArchetypeDescriptor descriptor = new OldArchetypeDescriptor();
        descriptor.setId (id);
        descriptor.setSources (sources);
        descriptor.setTestSources (testSources);
        descriptor.setResources (resources);
        descriptor.setTestResources (testResources);
        descriptor.setSiteResources (siteResources);

        return descriptor;
    }

    private void writeOldDescriptor (OldArchetypeDescriptor oldDescriptor, File oldDescriptorFile) throws IOException
    {
        OldArchetypeDescriptorXpp3Writer writer = new OldArchetypeDescriptorXpp3Writer ();
        writer.write ( new FileWriter ( oldDescriptorFile ), oldDescriptor );
    }

    private List removePackage (List sources, String packageAsDirectory)
    {
        if (sources == null)
        {
            return null;
        }

        List unpackagedSources = new ArrayList (sources.size ());
        Iterator sourcesIterator = sources.iterator ();
        while (sourcesIterator.hasNext ())
        {
            String source = (String) sourcesIterator.next ();
            String unpackagedSource = StringUtils.replace (source, packageAsDirectory,"");
            unpackagedSources.add (unpackagedSource);
        }

        return unpackagedSources;
    }
}
