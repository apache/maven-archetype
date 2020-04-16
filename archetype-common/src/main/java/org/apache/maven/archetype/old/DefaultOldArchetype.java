package org.apache.maven.archetype.old;

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

import org.apache.commons.io.input.XmlStreamReader;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.common.util.PomUtils;
import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptorBuilder;
import org.apache.maven.archetype.old.descriptor.TemplateDescriptor;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
@Component( role = OldArchetype.class )
public class DefaultOldArchetype
    extends AbstractLogEnabled
    implements OldArchetype
{
    private static final String DEFAULT_TEST_RESOURCE_DIR = "/src/test/resources";

    private static final String DEFAULT_TEST_SOURCE_DIR = "/src/test/java";

    private static final String DEFAULT_RESOURCE_DIR = "/src/main/resources";

    private static final String DEFAULT_SOURCE_DIR = "/src/main/java";

    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    @Requirement
    private VelocityComponent velocity;

    @Requirement
    private ArchetypeArtifactManager archetypeArtifactManager;

    // ----------------------------------------------------------------------
    // Implementation
    // ----------------------------------------------------------------------

    // groupId = maven
    // artifactId = maven-foo-archetype
    // version = latest

    @Override
    public void createArchetype( ArchetypeGenerationRequest request, ArtifactRepository archetypeRepository )
            throws UnknownArchetype, ArchetypeDescriptorException, ArchetypeTemplateProcessingException,
            InvalidPackaging
    {
        // ----------------------------------------------------------------------
        // Download the archetype
        // ----------------------------------------------------------------------

        File archetypeFile =
            archetypeArtifactManager.getArchetypeFile( request.getArchetypeGroupId(), request.getArchetypeArtifactId(),
                                                       request.getArchetypeVersion(), archetypeRepository,
                                                       request.getLocalRepository(),
                                                       request.getRemoteArtifactRepositories(),
                                                       request.getProjectBuildingRequest() );

        createArchetype( request, archetypeFile );
    }

    @Override
    public void createArchetype( ArchetypeGenerationRequest request, File archetypeFile )
            throws ArchetypeDescriptorException, ArchetypeTemplateProcessingException, InvalidPackaging
    {
        Map<String, String> parameters = new HashMap<>();

        parameters.put( "basedir", request.getOutputDirectory() );

        parameters.put( Constants.PACKAGE, request.getPackage() );

        parameters.put( "packageName", request.getPackage() );

        parameters.put( Constants.GROUP_ID, request.getGroupId() );

        parameters.put( Constants.ARTIFACT_ID, request.getArtifactId() );

        parameters.put( Constants.VERSION, request.getVersion() );

        // ---------------------------------------------------------------------
        // Get Logger and display all parameters used
        // ---------------------------------------------------------------------
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "----------------------------------------------------------------------------" );

            getLogger().info( "Using following parameters for creating project from Old (1.x) Archetype: "
                                  + request.getArchetypeArtifactId() + ":" + request.getArchetypeVersion() );

            getLogger().info( "----------------------------------------------------------------------------" );

            for ( Map.Entry<String, String> entry : parameters.entrySet() )
            {
                String parameterName = entry.getKey();

                String parameterValue = entry.getValue();

                getLogger().info( "Parameter: " + parameterName + ", Value: " + parameterValue );
            }
        }

        // ----------------------------------------------------------------------
        // Load the descriptor
        // ----------------------------------------------------------------------

        ArchetypeDescriptorBuilder builder = new ArchetypeDescriptorBuilder();

        ArchetypeDescriptor descriptor;

        URLClassLoader archetypeJarLoader;

        URL[] urls;
        try
        {
            urls = new URL[] {archetypeFile.toURI().toURL() };
        }
        catch ( MalformedURLException e )
        {
            throw new ArchetypeDescriptorException( e.getMessage() );
        }

        archetypeJarLoader = new URLClassLoader( urls );

        try ( InputStream is = getDescriptorInputStream( archetypeJarLoader ) )
        {
            descriptor = builder.build( new XmlStreamReader( is ) );
        }
        catch ( IOException | XmlPullParserException e )
        {
            throw new ArchetypeDescriptorException( "Error reading the " + ARCHETYPE_DESCRIPTOR + " descriptor.", e );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String artifactId = request.getArtifactId();

        File parentPomFile = new File( request.getOutputDirectory(), ARCHETYPE_POM );

        File outputDirectoryFile;

        boolean creating;
        File pomFile;
        if ( parentPomFile.exists() && descriptor.isAllowPartial() && artifactId == null )
        {
            outputDirectoryFile = new File( request.getOutputDirectory() );
            creating = false;
            pomFile = parentPomFile;
        }
        else
        {
            if ( artifactId == null )
            {
                throw new ArchetypeTemplateProcessingException(
                    "Artifact ID must be specified when creating a new project from an archetype." );
            }

            outputDirectoryFile = new File( request.getOutputDirectory(), artifactId );
            creating = true;

            if ( outputDirectoryFile.exists() )
            {
                if ( descriptor.isAllowPartial() )
                {
                    creating = false;
                }
                else
                {
                    throw new ArchetypeTemplateProcessingException( "Directory "
                        + outputDirectoryFile.getName() + " already exists - please run from a clean directory" );
                }
            }

            pomFile = new File( outputDirectoryFile, ARCHETYPE_POM );
        }

        if ( creating )
        {
            if ( request.getGroupId() == null )
            {
                throw new ArchetypeTemplateProcessingException(
                    "Group ID must be specified when creating a new project from an archetype." );
            }

            if ( request.getVersion() == null )
            {
                throw new ArchetypeTemplateProcessingException(
                    "Version must be specified when creating a new project from an archetype." );
            }
        }

        String outputDirectory = outputDirectoryFile.getAbsolutePath();

        String packageName = request.getPackage();

        // ----------------------------------------------------------------------
        // Set up the Velocity context
        // ----------------------------------------------------------------------

        Context context = new VelocityContext();

        context.put( Constants.PACKAGE, packageName );

        for ( Map.Entry<String, String> entry : parameters.entrySet() )
        {
            context.put( entry.getKey(), entry.getValue() );
        }

        // ----------------------------------------------------------------------
        // Process the templates
        // ----------------------------------------------------------------------

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader( archetypeJarLoader );

        Model parentModel = null;
        if ( creating )
        {
            if ( parentPomFile.exists() )
            {
                try ( Reader fileReader = ReaderFactory.newXmlReader( parentPomFile ) )
                {
                    MavenXpp3Reader reader = new MavenXpp3Reader();
                    parentModel = reader.read( fileReader );
                    if ( !"pom".equals( parentModel.getPackaging() ) )
                    {
                        throw new ArchetypeTemplateProcessingException(
                            "Unable to add module to the current project as it is not of packaging type 'pom'" );
                    }
                }
                catch ( IOException | XmlPullParserException e )
                {
                    throw new ArchetypeTemplateProcessingException( "Unable to read parent POM", e );
                }
                parentModel.getModules().add( artifactId );
            }
        }

        try
        {
            processTemplates( pomFile, outputDirectory, context, descriptor, packageName, parentModel );
        }
        catch ( IOException e )
        {
            throw new ArchetypeTemplateProcessingException( "Unable to process template", e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( old );
        }

        if ( parentModel != null )
        {
/*
        // TODO: would be nice to just write out with the xpp3 writer again, except that it loses a bunch of info and
        // reformats, so the module is just baked in as a string instead.
            FileWriter fileWriter = null;

            try
            {
                fileWriter = new FileWriter( parentPomFile );

                MavenXpp3Writer writer = new MavenXpp3Writer();
                writer.write( fileWriter, parentModel );
            }
            catch ( IOException e )
            {
                throw new ArchetypeTemplateProcessingException( "Unable to rewrite parent POM", e );
            }
            finally
            {
                IOUtil.close( fileWriter );
            }
*/
            
            boolean added;
            StringWriter w = new StringWriter();
            try ( Reader fileReader = ReaderFactory.newXmlReader( parentPomFile ) )
            {
                added = addModuleToParentPom( artifactId, fileReader, w );
            }
            catch ( IOException | SAXException | ParserConfigurationException | TransformerException  e )
            {
                throw new ArchetypeTemplateProcessingException( "Unable to rewrite parent POM", e );
            }

            if ( added )
            {
                try ( Writer out = WriterFactory.newXmlWriter( parentPomFile ) )
                {
                    IOUtil.copy( w.toString(), out );
                }
                catch ( IOException e )
                {
                    throw new ArchetypeTemplateProcessingException( "Unable to rewrite parent POM", e );
                }
            }
        }

        // ----------------------------------------------------------------------
        // Log message on OldArchetype creation
        // ----------------------------------------------------------------------
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "project created from Old (1.x) Archetype in dir: " + outputDirectory );
        }

    }

    private InputStream getDescriptorInputStream( ClassLoader archetypeJarLoader ) throws ArchetypeDescriptorException
    {
        InputStream is = getStream( ARCHETYPE_DESCRIPTOR, archetypeJarLoader );

        if ( is == null )
        {
            is = getStream( ARCHETYPE_OLD_DESCRIPTOR, archetypeJarLoader );
        }

        if ( is == null )
        {
            throw new ArchetypeDescriptorException( "The " + ARCHETYPE_DESCRIPTOR
                                                    + " descriptor cannot be found." );
        }
        
        return is;
    }

    static boolean addModuleToParentPom( String artifactId, Reader fileReader, Writer fileWriter )
            throws ArchetypeTemplateProcessingException, InvalidPackaging, IOException, ParserConfigurationException,
            SAXException, TransformerException
    {
        return PomUtils.addNewModule( artifactId, fileReader, fileWriter );
    }

    private void processTemplates( File pomFile, String outputDirectory, Context context,
                                   ArchetypeDescriptor descriptor, String packageName, Model parentModel )
            throws ArchetypeTemplateProcessingException, IOException
    {
        if ( !pomFile.exists() )
        {
            processTemplate( outputDirectory, context, ARCHETYPE_POM, new TemplateDescriptor(), false, null );
        }

        // ---------------------------------------------------------------------
        // Model generated for the new archetype, so process it now
        // ---------------------------------------------------------------------

        Model generatedModel;
        
        try ( Reader pomReader = ReaderFactory.newXmlReader( pomFile ) )
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();

            generatedModel = reader.read( pomReader );
        }
        catch ( IOException | XmlPullParserException e )
        {
            throw new ArchetypeTemplateProcessingException( "Error reading POM", e );
        }

        if ( parentModel != null )
        {
            Parent parent = new Parent();
            parent.setGroupId( parentModel.getGroupId() );
            if ( parent.getGroupId() == null )
            {
                parent.setGroupId( parentModel.getParent().getGroupId() );
            }
            parent.setArtifactId( parentModel.getArtifactId() );
            parent.setVersion( parentModel.getVersion() );
            if ( parent.getVersion() == null )
            {
                parent.setVersion( parentModel.getParent().getVersion() );
            }
            generatedModel.setParent( parent );

            try (  Writer pomWriter = WriterFactory.newXmlWriter( pomFile ) )
            {
                MavenXpp3Writer writer = new MavenXpp3Writer();
                writer.write( pomWriter, generatedModel );
            }
            catch ( IOException e )
            {
                throw new ArchetypeTemplateProcessingException( "Error rewriting POM", e );
            }
        }

        // XXX: Following POM processing block may be a candidate for
        // refactoring out into service methods or moving to
        // createProjectDirectoryStructure(outputDirectory)
        Build build = generatedModel.getBuild();

        boolean overrideSrcDir = false;

        boolean overrideResourceDir = false;

        boolean overrideTestSrcDir = false;

        boolean overrideTestResourceDir = false;

        boolean foundBuildElement = build != null;

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "********************* Debug info for resources created from generated Model ***********************" );
            getLogger().debug( "Was build element found in generated POM?: " + foundBuildElement );
        }

        // create source directory if specified in POM
        if ( foundBuildElement && null != build.getSourceDirectory() )
        {
            getLogger().debug( "Overriding default source directory " );

            overrideSrcDir = true;

            String srcDirectory = build.getSourceDirectory();

            srcDirectory = StringUtils.replace( srcDirectory, "\\", "/" );

            FileUtils.mkdir( getOutputDirectory( outputDirectory, srcDirectory ) );
        }

        // create script source directory if specified in POM
        if ( foundBuildElement && null != build.getScriptSourceDirectory() )
        {
            getLogger().debug( "Overriding default script source directory " );

            String scriptSourceDirectory = build.getScriptSourceDirectory();

            scriptSourceDirectory = StringUtils.replace( scriptSourceDirectory, "\\", "/" );

            FileUtils.mkdir( getOutputDirectory( outputDirectory, scriptSourceDirectory ) );
        }

        // create resource director(y/ies) if specified in POM
        if ( foundBuildElement && build.getResources().size() > 0 )
        {
            getLogger().debug( "Overriding default resource directory " );

            overrideResourceDir = true;

            Iterator<?> resourceItr = build.getResources().iterator();

            while ( resourceItr.hasNext() )
            {
                Resource resource = (Resource) resourceItr.next();

                String resourceDirectory = resource.getDirectory();

                resourceDirectory = StringUtils.replace( resourceDirectory, "\\", "/" );

                FileUtils.mkdir( getOutputDirectory( outputDirectory, resourceDirectory ) );
            }
        }
        // create test source directory if specified in POM
        if ( foundBuildElement && null != build.getTestSourceDirectory() )
        {
            getLogger().debug( "Overriding default test directory " );

            overrideTestSrcDir = true;

            String testDirectory = build.getTestSourceDirectory();

            testDirectory = StringUtils.replace( testDirectory, "\\", "/" );

            FileUtils.mkdir( getOutputDirectory( outputDirectory, testDirectory ) );
        }

        // create test resource directory if specified in POM
        if ( foundBuildElement && build.getTestResources().size() > 0 )
        {
            getLogger().debug( "Overriding default test resource directory " );

            overrideTestResourceDir = true;

            Iterator<?> testResourceItr = build.getTestResources().iterator();

            while ( testResourceItr.hasNext() )
            {
                Resource resource = (Resource) testResourceItr.next();

                String testResourceDirectory = resource.getDirectory();

                testResourceDirectory = StringUtils.replace( testResourceDirectory, "\\", "/" );

                FileUtils.mkdir( getOutputDirectory( outputDirectory, testResourceDirectory ) );
            }
        }

        getLogger().debug(
            "********************* End of debug info from resources from generated POM ***********************" );

        // ----------------------------------------------------------------------
        // Main
        // ----------------------------------------------------------------------

        if ( descriptor.getSources().size() > 0 )
        {
            if ( !overrideSrcDir )
            {
                FileUtils.mkdir( outputDirectory + DEFAULT_SOURCE_DIR );
                processSources( outputDirectory, context, descriptor, packageName, DEFAULT_SOURCE_DIR );
            }
            else
            {
                processSources( outputDirectory, context, descriptor, packageName, build.getSourceDirectory() );
            }
        }

        if ( descriptor.getResources().size() > 0 )
        {
            if ( !overrideResourceDir )
            {
                FileUtils.mkdir( outputDirectory + DEFAULT_RESOURCE_DIR );
            }
            processResources( outputDirectory, context, descriptor, packageName );
        }

        // ----------------------------------------------------------------------
        // Test
        // ----------------------------------------------------------------------

        if ( descriptor.getTestSources().size() > 0 )
        {
            if ( !overrideTestSrcDir )
            {
                FileUtils.mkdir( outputDirectory + DEFAULT_TEST_SOURCE_DIR );
                processTestSources( outputDirectory, context, descriptor, packageName, DEFAULT_TEST_SOURCE_DIR );
            }
            else
            {
                processTestSources( outputDirectory, context, descriptor, packageName, build.getTestSourceDirectory() );
            }
        }

        if ( descriptor.getTestResources().size() > 0 )
        {
            if ( !overrideTestResourceDir )
            {
                FileUtils.mkdir( outputDirectory + DEFAULT_TEST_RESOURCE_DIR );
            }
            processTestResources( outputDirectory, context, descriptor, packageName );
        }

        // ----------------------------------------------------------------------
        // Site
        // ----------------------------------------------------------------------

        if ( descriptor.getSiteResources().size() > 0 )
        {
            processSiteResources( outputDirectory, context, descriptor, packageName );
        }
    }

    private void processTemplate( String outputDirectory, Context context, String template,
                                  TemplateDescriptor descriptor, boolean packageInFileName, String packageName )
            throws ArchetypeTemplateProcessingException, IOException
    {
        processTemplate( outputDirectory, context, template, descriptor, packageInFileName, packageName, null );
    }

    private String getOutputDirectory( String outputDirectory, String testResourceDirectory )
    {
        return outputDirectory
            + ( testResourceDirectory.startsWith( "/" ) ? testResourceDirectory : "/" + testResourceDirectory );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void processSources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                   String packageName, String sourceDirectory )
            throws ArchetypeTemplateProcessingException, IOException
    {
        for ( String template : descriptor.getSources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getSourceDescriptor( template ), true,
                             packageName, sourceDirectory );
        }
    }

    protected void processTestSources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                       String packageName, String testSourceDirectory )
            throws ArchetypeTemplateProcessingException, IOException
    {
        for ( String template : descriptor.getTestSources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getTestSourceDescriptor( template ), true,
                             packageName, testSourceDirectory );
        }
    }

    protected void processResources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                     String packageName )
        throws IOException, ArchetypeTemplateProcessingException
    {
        for ( String template : descriptor.getResources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getResourceDescriptor( template ), false,
                             packageName );
        }
    }

    protected void processTestResources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                         String packageName )
        throws IOException, ArchetypeTemplateProcessingException
    {
        for ( String template : descriptor.getTestResources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getTestResourceDescriptor( template ),
                             false, packageName );
        }
    }

    protected void processSiteResources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                         String packageName )
        throws IOException, ArchetypeTemplateProcessingException
    {
        for ( String template : descriptor.getSiteResources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getSiteResourceDescriptor( template ),
                             false, packageName );
        }
    }

    protected void processTemplate( String outputDirectory, Context context, String template,
                                    TemplateDescriptor descriptor, boolean packageInFileName, String packageName,
                                    String sourceDirectory )
        throws IOException, ArchetypeTemplateProcessingException
    {
        File f;

        template = StringUtils.replace( template, "\\", "/" );

        if ( packageInFileName && packageName != null )
        {
            String templateFileName = StringUtils.replace( template, "/", File.separator );

            String path = packageName.replace( '.', '/' );

            String filename = FileUtils.filename( templateFileName );

            String dirname = FileUtils.dirname( templateFileName ).replace( '\\', '/' );

            sourceDirectory = sourceDirectory.replace( '\\', '/' );
            if ( sourceDirectory.startsWith( "/" ) )
            {
                sourceDirectory = sourceDirectory.substring( 1 );
            }

            if ( !dirname.startsWith( sourceDirectory ) )
            {
                throw new ArchetypeTemplateProcessingException(
                    "Template '" + template + "' not in directory '" + sourceDirectory + "'" );
            }

            String extraPackages = dirname.substring( sourceDirectory.length() );
            if ( extraPackages.startsWith( "/" ) )
            {
                extraPackages = extraPackages.substring( 1 );
            }
            if ( extraPackages.length() > 0 )
            {
                path += "/" + extraPackages;
            }

            f = new File( new File( new File( outputDirectory, sourceDirectory ), path ), filename );
        }
        else
        {
            f = new File( outputDirectory, template );
        }

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        if ( !f.exists() && !f.createNewFile() )
        {
            getLogger().warn( "Could not create new file \"" + f.getPath() + "\" or the file already exists." );
        }

        if ( descriptor.isFiltered() )
        {
            try ( Writer writer = new OutputStreamWriter( new FileOutputStream( f ), descriptor.getEncoding() ) )
            {
                StringWriter stringWriter = new StringWriter();

                template = ARCHETYPE_RESOURCES + "/" + template;

                velocity.getEngine().mergeTemplate( template, descriptor.getEncoding(), context, stringWriter );

                writer.write( StringUtils.unifyLineSeparators( stringWriter.toString() ) );
            }
            catch ( Exception e )
            {
                throw new ArchetypeTemplateProcessingException( "Error merging velocity templates", e );
            }
        }
        else
        {
            try ( InputStream is = getStream( ARCHETYPE_RESOURCES + "/" + template, null );
                  OutputStream fos = new FileOutputStream( f ) )
            {
                IOUtil.copy( is, fos );
            }
            catch ( Exception e )
            {
                throw new ArchetypeTemplateProcessingException( "Error copying file", e );
            }
        }
    }

    protected void createProjectDirectoryStructure( String outputDirectory )
    {
    }

    private InputStream getStream( String name, ClassLoader loader )
    {
        if ( loader == null )
        {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream( name );
        }
        return loader.getResourceAsStream( name );
    }
}
