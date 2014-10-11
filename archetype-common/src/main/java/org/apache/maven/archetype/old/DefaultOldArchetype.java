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
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor;
import org.apache.maven.archetype.old.descriptor.ArchetypeDescriptorBuilder;
import org.apache.maven.archetype.old.descriptor.TemplateDescriptor;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.exception.UnknownArchetype;
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
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

    public void createArchetype( ArchetypeGenerationRequest request, ArtifactRepository archetypeRepository )
        throws UnknownArchetype, ArchetypeNotFoundException, ArchetypeDescriptorException,
        ArchetypeTemplateProcessingException
    {
        // ----------------------------------------------------------------------
        // Download the archetype
        // ----------------------------------------------------------------------

        File archetypeFile = archetypeArtifactManager.getArchetypeFile(
                request.getArchetypeGroupId(), request.getArchetypeArtifactId(), request.getArchetypeVersion(),
                archetypeRepository, request.getLocalRepository(), request.getRemoteArtifactRepositories() );

        createArchetype( request, archetypeFile );
    }

    public void createArchetype( ArchetypeGenerationRequest request, File archetypeFile )
        throws ArchetypeDescriptorException, ArchetypeTemplateProcessingException
    {
        Map<String, String> parameters = new HashMap<String, String>();

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

        InputStream is = null;

        try
        {
            URL[] urls = new URL[1];

            urls[0] = archetypeFile.toURL();

            archetypeJarLoader = new URLClassLoader( urls );

            is = getStream( ARCHETYPE_DESCRIPTOR, archetypeJarLoader );

            if ( is == null )
            {
                is = getStream( ARCHETYPE_OLD_DESCRIPTOR, archetypeJarLoader );
            }

            if ( is == null )
            {
                throw new ArchetypeDescriptorException( "The " + ARCHETYPE_DESCRIPTOR
                                                        + " descriptor cannot be found." );
            }

            descriptor = builder.build( new XmlStreamReader( is ) );
        }
        catch ( IOException e )
        {
            throw new ArchetypeDescriptorException( "Error reading the " + ARCHETYPE_DESCRIPTOR + " descriptor.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeDescriptorException( "Error reading the " + ARCHETYPE_DESCRIPTOR + " descriptor.", e );
        }
        finally
        {
            IOUtil.close( is );
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
                Reader fileReader = null;

                try
                {
                    fileReader = ReaderFactory.newXmlReader( parentPomFile );
                    MavenXpp3Reader reader = new MavenXpp3Reader();
                    parentModel = reader.read( fileReader );
                    if ( !"pom".equals( parentModel.getPackaging() ) )
                    {
                        throw new ArchetypeTemplateProcessingException(
                            "Unable to add module to the current project as it is not of packaging type 'pom'" );
                    }
                }
                catch ( IOException e )
                {
                    throw new ArchetypeTemplateProcessingException( "Unable to read parent POM", e );
                }
                catch ( XmlPullParserException e )
                {
                    throw new ArchetypeTemplateProcessingException( "Unable to read parent POM", e );
                }
                finally
                {
                    IOUtil.close( fileReader );
                }

                parentModel.getModules().add( artifactId );
            }
        }

        try
        {
            processTemplates( pomFile, outputDirectory, context, descriptor, packageName, parentModel );
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
            Reader fileReader = null;
            boolean added;
            StringWriter w = new StringWriter();
            try
            {
                fileReader = ReaderFactory.newXmlReader( parentPomFile );
                added = addModuleToParentPom( artifactId, fileReader, w );
            }
            catch ( IOException e )
            {
                throw new ArchetypeTemplateProcessingException( "Unable to rewrite parent POM", e );
            }
            catch ( DocumentException e )
            {
                throw new ArchetypeTemplateProcessingException( "Unable to rewrite parent POM", e );
            }
            finally
            {
                IOUtil.close( fileReader );
            }

            if ( added )
            {
                Writer out = null;
                try
                {
                    out = WriterFactory.newXmlWriter( parentPomFile );
                    IOUtil.copy( w.toString(), out );
                }
                catch ( IOException e )
                {
                    throw new ArchetypeTemplateProcessingException( "Unable to rewrite parent POM", e );
                }
                finally
                {
                    IOUtil.close( out );
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

    static boolean addModuleToParentPom( String artifactId, Reader fileReader, Writer fileWriter )
        throws DocumentException, IOException, ArchetypeTemplateProcessingException
    {
        SAXReader reader = new SAXReader();
        Document document = reader.read( fileReader );
        Element project = document.getRootElement();

        String packaging = null;
        Element packagingElement = project.element( "packaging" );
        if ( packagingElement != null )
        {
            packaging = packagingElement.getStringValue();
        }
        if ( !"pom".equals( packaging ) )
        {
            throw new ArchetypeTemplateProcessingException(
                "Unable to add module to the current project as it is not of packaging type 'pom'" );
        }

        Element modules = project.element( "modules" );
        if ( modules == null )
        {
            modules = project.addText( "  " ).addElement( "modules" );
            modules.setText( "\n  " );
            project.addText( "\n" );
        }
        boolean found = false;
        for ( Iterator<?> i = modules.elementIterator( "module" ); i.hasNext() && !found; )
        {
            Element module = (Element) i.next();
            if ( module.getText().equals( artifactId ) )
            {
                found = true;
            }
        }
        if ( !found )
        {
            Node lastTextNode = null;
            for ( Iterator<?> i = modules.nodeIterator(); i.hasNext(); )
            {
                Node node = (Node) i.next();
                if ( node.getNodeType() == Node.ELEMENT_NODE )
                {
                    lastTextNode = null;
                }
                else if ( node.getNodeType() == Node.TEXT_NODE )
                {
                    lastTextNode = node;
                }
            }

            if ( lastTextNode != null )
            {
                modules.remove( lastTextNode );
            }

            modules.addText( "\n    " );
            modules.addElement( "module" ).setText( artifactId );
            modules.addText( "\n  " );

            XMLWriter writer = new XMLWriter( fileWriter );
            writer.write( document );
        }
        return !found;
    }

    private void processTemplates( File pomFile, String outputDirectory, Context context,
                                   ArchetypeDescriptor descriptor, String packageName, Model parentModel )
        throws ArchetypeTemplateProcessingException
    {
        if ( !pomFile.exists() )
        {
            processTemplate( outputDirectory, context, ARCHETYPE_POM, new TemplateDescriptor(), false, null );
        }

        // ---------------------------------------------------------------------
        // Model generated for the new archetype, so process it now
        // ---------------------------------------------------------------------

        Model generatedModel;
        Reader pomReader = null;
        try
        {
            pomReader = ReaderFactory.newXmlReader( pomFile );

            MavenXpp3Reader reader = new MavenXpp3Reader();

            generatedModel = reader.read( pomReader );
        }
        catch ( IOException e )
        {
            throw new ArchetypeTemplateProcessingException( "Error reading POM", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new ArchetypeTemplateProcessingException( "Error reading POM", e );
        }
        finally
        {
            IOUtil.close( pomReader );
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

            Writer pomWriter = null;
            try
            {
                pomWriter = WriterFactory.newXmlWriter( pomFile );

                MavenXpp3Writer writer = new MavenXpp3Writer();
                writer.write( pomWriter, generatedModel );
            }
            catch ( IOException e )
            {
                throw new ArchetypeTemplateProcessingException( "Error rewriting POM", e );
            }
            finally
            {
                IOUtil.close( pomWriter );
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
        throws ArchetypeTemplateProcessingException
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
        throws ArchetypeTemplateProcessingException
    {
        for ( String template : descriptor.getSources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getSourceDescriptor( template ), true,
                             packageName, sourceDirectory );
        }
    }

    protected void processTestSources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                       String packageName, String testSourceDirectory )
        throws ArchetypeTemplateProcessingException
    {
        for ( String template : descriptor.getTestSources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getTestSourceDescriptor( template ), true,
                             packageName, testSourceDirectory );
        }
    }

    protected void processResources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                     String packageName )
        throws ArchetypeTemplateProcessingException
    {
        for ( String template : descriptor.getResources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getResourceDescriptor( template ), false,
                             packageName );
        }
    }

    protected void processTestResources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                         String packageName )
        throws ArchetypeTemplateProcessingException
    {
        for ( String template : descriptor.getTestResources() )
        {
            processTemplate( outputDirectory, context, template, descriptor.getTestResourceDescriptor( template ),
                             false, packageName );
        }
    }

    protected void processSiteResources( String outputDirectory, Context context, ArchetypeDescriptor descriptor,
                                         String packageName )
        throws ArchetypeTemplateProcessingException
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
        throws ArchetypeTemplateProcessingException
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

        if ( descriptor.isFiltered() )
        {
            Writer writer = null;
            try
            {
                StringWriter stringWriter = new StringWriter();

                template = ARCHETYPE_RESOURCES + "/" + template;

                velocity.getEngine().mergeTemplate( template, descriptor.getEncoding(), context, stringWriter );

                writer = new OutputStreamWriter( new FileOutputStream( f ), descriptor.getEncoding() );

                writer.write( StringUtils.unifyLineSeparators( stringWriter.toString() ) );

                writer.flush();
            }
            catch ( Exception e )
            {
                throw new ArchetypeTemplateProcessingException( "Error merging velocity templates", e );
            }
            finally
            {
                IOUtil.close( writer );
            }
        }
        else
        {
            InputStream is = getStream( ARCHETYPE_RESOURCES + "/" + template, null );

            OutputStream fos = null;

            try
            {
                fos = new FileOutputStream( f );

                IOUtil.copy( is, fos );
            }
            catch ( Exception e )
            {
                throw new ArchetypeTemplateProcessingException( "Error copying file", e );
            }
            finally
            {
                IOUtil.close( fos );

                IOUtil.close( is );
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
