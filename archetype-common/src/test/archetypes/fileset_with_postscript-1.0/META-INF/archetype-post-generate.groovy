println "Executing the archetype-post-generate.groovy script...";

def outputDirectory = new File( request.getOutputDirectory() );

def packageFolder = request.getPackage().replace( '.', '/' );

def toDelete = new File( outputDirectory, request.getArtifactId() + "/src/main/java/${packageFolder}/ToDelete.java" );

println "Removing file: " + toDelete;
assert toDelete.delete();
