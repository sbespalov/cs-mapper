package org.carlspring.beans.mapper;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.beans.mapper.markup.CSExclude;
import org.carlspring.beans.mapper.markup.CSMappedBean;
import org.carlspring.beans.mapper.markup.CSProperty;

/**
 * @author Sergey Bespalov
 */
public class AnnotationMappingBuilder implements MappingBuilder
{

    private List<MappingBuilder> mappingBuilders = new ArrayList<MappingBuilder>();

    public AnnotationMappingBuilder(MappingProfile mappingProfile,
                                    Class type)
    {
        CSMappedBean mappedBeanAnnotation = (CSMappedBean) type.getAnnotation(CSMappedBean.class);
        if (mappedBeanAnnotation == null)
        {
            throw new RuntimeException("Failed to create bean mapping from annotation: beanClass-[" + type + "].");
        }
        Class[] targetBeans = mappedBeanAnnotation.targetBean();
        for (Class targetBean : targetBeans)
        {
            SimpleMappingBuilder mappingBuilder = new SimpleMappingBuilder(mappingProfile, type, targetBean);
            List<PropertyDescriptor> propertyDescriptors;
            try
            {
                propertyDescriptors = CSBeanUtils.getPropertyDescriptors(type);
            }
            catch (IntrospectionException e)
            {
                throw new RuntimeException(e);
            }
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
            {
                Method propertyMethod = propertyDescriptor.getReadMethod();
                propertyMethod = propertyMethod == null ? propertyDescriptor.getWriteMethod() : propertyMethod;
                CSExclude excludeProperty = getAnnotation(type, propertyMethod, CSExclude.class);
                if (excludeProperty != null)
                {
                    mappingBuilder.addExcludeProperty(propertyDescriptor.getName());
                }
                CSProperty mappedProperty = getAnnotation(type, propertyMethod, CSProperty.class);
                String targetProperty = mappedProperty == null ? null : mappedProperty.targetProperty();
                Class<?> targetType = mappedProperty == null ? null : mappedProperty.targetType();
                if (!StringUtils.isEmpty(targetProperty))
                {
                    mappingBuilder.addCustomMapping(propertyDescriptor.getName(), targetProperty);
                }
                if (targetType != null && !Object.class.equals(targetType))
                {
                    mappingBuilder.addTargetCustomType(propertyDescriptor.getName(), targetType);
                }
            }
            mappingBuilders.add(mappingBuilder);
        }
    }

    private <T extends Annotation> T getAnnotation(Class type,
                                                   Method method,
                                                   Class<T> annotationClass)
    {
        if (method.isAnnotationPresent(annotationClass))
        {
            return method.getAnnotation(annotationClass);
        }
        return null;
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

}
