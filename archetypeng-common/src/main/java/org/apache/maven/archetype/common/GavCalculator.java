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

package org.apache.maven.archetype.common;

/**
 * @author Jason van Zyl
 * @author cstamas
 */
public class GavCalculator
{
//    public static Gav calculate( String s )
//    {
//        int n1 = s.lastIndexOf( '/' );
//
//        if ( n1 == -1 )
//        {
//            return null;
//        }
//
//        int n2 = s.lastIndexOf( '/', n1 - 1 );
//
//        if ( n2 == -1 )
//        {
//            return null;
//        }
//
//        int n3 = s.lastIndexOf( '/', n2 - 1 );
//
//        if ( n3 == -1 )
//        {
//            return null;
//        }
//
//        String ext = s.substring( s.lastIndexOf( '.' ) + 1 );
//        String g = s.substring( 0, n3 ).replace( '/', '.' );
//        String a = s.substring( n3 + 1, n2 );
//        String v = s.substring( n2 + 1, n1 );
//        String n = s.substring( n1 + 1 );
//        boolean primary = n.equals( a + "-" + v + "." + ext );
//        String c = null;
//        if (!primary) {
//            c = s.substring( n1 + a.length() + v.length() + 3, s.lastIndexOf( '.' ) );
//        }
//
//        return new Gav( g, a, v, c, n, primary );
//    }
//
//    public static class Gav
//    {
//        private String groupId;
//
//        private String artifactId;
//
//        private String version;
//
//        private String classifier;
//
//        private String name;
//
//        private boolean primary;
//
//        public Gav( String groupId, String artifactId, String version, String classifier, String name, boolean primary )
//        {
//            this.groupId = groupId;
//            this.artifactId = artifactId;
//            this.version = version;
//            this.classifier = classifier;
//            this.name = name;
//            this.primary = primary;
//        }
//
//        public String getGroupId()
//        {
//            return groupId;
//        }
//
//        public String getArtifactId()
//        {
//            return artifactId;
//        }
//
//        public String getVersion()
//        {
//            return version;
//        }
//
//        public String getClassifier()
//        {
//            return classifier;
//        }
//
//        public String getName()
//        {
//            return name;
//        }
//
//        public boolean isPrimary()
//        {
//            return primary;
//        }
//
//    }
}
