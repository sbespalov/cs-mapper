package org.carlspring.beans.mapper;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.collections.iterators.ArrayIterator;

/**
 * BeanMapper provide functionality for merge POJO objects of different classes,
 * based on specific mappings.
 * 
 * @author Sergey Bespalov
 *
 */
public class BeanMapper
{

    private static final Logger LOGGER = Logger.getLogger(BeanMapper.class.getCanonicalName());

    private MappingConfig mappingConfig;

    public BeanMapper()
    {
        this(new MappingConfig());
    }

    public BeanMapper(MappingConfig mappingConfig)
    {
        super();
        this.mappingConfig = mappingConfig;
    }

    public MappingConfig getMappingConfig()
    {
        return mappingConfig;
    }

    public Object createMixin(Class targetClass,
                              Object... sourceObject)
    {
        Object result = CSBeanUtils.createBeanInstance(targetClass);
        for (Object object : sourceObject)
        {
            result = mergeBeans(result, object);
        }
        return result;
    }

    public Object mergeBeans(Object targetObject,
                             Object sourceObject)
    {
        if (sourceObject == null)
        {
            return targetObject;
        }
        Class sourceType = extractType(sourceObject);
        if (targetObject == null)
        {
            targetObject = CSBeanUtils.createInstance(sourceType);
        }
        Class targetType = extractType(targetObject);
        return mergeBeans(targetObject, sourceObject, targetType, sourceType);
    }

    public Object mergeBeans(Object targetObject,
                             Object sourceObject,
                             Class targetType,
                             Class sourceType)
    {
        return mergeBeans(targetObject, sourceObject, targetType, sourceType, null);
    }

    public Object mergeBeans(Object targetObject,
                             Object sourceObject,
                             Class targetType,
                             Class sourceType,
                             String mappingId)
    {
        if (targetObject == null)
        {
            targetObject = CSBeanUtils.createInstance(sourceType);
        }
        if (sourceObject == null || sourceObject == targetObject)
        {
            return targetObject;
        }
        List<PropertyMapping> propertyMappings = getPropertyMappings(targetObject, sourceObject, targetType, sourceType,
                                                                     mappingId);
        for (PropertyMapping propertyMapping : propertyMappings)
        {
            String targetPropertyName = propertyMapping.getTargetProperty().getPropertyName();
            String sourcePropertyName = propertyMapping.getSrcProperty().getPropertyName();
            Class targetPropertyType = propertyMapping.getTargetProperty().getPropertyType();
            Class sourcePropertyType = propertyMapping.getSrcProperty().getPropertyType();
            Object targetPropertyValue;
            Object sourcePropertyValue;
            try
            {
                targetPropertyValue = CSBeanUtils.getProperty(targetObject, targetPropertyName);
                sourcePropertyValue = CSBeanUtils.getProperty(sourceObject, sourcePropertyName);
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(
                        String.format("Failed to merge bean properties: targetType-[%s]; sourceType-[%s]; targetProperty-[%s]; sourceProperty-[%s].",
                                      targetType, sourceType, targetPropertyName, sourcePropertyName),
                        e);
            }
            if (sourcePropertyValue == null
                    || (targetPropertyValue == null && !propertyMapping.getTargetProperty().isWritable()))
            {
                continue;
            }
            if (Collection.class.isAssignableFrom(targetPropertyType))
            {
                Class targetElementType = propertyMapping.getTargetProperty().getTypeArgs()[0];
                Class sourceElementType = propertyMapping.getSrcProperty().getTypeArgs()[0];
                targetPropertyValue = targetPropertyValue == null
                        ? CSBeanUtils.createCollectionInstance(targetPropertyType) : targetPropertyValue;
                sourcePropertyValue = getCollection(sourcePropertyValue);
                try
                {
                    targetPropertyValue = megreCollection((Collection) targetPropertyValue,
                                                          (Collection) sourcePropertyValue, targetElementType);
                }
                catch (UnsupportedOperationException e)
                {
                    Collection localCollection = (Collection) CSBeanUtils.createCollectionInstance(targetPropertyType);
                    localCollection.addAll((Collection) targetPropertyValue);
                    targetPropertyValue = localCollection;
                    targetPropertyValue = megreCollection((Collection) targetPropertyValue,
                                                          (Collection) sourcePropertyValue, targetElementType);
                }
            }
            else
            {
                targetPropertyValue = mergeInternal(targetPropertyValue, sourcePropertyValue, targetPropertyType,
                                                    sourcePropertyType);
            }
            CSBeanUtils.setProperty(targetObject, targetPropertyName, targetPropertyValue);
        }
        return targetObject;
    }

    private List<PropertyMapping> getPropertyMappings(Object targetObject,
                                                      Object sourceObject,
                                                      Class targetType,
                                                      Class sourceType,
                                                      String mappingId)
    {
        if (!Map.class.isAssignableFrom(targetType) || !Map.class.isAssignableFrom(sourceType))
        {
            BeanMapping beanMapping = getMappingConfig().getBeanMapping(targetType, sourceType, mappingId);
            return beanMapping.getPropertyMappings();
        }

        // We are forced to create mappings dynamically in
        // case of `Map`
        List<PropertyMapping> propertyMappings = new ArrayList<PropertyMapping>();
        Map<String, Object> map = (Map<String, Object>) sourceObject;
        for (String key : map.keySet())
        {
            PropertyMapping propertyMapping = new PropertyMapping();
            BeanPropertyDescriptor descriptor = new BeanPropertyDescriptor();
            descriptor.setPropertyName(key);
            Class propertyType = extractType(map.get(key));
            descriptor.setPropertyType(propertyType);
            if (Collection.class.isAssignableFrom(propertyType))
            {
                descriptor.setTypeArgs(new Class[] { Object.class });
            }
            propertyMapping.setSrcProperty(descriptor);
            propertyMapping.setTargetProperty(descriptor);
            propertyMappings.add(propertyMapping);
        }
        return propertyMappings;
    }

    private Collection megreCollection(Collection targetCollection,
                                       Collection sourceCollection,
                                       Class targetElementType)
    {
        Collection result = targetCollection;

        // Remove orphan items
        List<Object> orphanElements = findOrphanElementList(sourceCollection, targetElementType, result);
        if (orphanElements.size() > 0)
        {
            result.removeAll(orphanElements);
        }

        // Merge items
        Collection localCollection = new ArrayList();
        Iterator sourceIterator = getCollectionIterator(sourceCollection);
        outer_loop: while (sourceIterator.hasNext())
        {
            Object sourceElement = convertObject(sourceIterator.next(), targetElementType);
            if (sourceElement == null)
            {
                continue;
            }
            Iterator targetIterator = getCollectionIterator(result);
            while (targetIterator.hasNext())
            {
                Object targetElement = targetIterator.next();
                if (targetElement != null && targetElement.equals(sourceElement))
                {
                    if (isMappingAllowedForType(extractType(targetElement)))
                    {
                        mergeInternal(targetElement, sourceElement);
                    }
                    continue outer_loop;
                }
            }
            localCollection.add(sourceElement);
        }

        // Add new items, if needed
        if (localCollection.size() > 0)
        {
            result.addAll(localCollection);
        }
        return result;
    }

    private List<Object> findOrphanElementList(Collection sourceCollection,
                                               Class targetElementType,
                                               Collection result)
    {
        List<Object> orphanElements = new ArrayList<Object>();
        Iterator targetIterator = getCollectionIterator(result);
        outer_loop: while (targetIterator.hasNext())
        {
            Object targetElement = targetIterator.next();
            Iterator sourceIterator = getCollectionIterator(sourceCollection);
            while (sourceIterator.hasNext())
            {
                Object sourceElement = convertObject(sourceIterator.next(), targetElementType);
                if (targetElement != null && targetElement.equals(sourceElement))
                {
                    continue outer_loop;
                }
            }
            orphanElements.add(targetElement);

        }
        return orphanElements;
    }

    public Object convertObject(Object object,
                                Class targetType)
    {
        return convertObject(object, targetType, (String) null);
    }

    public Object convertObject(Object object,
                                Class targetType,
                                String mappingId)
    {
        if (object == null)
        {
            return null;
        }
        return convertObject(object, targetType, extractType(object), mappingId);
    }

    public Object convertObject(Object object,
                                Class targetType,
                                Class sourceType)
    {
        return convertObject(object, targetType, sourceType, null);
    }

    public Object convertObject(Object object,
                                Class targetType,
                                Class sourceType,
                                String mappingId)
    {
        return mergeInternal(null, object, targetType, sourceType, mappingId);
    }

    public Collection convertCollection(Collection targetCollection,
                                        Collection sourceCollection,
                                        Class targetType)
    {
        return convertCollection(targetCollection, sourceCollection, targetType, (String) null);
    }

    public Collection convertCollection(Collection targetCollection,
                                        Collection sourceCollection,
                                        Class targetType,
                                        String mappingId)
    {
        if (sourceCollection == null)
        {
            return targetCollection;
        }
        for (Object object : sourceCollection)
        {
            targetCollection.add(convertObject(object, targetType, mappingId));
        }
        return targetCollection;
    }

    public Collection convertCollection(Collection targetCollection,
                                        Collection sourceCollection,
                                        Class targetType,
                                        Class sourceType)
    {
        return convertCollection(targetCollection, sourceCollection, targetType, sourceType, null);
    }

    public Collection convertCollection(Collection targetCollection,
                                        Collection sourceCollection,
                                        Class targetType,
                                        Class sourceType,
                                        String mappingId)
    {
        for (Object object : sourceCollection)
        {
            targetCollection.add(convertObject(object, targetType, sourceType, mappingId));
        }
        return targetCollection;
    }

    private Set<String> getPropertyNames(Object object)
    {
        Class type = extractType(object);
        HashSet<String> result = new HashSet<String>();
        if (Map.class.isAssignableFrom(type))
        {
            result.addAll(((Map) object).keySet());
        }
        else
        {
            List<PropertyDescriptor> propertyDescriptors;
            try
            {
                propertyDescriptors = CSBeanUtils.getPropertyDescriptors(type);
            }
            catch (IntrospectionException e)
            {
                throw new RuntimeException("Failed to extract proeprty names: type-[" + type + "]", e);
            }
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
            {
                result.add(propertyDescriptor.getName());
            }
        }
        return result;
    }

    private Collection getCollection(Object object)
    {
        if (Collection.class.isAssignableFrom(object.getClass()))
        {
            return (Collection) object;
        }
        return Arrays.asList(new Object[] { object });
    }

    private Iterator getCollectionIterator(Object collection)
    {
        if (collection == null)
        {
            return null;
        }
        Class collectionType = extractType(collection);
        if (collectionType.isArray())
        {
            return new ArrayIterator(collection);
        }
        else if (Collection.class.isAssignableFrom(collectionType))
        {
            return (getCollection(collection)).iterator();
        }
        throw new RuntimeException("Failed to create iterator for collection: collectionType-[" + collectionType + "]");
    }

    protected boolean isMappingAllowedForType(Class<?> type)
    {
        return !mappingConfig.getMappingProfile().isSimpleType(type);
    }

    private Object convert(Class<?> type,
                           Object value)
    {
        if (!type.isAssignableFrom(value.getClass()))
        {
            Converter converter = mappingConfig.getMappingProfile().lookupConverter(type);
            return converter.convert(type, value);
        }
        return value;
    }

    private BeanMapping getBeanMapping(Class targetClass,
                                       Class sourceClass,
                                       String mappingId)
    {
        return mappingConfig.getBeanMapping(targetClass, sourceClass, mappingId);
    }

    private Class extractType(Object targetObject)
    {
        Class result = targetObject.getClass();
        if (Map.class.isAssignableFrom(result))
        {
            return Map.class;
        }
        else if (Collection.class.isAssignableFrom(result))
        {
            return Collection.class;
        }
        else if (Proxy.isProxyClass(result))
        {
            return result.getInterfaces()[0];
        }
        try
        {
            Method method = result.getMethod("getUnproxiedClass");
            return (Class) method.invoke(targetObject);
        }
        catch (Exception e)
        {
            // do nothing
        }
        // Class<?> cglibProxyFactroryClass =
        // Class.forName("net.sf.cglib.proxy.Factory");
        // if
        // (cglibProxyFactroryClass.isAssignableFrom(result))
        // {
        // Class<?>[] interfaces = result.getInterfaces();
        // for (Class<?> clazz : interfaces) {
        // if (!clazz.equals(cglibProxyFactroryClass)) {
        // return clazz;
        // }
        // }
        // return result.getSuperclass();
        // }
        return result;
    }

    private Object mergeInternal(Object targetObject,
                                 Object sourceObject)
    {
        if (sourceObject == null && targetObject == null)
        {
            return null;
        }
        if (sourceObject == null)
        {
            return targetObject;
        }
        Class targetType = extractType(targetObject);
        Class sourceType = sourceObject == null ? targetType : extractType(sourceObject);
        return mergeInternal(targetObject, sourceObject, targetType, sourceType);
    }

    private Object mergeInternal(Object targetObject,
                                 Object sourceObject,
                                 Class targetType,
                                 Class sourceType)
    {
        return mergeInternal(targetObject, sourceObject, targetType, sourceType, null);
    }

    private Object mergeInternal(Object targetObject,
                                 Object sourceObject,
                                 Class targetType,
                                 Class sourceType,
                                 String mappingId)
    {
        if (!isMappingAllowedForType(targetType) || !isMappingAllowedForType(sourceType)
                || Object.class.equals(targetType) || Object.class.equals(sourceType))
        {
            return convert(targetType, sourceObject);
        }
        if (sourceObject == null)
        {
            return targetObject;
        }
        if (targetObject == null)
        {
            targetObject = CSBeanUtils.createBeanInstance(targetType);
        }
        targetObject = mergeBeans(targetObject, sourceObject, targetType, sourceType, mappingId);
        return targetObject;
    }

}