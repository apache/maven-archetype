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

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Jason van Zyl
 * @version $Revision:$
 */
public class FilteringCopier
    implements Copier
{
    private String find;

    private String replace;

    PatternMatcher matcher = new Perl5Matcher();

    Pattern pattern = null;

    PatternCompiler compiler = new Perl5Compiler();

    String regularExpression, input, sub, result;

    public FilteringCopier( String find, String replace )
    {
        this.find = find;

        this.replace = replace;
    }

    public void copy( File source, File destination )
        throws IOException
    {
        InputStream input = null;

        OutputStream output = null;

        try
        {
            /*
            input = new ReplaceStringInputStream( new FileInputStream( source ), find, replace );

            output = new FileOutputStream( destination );

            IOUtil.copy( input, output );
            */

            //input = new ReplaceStringInputStream( new FileInputStream( source ), find, replace );

            input = new FileInputStream( source );

            String s = streamToString( input );

            pattern = compiler.compile( find );

            // Perform substitution and print result.
            result = Util.substitute( matcher, pattern,
                                      new Perl5Substitution( replace, Perl5Substitution.INTERPOLATE_ALL ), s,
                                      Util.SUBSTITUTE_ALL );

            FileUtils.fileWrite( destination.getAbsolutePath(), result );

        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtil.close( input );

            IOUtil.close( output );
        }
    }

    public static String streamToString( InputStream in )
        throws IOException
    {
        StringBuffer text = new StringBuffer();
        try
        {
            int b;
            while ( ( b = in.read() ) != -1 )
            {
                text.append( (char) b );
            }
        }
        finally
        {
            in.close();
        }
        return text.toString();
    }

}
