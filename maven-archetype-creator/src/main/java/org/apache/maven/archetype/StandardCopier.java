package org.apache.maven.archetype;

/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Jason van Zyl
 * @version $Revision:$
 */
public class StandardCopier
    implements Copier
{
    public void copy( File source, File destination )
        throws IOException
    {
        FileInputStream input = null;

        FileOutputStream output = null;

        try
        {
            input = new FileInputStream( source );

            output = new FileOutputStream( destination );

            IOUtil.copy( input, output );
        }
        finally
        {
            IOUtil.close( input );

            IOUtil.close( output );
        }
    }
}
