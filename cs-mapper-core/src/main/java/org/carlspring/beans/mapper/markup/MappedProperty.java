package org.carlspring.beans.mapper.markup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.carlspring.beans.mapper.AnnotationMappingBuilder;

/**
 * Using this annotation, you can specify the target property, where the
 * annotated property value will be set.<br>
 * This annotation must be placed under property getter method.<br>
 * You can use nested properties, for
 * example:
 * 
 * <pre>
 * &#64;MappedProperty("pet.petStore.name")
 * </pre>
 * 
 * @author Sergey Bespalov
 * 
 * @see {@link MappedBean}, {@link AnnotationMappingBuilder}
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MappedProperty
{

    public String value();
    
    public Class<?> targetPropertyType() default Object.class;

}
