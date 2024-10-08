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
package org.apache.maven.archetype.ui;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.components.interactivity.InputHandler;
import org.codehaus.plexus.components.interactivity.OutputHandler;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

/**
 * @author raphaelpieroni
 */
@Named("archetype")
@Singleton
public class ArchetypePrompter implements Prompter {

    @Inject
    private OutputHandler outputHandler;

    @Inject
    private InputHandler inputHandler;

    @Override
    public String prompt(String message) throws PrompterException {
        writePrompt(message);

        return readLine();
    }

    @Override
    public String prompt(String message, String defaultReply) throws PrompterException {
        writePrompt(formatMessage(message, null, defaultReply));

        String line = readLine();

        if (line == null || line.isEmpty()) {
            line = defaultReply;
        }

        return line;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String prompt(String message, List possibleValues, String defaultReply) throws PrompterException {
        String formattedMessage = formatMessage(message, possibleValues, defaultReply);

        String line;

        do {
            writePrompt(formattedMessage);

            line = readLine();

            if (line == null || line.isEmpty()) {
                line = defaultReply;
            }

            if (line != null && !possibleValues.contains(line)) {
                try {
                    outputHandler.writeLine("Invalid selection.");
                } catch (IOException e) {
                    throw new PrompterException("Failed to present feedback", e);
                }
            }
        } while (line == null || !possibleValues.contains(line));

        return line;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String prompt(String message, List possibleValues) throws PrompterException {
        return prompt(message, possibleValues, null);
    }

    @Override
    public String promptForPassword(String message) throws PrompterException {
        writePrompt(message);

        try {
            return inputHandler.readPassword();
        } catch (IOException e) {
            throw new PrompterException("Failed to read user response", e);
        }
    }

    private String formatMessage(String message, List<String> possibleValues, String defaultReply) {
        StringBuilder formatted = new StringBuilder(message.length() * 2);

        formatted.append(message);
        /*
         * if ( possibleValues != null && !possibleValues.isEmpty() ) { formatted.append( " (" ); for ( Iterator it =
         * possibleValues.iterator(); it.hasNext(); ) { String possibleValue = (String) it.next(); formatted.append(
         * possibleValue ); if ( it.hasNext() ) { formatted.append( '/' ); } } formatted.append( ')' ); }
         */

        if (defaultReply != null) {
            formatted.append(defaultReply);
        }

        return formatted.toString();
    }

    private void writePrompt(String message) throws PrompterException {
        showMessage(message + ": ");
    }

    private String readLine() throws PrompterException {
        try {
            return inputHandler.readLine();
        } catch (IOException e) {
            throw new PrompterException("Failed to read user response", e);
        }
    }

    @Override
    public void showMessage(String message) throws PrompterException {
        try {
            outputHandler.write(message);
        } catch (IOException e) {
            throw new PrompterException("Failed to show message", e);
        }
    }
}
