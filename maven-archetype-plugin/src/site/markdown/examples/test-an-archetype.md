---
title: Test an archetype
date: 2026-10-31
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# Test an archetype

The `archetype:integration-test` goal checks that an archetype still generates the project you expect. It generates one project per test from the archetype you just built, optionally compares the result with a reference copy, runs Maven goals in the generated project, and runs a verification script. The [`maven-archetype` packaging](../../archetype-packaging/) binds the goal to the `integration-test` phase, so `mvn verify` runs it after `archetype:jar`. Running `mvn archetype:integration-test` on its own fails, because the archetype jar does not exist yet.

When you create an archetype with `archetype:create-from-project`, the plugin adds a first test named `basic` for you.

## Lay out a test

Each test is a directory under `src/test/resources/projects/` of the archetype project. The directory name is only the name of the test:

```text
src/test/resources/projects/basic
├── archetype.properties
├── goal.txt
├── reference
│   ├── .mvn
│   │   ├── jvm.config
│   │   └── maven.config
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── it
│       │           └── pkg
│       │               └── App.java
│       └── test
│           └── java
│               └── it
│                   └── pkg
│                       └── AppTest.java
└── verify.groovy
```

The goal finds tests by looking for `goal.txt` files, so every test needs one. The following table lists the files of a test.

| File | Required | Purpose |
| --- | --- | --- |
| `archetype.properties` | yes | The values for the generation, such as `groupId`, `artifactId`, `version` and `package`, plus every property that the archetype requires. |
| `goal.txt` | yes | The Maven goals to run in the generated project, separated by commas, for example `verify`. The file can be empty to skip the build. |
| `reference/` | no | The project that the generation must produce. |
| `verify.groovy` | no | A Groovy script that runs after the build and checks the result. |

The generated `archetype.properties` of the `basic` test looks like this:

```properties
artifactId=basic
groupId=archetype.it
package=it.pkg
version=0.1-SNAPSHOT
```

If a required property is missing, the build fails with `Missing required properties in archetype.properties` and the names of the missing properties.

## Compare with a reference project

If a test has a `reference/` directory, the goal compares it with the generated project before it runs any goals. The comparison is strict:

- Every file and directory of the generated project needs a counterpart in `reference/`, and the other way round.
- The contents must be byte-for-byte equal. By default the line endings count too. Set the `ignoreEOLStyle` parameter of the goal to `true` (property `archetype.test.ignoreEOLStyle`) to ignore them.

To create the reference, generate the project once without a reference, copy the result from `target/test-classes/projects/<name>/project/<artifactId>` into `reference/`, and delete the `build.log` file and the `target` directory that the build of the generated project leaves there. Review the copy by hand before you commit it.

If the trees differ, the build fails and lists the differences. In the following example, the `src/test` directory is missing from the reference:

```text
[ERROR] Remains [src/test/java/it/pkg/AppTest.java, src/test, src/test/java, src/test/java/it, src/test/java/it/pkg]
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-archetype-plugin:3.4.1:integration-test (default-integration-test) on project project-archetype:
[ERROR] Archetype IT 'basic' failed: Reference and generated project differs (missing: 0, unexpected: 5)
```

## Verify the result with a script

The goal runs `verify.groovy` after the build of the generated project, or directly after the generation if `goal.txt` is empty. The script sees the variable `context`, and `context.projectDir` is the generated project. The test fails if the script throws an exception or returns a value other than `true` or `null`. For example:

```groovy
def pom = new File(context.projectDir, 'pom.xml')
assert pom.text.contains('<artifactId>basic</artifactId>')
assert new File(context.projectDir, 'target/classes/it/pkg/App.class').exists()
```

A failed assertion fails the build:

```text
[ERROR] Archetype IT 'basic' failed: post build script failure failure: Assertion failed:
[ERROR] assert pom.text.contains('<artifactId>basicX</artifactId>')
```

## Run the tests

Run the tests with the lifecycle of the archetype project:

```shell
$ mvn verify
[INFO] --- archetype:3.4.1:integration-test (default-integration-test) @ project-archetype ---
[INFO] Processing Archetype IT project: basic
[INFO] Using following parameters for creating project from Archetype: project-archetype:1.0
[INFO] Project created from Archetype in dir: /home/user/project-archetype/target/test-classes/projects/basic/project/basic
[INFO] Comparing generated project with reference content: /home/user/project-archetype/target/test-classes/projects/basic/reference
[INFO] Invoking post-archetype-generation goals: verify
[INFO] Post-archetype-generation invoker exit code: 0
[INFO] BUILD SUCCESS
```

The build log of the generated project is in `target/test-classes/projects/<name>/project/<artifactId>/build.log`. To skip the tests, set the property `archetype.test.skip` to `true`.

## Test an archetype that needs a parent project

To generate the project below another project, add an `archetype.pom.properties` file to the parent directory of the test, with the `groupId`, `artifactId` and `version` of the archetype to generate the parent from, and an `archetype.properties` file next to it with the properties for that parent. The [goal documentation](../integration-test-mojo.html) describes the layout.
