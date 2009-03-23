/*
 *  Copyright 2009 raphaelpieroni.
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

package org.apache.maven.archetype.util
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy
import org.codehaus.plexus.logging.AbstractLogEnabled
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
import org.apache.maven.artifact.repository.DefaultArtifactRepository

/**
 *
 * @author raphaelpieroni
 * @plexus.component
 */
class DefaultRepositoryCreator 
extends AbstractLogEnabled
implements RepositoryCreator{

    /** @plexus.requirement */
    private ArtifactRepositoryFactory artifactRepositoryFactory

    /** @plexus.requirement roleHint="default" */
    private ArtifactRepositoryLayout defaultArtifactRepositoryLayout
    
	    def createRepository( String repository ) {
        String updatePolicyFlag = ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS
        String checksumPolicyFlag = ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN
        return artifactRepositoryFactory.createArtifactRepository(
                "archetype-repository",
                repository,
                defaultArtifactRepositoryLayout,
                new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag ),
                new ArtifactRepositoryPolicy( true, updatePolicyFlag, checksumPolicyFlag )
            )
    }
}

