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

import org.codehaus.plexus.util.SelectorUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for scanning a directory for files/directories which match certain criteria.
 * <p/>
 * <p>These criteria consist of selectors and patterns which have been specified. With the selectors
 * you can select which files you want to have included. Files which are not selected are excluded.
 * With patterns you can include or exclude files based on their filename.</p>
 * <p/>
 * <p>The idea is simple. A given directory is recursively scanned for all files and directories.
 * Each file/directory is matched against a set of selectors, including special support for matching
 * against filenames with include and and exclude patterns. Only files/directories which match at
 * least one pattern of the include pattern list or other file selector, and don't match any pattern
 * of the exclude pattern list or fail to match against a required selector will be placed in the
 * list of files/directories found.</p>
 * <p/>
 * <p>When no list of include patterns is supplied, "**" will be used, which means that everything
 * will be matched. When no list of exclude patterns is supplied, an empty list is used, such that
 * nothing will be excluded. When no selectors are supplied, none are applied.</p>
 * <p/>
 * <p>The filename pattern matching is done as follows: The name to be matched is split up in path
 * segments. A path segment is the name of a directory or file, which is bounded by <code>
 * File.separator</code> ('/' under UNIX, '\' under Windows). For example, "abc/def/ghi/xyz.java" is
 * split up in the segments "abc", "def","ghi" and "xyz.java". The same is done for the pattern
 * against which should be matched.</p>
 * <p/>
 * <p>The segments of the name and the pattern are then matched against each other. When '**' is
 * used for a path segment in the pattern, it matches zero or more path segments of the name.</p>
 * <p/>
 * <p>There is a special case regarding the use of <code>File.separator</code>s at the beginning of
 * the pattern and the string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string to match must also start
 * with a <code>File.separator</code>. When a pattern does not start with a <code>
 * File.separator</code>, the string to match may not start with a <code>File.separator</code>. When
 * one of these rules is not obeyed, the string will not match.</p>
 * <p/>
 * <p>When a name path segment is matched against a pattern path segment, the following special
 * characters can be used:<br>
 * '*' matches zero or more characters<br>
 * '?' matches one character.</p>
 * <p/>
 * <p>Examples:</p>
 * <p/>
 * <p>"**\*.class" matches all .class files/dirs in a directory tree.</p>
 * <p/>
 * <p>"test\a??.java" matches all files/dirs which start with an 'a', then two more characters and
 * then ".java", in a directory called test.</p>
 * <p/>
 * <p>"**" matches everything in a directory tree.</p>
 * <p/>
 * <p>"**\test\**\XYZ*" matches all files/dirs which start with "XYZ" and where there is a parent
 * directory called test (e.g. "abc\test\def\ghi\XYZ123").</p>
 * <p/>
 * <p>Case sensitivity may be turned off if necessary. By default, it is turned on.</p>
 * <p/>
 * <p>Example of usage:</p>
 * <p/>
 * <pre>
 * String[] includes = {"**\\*.class"};
 * String[] excludes = {"modules\\*\\**"};
 * ds.setIncludes(includes);
 * ds.setExcludes(excludes);
 * ds.setBasedir(new File("test"));
 * ds.setCaseSensitive(true);
 * ds.scan();
 * <p/>
 * System.out.println("FILES:");
 * String[] files = ds.getIncludedFiles();
 * for (int i = 0; i < files.length; i++) {
 * System.out.println(files[i]);
 * }
 * </pre>
 * <p/>
 * <p>This will scan a directory called test for .class files, but excludes all files in all proper
 * subdirectories of a directory called "modules"</p>
 * <p/>
 * <p>This class was stealed from rg.coudehaus.plexus.util.DirectoryScanner and adapted to search
 * from a List<String></p>
 *
 * @author Arnout J. Kuiper <a href="mailto:ajkuiper@wxs.nl">ajkuiper@wxs.nl</a>
 * @author Magesh Umasankar
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @author <a href="mailto:levylambert@tiscali-dsl.de">Antoine Levy-Lambert</a>
 */
public class ListScanner
{
    /**
     * Patterns which should be excluded by default.
     *
     * @see #addDefaultExcludes()
     */
    public static final String[] DEFAULTEXCLUDES =
        { // Miscellaneous typical temporary files
            "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",

            // CVS
            "**/CVS", "**/CVS/**", "**/.cvsignore",

            // SCCS
            "**/SCCS", "**/SCCS/**",

            // Visual SourceSafe
            "**/vssver.scc",

            // Subversion
            "**/.svn", "**/.svn/**",

            // Arch
            "**/.arch-ids", "**/.arch-ids/**",

            // Bazaar
            "**/.bzr", "**/.bzr/**",

            // SurroundSCM
            "**/.MySCMServerInfo",

            // Mac
            "**/.DS_Store"
        };

    /** The base directory to be scanned. */
    protected String basedir;

    /** Whether or not everything tested so far has been included. */
    protected boolean everythingIncluded = true;

    /** The patterns for the files to be excluded. */
    protected String[] excludes;

    /** The patterns for the files to be included. */
    protected String[] includes;

    /** Whether or not the file system should be treated as a case sensitive one. */
    protected boolean isCaseSensitive = true;

    /** Sole constructor. */
    public ListScanner()
    {
    }

    public static String getDefaultExcludes()
    {
        return StringUtils.join( DEFAULTEXCLUDES, "," );
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may contain two special
     * characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str     The string which must be matched against the pattern. Must not be <code>
     *                null</code>.
     * @return <code>true</code> if the string matches against the pattern, or <code>false</code>
     *         otherwise.
     */
    public static boolean match( String pattern,
                                 String str )
    {
        // default matches the SelectorUtils default
        return match( pattern, str, true );
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may contain two special
     * characters:<br>
     * '*' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern         The pattern to match against. Must not be <code>null</code>.
     * @param str             The string which must be matched against the pattern. Must not be
     *                        <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     * @return <code>true</code> if the string matches against the pattern, or <code>false</code>
     *         otherwise.
     */
    protected static boolean match( String pattern,
                                    String str,
                                    boolean isCaseSensitive )
    {
        return SelectorUtils.match( pattern, str, isCaseSensitive );
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str     The path to match, as a String. Must not be <code>null</code>.
     * @return <code>true</code> if the pattern matches against the string, or <code>false</code>
     *         otherwise.
     */
    protected static boolean matchPath( String pattern,
                                        String str )
    {
        // default matches the SelectorUtils default
        return matchPath( pattern, str, true );
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern         The pattern to match against. Must not be <code>null</code>.
     * @param str             The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     * @return <code>true</code> if the pattern matches against the string, or <code>false</code>
     *         otherwise.
     */
    protected static boolean matchPath( String pattern,
                                        String str,
                                        boolean isCaseSensitive )
    {
        return
            SelectorUtils.matchPath(
                PathUtils.convertPathForOS( pattern ),
                PathUtils.convertPathForOS( str ),
                isCaseSensitive
            );
    }

    /**
     * Tests whether or not a given path matches the start of a given pattern up to the first "**".
     * <p/>
     * <p>This is not a general purpose test and should only be used if you can live with false
     * positives. For example, <code>pattern=**\a</code> and <code>str=b</code> will yield <code>
     * true</code>.</p>
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str     The path to match, as a String. Must not be <code>null</code>.
     * @return whether or not a given path matches the start of a given pattern up to the first
     *         "**".
     */
    protected static boolean matchPatternStart( String pattern,
                                                String str )
    {
        // default matches SelectorUtils default
        return matchPatternStart( pattern, str, true );
    }

    /**
     * Tests whether or not a given path matches the start of a given pattern up to the first "**".
     * <p/>
     * <p>This is not a general purpose test and should only be used if you can live with false
     * positives. For example, <code>pattern=**\a</code> and <code>str=b</code> will yield <code>
     * true</code>.</p>
     *
     * @param pattern         The pattern to match against. Must not be <code>null</code>.
     * @param str             The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     * @return whether or not a given path matches the start of a given pattern up to the first
     *         "**".
     */
    protected static boolean matchPatternStart(
        String pattern,
        String str,
        boolean isCaseSensitive
    )
    {
        return
            SelectorUtils.matchPatternStart(
                PathUtils.convertPathForOS( pattern ),
                PathUtils.convertPathForOS( str ),
                isCaseSensitive
            );
    }

    /** Adds default exclusions to the current exclusions set. */
    public void addDefaultExcludes()
    {
        int excludesLength = ( excludes == null ) ? 0 : excludes.length;
        String[] newExcludes;
        newExcludes = new String[excludesLength + DEFAULTEXCLUDES.length];
        if ( excludesLength > 0 )
        {
            System.arraycopy( excludes, 0, newExcludes, 0, excludesLength );
        }
        for ( int i = 0; i < DEFAULTEXCLUDES.length; i++ )
        {
            newExcludes[i + excludesLength] =
                DEFAULTEXCLUDES[i].replace( '/', File.separatorChar ).replace(
                    '\\',
                    File.separatorChar
                );
        }
        excludes = newExcludes;
    }

    /**
     * Returns the base directory to be scanned. This is the directory which is scanned recursively.
     *
     * @return the base directory to be scanned
     */
    public String getBasedir()
    {
        return basedir;
    }

    /**
     * Sets the base directory to be scanned. This is the directory which is scanned recursively.
     * This directory is normalized for multiple os's (all / and \\ are replaced with
     * File.separatorChar
     *
     * @param basedir The base directory for scanning. Should not be <code>null</code>.
     */
    public void setBasedir( String basedir )
    {
        this.basedir = basedir;
    }

    /**
     * Sets whether or not the file system should be regarded as case sensitive.
     *
     * @param isCaseSensitive whether or not the file system should be regarded as a case
     *                        sensitive one
     */
    public void setCaseSensitive( boolean isCaseSensitive )
    {
        this.isCaseSensitive = isCaseSensitive;
    }

    /**
     * Sets the list of exclude patterns to use. All '/' and '\' characters are replaced by <code>
     * File.separatorChar</code>, so the separator used need not match <code>
     * File.separatorChar</code>.
     * <p/>
     * <p>When a pattern ends with a '/' or '\', "**" is appended.</p>
     *
     * @param excludes A list of exclude patterns. May be <code>null</code>, indicating that no
     *                 files should be excluded. If a non-<code>null</code> list is given, all
     *                 elements must be non-<code>null</code>.
     */
    public void setExcludes( List excludesList )
    {
        String[] excludes = (String[]) excludesList.toArray( new String[excludesList.size()] );
        if ( excludes == null )
        {
            this.excludes = null;
        }
        else
        {
            setExcludes( excludes );
        }
    }

    public void setExcludes( String excludes )
    {
        if ( excludes == null )
        {
            this.excludes = null;
        }
        else
        {
            setExcludes( StringUtils.split( excludes, "," ) );
        }
    }

    /**
     * Sets the list of include patterns to use. All '/' and '\' characters are replaced by <code>
     * File.separatorChar</code>, so the separator used need not match <code>
     * File.separatorChar</code>.
     * <p/>
     * <p>When a pattern ends with a '/' or '\', "**" is appended.</p>
     *
     * @param includes A list of include patterns. May be <code>null</code>, indicating that all
     *                 files should be included. If a non-<code>null</code> list is given, all
     *                 elements must be non-<code>null</code>.
     */
    public void setIncludes( List includesList )
    {
        String[] includes = (String[]) includesList.toArray( new String[includesList.size()] );
        if ( includes == null )
        {
            this.includes = null;
        }
        else
        {
            setIncludes( includes );
        }
    }

    public void setIncludes( String includes )
    {
        if ( includes == null )
        {
            this.includes = null;
        }
        else
        {
            setIncludes( StringUtils.split( includes, "," ) );
        }
    }

    /**
     * Scans the base directory for files which match at least one include pattern and don't match
     * any exclude patterns. If there are selectors then the files must pass muster there, as well.
     *
     * @throws IllegalStateException if the base directory was set incorrectly (i.e. if it is
     *                               <code>null</code>, doesn't exist, or isn't a directory).
     */
    public List scan( List files )
        throws
        IllegalStateException
    {
//        System.err.println("Scanning \nbasedir="+basedir +
//                " \nincludes=" + java.util.Arrays.toString(includes) +
//                " \nexcludes=" + java.util.Arrays.toString(excludes) +
//                " \non files="+files);
        if ( basedir == null )
        {
            throw new IllegalStateException( "No basedir set" );
        }

        if ( includes == null )
        {
            // No includes supplied, so set it to 'matches all'
            includes = new String[1];
            includes[0] = "**";
        }
        if ( excludes == null )
        {
            excludes = new String[0];
        }

        List result = new ArrayList();

        Iterator iterator = files.iterator();
        while ( iterator.hasNext() )
        {
            String fileName = (String) iterator.next();
//            System.err.println("Checking "+(isIncluded ( fileName )?"I":"-")+(isExcluded ( fileName )?"E":"-")+fileName);
            if ( isIncluded( fileName ) && !isExcluded( fileName ) )
            {
                result.add( fileName );
            }
        }
//        System.err.println("Result "+result+"\n\n\n");
        return result;
    }

    /**
     * Tests whether or not a name matches against at least one exclude pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one exclude pattern, or
     *         <code>false</code> otherwise.
     */
    protected boolean isExcluded( String name )
    {
        return matchesPatterns( name, excludes );
    }

    /**
     * Tests whether or not a name matches against at least one include pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one include pattern, or
     *         <code>false</code> otherwise.
     */
    protected boolean isIncluded( String name )
    {
        return matchesPatterns( name, includes );
    }

    /**
     * Tests whether or not a name matches against at least one include pattern.
     *
     * @param name     The name to match. Must not be <code>null</code>.
     * @param patterns The list of patterns to match.
     * @return <code>true</code> when the name matches against at least one include pattern, or
     *         <code>false</code> otherwise.
     */
    protected boolean matchesPatterns( String name,
                                       String[] patterns )
    {
        // avoid extra object creation in the loop
        String path = null;

        String baseDir = getBasedir();
        if ( baseDir.length() > 0 )
        {
            baseDir = baseDir.concat( File.separator );
        }

        for ( int i = 0; i < patterns.length; i++ )
        {
            path = PathUtils.convertPathForOS( baseDir + patterns[i] );
//            System.err.println("path="+path);
            if ( matchPath( path, name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    private void setExcludes( String[] excludes )
    {
        this.excludes = setPatterns( excludes );
    }

    private void setIncludes( String[] includes )
    {
        this.includes = setPatterns( includes );
    }

    private String[] setPatterns( String[] patterns )
    {
        String[] result = null;
        if ( ( patterns != null ) && ( patterns.length > 0 ) )
        {
            result = new String[patterns.length];
            for ( int i = 0; i < patterns.length; i++ )
            {
                String pattern = patterns[i].trim();

                // don't normalize the pattern here, we internalize the normalization
                // just normalize for comparison purposes
                if ( PathUtils.convertPathForOS( pattern ).endsWith( File.separator ) )
                {
                    pattern += "**";
                }
                result[i] = pattern;
            }
        }
        return result;
    }
}
