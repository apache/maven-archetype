package org.apache.maven.archetype.ui.generation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.archetype.ui.ArchetypeConfiguration;

/**
 * Class for ensuring that Required Properties are sorted in the right order. 
 * 
 * Ideally archetype declarations will have their required properties
 * in an order that means that any properties required for a default value
 * will have already been prompted for. If this is the case then the order
 * of the properties will not be altered. 
 * 
 * However, if this detects that the properties are not in the correct order,
 * this class will attempt to sort them. 
 */
class RequiredPropertySorter
{

    List<String> sortRequiredPropertyKeys( final ArchetypeConfiguration archetypeConfiguration )
    {

        /* Get the required properties and convert into an object structure holding the default values and dependencies */
        final List<RequiredProperty> requiredPropertyObjects = createRequiredPropertyObjects( archetypeConfiguration );

        /* Check if the keys are in the right order */
        if ( !areRightOrder( requiredPropertyObjects ) )
        {

            /* Keys are not in a compatible order, perform the sort and return */
            return sort( requiredPropertyObjects );
        } 
        else
        {
            /* Return the original list of keys */
            return archetypeConfiguration.getRequiredProperties();
        }
    }

    /**
     * Helper method to determine if the supplied objects are in an order where the dependencies relate
     * to property keys earlier in the list. 
     * @param requiredPropertyObjects the list of objects to check. 
     * @return true if they are in the correct order, false if they require to be sorted. 
     */
    private boolean areRightOrder( final List<RequiredProperty> requiredPropertyObjects ) 
    {

        /* Initially start as the items being correct */
        boolean rightOrder = true;

        /* Walk through the items checking */
        final List<String> seenKeys = new ArrayList<>();
        for ( RequiredProperty property : requiredPropertyObjects )
        {
            for ( String dependency : property.dependsOnProperties ) 
            {
                if ( !seenKeys.contains( dependency ) )
                {
                    /* If the dependency is not in the keys we have seen so far, the
                     * keys are in the wrong order
                     */
                    rightOrder = false;
                }
            }

            seenKeys.add( property.propertyKey );
        }

        return rightOrder;
    }

    private List<String> sort( final List<RequiredProperty> requiredPropertyObjects ) 
    {

        final Graph graph = new Graph( requiredPropertyObjects );
        return graph.topologicalSort();
    }

    /**
     * Extract out the required properties and default values to create
     * a list of objects containing the property keys and they properties
     * they depend on. 
     * 
     * @param archetypeConfiguration the archetype configuration to parse
     * @return list of created objects. 
     */
    private List<RequiredProperty> createRequiredPropertyObjects(
        final ArchetypeConfiguration archetypeConfiguration )
    {

        final List<RequiredProperty> list = new ArrayList<>();
        for ( String key : archetypeConfiguration.getRequiredProperties() )
        {
            list.add( new RequiredProperty( key, archetypeConfiguration.getDefaultValue( key ) ) );
        }

        return list;
    }


    /**
     * Object holding a required property and any other required properties
     * it depends on. 
     */
    private class RequiredProperty
    {

        private final String propertyKey;
        private final List<String> dependsOnProperties;

        /**
         * Create a new RequiredProperty. 
         * @param propertyKey the property key
         * @param defaultValue the default value for the property. 
         */
        private RequiredProperty ( final String propertyKey, final String defaultValue )
        {

            this.propertyKey = propertyKey;

            /* Get out the dependency keys */
            List<String> dependencyKeys = new ArrayList<>();
            if ( defaultValue != null && !defaultValue.isEmpty() )
            {
                Pattern regex = Pattern.compile( "\\$\\{(.*?)\\}" );
                Matcher regexMatcher = regex.matcher( defaultValue );

                while ( regexMatcher.find() ) 
                {
                    dependencyKeys.add( regexMatcher.group( 1 ) );
                }
            }
            this.dependsOnProperties = Collections.unmodifiableList( dependencyKeys );
        }

    }

    /**
     * Graph class to allow us to model the dependency graph and perform a topological sort. 
     */
    private class Graph
    {
    
        /**
         * Map of all the nodes.
         * Key is the property name, value is the property definition. 
         */
        private final Map<String, RequiredProperty> nodes;

        /**
         * List holding the keys for the nodes which have already been visited, 
         * to prevent cycles. 
         */
        private final List<String> nodeVisited;
        
        /**
         * Create a new Graph with the supplied nodes
         */
        private Graph( final List<RequiredProperty> requiredProperties )
        {
            this.nodes = new LinkedHashMap<>();
            this.nodeVisited = new ArrayList<>();

            for ( RequiredProperty property : requiredProperties ) 
            {
                this.nodes.put( property.propertyKey, property );
            }
        }
        
        /**
         * Perform a topological sort on this graph. 
         * @return the sorted property keys. 
         */
        public List<String> topologicalSort()
        {
            
            /* Reset the visited state in case we are called a second time 
            * for the same object. 
            */
            this.nodeVisited.clear();

            /* Setup our stack. 
            * When traversing through nodes we will add to this using the
            * Stack equivalent methods. 
            * For the return, we use the Queue approach to return due to the 
            * way we structure our graph. 
            */
            final Deque<String> stack = new ArrayDeque<>();
            
            /* iterate through all the property nodes and their neighbours (dependencies),
            * if not already visited. 
            */
            for ( String propertyKey : nodes.keySet() )
            {
                if ( !nodeVisited.contains( propertyKey ) )
                {
                    sort( propertyKey, stack );
                }
            }

            /* Return all the elements in the stack in reverse order */
            final List<String> sortedKeys = new ArrayList<>();
            while ( !stack.isEmpty() )
            {
                sortedKeys.add( stack.pollLast() );
            }

            return sortedKeys;
        }
        
        /**
         * Recusively iterate through all the property nodes and their neighbours
         * (dependencies). 
         * This will update the stack and the list of visited nodes as it goes.
         *  
         * @param propertyKey the current property node we are visiting
         * @param stack the stack to update with progress. 
         */
        private void sort( final String propertyKey, final Deque<String> stack )
        {
            /* add the visited node to list, so we don't repeat this node again */
            nodeVisited.add( propertyKey );

            /* Get the node detail from the map */
            final RequiredProperty node = this.nodes.get( propertyKey );
            
            /* This is generally expected to always be true, this would only be
            * null if we had a dependency on a required property that wasn't 
            * defined.
            */
            if ( node != null )
            {
                
                /* if an edge exists for the node, then visit that neighbor node, 
                * unless we have already visited it
                */
                for ( String dependency : node.dependsOnProperties )
                {
                    if ( !nodeVisited.contains( dependency ) )
                    {
                        sort( dependency, stack );
                    }
                }
            }

            /* push this latest node on to the stack */
            stack.push( propertyKey );
        }

    }
    
}
