package org.apache.maven.archetype.ui.generation;

import java.util.ArrayList;
import java.util.List;

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

import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.metadata.ArchetypeDescriptor;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.archetype.ui.ArchetypeConfiguration;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.PlexusTestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Tests the ability to use variables in default fields in batch mode.
 */
public class DefaultArchetypeGenerationConfigurator2Test
    extends PlexusTestCase
{
    private DefaultArchetypeGenerationConfigurator configurator;
    private ArchetypeGenerationQueryer queryer;
    private ArchetypeDescriptor descriptor;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        configurator = (DefaultArchetypeGenerationConfigurator) lookup( ArchetypeGenerationConfigurator.ROLE );

        ProjectBuildingRequest buildingRequest = null;
        
        descriptor = new ArchetypeDescriptor();
        RequiredProperty groupId = new RequiredProperty();
        groupId.setKey( "groupId" );
        groupId.setDefaultValue( "com.example.${groupName}" );
        RequiredProperty artifactId = new RequiredProperty();
        artifactId.setKey( "artifactId" );
        artifactId.setDefaultValue( "${serviceName}" );
        RequiredProperty thePackage = new RequiredProperty();
        thePackage.setKey( "package" );
        thePackage.setDefaultValue( "com.example.${groupName}" );
        RequiredProperty groupName = new RequiredProperty();
        groupName.setKey( "groupName" );
        groupName.setDefaultValue( null );
        RequiredProperty serviceName = new RequiredProperty();
        serviceName.setKey( "serviceName" );
        serviceName.setDefaultValue( null );
        descriptor.addRequiredProperty( groupId );
        descriptor.addRequiredProperty( artifactId );
        descriptor.addRequiredProperty( thePackage );
        descriptor.addRequiredProperty( groupName );
        descriptor.addRequiredProperty( serviceName );
        
        ArchetypeArtifactManager manager = EasyMock.createMock ( ArchetypeArtifactManager.class );
        
        List<ArtifactRepository> x = new ArrayList<>();
        EasyMock.expect( manager.exists( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null, null, x,
                                         buildingRequest ) ).andReturn( true );
        EasyMock.expect( manager.isFileSetArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion",
                                                     null, null, x, buildingRequest ) ).andReturn( true );
        EasyMock.expect( manager.isOldArchetype( "archetypeGroupId", "archetypeArtifactId", "archetypeVersion", null,
                                                 null, x, buildingRequest ) ).andReturn( false );
        EasyMock.expect( manager.getFileSetArchetypeDescriptor( "archetypeGroupId", "archetypeArtifactId",
                                                                "archetypeVersion", null, null, x,
                                                                buildingRequest ) ).andReturn( descriptor );
       
        EasyMock.replay( manager );
        configurator.setArchetypeArtifactManager( manager );
   
        queryer = EasyMock.mock( ArchetypeGenerationQueryer.class );
        configurator.setArchetypeGenerationQueryer( queryer );
    }

    public void testJIRA_509_FileSetArchetypeDefaultsWithVariables() throws Exception
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();
        properties.setProperty( "groupName", "myGroupName" );
        properties.setProperty( "serviceName", "myServiceName" );
        
        configurator.configureArchetype( request, Boolean.FALSE, properties );
        
        assertEquals( "com.example.myGroupName", request.getGroupId() );
        assertEquals( "myServiceName", request.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", request.getVersion() );
        assertEquals( "com.example.myGroupName", request.getPackage() );
    }

    public void testInteractive() throws Exception
    {
        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();

        EasyMock.expect( queryer.getPropertyValue( EasyMock.eq("groupName"), EasyMock.anyString(),
                        EasyMock.<Pattern> isNull() ) ).andReturn( "myGroupName" );

        EasyMock.expect( queryer.getPropertyValue( EasyMock.eq("serviceName"), EasyMock.anyString(),
                        EasyMock.<Pattern> isNull() ) ).andReturn( "myServiceName" );

        EasyMock.expect( queryer.getPropertyValue( EasyMock.anyString(), EasyMock.anyString(),
                        EasyMock.<Pattern> anyObject())).andAnswer( new IAnswer<String>() {

                            @Override
                            public String answer() throws Throwable {
                                return (String) EasyMock.getCurrentArguments()[1];
                            }}
                        ).anyTimes();

        EasyMock.expect( queryer.confirmConfiguration( EasyMock.<ArchetypeConfiguration> anyObject() ) )
                        .andReturn( Boolean.TRUE );

        EasyMock.replay( queryer );
        configurator.configureArchetype( request, Boolean.TRUE, properties );

        assertEquals( "com.example.myGroupName", request.getGroupId() );
        assertEquals( "myServiceName", request.getArtifactId() );
        assertEquals( "1.0-SNAPSHOT", request.getVersion() );
        assertEquals( "com.example.myGroupName", request.getPackage() );
    }

    public void testArchetype406ComplexCustomPropertyValue() throws Exception
    {
        RequiredProperty custom = new RequiredProperty();
        custom.setKey( "serviceUpper" );
        custom.setDefaultValue( "${serviceName.toUpperCase()}" );
        descriptor.addRequiredProperty( custom );

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();

        EasyMock.expect( queryer.getPropertyValue( EasyMock.eq("groupName"), EasyMock.anyString(),
                        EasyMock.<Pattern> isNull() ) ).andReturn( "myGroupName" );

        EasyMock.expect( queryer.getPropertyValue( EasyMock.eq("serviceName"), EasyMock.anyString(),
                        EasyMock.<Pattern> isNull() ) ).andReturn( "myServiceName" );

        EasyMock.expect( queryer.getPropertyValue( EasyMock.anyString(), EasyMock.anyString(),
                        EasyMock.<Pattern> anyObject())).andAnswer( new IAnswer<String>() {

                            @Override
                            public String answer() throws Throwable {
                                return (String) EasyMock.getCurrentArguments()[1];
                            }}
                        ).anyTimes();

        EasyMock.expect( queryer.confirmConfiguration( EasyMock.<ArchetypeConfiguration> anyObject() ) )
                        .andReturn( Boolean.TRUE );

        EasyMock.replay( queryer );
        configurator.configureArchetype( request, Boolean.TRUE, properties );

        assertEquals( "MYSERVICENAME", request.getProperties().get( "serviceUpper" ) );
    }

    public void testArchetype618() throws Exception
    {
        RequiredProperty custom = getRequiredProperty( "serviceName" );
        custom.setKey( "camelArtifact" );
        custom.setDefaultValue( "${artifactId.class.forName('org.codehaus.plexus.util.StringUtils').capitaliseAllWords($artifactId.replaceAll('[^A-Za-z_\\$0-9]', ' ').replaceFirst('^(\\d)', '_$1').replaceAll('\\d', '$0 ').replaceAll('[A-Z](?=[^A-Z])', ' $0').toLowerCase()).replaceAll('\\s', '')}" );
        descriptor.addRequiredProperty( custom );

        getRequiredProperty( "artifactId" ).setDefaultValue( null );

        ArchetypeGenerationRequest request = new ArchetypeGenerationRequest();
        request.setArchetypeGroupId( "archetypeGroupId" );
        request.setArchetypeArtifactId( "archetypeArtifactId" );
        request.setArchetypeVersion( "archetypeVersion" );
        Properties properties = new Properties();

        EasyMock.expect( queryer.getPropertyValue( EasyMock.eq("groupName"), EasyMock.anyString(),
                        EasyMock.<Pattern> isNull() ) ).andReturn( "myGroupName" );

        EasyMock.expect( queryer.getPropertyValue( EasyMock.eq("artifactId"), EasyMock.anyString(),
                        EasyMock.<Pattern> isNull() ) ).andReturn( "my-service-name" );

        EasyMock.expect( queryer.getPropertyValue( EasyMock.anyString(), EasyMock.anyString(),
                        EasyMock.<Pattern> anyObject())).andAnswer( new IAnswer<String>() {

                            @Override
                            public String answer() throws Throwable {
                                return (String) EasyMock.getCurrentArguments()[1];
                            }}
                        ).anyTimes();

        EasyMock.expect( queryer.confirmConfiguration( EasyMock.<ArchetypeConfiguration> anyObject() ) )
                        .andReturn( Boolean.TRUE );

        EasyMock.replay( queryer );
        configurator.configureArchetype( request, Boolean.TRUE, properties );

        assertEquals( "MyServiceName", request.getProperties().get( "camelArtifact" ) );
    }

    private RequiredProperty getRequiredProperty( String propertyName )
    {
        if ( propertyName != null )
        {
            for ( RequiredProperty candidate : descriptor.getRequiredProperties() )
            {
                if ( propertyName.equals( candidate.getKey() ) )
                {
                    return candidate;
                }
            }
        }
        return null;
    }
}