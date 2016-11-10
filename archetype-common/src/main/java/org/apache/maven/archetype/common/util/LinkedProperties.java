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

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The {@code LinkedProperties} class represents a persistent set of
 * properties. The {@code LinkedProperties} can be saved to a stream
 * or loaded from a stream. Each key and its corresponding value in
 * the property list is a string. It is similar to {@code Properties}
 * except that the insertion order of the keys is preserved.
 *
 * @author  Nathan Figueroa
 */
public class LinkedProperties extends Properties
{
    private static final long serialVersionUID = 0;

    private LinkedHashSet<Object> keySet = new LinkedHashSet<Object>();

    @Override
    public synchronized Object put( Object key, Object value )
    {
        keySet.add( key );

        return super.put( key, value );
    }

    @Override
    public synchronized void putAll( Map values )
    {
        keySet.addAll( values.keySet() );

        super.putAll( values );
    }

    @Override
    public synchronized Object remove( Object key )
    {
        keySet.remove( key );

        return super.remove( key );
    }

    @Override
    public synchronized void clear()
    {
        keySet.clear();

        super.clear();
    }

    @Override
    public synchronized Enumeration keys()
    {
        return Collections.enumeration( keySet );
    }

    @Override
    public Set<Object> keySet()
    {
        return Collections.unmodifiableSet( keySet );
    }
}
