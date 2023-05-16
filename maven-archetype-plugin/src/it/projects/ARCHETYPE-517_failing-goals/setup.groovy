

// We can't run "mvn clean" as there is no pom. So we need to remove any already
// created project before executing.
directory = new File( basedir, "project" )
directory.deleteDir()
