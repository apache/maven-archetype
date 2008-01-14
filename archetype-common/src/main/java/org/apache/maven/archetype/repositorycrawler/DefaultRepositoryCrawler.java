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

package org.apache.maven.archetype.repositorycrawler;

import org.apache.commons.io.FileUtils;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.common.ArchetypeArtifactManager;
import org.apache.maven.archetype.exception.UnknownArchetype;
import org.apache.maven.model.Model;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;

/**
 * @author            rafale
 * @plexus.component
 */
public class DefaultRepositoryCrawler
extends AbstractLogEnabled
implements RepositoryCrawler
{
    /**
     * @plexus.requirement
     */
    private ArchetypeArtifactManager archetypeArtifactManager;

    public ArchetypeCatalog crawl ( File repository )
    {
        if ( repository.isDirectory () )
        {
            ArchetypeCatalog catalog = new ArchetypeCatalog ();
            Iterator jars =
                FileUtils.listFiles ( repository, new String[] { "jar" }, true ).iterator ();

            while ( jars.hasNext () )
            {
                File jar = (File) jars.next ();
                getLogger ().info ( "Scanning " + jar );
                if ( archetypeArtifactManager.isFileSetArchetype ( jar )
                    || archetypeArtifactManager.isOldArchetype ( jar )
                )
                {
                    try
                    {
                        Archetype archetype = new Archetype ();

                        Model pom = archetypeArtifactManager.getArchetypePom ( jar );

                        if ( archetypeArtifactManager.isFileSetArchetype ( jar ) )
                        {
                            org.apache.maven.archetype.metadata.ArchetypeDescriptor descriptor =
                                archetypeArtifactManager.getFileSetArchetypeDescriptor ( jar );
                            archetype.setDescription ( descriptor.getName () );
                        }
                        else
                        {
                            org.apache.maven.archetype.old.descriptor.ArchetypeDescriptor descriptor =
                                archetypeArtifactManager.getOldArchetypeDescriptor ( jar );
                            archetype.setDescription ( descriptor.getId () );
                        }
                        if ( pom != null )
                        {
                            if ( pom.getGroupId () != null )
                            {
                                archetype.setGroupId ( pom.getGroupId () );
                            }
                            else
                            {
                                archetype.setGroupId ( pom.getParent ().getGroupId () );
                            }
                            archetype.setArtifactId ( pom.getArtifactId () );
                            if ( pom.getVersion () != null )
                            {
                                archetype.setVersion ( pom.getVersion () );
                            }
                            else
                            {
                                archetype.setVersion ( pom.getParent ().getVersion () );
                            }
                            getLogger ().info ( "\tArchetype " + archetype + " found in pom" );
                        }
                        else
                        {
                            String version = jar.getParentFile ().getName ();
                            
                            String artifactId = jar.getParentFile ().getParentFile ().getName ();
                            
                            String groupIdSep =
                                jar.getParentFile ().getParentFile ().getParentFile ().getPath ()
                                .replace ( repository.getPath (), "" );
                            String groupId = groupIdSep.replace ( File.separator, "." );
                            groupId =
                                groupId.startsWith ( "." ) ? groupId.substring ( 1 ) : groupId;
                            groupId =
                                groupId.endsWith ( "." )
                                ? groupId.substring ( 0, groupId.length () - 1 )
                                : groupId;
                            
                            archetype.setGroupId ( groupId );
                            archetype.setArtifactId ( artifactId );
                            archetype.setVersion ( version );
                            
                            getLogger ().info (
                                "\tArchetype " + archetype + " defined by repository path"
                            );
                        } // end if-else

                        catalog.addArchetype ( archetype );
                    }
                    catch ( XmlPullParserException ex )
                    {
                        ex.printStackTrace ();
                    }
                    catch ( IOException ex )
                    {
                        ex.printStackTrace ();
                    }
                    catch ( UnknownArchetype ex )
                    {
                        ex.printStackTrace ();
                    } // end try-catch
                } // end if
            } // end while
            return catalog;
        }
        else
        {
            getLogger ().warn ( "File is not a directory" );
            return null;
        } // end if-else
    }

    public boolean writeCatalog ( ArchetypeCatalog archetypeCatalog, File archetypeCatalogFile )
    {
        try
        {
            ArchetypeCatalogXpp3Writer catalogWriter = new ArchetypeCatalogXpp3Writer ();

            catalogWriter.write ( new FileWriter ( archetypeCatalogFile ), archetypeCatalog );
            return true;
        }
        catch ( IOException ex )
        {
            getLogger ().warn ( "Catalog can not be writen to " + archetypeCatalogFile, ex );
            return false;
        }
    }
}
