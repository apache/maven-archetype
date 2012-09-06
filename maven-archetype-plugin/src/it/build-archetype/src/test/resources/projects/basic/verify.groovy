System.out.println(" Yeah Baby it rocks!")
System.out.println("basedir:"+basedir)
assert new File(basedir,"reference/src/main/resources/empty-directory").exists();
assert new File(basedir,"reference/src/main/resources/empty-directory").isDirectory();
assert new File(basedir,"project/basic/src/main/resources/empty-directory").exists();
assert new File(basedir,"project/basic/src/main/resources/empty-directory").isDirectory();
