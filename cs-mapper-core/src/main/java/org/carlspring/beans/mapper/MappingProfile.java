package org.carlspring.beans.mapper;

import org.apache.commons.beanutils.Converter;

/**
 * This interface is responsible for how the merge operation will be performed
 * and strategies to build mappings for types.
 * 
 * @author Sergey Bespalov
 *
 */
public interface MappingProfile
{

    /**
     * Determines whether the type is simple. Property mappings not allowed for
     * simple types.
     * 
     * @param type
     * @return
     */
    public boolean isSimpleType(Class type);

    /**
     * Defines a converter for converting a type to other types.
     * 
     * @param type
     * @return
     */
    public Converter lookupConverter(Class type);

    /**
     * Defines the ability to map properties by name.
     * 
     * @return
     */
    public boolean isAllowDefaultMapping();
}
