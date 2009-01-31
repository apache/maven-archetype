/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.maven.archetype.ui;

import java.io.IOException;
import java.util.List;
import org.codehaus.plexus.components.interactivity.InputHandler;
import org.codehaus.plexus.components.interactivity.OutputHandler;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author raphaelpieroni
 * @plexus.component role-hint="archetype"
 */
public class ArchetypePrompter implements Prompter {

    /**
     * @plexus.requirement
     */
    private OutputHandler outputHandler;

    /**
     * @plexus.requirement
     */
    private InputHandler inputHandler;

    public String prompt( String message )
        throws PrompterException
    {
        try
        {
            writePrompt( message );
        }

        catch ( IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

        try
        {
            return inputHandler.readLine();
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    public String prompt( String message, String defaultReply )
        throws PrompterException
    {
        try
        {
            writePrompt( formatMessage( message, null, defaultReply ) );
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

        try
        {
            String line = inputHandler.readLine();

            if ( StringUtils.isEmpty( line ) )
            {
                line = defaultReply;
            }

            return line;
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    public String prompt( String message, List possibleValues, String defaultReply )
        throws PrompterException
    {
        String formattedMessage = formatMessage( message, possibleValues, defaultReply );

        String line;

        do
        {
            try
            {
                writePrompt( formattedMessage );
            }
            catch ( IOException e )
            {
                throw new PrompterException( "Failed to present prompt", e );
            }

            try
            {
                line = inputHandler.readLine();
            }
            catch ( IOException e )
            {
                throw new PrompterException( "Failed to read user response", e );
            }

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

    public String prompt( String message, List possibleValues )
        throws PrompterException
    {
        return prompt( message, possibleValues, null );
    }

    public String promptForPassword( String message )
        throws PrompterException
    {
        try
        {
            writePrompt( message );
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

        try
        {
            return inputHandler.readPassword();
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to read user response", e );
        }
    }

    private String formatMessage( String message, List possibleValues, String defaultReply )
    {
        StringBuffer formatted = new StringBuffer( message.length() * 2 );

        formatted.append( message );
        /*if ( possibleValues != null && !possibleValues.isEmpty() )
        {
        formatted.append( " (" );

        for ( Iterator it = possibleValues.iterator(); it.hasNext(); )
        {
        String possibleValue = (String) it.next();

        formatted.append( possibleValue );

        if ( it.hasNext() )
        {
        formatted.append( '/' );
        }
        }

        formatted.append( ')' );
        }*/

        if ( defaultReply != null )
        {
            formatted.append( defaultReply );
        }

        return formatted.toString();
    }

    private void writePrompt( String message )
        throws IOException
    {
        outputHandler.write( message + ": " );
    }

    public void showMessage( String message )
        throws PrompterException
    {
        try
        {
            writePrompt( message );
        }
        catch ( IOException e )
        {
            throw new PrompterException( "Failed to present prompt", e );
        }

    }


}
