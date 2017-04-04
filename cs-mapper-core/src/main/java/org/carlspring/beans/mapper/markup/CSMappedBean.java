package org.carlspring.beans.mapper.markup;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre class="code">
 * 	&ltbean id="beanHelper"
 *		class="ru.diasoft.fa.xdscall.beans.spring.BeanHelperFactoryBean"&gt
 *		&ltproperty name="mappedClasses"&gt
 *			&ltlist&gt
 *				&ltvalue&gtru.diasoft.fa.myapp.SampleDto&lt/value&gt
 *			&lt/list&gt
 *		&lt/property&gt
 *	&lt/bean&gt
 * </pre>
 * 
 * @author Sergey Bespalov
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CSMappedBean
{

    public Class[] targetBean();

}
