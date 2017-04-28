package org.carlspring.beans.mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Bespalov
 */
public class BeanMapping
{

    private Class sourceClass;
    private Class targetClass;
    private List<PropertyMapping> propertyMappings = new ArrayList<PropertyMapping>();

    public Class getSourceClass()
    {
        return sourceClass;
    }

    public void setSourceClass(Class sourceClass)
    {
        this.sourceClass = sourceClass;
    }

    public Class getTargetClass()
    {
        return targetClass;
    }

    public void setTargetClass(Class targetClass)
    {
        this.targetClass = targetClass;
    }

    public List<PropertyMapping> getPropertyMappings()
    {
        return propertyMappings;
    }

    public void setPropertyMappings(List<PropertyMapping> propertyMappings)
    {
        this.propertyMappings = propertyMappings;
    }

    public void addPropertyMapping(PropertyMapping mapping)
    {
        propertyMappings.add(mapping);
    }

}