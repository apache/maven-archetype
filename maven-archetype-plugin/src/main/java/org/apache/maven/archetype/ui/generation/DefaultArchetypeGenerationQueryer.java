package org.apache.maven.archetype.ui.generation;

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

import org.apache.maven.archetype.ui.ArchetypeConfiguration;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.util.regex.Pattern;

@Component( role = ArchetypeGenerationQueryer.class, hint = "default" )
public class DefaultArchetypeGenerationQueryer
    extends AbstractLogEnabled
    implements ArchetypeGenerationQueryer
{
    @Requirement
    private Prompter prompter;

    @Override
    public boolean confirmConfiguration( ArchetypeConfiguration archetypeConfiguration )
        throws PrompterException
    {
        StringBuilder query = new StringBuilder( "Confirm properties configuration:\n" );

        for ( String property : archetypeConfiguration.getRequiredProperties() )
        {
            query.append( property + ": " + archetypeConfiguration.getProperty( property ) + "\n" );
        }

        String answer = prompter.prompt( query.toString(), "Y" );

        return "Y".equalsIgnoreCase( answer );
    }

    @Override
    public String getPropertyValue( String requiredProperty, String defaultValue, Pattern validationRegex )
        throws PrompterException
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append( "Define value for property '" );
        queryBuilder.append( requiredProperty );
        queryBuilder.append( '\'' );

        if ( validationRegex != null )
        {
            queryBuilder.append( " (should match expression '" );
            queryBuilder.append( validationRegex );
            queryBuilder.append( "')" );
        }

        String query = queryBuilder.toString();
        String answer;
        boolean validAnswer = false;

        do
        {
            if ( ( defaultValue != null ) && !defaultValue.equals( "null" ) )
            {
                answer = prompter.prompt( query, defaultValue );
            }
            else
            {
                answer = prompter.prompt( query );
            }

            if ( validationRegex == null || validationRegex.matcher( answer ).matches() )
            {
                validAnswer = true;
            }
            else
            {
                query = "Value does not match the expression, please try again";
            }

        }
        while ( !validAnswer );

        return answer;
    }

    public void setPrompter( Prompter prompter )
    {
        this.prompter = prompter;
    }
}
