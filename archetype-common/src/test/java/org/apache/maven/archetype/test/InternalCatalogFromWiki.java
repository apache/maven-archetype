/*
 *  Copyright 2008 rafale.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.maven.archetype.test;

import java.io.StringWriter;
import java.util.Properties;
import org.apache.maven.archetype.catalog.ArchetypeCatalog;
import org.apache.maven.archetype.catalog.io.xpp3.ArchetypeCatalogXpp3Writer;
import org.apache.maven.archetype.source.ArchetypeDataSource;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 * @author rafale
 */
public class InternalCatalogFromWiki
    extends PlexusTestCase {

    public void testInternalCatalog ()
    throws Exception
    {
        Properties properties = new Properties();
        
//        ArchetypeDataSource ads =  (ArchetypeDataSource) lookup( ArchetypeDataSource.ROLE, "wiki" );
        ArchetypeDataSource ads =  new WikiArchetypeDataSource();
        
        ArchetypeCatalog ac=ads.getArchetypeCatalog(properties);
        
        System.out.println("AR="+ac.getArchetypes());
        
        StringWriter sw=new StringWriter();
        
        ArchetypeCatalogXpp3Writer acxw=new ArchetypeCatalogXpp3Writer();
        acxw.write(sw, ac);
        
        System.out.println("AC="+sw.toString());
    }
}
