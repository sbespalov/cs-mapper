package org.carlspring.beans.mapper;

import java.util.List;

/**
 * The purpose of this interface is to build mappings based on different types
 * of metadata, such as POJO property names or annotations.
 * 
 * @author Sergey Bespalov
 * 
 * @see {@link AnnotationMappingBuilder}, {@link SimpleMappingBuilder},
 *      {@link BeanMapping}
 */
public interface MappingBuilder
{

    public List<BeanMapping> buildMappings();

}
