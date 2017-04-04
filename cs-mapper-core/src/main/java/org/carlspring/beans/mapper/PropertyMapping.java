package org.carlspring.beans.mapper;

public class PropertyMapping
{

    private BeanPropertyDescriptor srcProperty;

    private BeanPropertyDescriptor targetProperty;

    public BeanPropertyDescriptor getSrcProperty()
    {
        return srcProperty;
    }

    public void setSrcProperty(BeanPropertyDescriptor srcProperty)
    {
        this.srcProperty = srcProperty;
    }

    public BeanPropertyDescriptor getTargetProperty()
    {
        return targetProperty;
    }

    public void setTargetProperty(BeanPropertyDescriptor targetProperty)
    {
        this.targetProperty = targetProperty;
    }

}
