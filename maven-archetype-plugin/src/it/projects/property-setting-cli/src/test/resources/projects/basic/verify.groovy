 

assert new File(context.projectDir, 'pom.xml').exists();
content = new File(context.projectDir, 'pom.xml').text;
assert content.contains( '<foo>bar</foo>' );