---
title: Generate project using an alternative catalog
author: 
  - Raphaël Piéroni
date: 2010-09-11
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

# Generate project using an alternative catalog

You can choose the catalogs that `archetype:generate` lists by setting the `archetypeCatalog` property to a comma-separated list of the following values:

- `remote` to use the `archetype-catalog.xml` that Maven Central publishes, or the one from a repository with the id `archetype`. If the remote catalog is empty or cannot be retrieved, the plugin uses the internal catalog instead.
- `local` to use the `archetype-catalog.xml` in your local repository, which `archetype:create-from-project` updates.
- `internal` to use the catalog bundled with the plugin.

The default value is `remote,local`, so the local catalog is listed just after the remote one. Other values are rejected.

To list only the archetypes of your local repository, run the following command:

```shell
mvn archetype:generate -DarchetypeCatalog=local
```
