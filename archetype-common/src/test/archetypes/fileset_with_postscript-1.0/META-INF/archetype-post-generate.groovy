println "Executing the archetype-post-generate.groovy script...";

def outputDirectory = new File( request.getOutputDirectory() );

// TODO: file-value and file/value/package should be calculated
def toDelete = new File( outputDirectory, "file-value/src/main/java/file/value/package/ToDelete.java" );

println "Removing file: " + toDelete;
assert toDelete.delete();
