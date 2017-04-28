package org.carlspring.beans.mapper.markup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.carlspring.beans.mapper.AnnotationMappingBuilder;

/**
 * Using this annotation, you must specify the target class for the properties,
 * specified with {@link MappedProperty} annotation.
 * 
 * @author Sergey Bespalov
 * 
 * @see {@link MappedProperty} {@link AnnotationMappingBuilder}
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappedBean
{

    public Class[] value() default Map.class;

}
