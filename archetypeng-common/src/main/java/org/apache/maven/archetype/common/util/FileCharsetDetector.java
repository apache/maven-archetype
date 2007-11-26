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

package org.apache.maven.archetype.common.util;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/** @author rafale */
public class FileCharsetDetector
    extends AbstractLogEnabled
{
    private String charset = null;

    private boolean found = false;

    public FileCharsetDetector( File detectedFile )
        throws
        FileNotFoundException,
        IOException
    {
        nsDetector det = new nsDetector( nsPSMDetector.ALL );

        det.Init(
            new nsICharsetDetectionObserver()
            {
                public void Notify( String charset )
                {
                    FileCharsetDetector.this.charset = charset;
                    FileCharsetDetector.this.found = true;
                }
            }
        );

        BufferedInputStream imp = new BufferedInputStream( new FileInputStream( detectedFile ) );

        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;

        while ( ( len = imp.read( buf, 0, buf.length ) ) != -1 )
        {
            // Check if the stream is only ascii.
            if ( isAscii )
            {
                isAscii = det.isAscii( buf, len );
            }

            // DoIt if non-ascii and not done yet.
            if ( !isAscii && !done )
            {
                done = det.DoIt( buf, len, false );
                found = done;
            }
        }
        det.DataEnd();

        if ( !isFound() )
        {
            String[] prob = det.getProbableCharsets();

            if ( prob.length > 0 )
            {
                charset = prob[0];
            }
        }

        if ( isAscii )
        {
            charset = "ASCII";
        }
    }

    public String getCharset()
    {
        return charset;
    }

    public boolean isFound()
    {
        return found;
    }
}
