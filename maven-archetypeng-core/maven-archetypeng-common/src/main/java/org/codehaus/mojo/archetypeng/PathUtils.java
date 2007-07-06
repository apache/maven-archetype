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

/*
 * PathUtils.java
 *
 * Created on 7 mai 2007, 21:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.codehaus.mojo.archetypeng;

import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  rafale
 */
public class PathUtils
{
    public static String getDirectory ( String file, int level )
    {
        String[] fileAsArray = StringUtils.split ( file, "/" );
        List directoryAsArray = new ArrayList ();

        for ( int i = 0; ( i < level ) && ( i < ( fileAsArray.length - 1 ) ); i++ )
        {
            directoryAsArray.add ( fileAsArray[i] );
        }

        return
            StringUtils.join (
                directoryAsArray.toArray ( new String[directoryAsArray.size ()] ),
                "/"
            );
    }
}
