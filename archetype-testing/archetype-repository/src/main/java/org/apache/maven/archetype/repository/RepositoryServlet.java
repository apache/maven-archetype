/*
 *  Copyright 2007 rafale.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.maven.archetype.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.IO;
import org.mortbay.util.StringUtil;

/**
 *
 * @author rafale
 */
public class RepositoryServlet
    extends HttpServlet
{
    private File getFile( HttpServletRequest request )
    {
        log( "Requested file = " + request.getRequestURI() );

        String filePath =
            System.getProperty( "org.apache.maven.archetype.repository.directory" ).trim() + "/"
                + request.getRequestURI();
        filePath = StringUtil.replace( filePath, "\\", File.separator );
        filePath = StringUtil.replace( filePath, File.separator, "/" );
        filePath = filePath.replaceAll( "/repo/", "/" );
        filePath = filePath.replaceAll( "//", "/" );
        filePath = filePath.replaceAll( "//", "/" );
        filePath = filePath.replaceAll( "//", "/" );
        filePath = StringUtil.replace( filePath, "/", File.separator );
        log( "Complete file path = " + filePath );
        
        return new File( filePath );
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException
    {
        log( "Getting file" );
        InputStream is = null;
        try
        {
            is = new FileInputStream( getFile( request ) );

            IO.copy( is, response.getOutputStream() );
            response.setStatus( HttpServletResponse.SC_OK );
            log( "File sent" );
        }
        catch ( FileNotFoundException fileNotFoundException )
        {
            response.setStatus( HttpServletResponse.SC_NOT_FOUND );
            log( "Requested file not found " );
        }
        catch ( IOException iOException )
        {
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            log( "Cannot send file", iOException );
        }
        finally
        {
            IO.close( is );
        }
    }
}