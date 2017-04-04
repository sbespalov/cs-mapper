package org.carlspring.beans.mapper.markup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Sergey Bespalov
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CSProperty {

	public String targetProperty() default "";
	
	public Class<?> targetType() default Object.class;
}
