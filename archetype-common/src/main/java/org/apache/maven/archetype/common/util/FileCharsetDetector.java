package org.apache.maven.archetype.common.util;

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

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * @author rafale
 */
public class FileCharsetDetector
    extends AbstractLogEnabled
{
    private final String charset;

    public FileCharsetDetector( File detectedFile )
        throws IOException
    {
        try ( FileInputStream fileInputStream = new FileInputStream( detectedFile );
              BufferedInputStream is = new BufferedInputStream( fileInputStream ) )
        {
            CharsetDetector detector = new CharsetDetector();
            detector.setText( is );
            CharsetMatch match = detector.detect();

            charset = match.getName().toUpperCase( Locale.ENGLISH );
        }
    }



    public FileCharsetDetector( InputStream detectedStream )
        throws IOException
    {
        CharsetDetector detector = new CharsetDetector();
        detector.setText( detectedStream );
        CharsetMatch match = detector.detect();

        charset = match.getName().toUpperCase( Locale.ENGLISH );
    }

    public String getCharset()
    {
        return charset;
    }

    public boolean isFound()
    {
        return true;
    }
}
