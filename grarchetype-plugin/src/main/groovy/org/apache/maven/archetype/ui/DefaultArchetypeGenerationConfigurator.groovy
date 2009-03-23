package org.apache.maven.archetype.ui
import org.codehaus.plexus.logging.AbstractLogEnabled
import org.apache.maven.archetype.artifact.ArchetypeArtifactManager
import org.codehaus.plexus.components.interactivity.Prompter
import org.codehaus.plexus.util.StringUtils

/**
 *
 * @author raphaelpieroni
 * @plexus.component
 */
class DefaultArchetypeGenerationConfigurator
extends AbstractLogEnabled
implements ArchetypeGenerationConfigurator {

    /** @plexus.requirement */
    private ArchetypeArtifactManager artifactManager

    /** @plexus.requirement */
    private Prompter prompter

    void configureArchetype( request, interactiveMode, executionProperties ) {
        def requiredProperties = loadRequiredProperties( request )
        applyIncomingProperties requiredProperties, executionProperties
//        applyDefaultValues requiredProperties

        if( !interactiveMode ) {
            if( !configured( requiredProperties ) ) {
                describeMissingProperties requiredProperties
                throw new Exception( "There is some missing properties" )
            }
        } else {
            def confirmed = false
            while( !confirmed ){
                if( !configured( requiredProperties ) ) {
                    askForUnconfiguredProperties requiredProperties
                }
                if( askForConfirmation( request, requiredProperties  ) ) {
                    confirmed = true
                } else {
                    resetConfiguredValues requiredProperties
                    applyIncomingProperties requiredProperties, executionProperties
                }
            }
        }
        applyProperties requiredProperties, request
    }
    def loadRequiredProperties( request ) {
        def unsorted = [:]
        def compareProperty = { left, right ->
            def leftDefault = unsorted[left]?.defaultValue
            def rightDefault = unsorted[right]?.defaultValue
            if( leftDefault?.indexOf( "\${$right}" ) ) return 1
            if( rightDefault?.indexOf( "\${$left}" ) ) return -1
            return comparePropertyName( left, right )
        }
        def requiredProperties = new TreeMap([compare:compareProperty] as Comparator)
        def archetypeFileMap = artifactManager.getNestedArchetypeFiles(
            [groupId:request.groupId, artifactId:request.artifactId, version:request.version],
            request.localRepository, request.repositories )
        archetypeFileMap.each { key, archetypeFile ->
            if( !artifactManager.isLegacyArchetype( archetypeFile ) ) {
                def descriptor = new XmlSlurper().parse( artifactManager.getFilesetArchetypeDescriptor( archetypeFile ) )
                descriptor.requiredProperties.requiredProperty.each {
                    def requiredProperty = [name:it.@key.toString(), archetype:key]
                    if( it.defaultValue.text().toString() ) requiredProperty.defaultValue = it.defaultValue.text().toString()
                    if( requiredProperty.defaultValue ) requiredProperty.value = requiredProperty.defaultValue
                    else requiredProperty.defaultValue = null
                    if( it.description.text().toString() ) {
                        requiredProperty.description = it.description.text().toString()
                    } else {
                        requiredProperty.description = requiredProperty.name
                    }
                    unsorted[requiredProperty.name] = requiredProperty
                    requiredProperties[requiredProperty.name] = requiredProperty
                }
            }
        }
        if( !requiredProperties.containsKey( "groupId" ) ) {
            def requiredProperty = [name:"groupId", description:"", archetype:request, defaultValue:null]
            unsorted[requiredProperty.name] = requiredProperty
            requiredProperties[requiredProperty.name] = requiredProperty
        }
        if( !requiredProperties.containsKey( "artfactId" ) ) {
            def requiredProperty = [name:"artifactId", description:"", archetype:request, defaultValue:null]
            unsorted[requiredProperty.name] = requiredProperty
            requiredProperties[requiredProperty.name] = requiredProperty
        }
        if( !requiredProperties.containsKey( "version" ) ) {
            def requiredProperty = [name:"version", description:"", archetype:request, defaultValue:null]
            unsorted[requiredProperty.name] = requiredProperty
            requiredProperties[requiredProperty.name] = requiredProperty
        }
        if( !requiredProperties.containsKey( "packageName" ) ) { // package if no package name defined
            def requiredProperty = [name:"packageName", description:"", archetype:request, defaultValue:'${groupId}']
            unsorted[requiredProperty.name] = requiredProperty
            requiredProperties[requiredProperty.name] = requiredProperty
        }
        if( !requiredProperties.containsKey( "package" ) ) {
            def requiredProperty = [name:"package", description:"", archetype:request, defaultValue:
            requiredProperties["packageName"]?.value?:requiredProperties["packageName"]?.defaultValue?:'${groupId}']
            unsorted[requiredProperty.name] = requiredProperty
            requiredProperties[requiredProperty.name] = requiredProperty
        }
        unsorted["packageName"] = null //package removed in favor of packageName
        unsorted.remove "packageName" //package removed in favor of packageName
        requiredProperties["packageName"] = null //package removed in favor of packageName
        requiredProperties.remove "packageName"//package removed in favor of packageName
println"DDD ${requiredProperties?.keySet()}"
        return requiredProperties
    }
    def comparePropertyName( left, right ){
        if( "groupId" == left ) return -1
        if( "groupId" == right ) return 1
        if( "artifactId" == left ) return -1
        if( "artifactId" == right ) return 1
        if( "version" == left ) return -1
        if( "version" == right ) return 1
        if( "package" == left ) return -1
        if( "package" == right ) return 1
        return left.compareTo( right )
    }
    def applyIncomingProperties( requiredProperties, executionProperties ) {
        requiredProperties.each { key, property ->
            if( executionProperties.containsKey( key ) ) {
                property.value = executionProperties[key]
            }
        }
    }
    def configured( requiredProperties ) {
        def configured = true
        requiredProperties.each { key, property ->
            if( !property?.value ) configured = false
        }
        return configured
    }
    def describeMissingProperties( requiredProperties ) {
        requiredProperties.each { key, property ->
            if( !property.value ) logger.warn "Property '${key}' is missing. Add -D${key}=someValue"
        }
    }
    def askForUnconfiguredProperties( requiredProperties ) {
        requiredProperties.each { key, property ->
            if( !property.value ) {
                property.value = askForUnconfiguredProperty( key, property.description, getDefaultValue( property.defaultValue, requiredProperties ) )
            }
        }
    }
    def getDefaultValue( initialDefault, requiredProperties ) {
        String defaultValue = initialDefault
        requiredProperties.each { key, property ->
            defaultValue = StringUtils.replace( defaultValue, '${' + key + '}', property.value )
        }
        return defaultValue
    }
    def askForUnconfiguredProperty( name, description, defaultValue ) {
        def query = "Define value for property '$name${description ? '(' + description + ')' : ''}': "
        if ( defaultValue ) {
            return prompter.prompt( query, defaultValue )
        } else {
            return prompter.prompt( query )
        }
    }
    def askForConfirmation( request, requiredProperties  ) {
        def query = "Confirm properties configuration:\n"
        requiredProperties.each { key, property ->
            query += "$key: ${property.value}\n"
        }
        return "Y" == prompter.prompt( query, "Y" ).toString().toUpperCase()
    }
    def applyDefaultValues( requiredProperties ) {
//dont reset default!!!
    }
    def resetConfiguredValues( requiredProperties ) {
        requiredProperties.each { key, property ->
            property.value = null
        }
    }
    def applyProperties( requiredProperties, request ) {
      def requestProperties = [:]
      requiredProperties.each { key, property ->
            if(key == 'package') key = 'packageName'
            requestProperties[key] = property.value
        }
      request.generationFilterProperties = requestProperties as Properties
    }
}
