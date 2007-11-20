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
package org.apache.maven.archetype.proxy;

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
        
        
        
        log( "A = " + request.getAuthType() );
        log( "A = " + request.getCharacterEncoding() );
        log( "A = " + request.getContentType() );
        log( "B = " + request.getContextPath() );
        log( "B = " + request.getLocalAddr() );
        log( "B = " + request.getLocalName() );
        log( "C = " + request.getMethod() );
        log( "C = " + request.getPathInfo() );
        log( "C = " + request.getPathTranslated() );
        log( "D = " + request.getProtocol() );
        log( "D = " + request.getQueryString() );
        log( "D = " + request.getRemoteAddr() );
        log( "E = " + request.getRemoteHost() );
        log( "E = " + request.getRemoteUser() );
        log( "E = " + request.getRequestURI() );
        log( "F = " + request.getRequestedSessionId() );
        log( "F = " + request.getScheme() );
        log( "F = " + request.getServerName() );
        log( "G = " + request.getServletPath() );
        log( "G = " + request.getAttributeNames() );
        log( "G = " + request.getCookies() );
        log( "H = " + request.getHeaderNames() );
//        log( "H = " + request.get );
//        log( "H = " + request.get );
//        log( "I = " + request.get );
//        log( "I = " + request.get );
//        log( "I = " + request.get );
//        log( "J = " + request.get );
//        log( "J = " + request.get );
//        log( "J = " + request.get );
//        log( "K = " + request.get );
//        log( "K = " + request.get );
//        log( "K = " + request.get );
        
        
        
        
        
        
        
        
        
        
        

        response.setHeader( "Date", null );
        response.setHeader( "Server", null );


        log( "Requested file = " + request.getRequestURI(  ) );
        String filePath =
            System.getProperty( "org.apache.maven.archetype.reporitory.directory" ).trim(  ) + "/" +
            request.getRequestURI(  );
        log( "Complete file path = " + filePath );

        String method = request.getMethod(  );

        if ( "GET".equalsIgnoreCase( method ) )
        {
            log( "Getting file" );
            try
            {
                File requestedFile = new File( filePath );

                InputStream is = new FileInputStream( requestedFile );

                if ( is != null )
                {
                    IO.copy( is, response.getOutputStream(  ) );
                    response.setStatus( HttpServletResponse.SC_OK );
                    log( "File sent" );
                }
                else
                {
                    log( "Can not send file no content" );
                }
            }
            catch ( FileNotFoundException fileNotFoundException )
            {
                response.setStatus( HttpServletResponse.SC_NOT_FOUND );
                log( "Requested file not found ", fileNotFoundException );
            }
            catch ( IOException iOException )
            {
                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                log( "Can not send file", iOException );
            }
        }
        else if ( "PUT".equalsIgnoreCase( method ) )
        {
            log( "Putting file" );
            File uploadedFile = new File( filePath );
            if ( uploadedFile.exists(  ) )
            {
                uploadedFile.delete(  );
                log( "Removed old file" );
            }
            else if ( !uploadedFile.getParentFile(  ).exists(  ) )
            {
                uploadedFile.getParentFile(  ).mkdirs(  );
                log( "Created directory " + uploadedFile.getParent(  ) );
            }

            try
            {
                FileWriter fw = new FileWriter( uploadedFile );
                IO.copy( request.getReader(  ), fw );
                response.setStatus( HttpServletResponse.SC_OK );
                log( "File copied" );
            }
            catch ( IOException iOException )
            {

                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                log( "Can not send file", iOException );
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
            }
            catch ( IOException iOException )
            {
                log( "Error in unnknown method", iOException );
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