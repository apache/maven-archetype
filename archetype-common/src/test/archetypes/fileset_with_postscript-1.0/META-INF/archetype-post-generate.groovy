println "Executing the post_create script...";

def projectFolder = this.'user.dir' + this.'file.separator' + "target/test-classes/projects" + this.'file.separator' + "generate-13" + this.'file.separator' + "file-value" + this.'file.separator';

println "Removing file: ${projectFolder}src/main/java/file/value/package/ToDelete.java";
new File(projectFolder + "src/main/java/file/value/package/ToDelete.java").delete();
