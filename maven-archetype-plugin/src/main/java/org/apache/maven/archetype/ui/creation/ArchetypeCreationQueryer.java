package org.apache.maven.archetype.ui.creation;

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

import org.apache.maven.archetype.ui.ArchetypeConfiguration;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * User interaction component to query informations necessary for an archetype creation from a project.
 * 
 * @todo this interface is bound to its implementation through the prompter exception
 */
public interface ArchetypeCreationQueryer
{
    String ROLE = ArchetypeCreationQueryer.class.getName();

    /**
     */
    String getArchetypeArtifactId( String defaultValue )
        throws PrompterException;

    /**
     */
    String getArchetypeGroupId( String defaultValue )
        throws PrompterException;

    /**
     */
    String getArchetypeVersion( String defaultValue )
        throws PrompterException;

    /**
     */
    String getArtifactId( String defaultValue )
        throws PrompterException;

    /**
     */
    boolean askAddAnotherProperty()
        throws PrompterException;

    /**
     */
    String askNewPropertyKey()
        throws PrompterException;

    /**
     */
    String askReplacementValue( String propertyKey, String defaultValue )
        throws PrompterException;

    /**
     */
    boolean confirmConfiguration( ArchetypeConfiguration archetypeConfiguration )
        throws PrompterException;

    /**
     */
    String getGroupId( String defaultValue )
        throws PrompterException;

    /**
     */
    String getPackage( String defaultValue )
        throws PrompterException;

    /**
     */
    String getVersion( String defaultValue )
        throws PrompterException;
}
