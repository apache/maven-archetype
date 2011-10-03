package org.apache.maven.archetype.generator;

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

import org.apache.maven.archetype.old.OldArchetype;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.common.ArchetypeRegistryManager;
import org.apache.maven.archetype.exception.ArchetypeException;
import org.apache.maven.archetype.exception.ArchetypeGenerationFailure;
import org.apache.maven.archetype.exception.ArchetypeNotDefined;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component( role = ArchetypeGenerator.class )
public class DefaultArchetypeGenerator
    extends AbstractLogEnabled
    implements ArchetypeGenerator
{
    @Requirement
    private ArchetypeRegistryManager archetypeRegistryManager;

    @Requirement
    private ArchetypeArtifactManager archetypeArtifactManager;

    @Requirement
    private FilesetArchetypeGenerator filesetGenerator;

    @Requirement
    private OldArchetype oldArchetype;

    private File getArchetypeFile( ArchetypeGenerationRequest request, ArtifactRepository localRepository )
        throws IOException, ArchetypeException, XmlPullParserException, DocumentException
    {
        if ( !isArchetypeDefined( request ) )
        {
            throw new ArchetypeNotDefined( "The archetype is not defined" );
        }

        List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();

        ArtifactRepository remoteRepo = null;
        if ( request != null && request.getArchetypeRepository() != null )
        {
            remoteRepo =
                archetypeRegistryManager.createRepository( request.getArchetypeRepository(),
                                                           request.getArchetypeArtifactId() + "-repo" );

            repos.add( remoteRepo );
        }

        if ( !archetypeArtifactManager.exists( request.getArchetypeGroupId(), request.getArchetypeArtifactId(),
                                               request.getArchetypeVersion(), remoteRepo, localRepository, repos ) )
        {
            throw new UnknownArchetype( "The desired archetype does not exist (" + request.getArchetypeGroupId() + ":"
                + request.getArchetypeArtifactId() + ":" + request.getArchetypeVersion() + ")" );
        }

        File archetypeFile =
            archetypeArtifactManager.getArchetypeFile( request.getArchetypeGroupId(), request.getArchetypeArtifactId(),
                                                       request.getArchetypeVersion(), remoteRepo, localRepository,
                                                       repos );
        return archetypeFile;
    }

    private void generateArchetype( ArchetypeGenerationRequest request, File archetypeFile )
        throws IOException, ArchetypeException, XmlPullParserException, DocumentException
    {
        if ( archetypeArtifactManager.isFileSetArchetype( archetypeFile ) )
        {
            processFileSetArchetype( request, archetypeFile );
        }
        else if ( archetypeArtifactManager.isOldArchetype( archetypeFile ) )
        {
            processOldArchetype( request, archetypeFile );
        }
        else
        {
            throw new ArchetypeGenerationFailure( "The defined artifact is not an archetype" );
        }
    }

    /** Common */
    public String getPackageAsDirectory( String packageName )
    {
        return StringUtils.replace( packageName, ".", "/" );
    }

    private boolean isArchetypeDefined( ArchetypeGenerationRequest request )
    {
        return StringUtils.isNotEmpty( request.getArchetypeGroupId() )
            && StringUtils.isNotEmpty( request.getArchetypeArtifactId() )
            && StringUtils.isNotEmpty( request.getArchetypeVersion() );
    }

    /** FileSetArchetype */
    private void processFileSetArchetype( ArchetypeGenerationRequest request, File archetypeFile )
        throws ArchetypeException
    {
        filesetGenerator.generateArchetype( request, archetypeFile );
    }

    private void processOldArchetype( ArchetypeGenerationRequest request, File archetypeFile )
        throws UnknownArchetype, ArchetypeGenerationFailure
    {
        oldArchetype.createArchetype( request, archetypeFile );
    }

    public void generateArchetype( ArchetypeGenerationRequest request, File archetypeFile,
                                   ArchetypeGenerationResult result )
    {
        try
        {
            generateArchetype( request, archetypeFile );
        }
        catch ( IOException ex )
        {
            result.setCause( ex );
        }
        catch ( ArchetypeException ex )
        {
            result.setCause( ex );
        }
        catch ( XmlPullParserException ex )
        {
            result.setCause( ex );
        }
        catch ( DocumentException ex )
        {
            result.setCause( ex );
        }
    }

    public void generateArchetype( ArchetypeGenerationRequest request, ArchetypeGenerationResult result )
    {
        try
        {
            File archetypeFile = getArchetypeFile( request, request.getLocalRepository() );

            generateArchetype( request, archetypeFile, result );
        }
        catch ( IOException ex )
        {
            result.setCause( ex );
        }
        catch ( ArchetypeException ex )
        {
            result.setCause( ex );
        }
        catch ( XmlPullParserException ex )
        {
            result.setCause( ex );
        }
        catch ( DocumentException ex )
        {
            result.setCause( ex );
        }
    }
}
