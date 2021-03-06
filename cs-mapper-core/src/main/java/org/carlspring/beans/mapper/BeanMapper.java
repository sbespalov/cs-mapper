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
import org.carlspring.beans.mapper.markup.MappedBean;
import org.carlspring.beans.mapper.markup.MappedProperty;

/**
 * BeanMapper provides functionality for merge POJO objects of different
 * classes,
 * based on specific mappings. In general, a mappings is obtained based on the
 * POJO property names, an explicit mappings also possible using combination of
 * {@link MappedBean} and {@link MappedProperty} annotations.<br>
 * All currently used mappings are cached in the {@link MappingConfig}
 * instance, which is also provides specific {@link MappingProfile}.<br>
 * 
 * As mentioned above, almost all functionality provided by this class are
 * reduced to call the main method:
 * 
 * <pre>
 *  Object mergeBeans(Object targetObject, Object sourceObject, Class targetType, Class sourceType)
 * </pre>
 * 
 * You can merge a Collections, create Mixins or convert a POJOs from one
 * Class to another, but all of this will perform Merge beans operation at the
 * end.
 * 
 * 
 * @author Sergey Bespalov
 *
 * @see {@link MappingBuilder}, {@link MappingConfig}, {@link MappingProfile}
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

    /**
     * With this method you can combine multiply Domain DTO interfaces in one
     * resulting Mixin Interface, and populate it with values of source objects
     * properties.
     * 
     * @param targetClass
     *            the Mixin interface, which can extend a set of Domain DTO
     *            Interfaces.
     * @param sourceObject
     *            POJOs to merge their property values in resulting Mixin
     * @return Mixin POJO instance, which is the JDK Proxy, based on HashMap of
     *         POJO property names and values..
     * 
     */
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

    /**
     * Merge property values from source POJO into target POJO, using
     * appropriate mappings.
     * 
     * @param targetObject
     * @param sourceObject
     * @return
     */
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

    /**
     * Merge property values from source POJO into target POJO, using
     * sourceType->targetType mappings.
     * 
     * @param targetObject
     * @param sourceObject
     * @param targetType
     * @param sourceType
     * @return
     */
    public Object mergeBeans(Object targetObject,
                             Object sourceObject,
                             Class targetType,
                             Class sourceType)
    {
        if (targetObject == null)
        {
            targetObject = CSBeanUtils.createInstance(sourceType);
        }
        if (sourceObject == null || sourceObject == targetObject)
        {
            return targetObject;
        }
        List<PropertyMapping> propertyMappings = getPropertyMappings(targetObject, sourceObject, targetType,
                                                                     sourceType);
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
                                                      Class sourceType)
    {
        if (!Map.class.isAssignableFrom(targetType) || !Map.class.isAssignableFrom(sourceType))
        {
            BeanMapping beanMapping = getMappingConfig().getBeanMapping(targetType, sourceType);
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

    /**
     * Creates a targetType POJO instance and populate it with source POJO
     * property values, using appropriate mappings.
     * 
     * @param object
     * @param targetType
     * @return
     */
    public Object convertObject(Object object,
                                Class targetType)
    {
        if (object == null)
        {
            return null;
        }
        return convertObject(object, targetType, extractType(object));
    }

    /**
     * Creates a targetType POJO instance and populate it with source POJO
     * property values, using sourceType->targetType mappings.
     * 
     * @param object
     * @param targetType
     * @param sourceType
     * @return
     */
    public Object convertObject(Object object,
                                Class targetType,
                                Class sourceType)
    {
        return mergeInternal(null, object, targetType, sourceType);
    }

    /**
     * Merge Target Collection items with Source Collection items, using
     * sourceCollectionElementType->targetType mappings.
     * 
     * @param targetCollection
     * @param sourceCollection
     * @param targetType
     * @return
     */
    public Collection convertCollection(Collection targetCollection,
                                        Collection sourceCollection,
                                        Class targetType)
    {
        if (sourceCollection == null)
        {
            return targetCollection;
        }
        for (Object object : sourceCollection)
        {
            targetCollection.add(convertObject(object, targetType));
        }
        return targetCollection;
    }

    /**
     * Merge Target Collection items with Source Collection items, using
     * sourceType->targetType mappings.
     * 
     * @param targetCollection
     * @param sourceCollection
     * @param targetType
     * @param sourceType
     * @return
     */
    public Collection convertCollection(Collection targetCollection,
                                        Collection sourceCollection,
                                        Class targetType,
                                        Class sourceType)
    {
        for (Object object : sourceCollection)
        {
            targetCollection.add(convertObject(object, targetType, sourceType));
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
                                       Class sourceClass)
    {
        return mappingConfig.getBeanMapping(targetClass, sourceClass);
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
        targetObject = mergeBeans(targetObject, sourceObject, targetType, sourceType);
        return targetObject;
    }

}