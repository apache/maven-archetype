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

package org.apache.maven.archetype.generator;

import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import java.util.ArrayList;
import java.util.List;

public class MockPrompter
implements Prompter
{
    List prompts = new ArrayList ();

    public void addAnswer ( String prompt )
    {
        prompts.add ( prompt );
    }

    public String prompt ( String string )
    throws PrompterException
    {
        System.err.println ( string );

        String answer = (String) prompts.remove ( 0 );
        System.err.println ( "answer = " + answer );
        return answer;
    }

    public String prompt ( String string, String string0 )
    throws PrompterException
    {
        System.err.println ( string );
        System.err.println ( string0 );

        String answer = (String) prompts.remove ( 0 );
        System.err.println ( "answer = " + answer );
        return answer;
    }

    public String prompt ( String string, List list )
    throws PrompterException
    {
        System.err.println ( string );
        System.err.println ( list );

        String answer = (String) prompts.remove ( 0 );
        System.err.println ( "answer = " + answer );
        return answer;
    }

    public String prompt ( String string, List list, String string0 )
    throws PrompterException
    {
        System.err.println ( string );
        System.err.println ( list );
        System.err.println ( string0 );

        String answer = (String) prompts.remove ( 0 );
        System.err.println ( "answer = " + answer );
        return answer;
    }

    public String promptForPassword ( String string )
    throws PrompterException
    {
        System.err.println ( string );

        String answer = (String) prompts.remove ( 0 );
        System.err.println ( "answer = " + answer );
        return answer;
    }

    public void showMessage ( String string )
    throws PrompterException
    {
        System.err.println ( string );
    }
}
