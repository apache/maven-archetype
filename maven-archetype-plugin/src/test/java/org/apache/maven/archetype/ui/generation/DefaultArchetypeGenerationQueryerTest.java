package org.apache.maven.archetype.ui.generation;

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

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.easymock.AbstractMatcher;
import org.easymock.ArgumentsMatcher;
import org.easymock.MockControl;

import java.util.regex.Pattern;

public class DefaultArchetypeGenerationQueryerTest
    extends PlexusTestCase
{

    private DefaultArchetypeGenerationQueryer queryer;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        queryer = (DefaultArchetypeGenerationQueryer) lookup( ArchetypeGenerationQueryer.class.getName() );
    }

    public void testPropertyRegexValidationRetry()
        throws PrompterException
    {

        MockControl control = MockControl.createControl( Prompter.class );

        Prompter prompter = (Prompter) control.getMock();
        prompter.prompt( "" );
        control.setMatcher( createArgumentMatcher() );
        control.setReturnValue( "invalid-answer" );
        queryer.setPrompter( prompter );
        prompter.prompt( "" );
        control.setReturnValue( "valid-answer" );
        queryer.setPrompter( prompter );
        control.replay();

        String value = queryer.getPropertyValue( "custom-property", null, Pattern.compile( "^valid-.*" ) );

        assertEquals( "valid-answer", value );

    }

    private static ArgumentsMatcher createArgumentMatcher()
    {
        return new AbstractMatcher()
        {
            @Override
            protected boolean argumentMatches( Object o, Object o1 )
            {
                return true;
            }

            @Override
            protected String argumentToString( Object o )
            {
                return "...";
            }
        };
    }

}
