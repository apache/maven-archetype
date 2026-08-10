---
title: Frequently Asked Questions
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

<a id="top"></a>

# Frequently Asked Questions

1. [What packaging should I use: `maven-archetype` or `jar`? What is the difference?](#packaging)
1. [How to generate a project from an archetype in a custom (potentially authenticated) repository?](#authentication)
1. [What is &quot;Old Archetype 1.0.x&quot;?](#old)
1. [How do I get files like `.gitignore` included into my archetype?](#excludes)

<a id="packaging"></a>

### What packaging should I use: `maven-archetype` or `jar`? What is the difference?

`maven-archetype` packaging is available since 2.0-alpha-1: it should be used instead
of `jar`, which was used for Old Archetype 1.0.x.

Using `maven-archetype` packaging helps identifying archetypes in repositories and
adds archetype related bindings to build lifecycle: see
[`maven-archetype` packaging documentation](../archetype-packaging/) for more
information.

<a id="authentication"></a>

### How to generate a project from an archetype in a custom (potentially authenticated) repository?

The server/repository id used to download the **catalog** is `archetype`: You have to
[define corresponding server configuration and repository in `settings.xml`](/settings.html#Servers)
with this id to generate a project based on the catalog In case of
[mirroring](/guides/mini/guide-mirror-settings.html) the mirror id of that mirror
which matches repository id `archetype` or `central` is used for authentication.
For resolving the **actual archetype** an artificial repository with id
`<archetype-artifactId>-repo` is used in case the underlying
[Archetype Catalog Item](/archetype/archetype-models/archetype-catalog/archetype-catalog.html)
contained a repository (leveraging its URL). As fallback all regular remote
repositories (as defined in the `settings.xml`) are tried.

<a id="old"></a>

### What is &quot;Old Archetype 1.0.x&quot;?

Old Archetype 1.0.x was using a `archetype` XML descriptor defined in
`/xsd/archetype-1.0.0.xsd`: see
[old descriptor reference](/archetype/archetype-common/archetype.html).

Archetype is now using `archetype-descriptor` XML descriptor defined in
`/xsd/archetype-descriptor-1.1.0.xsd`: see
[Archetype descriptor reference](/archetype/archetype-models/archetype-descriptor/archetype-descriptor.html).

For compatibility, old archetypes can still be used to generate new projects, but
archetypes should be updated to the new descriptor format: in the future, old format
will be removed.

<a id="excludes"></a>

### How do I get files like `.gitignore` included into my archetype?

First make sure you set `addDefaultExcludes` to `false` in the
`maven-resources-plugin` so that the archetype-plugin gets to see these files.

Then set `useDefaultExcludes` to `false` in the `maven-archetype-plugin`.
