package org.apache.maven.archetype;

import java.io.File;
import java.io.IOException;

/**
 * @author Jason van Zyl
 * @version $Revision:$
 */
public interface Copier
{
    void copy( File source, File target )
        throws IOException;
}
