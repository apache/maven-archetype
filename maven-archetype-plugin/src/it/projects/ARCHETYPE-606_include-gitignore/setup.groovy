 

// This is a workaround. The invoker-plugin does not copy the .gitignore file to the clone
// target directory. So we create the .gitignore here in the setup task.

import java.io.*;
import org.codehaus.plexus.util.*;

srcGitignore = new File ( basedir, "src/main/resources/archetype-resources/.gitignore" );
targetGitignore = new File ( basedir, "src/test/resources/projects/basic/reference/.gitignore" );

FileUtils.fileWrite(srcGitignore, "UTF-8", "#Dummy");
FileUtils.fileWrite(targetGitignore, "UTF-8", "#Dummy");