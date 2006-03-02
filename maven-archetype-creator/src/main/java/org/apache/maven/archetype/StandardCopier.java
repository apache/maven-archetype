/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package org.apache.maven.archetype;

import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

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
