package org.apache.maven.archetype.common;

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

import org.apache.maven.archetype.common.util.Format;
import org.apache.maven.archetype.common.util.PomUtils;
import org.apache.maven.archetype.exception.InvalidPackaging;
import org.apache.maven.archetype.old.ArchetypeTemplateProcessingException;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component( role = PomManager.class )
public class DefaultPomManager
    extends AbstractLogEnabled
    implements PomManager
{
    @Override
    public void addModule( File pom, String artifactId ) throws IOException, ParserConfigurationException,
            TransformerException, SAXException, InvalidPackaging, ArchetypeTemplateProcessingException
    {
        StringWriter out = new StringWriter();
        boolean found = PomUtils.addNewModule( artifactId, ReaderFactory.newXmlReader( pom ), out );
        if ( found )
        {
            FileUtils.fileWrite( pom.getAbsolutePath(), out.toString() );
        }
    }

    @Override
    public void addParent( File pom, File parentPom )
        throws IOException, XmlPullParserException
    {
        Model generatedModel = readPom( pom );
        if ( null != generatedModel.getParent() )
        {
            getLogger().info( "Parent element not overwritten in " + pom );
            return;
        }

        Model parentModel = readPom( parentPom );

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

        writePom( generatedModel, pom, pom );
    }

    @Override
    public void mergePoms( File pom, File temporaryPom )
        throws IOException, XmlPullParserException
    {
        Model model = readPom( pom );
        Model generatedModel = readPom( temporaryPom );

        model.getProperties().putAll( generatedModel.getProperties() );

        mergeModelBase( model, generatedModel );
        mergeModelBuild( model, generatedModel );
        mergeProfiles( model, generatedModel );
        mergeReportPlugins( model, generatedModel );

//
//        // Potential merging
//
//        model.getModelEncoding ();
//        model.getModelVersion ();
//
//        model.getGroupId ();
//        model.getArtifactId ();
//        model.getVersion ();
//        model.getParent ();
//
//        model.getId ();
//        model.getName ();
//        model.getInceptionYear ();
//        model.getDescription ();
//        model.getUrl ();
//        model.getLicenses ();
//        model.getProperties ();
//
//        model.getOrganization ();
//        model.getMailingLists ();
//        model.getContributors ();
//        model.getDevelopers ();
//
//        model.getScm ();
//        model.getCiManagement ();
//        model.getDistributionManagement ();
//        model.getIssueManagement ();
//
//        model.getPackaging ();
////        model.getDependencies (); // done
//        model.getDependencyManagement ();
//        model.getPrerequisites ().getMaven ();
//        model.getPrerequisites ().getModelEncoding ();
//
//        model.getProfiles ();
//        model.getModules ();
//        model.getRepositories ();
//        model.getPluginRepositories ();
//
//        model.getBuild ().getDefaultGoal ();
//        model.getBuild ().getFinalName ();
//        model.getBuild ().getModelEncoding ();
//        model.getBuild ().getFilters ();
//        model.getBuild ().getDirectory ();
//        model.getBuild ().getOutputDirectory ();
//        model.getBuild ().getSourceDirectory ();
//        model.getBuild ().getResources ();
//        model.getBuild ().getScriptSourceDirectory ();
//        model.getBuild ().getTestOutputDirectory ();
//        model.getBuild ().getTestResources ();
//        model.getBuild ().getTestSourceDirectory ();
//        model.getBuild ().getPluginManagement ();
//        model.getBuild ().getExtensions ();
////        model.getBuild ().getPluginsAsMap (); // done
//
//        model.getReporting ().getModelEncoding ();
//        model.getReporting ().getOutputDirectory ();
////        model.getReporting ().getReportPluginsAsMap (); // done
//

        writePom( model, pom, pom );
    }

    @Override
    public Model readPom( final File pomFile )
        throws IOException, XmlPullParserException
    {
        try ( Reader pomReader = ReaderFactory.newXmlReader( pomFile ) )
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();

            return reader.read( pomReader );
        }
    }


    @Override
    public Model readPom( InputStream pomStream )
        throws IOException, XmlPullParserException
    {
        try ( Reader pomReader = ReaderFactory.newXmlReader( pomStream ) )
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();

            return reader.read( pomReader );
        }
    }

    @Override
    public void writePom( final Model model, final File pomFile, final File initialPomFile )
        throws IOException
    {
        String fileEncoding =
            StringUtils.isEmpty( model.getModelEncoding() ) ? "UTF-8" : model.getModelEncoding();

        org.jdom.Document doc;
        try ( InputStream inputStream = new FileInputStream( initialPomFile ) )
        {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build( inputStream );
        }
        catch ( JDOMException exc )
        {
            IOException ioe = new IOException( "Cannot parse the POM by JDOM while reading " + initialPomFile + ": "
                                               + exc.getMessage() );
            ioe.initCause( exc );
            throw ioe;
        }


        if ( !pomFile.exists() && !pomFile.createNewFile() )
        {
            getLogger().warn( "Could not create new file \"" + pomFile.getPath() + "\" or the file already exists." );
        }
        
        try ( Writer outputStreamWriter = new OutputStreamWriter( new FileOutputStream( pomFile ), fileEncoding ) ) 
        {
            // The cdata parts of the pom are not preserved from initial to target
            MavenJDOMWriter writer = new MavenJDOMWriter();

            final String ls = System.lineSeparator();
            Format form = Format.getRawFormat().setEncoding( fileEncoding ).setLineSeparator( ls );
            writer.write( model, doc, outputStreamWriter, form );
        }
        catch ( FileNotFoundException e )
        {
            getLogger().debug( "Creating pom file " + pomFile );

            try ( Writer pomWriter = new OutputStreamWriter( new FileOutputStream( pomFile ), fileEncoding ) )
            {
                MavenXpp3Writer writer = new MavenXpp3Writer();
                writer.write( pomWriter, model );
            }
        }
    }

    private Map<String, Dependency> createDependencyMap( List<Dependency> dependencies )
    {
        Map<String, Dependency> dependencyMap = new HashMap<>();
        for ( Dependency dependency : dependencies )
        {
            dependencyMap.put( dependency.getManagementKey(), dependency );
        }

        return dependencyMap;
    }

    private void mergeModelBuild( Model model, Model generatedModel )
    {
        if ( generatedModel.getBuild() != null )
        {
            if ( model.getBuild() == null )
            {
                model.setBuild( new Build() );
            }

            mergeBuildPlugins( model.getBuild(), generatedModel.getBuild() );
        }
    }

    private void mergeProfiles( Model model, Model generatedModel )
    {
        List<Profile> generatedProfiles = generatedModel.getProfiles();
        if ( generatedProfiles != null && generatedProfiles.size() > 0 )
        {
            List<Profile> modelProfiles = model.getProfiles();
            Map<String, Profile> modelProfileIdMap = new HashMap<>();
            if ( modelProfiles == null )
            {
                modelProfiles = new ArrayList<>();
                model.setProfiles( modelProfiles );
            }
            else if ( modelProfiles.size() > 0 )
            {
                // add profile ids from the model for later lookups to the modelProfileIds set
                for ( Profile modelProfile : modelProfiles )
                {
                    modelProfileIdMap.put( modelProfile.getId(), modelProfile );
                }
            }

            for ( Profile generatedProfile : generatedProfiles )
            {
                String generatedProfileId = generatedProfile.getId();
                if ( !modelProfileIdMap.containsKey( generatedProfileId ) )
                {
                    model.addProfile( generatedProfile );
                }
                else
                {
                    getLogger().warn( "Try to merge profiles with id " + generatedProfileId );
                    mergeModelBase( modelProfileIdMap.get( generatedProfileId ), generatedProfile );
                    mergeProfileBuild( modelProfileIdMap.get( generatedProfileId ), generatedProfile );
                }
            }
        }
    }

    private void mergeProfileBuild( Profile modelProfile, Profile generatedProfile )
    {
        if ( generatedProfile.getBuild() != null )
        {
            if ( modelProfile.getBuild() == null )
            {
                modelProfile.setBuild( new Build() );
            }
            mergeBuildPlugins( modelProfile.getBuild(), generatedProfile.getBuild() );
            // TODO: merge more than just plugins in the profile...
        }
    }

    private void mergeModelBase( ModelBase model, ModelBase generatedModel )
    {
        // ModelBase can be a Model or a Profile...
        Map<String, Dependency> dependenciesByIds = createDependencyMap( model.getDependencies() );

        Map<String, Dependency> generatedDependenciesByIds = createDependencyMap( generatedModel.getDependencies() );

        for ( String generatedDependencyId : generatedDependenciesByIds.keySet() )
        {
            if ( !dependenciesByIds.containsKey( generatedDependencyId ) )
            {
                model.addDependency( generatedDependenciesByIds.get( generatedDependencyId ) );
            }
            else
            {
                getLogger().warn( "Can not override property: " + generatedDependencyId );
            }

        // TODO: maybe warn, if a property key gets overridden?
        model.getProperties().putAll( generatedModel.getProperties() );

        // TODO: maybe merge more than just dependencies and properties...
        }
    }

    private void mergeReportPlugins( Model model, Model generatedModel )
    {
        if ( generatedModel.getReporting() != null )
        {
            if ( model.getReporting() == null )
            {
                model.setReporting( new Reporting() );
            }

            Map<String, ReportPlugin> reportPluginsByIds = model.getReporting().getReportPluginsAsMap();

            Map<String, ReportPlugin> generatedReportPluginsByIds =
                generatedModel.getReporting().getReportPluginsAsMap();

            for ( String generatedReportPluginsId : generatedReportPluginsByIds.keySet() )
            {
                if ( !reportPluginsByIds.containsKey( generatedReportPluginsId ) )
                {
                    model.getReporting().addPlugin( generatedReportPluginsByIds.get( generatedReportPluginsId ) );
                }
                else
                {
                    getLogger().warn( "Can not override report: " + generatedReportPluginsId );
                }
            }
        }
    }

    private void mergeBuildPlugins( BuildBase modelBuild, BuildBase generatedModelBuild )
    {
        Map<String, Plugin> pluginsByIds = modelBuild.getPluginsAsMap();

        List<Plugin> generatedPlugins = generatedModelBuild.getPlugins();

        for ( Plugin generatedPlugin : generatedPlugins )
        {
            String generatedPluginsId = generatedPlugin.getKey();

            if ( !pluginsByIds.containsKey( generatedPluginsId ) )
            {
                modelBuild.addPlugin( generatedPlugin );
            }
            else
            {
                getLogger().info( "Try to merge plugin configuration of plugins with id: " + generatedPluginsId );
                Plugin modelPlugin = pluginsByIds.get( generatedPluginsId );

                modelPlugin.setConfiguration( Xpp3DomUtils.mergeXpp3Dom( (Xpp3Dom) generatedPlugin.getConfiguration(),
                                                                         (Xpp3Dom) modelPlugin.getConfiguration() ) );
            }
        }
    }
}
