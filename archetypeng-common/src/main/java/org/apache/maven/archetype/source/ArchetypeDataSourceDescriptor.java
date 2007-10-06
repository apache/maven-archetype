package org.apache.maven.archetype.source;

import java.util.ArrayList;
import java.util.List;

/** @author Jason van Zyl */
public class ArchetypeDataSourceDescriptor
{
    private List parameters;

    public void addParameter( String name, Class type, String defaultValue, String description )
    {
        addParameter( new Parameter( name, type, defaultValue, description ) );
    }

    public void addParameter( Parameter parameter )
    {
        if ( parameters == null )
        {
            parameters = new ArrayList();
        }

        parameters.add( parameter );
    }

    public class Parameter
    {
        public Parameter( String name,
                          Class type,
                          String defaultValue,
                          String description )
        {
            this.name = name;

            this.type = type;

            this.defaultValue = defaultValue;

            this.description = description;

        }

        private String name;

        private Class type;

        private String defaultValue;

        private String description;

        public Class getType()
        {
            return type;
        }

        public String getDefaultValue()
        {
            return defaultValue;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }
    }
}
