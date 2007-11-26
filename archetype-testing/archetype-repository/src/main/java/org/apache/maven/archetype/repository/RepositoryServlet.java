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
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.util.IO;

/**
 *
 * @author rafale
 */
public class RepositoryServlet
    extends HttpServlet
{
    private ServletConfig config;

    private ServletContext context;

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init( ServletConfig config ) throws ServletException
    {
        this.config = config;
        this.context = config.getServletContext(  );
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#getServletConfig()
     */
    public ServletConfig getServletConfig( )
    {
        return config;
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service( ServletRequest req, ServletResponse res ) throws ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader( "Date", null );
        response.setHeader( "Server", null );


        log( "Requested file = " + request.getRequestURI(  ) );
        System.err.println( "Requested file = " + request.getRequestURI(  ) );
        String filePath =
            System.getProperty( "org.apache.maven.archetype.reporitory.directory" ).trim(  ) + "/" +
            request.getRequestURI(  );
        filePath=filePath.replace("repo/", "/");
        filePath=filePath.replaceAll("//", "/");
        filePath=filePath.replaceAll("//", "/");
        filePath=filePath.replaceAll("//", "/");
        log( "Complete file path = " + filePath );
        System.err.println( "Complete file path = " + filePath );

        String method = request.getMethod(  );

        if ( "GET".equalsIgnoreCase( method ) )
        {
            log( "Getting file" );
            System.err.println( "Getting file" );
            try
            {
                File requestedFile = new File( filePath );

                InputStream is = new FileInputStream( requestedFile );

                if ( is != null )
                {
                    IO.copy( is, response.getOutputStream(  ) );
                    response.setStatus( HttpServletResponse.SC_OK );
                    log( "File sent" );
                    System.err.println( "File sent" );
                }
                else
                {
                    log( "Can not send file no content" );
                    System.err.println( "Can not send file no content" );
                }
            }
            catch ( FileNotFoundException fileNotFoundException )
            {
                response.setStatus( HttpServletResponse.SC_NOT_FOUND );
                log( "Requested file not found ", fileNotFoundException );
                System.err.println( "Requested file not found "+ fileNotFoundException );
                fileNotFoundException.printStackTrace(System.err);
            }
            catch ( IOException iOException )
            {
                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                log( "Can not send file", iOException );
                System.err.println( "Can not send file"+ iOException );
                iOException.printStackTrace(System.err);
            }
        }
        else if ( "PUT".equalsIgnoreCase( method ) )
        {
            log( "Putting file" );
            System.err.println( "Putting file" );
            File uploadedFile = new File( filePath );
            if ( uploadedFile.exists(  ) )
            {
                uploadedFile.delete(  );
                log( "Removed old file" );
                System.err.println( "Removed old file" );
            }
            else if ( !uploadedFile.getParentFile(  ).exists(  ) )
            {
                uploadedFile.getParentFile(  ).mkdirs(  );
                log( "Created directory " + uploadedFile.getParent(  ) );
                System.err.println( "Created directory " + uploadedFile.getParent(  ) );
            }

            try
            {
                FileWriter fw = new FileWriter( uploadedFile );
                IO.copy( request.getReader(  ), fw );
                response.setStatus( HttpServletResponse.SC_OK );
                log( "File copied" );
                System.err.println( "File copied" );
            }
            catch ( IOException iOException )
            {

                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                log( "Can not send file", iOException );
                System.err.println( "Can not send file"+ iOException );
                iOException.printStackTrace(System.err);
            }
        }
        else
        {
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            try
            {
                log( "Method " + request.getMethod(  ) );
                log( "ContextPath " + request.getContextPath(  ) );
                log( "QueryString" + request.getQueryString(  ) );
                log( "PathInfo " + request.getPathInfo(  ) );
                log( "ServletPath " + request.getServletPath(  ) );
                log( "AttributeNames " + request.getAttributeNames(  ).toString(  ) );
                log( "HeaderNames " + request.getHeaderNames(  ).toString(  ) );
                log( "RequestURL " + request.getRequestURL(  ).toString(  ) );
                log( "ParameterNames " + request.getParameterNames(  ).toString(  ) );
                StringWriter w = new StringWriter(  );
                IO.copy( request.getReader(  ), w );
                log( "Content " + w.toString(  ) );


//                System.err.println( "Method " + request.getMethod(  ) );
//                System.err.println( "ContextPath " + request.getContextPath(  ) );
//                System.err.println( "QueryString" + request.getQueryString(  ) );
//                System.err.println( "PathInfo " + request.getPathInfo(  ) );
//                System.err.println( "ServletPath " + request.getServletPath(  ) );
//                System.err.println( "AttributeNames " + request.getAttributeNames(  ).toString(  ) );
//                System.err.println( "HeaderNames " + request.getHeaderNames(  ).toString(  ) );
//                System.err.println( "RequestURL " + request.getRequestURL(  ).toString(  ) );
//                System.err.println( "ParameterNames " + request.getParameterNames(  ).toString(  ) );
//                System.err.println( "Content " + w.toString(  ) );
            }
            catch ( IOException iOException )
            {
                log( "Error in unnknown method", iOException );
                System.err.println( "Error in unnknown method"+ iOException );
                iOException.printStackTrace(System.err);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#getServletInfo()
     */
    public String getServletInfo( )
    {
        return "Repository Servlet";
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy( )
    {
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest( HttpServletRequest request,
        HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/html;charset=UTF-8" );
        PrintWriter out = response.getWriter(  );
        try
        {
            /* TODO output your page here
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RepositoryServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RepositoryServlet at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
             */
        }
        finally
        {
            out.close(  );
        }
    }

    public String getServletName()
    {
        return "Repository Servlet";
    }


}