package org.apache.maven.archetype.ui;

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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.InputHandler;
import org.codehaus.plexus.components.interactivity.OutputHandler;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * @author raphaelpieroni
 */
@Component( role = Prompter.class, hint = "archetype" )
public class ArchetypePrompter
    implements Prompter
{

    @Requirement
    private OutputHandler outputHandler;

    @Requirement
    private InputHandler inputHandler;

    public String prompt( String message )
        throws PrompterException
    {
        writePrompt( message );

        return readLine();
    }

    public String prompt( String message, String defaultReply )
        throws PrompterException
    {
        writePrompt( formatMessage( message, null, defaultReply ) );

        String line = readLine();

        if ( StringUtils.isEmpty( line ) )
        {
            line = defaultReply;
        }

        return line;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" })
    public String prompt( String message, List possibleValues, String defaultReply )
        throws PrompterException
    {
        String formattedMessage = formatMessage( message, possibleValues, defaultReply );

        String line;

        do
        {
            writePrompt( formattedMessage );

            line = readLine();

            if ( StringUtils.isEmpty( line ) )
            {
                line = defaultReply;
            }

            if ( line != null && !possibleValues.contains( line ) )
            {
                try
                {
                    outputHandler.writeLine( "Invalid selection." );
                }
                catch ( IOException e )
                {
                    throw new PrompterException( "Failed to present feedback", e );
                }
            }
        }
        while ( line == null || !possibleValues.contains( line ) );

        return line;
    }

    @SuppressWarnings( "rawtypes" )
    public String prompt( String message, List possibleValues )
        throws PrompterException
    {
        return prompt( message, possibleValues, null );
    }

    public String promptForPassword( String message )
        throws PrompterException
    {
        writePrompt( message );

        try
        {
            return inputHandler.readPassword();
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    private String formatMessage( String message, List<String> possibleValues, String defaultReply )
    {
        StringBuffer formatted = new StringBuffer( message.length() * 2 );

        formatted.append( message );
        /*
         * if ( possibleValues != null && !possibleValues.isEmpty() ) { formatted.append( " (" ); for ( Iterator it =
         * possibleValues.iterator(); it.hasNext(); ) { String possibleValue = (String) it.next(); formatted.append(
         * possibleValue ); if ( it.hasNext() ) { formatted.append( '/' ); } } formatted.append( ')' ); }
         */

        if ( defaultReply != null )
        {
            formatted.append( defaultReply );
        }

        return formatted.toString();
    }

    private void writePrompt( String message )
        throws PrompterException
    {
        showMessage( message + ": " );
    }

    private String readLine()
        throws PrompterException
    {
        try
        {
            return inputHandler.readLine();
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    public void showMessage( String message )
        throws PrompterException
    {
        try
        {
            outputHandler.write( message );
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to show message", e );
        }
    }

}
