package org.apache.maven.archetype.ui.creation;

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

import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.ui.ArchetypeConfiguration;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.Iterator;

@Component( role = ArchetypeCreationQueryer.class )
public class DefaultArchetypeCreationQueryer
    extends AbstractLogEnabled
    implements ArchetypeCreationQueryer
{
    @Requirement
    private Prompter prompter;

    public String getArchetypeArtifactId( String defaultValue )
        throws PrompterException
    {
        return getValue( Constants.ARCHETYPE_ARTIFACT_ID, defaultValue );
    }

    public String getArchetypeGroupId( String defaultValue )
        throws PrompterException
    {
        return getValue( Constants.ARCHETYPE_GROUP_ID, defaultValue );
    }

    public String getArchetypeVersion( String defaultValue )
        throws PrompterException
    {
        return getValue( Constants.ARCHETYPE_VERSION, defaultValue );
    }

    public String getArtifactId( String defaultValue )
        throws PrompterException
    {
        return getValue( Constants.ARTIFACT_ID, defaultValue );
    }

    public boolean askAddAnotherProperty()
        throws PrompterException
    {
        String query = "Add a new custom property";

        String answer = prompter.prompt( query, "Y" );

        return "Y".equalsIgnoreCase( answer );
    }

    public String askNewPropertyKey()
        throws PrompterException
    {
        String query = "Define property key";

        String answer = prompter.prompt( query );

        return answer;
    }

    public String askReplacementValue( String propertyKey, String defaultValue )
        throws PrompterException
    {
        return getValue( propertyKey, defaultValue );
    }

    public boolean confirmConfiguration( ArchetypeConfiguration archetypeConfiguration )
        throws PrompterException
    {
        StringBuilder query =
            new StringBuilder( "Confirm archetype configuration:\n" + Constants.ARCHETYPE_GROUP_ID + "="
                + archetypeConfiguration.getGroupId() + "\n" + Constants.ARCHETYPE_ARTIFACT_ID + "="
                + archetypeConfiguration.getArtifactId() + "\n" + Constants.ARCHETYPE_VERSION + "="
                + archetypeConfiguration.getVersion() + "\n" );

        for ( Iterator<?> propertiesIter = archetypeConfiguration.getProperties().keySet().iterator();
            propertiesIter.hasNext(); )
        {
            String property = (String) propertiesIter.next();
            query.append( property + "=" + archetypeConfiguration.getProperty( property ) + "\n" );
        }

        String answer = prompter.prompt( query.toString(), "Y" );

        return "Y".equalsIgnoreCase( answer );
    }

    public String getGroupId( String defaultValue )
        throws PrompterException
    {
        return getValue( Constants.GROUP_ID, defaultValue );
    }

    public String getPackage( String defaultValue )
        throws PrompterException
    {
        return getValue( Constants.PACKAGE, defaultValue );
    }

    public String getVersion( String defaultValue )
        throws PrompterException
    {
        return getValue( Constants.VERSION, defaultValue );
    }

    private String getValue( String requiredProperty, String defaultValue )
        throws PrompterException
    {
        String query = "Define value for " + requiredProperty + ": ";
        String answer;

        if ( ( defaultValue != null ) && !defaultValue.equals( "null" ) )
        {
            answer = prompter.prompt( query, defaultValue );
        }
        else
        {
            answer = prompter.prompt( query );
        }
        return answer;
    }
}
