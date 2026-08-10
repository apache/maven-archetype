<?xml version="1.0"?>

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

<faqs id="FAQ" title="Frequently Asked Questions">

  <part id="General">

    <faq id="packaging">
      <question>What packaging should I use: <code>maven-archetype</code> or <code>jar</code>? What is the difference?</question>
      <answer>
        <p><code>maven-archetype</code> packaging is available since 2.0-alpha-1: it should be used instead of <code>jar</code>, which was used
        for Old Archetype 1.0.x.</p>
        <p>Using <code>maven-archetype</code> packaging helps identifying archetypes in repositories and adds archetype related bindings to
        build lifecycle: see <a href="../archetype-packaging/"><code>maven-archetype</code> packaging documentation</a>
        for more information.</p>
      </answer>
    </faq>

    <faq id="authentication">
      <question>How to generate a project from an archetype in a custom (potentially authenticated) repository?</question>
      <answer>
        <p>The server/repository id used to download the <strong>catalog</strong> is <code>archetype</code>: You have to
        <a href="/settings.html#Servers">define corresponding server configuration and repository in
        <code>settings.xml</code></a> with this id to generate a project based on the catalog
        In case of <a href="/guides/mini/guide-mirror-settings.html">mirroring</a> the mirror id of that mirror which
        matches repository id <code>archetype</code> or <code>central</code> is used for authentication.
        For resolving the <strong>actual archetype</strong> an artificial repository with id <code>&lt;archetype-artifactId&gt;-repo</code> is used in case
        the underlying <a href="/archetype/archetype-models/archetype-catalog/archetype-catalog.html">Archetype Catalog Item</a> contained a repository (leveraging its URL).
        As fallback all regular remote repositories (as defined in the <code>settings.xml</code>) are tried.</p>
      </answer>
    </faq>

    <faq id="old">
      <question>What is "Old Archetype 1.0.x"?</question>
      <answer>
        <p>Old Archetype 1.0.x was using a <code>archetype</code> XML descriptor defined in <code>/xsd/archetype-1.0.0.xsd</code>:
        see <a href="/archetype/archetype-common/archetype.html">old descriptor reference</a>.</p>
        <p>Archetype is now using <code>archetype-descriptor</code> XML descriptor defined in <code>/xsd/archetype-descriptor-1.1.0.xsd</code>:
        see <a href="/archetype/archetype-models/archetype-descriptor/archetype-descriptor.html">Archetype descriptor reference</a>.</p>
        <p>For compatibility, old archetypes can still be used to generate new projects, but archetypes should be updated to the new
        descriptor format: in the future, old format will be removed.</p>
      </answer>
    </faq>

    <faq id="excludes">
      <question>How do I get files like <code>.gitignore</code> included into my archetype?</question>
      <answer>
        <p>First make sure you set <code>addDefaultExcludes</code> to <code>false</code> in the <code>maven-resources-plugin</code>
        so that the archetype-plugin gets to see these files.</p>
        <p>Then set <code>useDefaultExcludes</code> to <code>false</code> in the <code>maven-archetype-plugin</code>.</p>
      </answer>
    </faq>
  </part>

</faqs>
