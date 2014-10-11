package org.apache.maven.archetype.proxy;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

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
 * Stolen code from Mortbay
 *
 * @author rafale
 */
public class ProxyServlet
    extends HttpServlet
{
    protected Set<String> dontProxyHeaders = new HashSet<String>();

    {
        dontProxyHeaders.add( "proxy-connection" );
        dontProxyHeaders.add( "connection" );
        dontProxyHeaders.add( "keep-alive" );
        dontProxyHeaders.add( "transfer-encoding" );
        dontProxyHeaders.add( "te" );
        dontProxyHeaders.add( "trailer" );
        dontProxyHeaders.add( "proxy-authorization" );
        dontProxyHeaders.add( "proxy-authenticate" );
        dontProxyHeaders.add( "upgrade" );
    }

    private ServletConfig config;

    private ServletContext context;

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init( ServletConfig config )
        throws ServletException
    {
        this.config = config;
        this.context = config.getServletContext();
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#getServletConfig()
     */
    public ServletConfig getServletConfig()
    {
        return config;
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    @SuppressWarnings( "checkstyle:methodlength" )
    public void service( ServletRequest req, ServletResponse res )
        throws ServletException,
               IOException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if ( "CONNECT".equalsIgnoreCase( request.getMethod() ) )
        {
            handleConnect( request, response );
        }
        else
        {
            String uri = request.getRequestURI();
            if ( request.getQueryString() != null )
            {
                uri += "?" + request.getQueryString();
            }
            URL url =
                new URL( request.getScheme(), request.getServerName(),
                request.getServerPort(),
                uri );

            context.log( "\n\n\nURL=" + url );

            URLConnection connection = url.openConnection();
            connection.setAllowUserInteraction( false );

            // Set method
            HttpURLConnection http = null;
            if ( connection instanceof HttpURLConnection )
            {
                http = (HttpURLConnection) connection;
                http.setRequestMethod( request.getMethod() );
                http.setInstanceFollowRedirects( false );
            }

            // check connection header
            String connectionHdr = request.getHeader( "Connection" );
            if ( connectionHdr != null )
            {
                connectionHdr = connectionHdr.toLowerCase();
                if ( connectionHdr.equals( "keep-alive" ) || connectionHdr.equals( "close" ) )
                {
                    connectionHdr = null;
                }
            }

            // copy headers
            boolean xForwardedFor = false;
            boolean hasContent = false;
            Enumeration enm = request.getHeaderNames();
            while ( enm.hasMoreElements() )
            {
                // TODO could be better than this!
                String hdr = (String) enm.nextElement();
                String lhdr = hdr.toLowerCase();

                if ( dontProxyHeaders.contains( lhdr ) )
                {
                    continue;
                }
                if ( connectionHdr != null && connectionHdr.indexOf( lhdr ) >= 0 )
                {
                    continue;
                }
                if ( "content-type".equals( lhdr ) )
                {
                    hasContent = true;
                }
                Enumeration vals = request.getHeaders( hdr );
                while ( vals.hasMoreElements() )
                {
                    String val = (String) vals.nextElement();
                    if ( val != null )
                    {
                        connection.addRequestProperty( hdr, val );
                        context.log( "req " + hdr + ": " + val );
                        xForwardedFor |= "X-Forwarded-For".equalsIgnoreCase( hdr );
                    }
                }
            }

            // Proxy headers
            connection.setRequestProperty( "Via", "1.1 (jetty)" );
            if ( !xForwardedFor )
            {
                connection.addRequestProperty( "X-Forwarded-For", request.getRemoteAddr() );
            }
            // a little bit of cache control
            String cacheControl = request.getHeader( "Cache-Control" );
            if ( cacheControl != null
                && ( cacheControl.indexOf( "no-cache" ) >= 0 || cacheControl.indexOf( "no-store" ) >= 0 ) )
            {
                connection.setUseCaches( false );

            // customize Connection
            }
            try
            {
                connection.setDoInput( true );

                // do input thang!
                InputStream in = request.getInputStream();
                if ( hasContent )
                {
                    connection.setDoOutput( true );
                    IO.copy( in, connection.getOutputStream() );
                }

                // Connect
                connection.connect();
            }
            catch ( Exception e )
            {
                context.log( "proxy", e );
            }

            InputStream proxyIn = null;

            // handler status codes etc.
            int code = HttpURLConnection.HTTP_INTERNAL_ERROR;
            if ( http != null )
            {
                proxyIn = http.getErrorStream();

                code = http.getResponseCode();
                response.setStatus( code, http.getResponseMessage() );
                context.log( "response = " + http.getResponseCode() );
            }

            if ( proxyIn == null )
            {
                try
                {
                    proxyIn = connection.getInputStream();
                }
                catch ( Exception e )
                {
                    context.log( "stream", e );
                    proxyIn = http.getErrorStream();
                }
            }

            // clear response defaults.
            response.setHeader( "Date", null );
            response.setHeader( "Server", null );

            // set response headers
            int h = 0;
            String hdr = connection.getHeaderFieldKey( h );
            String val = connection.getHeaderField( h );
            while ( hdr != null || val != null )
            {
                String lhdr = hdr != null ? hdr.toLowerCase() : null;
                if ( hdr != null && val != null && !dontProxyHeaders.contains( lhdr ) )
                {
                    response.addHeader( hdr, val );
                }
                context.log( "res " + hdr + ": " + val );

                h++;
                hdr = connection.getHeaderFieldKey( h );
                val = connection.getHeaderField( h );
            }
            response.addHeader( "Via", "1.1 (jetty)" );

            // Handle
            if ( proxyIn != null )
            {
                IO.copy( proxyIn, response.getOutputStream() );
            }
        }
    }

    /* ------------------------------------------------------------ */
    public void handleConnect( HttpServletRequest request,
        HttpServletResponse response )
        throws IOException
    {
        String uri = request.getRequestURI();

        context.log( "CONNECT: " + uri );

        String port = "";
        String host = "";

        int c = uri.indexOf( ':' );
        if ( c >= 0 )
        {
            port = uri.substring( c + 1 );
            host = uri.substring( 0, c );
            if ( host.indexOf( '/' ) > 0 )
            {
                host = host.substring( host.indexOf( '/' ) + 1 );
            }
        }

        InetSocketAddress inetAddress =
            new InetSocketAddress( host, Integer.parseInt( port ) );

        InputStream in = request.getInputStream();
        OutputStream out = response.getOutputStream();

        Socket socket = new Socket( inetAddress.getAddress(), inetAddress.getPort() );
        context.log( "Socket: " + socket );

        response.setStatus( HttpURLConnection.HTTP_OK );
        response.setHeader( "Connection", "close" );
        response.flushBuffer();



        context.log( "out<-in" );
        IO.copyThread( socket.getInputStream(), out );
        context.log( "in->out" );
        IO.copy( in, socket.getOutputStream() );
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#getServletInfo()
     */
    public String getServletInfo()
    {
        return "Proxy Servlet";
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy()
    {
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest( HttpServletRequest request,
        HttpServletResponse response )
        throws ServletException, IOException
    {
        response.setContentType( "text/html;charset=UTF-8" );
        PrintWriter out = response.getWriter();
        try
        {
        /* TODO output your page here
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet ProxyServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet ProxyServlet at " + request.getContextPath () + "</h1>");
        out.println("</body>");
        out.println("</html>");
         */
        }
        finally
        {
            out.close();
        }
    }
}
