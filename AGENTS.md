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

# Agent Guide for maven-archetype

This file is read by automated agents (security scanners, code
analyzers, AI assistants) operating on this repository.

## Security

Security model: [SECURITY.md](./SECURITY.md)

Agents that scan this repository should consult `SECURITY.md` and the
threat model it links before reporting issues.

Points at the Apache Maven family umbrella security model.

## Building and testing

The Java floor is 8 (`javaVersion` 8 from maven-parent, compiled with `--release 8`) and the plugin
requires Maven 3.9.0. Use no Java 9+ APIs in main or test code.

Run a single plugin integration test from the repository root. The ITs are off by default and
start with the `run-its` profile; `-DskipTests` skips the unit tests, and `-am` builds the modules
the plugin depends on first:

```
mvn -B -pl maven-archetype-plugin -am verify -P run-its -DskipTests -Dinvoker.test=create-1
```

`-Dinvoker.test` takes a comma-separated list of directories under `maven-archetype-plugin/src/it/projects`.
The log of each one is in `maven-archetype-plugin/target/it/logs/<name>/build.log`.

The unit tests of `archetype-common` read their archetypes from a repository layout that the build
creates in `process-test-resources`. The `maven-antrun-plugin` executions in `archetype-common/pom.xml`
jar each directory of `archetype-common/src/test/archetypes` (`basic-1.0`, `partial-1.0`, `site-1.0`,
`old-1.0`, `fileset-1.0`, `fileset_with_postscript-1.0`) and copy the jar, together with its
`META-INF/maven/archetypes/<name>/pom.xml`, to
`target/test-classes/repositories/central/archetypes/<name>/<version>/`. Edit the directory under
`src/test/archetypes`, never the jar under `target`.

`InternalCatalogArchetypesVerificationTest` generates a project from every archetype in the internal
catalog and downloads them from Maven Central, so it fails offline.
