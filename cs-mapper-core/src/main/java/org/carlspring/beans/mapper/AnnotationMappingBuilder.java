package org.carlspring.beans.mapper;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.beans.mapper.markup.CSExclude;
import org.carlspring.beans.mapper.markup.CSMappedBean;
import org.carlspring.beans.mapper.markup.CSProperty;
import org.reflections.ReflectionUtils;

import com.google.common.base.Predicate;

/**
 * @author Sergey Bespalov
 */
public class AnnotationMappingBuilder implements MappingBuilder
{

    private List<MappingBuilder> mappingBuilders = new ArrayList<MappingBuilder>();

    private static Class[] getTarget(Class source)
    {
        Set<Class> result = new HashSet<Class>();
        for (Annotation annotation : ReflectionUtils.getAllAnnotations(source, new MappedBeanPredicate()))
        {
            for (Class target : ((CSMappedBean) annotation).value())
            {
                result.add(target);
            }
        }
        return result.toArray(new Class[] {});
    }

    public AnnotationMappingBuilder(MappingProfile mappingProfile,
                                    Class source)
    {
        this(mappingProfile, source, getTarget(source));
    }

    public AnnotationMappingBuilder(MappingProfile mappingProfile,
                                    Class source,
                                    Class... target)
    {
        if (target == null || target.length == 0)
        {
            target = new Class[] { source };
        }
        for (Class targetBean : target)
        {
            SimpleMappingBuilder mappingBuilder = new SimpleMappingBuilder(mappingProfile, source, targetBean);
            List<PropertyDescriptor> propertyDescriptors;
            try
            {
                propertyDescriptors = CSBeanUtils.getPropertyDescriptors(source);
            }
            catch (IntrospectionException e)
            {
                throw new RuntimeException(e);
            }
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
            {
                Method propertyMethod = propertyDescriptor.getReadMethod();
                propertyMethod = propertyMethod == null ? propertyDescriptor.getWriteMethod() : propertyMethod;
                if (getAnnotation(source, propertyMethod, CSExclude.class).size() > 0)
                {
                    mappingBuilder.addExcludeProperty(propertyDescriptor.getName());
                }
                for (CSProperty csProperty : getAnnotation(source, propertyMethod, CSProperty.class))
                {
                    String targetProperty = csProperty.targetProperty();
                    Class<?> targetType = csProperty.targetType();
                    if (!StringUtils.isEmpty(targetProperty))
                    {
                        mappingBuilder.addCustomMapping(propertyDescriptor.getName(), targetProperty);
                    }
                    if (targetType != null && !Object.class.equals(targetType))
                    {
                        mappingBuilder.addTargetCustomType(propertyDescriptor.getName(), targetType);
                    }
                }
            }
            mappingBuilders.add(mappingBuilder);
        }
    }

    private <T extends Annotation> Set<T> getAnnotation(Class type,
                                                        Method method,
                                                        final Class<T> annotationClass)
    {
        Set<T> result = new HashSet<T>();
        for (Method m : ReflectionUtils.getAllMethods(type, new AnnotatedMethodPredicate(annotationClass)))
        {
            result.add(m.getAnnotation(annotationClass));
        }
        return result;
    }

    public List<BeanMapping> buildMappings()
    {
        List<BeanMapping> result = new ArrayList<BeanMapping>();
        for (MappingBuilder mappingBuilder : mappingBuilders)
        {
            result.addAll(mappingBuilder.buildMappings());
        }
        return result;
    }

    private static class MappedBeanPredicate implements Predicate<Annotation>
    {
        public boolean apply(Annotation input)
        {
            return input instanceof CSMappedBean;
        }
    }

    private static class AnnotatedMethodPredicate implements Predicate<Method>
    {

        private Class<? extends Annotation> annotationClass;

        public AnnotatedMethodPredicate(Class<? extends Annotation> annotationClass)
        {
            super();
            this.annotationClass = annotationClass;
        }

        public boolean apply(Method input)
        {
            return input.isAnnotationPresent(annotationClass);
        }

    }
}
