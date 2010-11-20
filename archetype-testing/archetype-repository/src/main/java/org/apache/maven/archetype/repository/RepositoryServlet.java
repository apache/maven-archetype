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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
    /*
     * (non-Javadoc)
     * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service( ServletRequest req, ServletResponse res )
        throws ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        log( "Requested file = " + request.getRequestURI() );
        String filePath =
            System.getProperty( "org.apache.maven.archetype.reporitory.directory" ).trim() + "/"
                + request.getRequestURI();
        filePath = StringUtil.replace( filePath, "\\", File.separator );
        filePath = StringUtil.replace( filePath, File.separator, "/" );
        filePath = filePath.replaceAll( "/repo/", "/" );
        filePath = filePath.replaceAll( "//", "/" );
        filePath = filePath.replaceAll( "//", "/" );
        filePath = filePath.replaceAll( "//", "/" );
        filePath = StringUtil.replace( filePath, "/", File.separator );
        log( "Complete file path = " + filePath );

        String method = request.getMethod();

        if ( "GET".equalsIgnoreCase( method ) )
        {
            log( "Getting file" );
            try
            {
                File requestedFile = new File( filePath );

                InputStream is = new FileInputStream( requestedFile );

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
        }
        else if ( "PUT".equalsIgnoreCase( method ) )
        {
            log( "Putting file" );
            File uploadedFile = new File( filePath );
            if ( uploadedFile.exists() )
            {
                uploadedFile.delete();
                log( "Removed old file" );
            }
            else if ( !uploadedFile.getParentFile().exists() )
            {
                uploadedFile.getParentFile().mkdirs();
                log( "Created directory " + uploadedFile.getParent() );
            }

            try
            {
                FileWriter fw = new FileWriter( uploadedFile );
                IO.copy( request.getReader(), fw );
                response.setStatus( HttpServletResponse.SC_OK );
                log( "File copied" );
            }
            catch ( IOException iOException )
            {

                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                log( "Cannot send file", iOException );
            }
        }
        else
        {
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            try
            {
                log( "Method " + request.getMethod() );
                log( "ContextPath " + request.getContextPath() );
                log( "QueryString" + request.getQueryString() );
                log( "PathInfo " + request.getPathInfo() );
                log( "ServletPath " + request.getServletPath() );
                log( "AttributeNames " + request.getAttributeNames().toString() );
                log( "HeaderNames " + request.getHeaderNames().toString() );
                log( "RequestURL " + request.getRequestURL().toString() );
                log( "ParameterNames " + request.getParameterNames().toString() );
                StringWriter w = new StringWriter();
                IO.copy( request.getReader(), w );
                log( "Content " + w.toString() );

                // System.err.println( "Method " + request.getMethod( ) );
                // System.err.println( "ContextPath " + request.getContextPath( ) );
                // System.err.println( "QueryString" + request.getQueryString( ) );
                // System.err.println( "PathInfo " + request.getPathInfo( ) );
                // System.err.println( "ServletPath " + request.getServletPath( ) );
                // System.err.println( "AttributeNames " + request.getAttributeNames( ).toString( ) );
                // System.err.println( "HeaderNames " + request.getHeaderNames( ).toString( ) );
                // System.err.println( "RequestURL " + request.getRequestURL( ).toString( ) );
                // System.err.println( "ParameterNames " + request.getParameterNames( ).toString( ) );
                // System.err.println( "Content " + w.toString( ) );
            }
            catch ( IOException iOException )
            {
                log( "Error in unnknown method", iOException );
            }
        }
    }
}