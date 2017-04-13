package org.carlspring.beans.mapper;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.beans.mapper.markup.ExcludeProperty;
import org.carlspring.beans.mapper.markup.MappedBean;
import org.carlspring.beans.mapper.markup.MappedProperty;
import org.reflections.ReflectionUtils;

import com.google.common.base.Predicate;

/**
 * @author Sergey Bespalov
 */
public class AnnotationMappingBuilder implements MappingBuilder
{

    private static final Logger LOGGER = Logger.getLogger(AnnotationMappingBuilder.class.getName());

    private Class<?> sourceClass;
    private Class<?> targetClass;
    private MappingProfile mappingProfile;

    private static Set<Class<?>> getTargetSet(Class source)
    {
        Set<Class<?>> result = new HashSet<Class<?>>();
        for (Annotation annotation : ReflectionUtils.getAllAnnotations(source, new MappedBeanPredicate()))
        {
            for (Class target : ((MappedBean) annotation).value())
            {
                result.add(target);
            }
        }
        return result;
    }

    public AnnotationMappingBuilder(MappingProfile mappingProfile,
                                    Class sourceClass)
    {
        this(mappingProfile, sourceClass, null);
    }

    public AnnotationMappingBuilder(MappingProfile mappingProfile,
                                    Class sourceClass,
                                    Class targetClass)
    {
        this.mappingProfile = mappingProfile;
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    public List<MappingBuilder> getMappingBuilderList()
    {
        LOGGER.log(Level.INFO,
                   String.format("Init mappings: source-[%s]; target-[%s];", sourceClass, targetClass));

        Set<Class<?>> targetList = getTargetSet(sourceClass);
        if (targetClass != null)
        {
            targetList.add(targetClass);
        }
        else if (targetList.isEmpty())
        {
            targetList.add(sourceClass);
        }

        List<PropertyDescriptor> propertyDescriptors;
        try
        {
            propertyDescriptors = CSBeanUtils.getPropertyDescriptors(sourceClass);
        }
        catch (IntrospectionException e)
        {
            throw new RuntimeException(e);
        }

        List<MappingBuilder> mappingBuilders = new ArrayList<MappingBuilder>();

        for (Class target : targetList)
        {
            LOGGER.log(Level.FINE, String.format("Build mapping: source-[%s]; target-[%s];", sourceClass, target));

            SimpleMappingBuilder mappingBuilder = new SimpleMappingBuilder(mappingProfile, sourceClass, target);

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
            {
                Method propertyMethod = propertyDescriptor.getReadMethod();
                propertyMethod = propertyMethod == null ? propertyDescriptor.getWriteMethod() : propertyMethod;
                if (getAnnotation(sourceClass, propertyMethod, ExcludeProperty.class).size() > 0)
                {
                    LOGGER.log(Level.FINE, String.format("Exclude property mapping: source-[%s.%s]; target-[%s]; ",
                                                         sourceClass.getSimpleName(), propertyDescriptor.getName(),
                                                         target.getSimpleName()));

                    mappingBuilder.addExcludeProperty(propertyDescriptor.getName());
                }
                for (MappedProperty csProperty : getAnnotation(sourceClass, propertyMethod, MappedProperty.class))
                {
                    String targetProperty = csProperty.value();
                    if (!StringUtils.isEmpty(targetProperty))
                    {
                        LOGGER.log(Level.FINE,
                                   String.format("Custom property mapping: source-[%s.%s]; target-[%s.%s];",
                                                 sourceClass.getSimpleName(), propertyDescriptor.getName(),
                                                 target.getSimpleName(),
                                                 targetProperty));
                        mappingBuilder.addCustomMapping(propertyDescriptor.getName(), targetProperty);
                    }
                }
            }
            mappingBuilders.add(mappingBuilder);
        }
        return mappingBuilders;
    }

    private <T extends Annotation> Set<T> getAnnotation(Class type,
                                                        Method method,
                                                        final Class<T> annotationClass)
    {
        Set<T> result = new HashSet<T>();
        for (Method m : ReflectionUtils.getAllMethods(type, new AnnotatedMethodPredicate(method, annotationClass)))
        {
            result.add(m.getAnnotation(annotationClass));
        }
        return result;
    }

    public List<BeanMapping> buildMappings()
    {
        List<BeanMapping> result = new ArrayList<BeanMapping>();
        for (MappingBuilder mappingBuilder : getMappingBuilderList())
        {
            result.addAll(mappingBuilder.buildMappings());
        }
        return result;
    }

    private static class MappedBeanPredicate implements Predicate<Annotation>
    {
        public boolean apply(Annotation input)
        {
            return input instanceof MappedBean;
        }
    }

    private static class AnnotatedMethodPredicate implements Predicate<Method>
    {

        private Class<? extends Annotation> annotationClass;
        private Method method;

        public AnnotatedMethodPredicate(Method method,
                                        Class<? extends Annotation> annotationClass)
        {
            super();
            this.method = method;
            this.annotationClass = annotationClass;
        }

        public boolean apply(Method input)
        {
            return method.getName().equals(input.getName()) && input.isAnnotationPresent(annotationClass);
        }

    }
}
