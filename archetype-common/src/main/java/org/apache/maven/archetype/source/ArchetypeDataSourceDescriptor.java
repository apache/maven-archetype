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

package org.apache.maven.archetype.source;

import java.util.ArrayList;
import java.util.List;

/** @author Jason van Zyl */
public class ArchetypeDataSourceDescriptor
{
    private List parameters;

    public void addParameter( String name, Class type, String defaultValue, String description )
    {
        addParameter( new Parameter( name, type, defaultValue, description ) );
    }

    public void addParameter( Parameter parameter )
    {
        if ( parameters == null )
        {
            parameters = new ArrayList();
        }

        parameters.add( parameter );
    }

    public class Parameter
    {
        public Parameter( String name,
                          Class type,
                          String defaultValue,
                          String description )
        {
            this.name = name;

            this.type = type;

            this.defaultValue = defaultValue;

            this.description = description;

        }

        private String name;

        private Class type;

        private String defaultValue;

        private String description;

        public Class getType()
        {
            return type;
        }

        public String getDefaultValue()
        {
            return defaultValue;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
