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
package org.apache.maven.archetype.ui.creation;

import java.util.Iterator;

import org.apache.maven.archetype.common.Constants;
import org.apache.maven.archetype.ui.ArchetypeConfiguration;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.logging.AbstractLogEnabled;

@Component(role = ArchetypeCreationQueryer.class, hint = "default")
public class DefaultArchetypeCreationQueryer extends AbstractLogEnabled implements ArchetypeCreationQueryer {
    @Requirement
    private Prompter prompter;

    @Override
    public String getArchetypeArtifactId(String defaultValue) throws PrompterException {
        return getValue(Constants.ARCHETYPE_ARTIFACT_ID, defaultValue);
    }

    @Override
    public String getArchetypeGroupId(String defaultValue) throws PrompterException {
        return getValue(Constants.ARCHETYPE_GROUP_ID, defaultValue);
    }

    @Override
    public String getArchetypeVersion(String defaultValue) throws PrompterException {
        return getValue(Constants.ARCHETYPE_VERSION, defaultValue);
    }

    @Override
    public String getArtifactId(String defaultValue) throws PrompterException {
        return getValue(Constants.ARTIFACT_ID, defaultValue);
    }

    @Override
    public boolean askAddAnotherProperty() throws PrompterException {
        String query = "Add a new custom property";

        String answer = prompter.prompt(query, "Y");

        return "Y".equalsIgnoreCase(answer);
    }

    @Override
    public String askNewPropertyKey() throws PrompterException {
        String query = "Define property key";

        String answer = prompter.prompt(query);

        return answer;
    }

    @Override
    public String askReplacementValue(String propertyKey, String defaultValue) throws PrompterException {
        return getValue(propertyKey, defaultValue);
    }

    @Override
    public boolean confirmConfiguration(ArchetypeConfiguration archetypeConfiguration) throws PrompterException {
        StringBuilder query =
                new StringBuilder("Confirm archetype configuration:\n" + Constants.ARCHETYPE_GROUP_ID + "="
                        + archetypeConfiguration.getGroupId() + "\n" + Constants.ARCHETYPE_ARTIFACT_ID + "="
                        + archetypeConfiguration.getArtifactId() + "\n" + Constants.ARCHETYPE_VERSION + "="
                        + archetypeConfiguration.getVersion() + "\n");

        for (Iterator<?> propertiesIter =
                        archetypeConfiguration.getProperties().keySet().iterator();
                propertiesIter.hasNext(); ) {
            String property = (String) propertiesIter.next();
            query.append(property + "=" + archetypeConfiguration.getProperty(property) + "\n");
        }

        String answer = prompter.prompt(query.toString(), "Y");

        return "Y".equalsIgnoreCase(answer);
    }

    @Override
    public String getGroupId(String defaultValue) throws PrompterException {
        return getValue(Constants.GROUP_ID, defaultValue);
    }

    @Override
    public String getPackage(String defaultValue) throws PrompterException {
        return getValue(Constants.PACKAGE, defaultValue);
    }

    @Override
    public String getVersion(String defaultValue) throws PrompterException {
        return getValue(Constants.VERSION, defaultValue);
    }

    private String getValue(String requiredProperty, String defaultValue) throws PrompterException {
        String query = "Define value for " + requiredProperty + ": ";
        String answer;

        if ((defaultValue != null) && !defaultValue.equals("null")) {
            answer = prompter.prompt(query, defaultValue);
        } else {
            answer = prompter.prompt(query);
        }
        return answer;
    }
}
