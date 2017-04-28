package org.carlspring.beans.mapper;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.collections.CollectionUtils;
import org.carlspring.beans.Identifiable;
import org.carlspring.beans.mapper.converter.EntityConverter;
import org.carlspring.beans.mapper.converter.LongConverter;

public class SimpleMappingBuilder implements MappingBuilder
{

    private static final Logger LOGGER = Logger.getLogger(SimpleMappingBuilder.class.getName());

    private Class<?> targetClass;
    private Class<?> sourceClass;
    private Set<String> excludeProperties = new HashSet<String>();
    private Map<String, String> sourceCustomMappings = new HashMap<String, String>();
    private Map<String, String> targetCustomMappings = new HashMap<String, String>();
    private Map<String, Class> sourceCustomTypes = new HashMap<String, Class>();
    private Map<String, Class> targetCustomTypes = new HashMap<String, Class>();
    private MappingProfile mappingProfile;
    private boolean reverceMapping = true;

    public SimpleMappingBuilder(MappingProfile mappingProfile,
                                Class clazz)
    {
        this(mappingProfile, clazz, clazz);
    }

    public SimpleMappingBuilder(MappingProfile mappingProfile,
                                Class sourceClass,
                                Class targetClass)
    {
        super();
        this.mappingProfile = mappingProfile;
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    public Class<?> getTargetClass()
    {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass)
    {
        this.targetClass = targetClass;
    }

    public Class<?> getSourceClass()
    {
        return sourceClass;
    }

    public void setSourceClass(Class<?> sourceClass)
    {
        this.sourceClass = sourceClass;
    }

    public boolean isReverceMapping()
    {
        return reverceMapping;
    }

    public void setReverceMapping(boolean reverceMapping)
    {
        this.reverceMapping = reverceMapping;
    }

    public void addExcludeProperty(String propertyName)
    {
        excludeProperties.add(propertyName);
    }

    public void addCustomMapping(String sourceProperty,
                                 String targetProperty)
    {
        sourceCustomMappings.put(sourceProperty, targetProperty);
        targetCustomMappings.put(targetProperty, sourceProperty);
    }

    public void addSourceCustomType(String propertyName,
                                    Class type)
    {
        sourceCustomTypes.put(propertyName, type);
    }

    public void addTargetCustomType(String propertyName,
                                    Class type)
    {
        targetCustomTypes.put(propertyName, type);
    }

    public List<BeanMapping> buildMappings()
    {
        List<BeanMapping> result = new ArrayList<BeanMapping>();

        BeanMapping beanMapping = new BeanMapping();
        result.add(beanMapping);

        beanMapping.setSourceClass(getSourceClass());
        beanMapping.setTargetClass(getTargetClass());

        addConverters();

        Map<String, BeanPropertyDescriptor> sourceProperties = getProperties(getSourceClass());
        Map<String, BeanPropertyDescriptor> targetProperties = getProperties(getTargetClass());

        if (mappingProfile.isAllowDefaultMapping())
        {
            if (!Map.class.isAssignableFrom(targetClass))
            {
                for (Object propertyName : CollectionUtils.subtract(sourceProperties.keySet(),
                                                                    targetProperties.keySet()))
                {
                    sourceProperties.remove(propertyName);
                }
            }
            if (!Map.class.isAssignableFrom(sourceClass))
            {
                for (Object propertyName : CollectionUtils.subtract(targetProperties.keySet(),
                                                                    sourceProperties.keySet()))
                {
                    targetProperties.remove(propertyName);
                }
            }
        }
        sourceProperties.putAll(getCustomProperties(sourceClass, sourceCustomMappings.keySet()));
        targetProperties.putAll(getCustomProperties(targetClass, targetCustomMappings.keySet()));
        List<PropertyMapping> propertyMappings = createPropertyMappings(sourceProperties, targetProperties);
        beanMapping.setPropertyMappings(propertyMappings);
        for (PropertyMapping propertyMapping : propertyMappings)
        {
            BeanPropertyDescriptor srcProperty = propertyMapping.getSrcProperty();
            BeanPropertyDescriptor targetProperty = propertyMapping.getTargetProperty();
            if (sourceCustomTypes.containsKey(srcProperty.getPropertyName()))
            {
                srcProperty.setPropertyType(sourceCustomTypes.get(srcProperty.getPropertyName()));
            }
            if (targetCustomTypes.containsKey(targetProperty.getPropertyName()))
            {
                targetProperty.setPropertyType(targetCustomTypes.get(targetProperty.getPropertyName()));
            }
        }
        if (reverceMapping)
        {
            result.add(buildReverceMapping(beanMapping));
        }
        return result;
    }

    private void addConverters()
    {
        DefaultMappingProfile defaultProfile = getCastedDefaultProfile();
        if (defaultProfile == null || defaultProfile.getEntityManagerLocator() == null)
        {
            return;
        }
        Map<Class, Converter> converters = defaultProfile.getConverters();
        if (Identifiable.class.isAssignableFrom(sourceClass) || Identifiable.class.isAssignableFrom(targetClass))
        {
            defaultProfile.registerConverter(Long.class, new LongConverter());
        }
        if (isEntityClass(sourceClass))
        {
            defaultProfile.registerConverter(sourceClass,
                                             new EntityConverter(defaultProfile.getEntityManagerLocator(),
                                                     (Class<? extends Identifiable>) sourceClass));
        }
        if (isEntityClass(targetClass))
        {
            defaultProfile.registerConverter(targetClass,
                                             new EntityConverter(defaultProfile.getEntityManagerLocator(),
                                                     (Class<? extends Identifiable>) targetClass));
        }
    }

    private DefaultMappingProfile getCastedDefaultProfile()
    {
        return mappingProfile instanceof DefaultMappingProfile ? (DefaultMappingProfile) mappingProfile : null;
    }

    protected boolean isEntityClass(Class<?> c)
    {
        return Identifiable.class.isAssignableFrom(c) && c.isAnnotationPresent(Entity.class);
    }

    private List<PropertyMapping> createPropertyMappings(Map<String, BeanPropertyDescriptor> sourceProperties,
                                                         Map<String, BeanPropertyDescriptor> targetProperties)
    {
        List<PropertyMapping> result = new ArrayList<PropertyMapping>();
        if (!Map.class.isAssignableFrom(sourceClass) && !Map.class.isAssignableFrom(targetClass))
        {
            for (String sourcePropertyName : sourceProperties.keySet())
            {
                if (excludeProperties.contains(sourcePropertyName))
                {
                    continue;
                }
                String targetPropertyName = sourcePropertyName;
                if (sourceCustomMappings.containsKey(sourcePropertyName))
                {
                    targetPropertyName = sourceCustomMappings.get(sourcePropertyName);
                }
                else if (!targetProperties.containsKey(sourcePropertyName))
                {
                    throw new RuntimeException("Failed to create class mapping [" + sourceClass + "]-[" + targetClass
                            + "]. Property [" + sourcePropertyName + "] not found for class [" + targetClass + "]");
                }
                LOGGER.log(Level.FINE,
                           String.format("Property mapping: source-[%s.%s]; target-[%s.%s];",
                                         sourceClass.getSimpleName(),
                                         sourcePropertyName, targetClass.getSimpleName(), targetPropertyName));

                PropertyMapping propertyMapping = new PropertyMapping();
                propertyMapping.setSrcProperty(sourceProperties.get(sourcePropertyName));
                propertyMapping.setTargetProperty(targetProperties.get(targetPropertyName));
                result.add(propertyMapping);
            }
        }
        else if (Map.class.isAssignableFrom(sourceClass))
        {
            for (String targetPropertyName : targetProperties.keySet())
            {
                if (excludeProperties.contains(targetPropertyName))
                {
                    continue;
                }
                String sourcePropertyName = targetPropertyName;
                if (targetCustomMappings.containsKey(targetPropertyName))
                {
                    sourcePropertyName = targetCustomMappings.get(targetPropertyName);
                }
                PropertyMapping propertyMapping = new PropertyMapping();
                BeanPropertyDescriptor targetPropertyDescriptor = targetProperties.get(targetPropertyName);
                propertyMapping.setTargetProperty(targetPropertyDescriptor);
                BeanPropertyDescriptor sourcePropertyDescriptor = createMapPropertyDescriptor(sourcePropertyName,
                                                                                              targetPropertyDescriptor);
                propertyMapping.setSrcProperty(sourcePropertyDescriptor);
                result.add(propertyMapping);
            }
        }
        else if (Map.class.isAssignableFrom(targetClass))
        {
            for (String sourcePropertyName : sourceProperties.keySet())
            {
                if (excludeProperties.contains(sourcePropertyName))
                {
                    continue;
                }
                String targetPropertyName = sourcePropertyName;
                if (sourceCustomMappings.containsKey(sourcePropertyName))
                {
                    targetPropertyName = sourceCustomMappings.get(sourcePropertyName);
                }
                PropertyMapping propertyMapping = new PropertyMapping();
                BeanPropertyDescriptor sourcePropertyDescriptor = sourceProperties.get(sourcePropertyName);
                propertyMapping.setSrcProperty(sourcePropertyDescriptor);
                BeanPropertyDescriptor targetPropertyDescriptor = createMapPropertyDescriptor(targetPropertyName,
                                                                                              sourcePropertyDescriptor);
                propertyMapping.setTargetProperty(targetPropertyDescriptor);
                result.add(propertyMapping);
            }
        }
        return result;
    }

    private BeanPropertyDescriptor createMapPropertyDescriptor(String propertyName,
                                                               BeanPropertyDescriptor propertyDescriptor)
    {
        BeanPropertyDescriptor result = new BeanPropertyDescriptor();
        result.setPropertyName(propertyName);
        if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType()))
        {
            result.setPropertyType(propertyDescriptor.getPropertyType());
            Class listElementType = propertyDescriptor.getTypeArgs()[0];
            if (!isConvertableToMap(listElementType))
            {
                result.setTypeArgs(new Class[] { listElementType });
            }
            else
            {
                result.setTypeArgs(new Class[] { Map.class });
            }
        }
        else
        {
            if (!isConvertableToMap(propertyDescriptor.getPropertyType()))
            {
                result.setPropertyType(propertyDescriptor.getPropertyType());
            }
            else
            {
                result.setPropertyType(Map.class);
            }
        }
        return result;
    }

    private BeanPropertyDescriptor createBeanPropertyDescriptor(String propertyName,
                                                                PropertyDescriptor propertyDescriptor)
    {
        try
        {
            BeanPropertyDescriptor result = new BeanPropertyDescriptor();
            result.setPropertyName(propertyName);
            result.setWritable(propertyDescriptor.getWriteMethod() != null);
            result.setPropertyType(propertyDescriptor.getPropertyType());
            if (!propertyDescriptor.getPropertyType().isPrimitive()
                    && Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType()))
            {
                Class collectionElementType = getCollectionElementType(propertyDescriptor);
                result.setTypeArgs(new Class[] { collectionElementType });
            }
            return result;
        }
        catch (RuntimeException e)
        {
            LOGGER.log(Level.SEVERE, "Failed to create BeanPropertyDescriptor for property [" + propertyName
                    + "] of type [" + String.valueOf(propertyDescriptor.getPropertyType()) + "].", e);
            throw e;
        }
    }

    private Class getCollectionElementType(PropertyDescriptor propertyDescriptor)
    {
        Class[] genericTypeArguments;
        if (propertyDescriptor.getReadMethod() != null)
        {
            genericTypeArguments = CSBeanUtils.extractGenericTypeArguments(propertyDescriptor.getReadMethod(), -1);
        }
        else
        {
            genericTypeArguments = CSBeanUtils.extractGenericTypeArguments(propertyDescriptor.getWriteMethod(), 0);
        }
        Class collectionElementType = genericTypeArguments == null || genericTypeArguments.length == 0 ? Object.class
                : genericTypeArguments[0];
        return collectionElementType;
    }

    private Map<String, BeanPropertyDescriptor> getCustomProperties(Class type,
                                                                    Set<String> keySet)
    {
        Map<String, BeanPropertyDescriptor> result = new HashMap<String, BeanPropertyDescriptor>();
        if (Map.class.isAssignableFrom(type))
        {
            return result;
        }
        try
        {
            for (String property : keySet)
            {
                result.put(property,
                           createBeanPropertyDescriptor(property, CSBeanUtils.findPropertyDescriptor(type, property)));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to get properties descriptors for class - [" + type + "]", e);
        }
        return result;
    }

    private Map<String, BeanPropertyDescriptor> getProperties(Class type)
    {
        Map<String, BeanPropertyDescriptor> result = new HashMap<String, BeanPropertyDescriptor>();
        if (Map.class.isAssignableFrom(type))
        {
            return result;
        }
        try
        {
            for (PropertyDescriptor propertyDescriptor : CSBeanUtils.getPropertyDescriptors(type))
            {
                result.put(propertyDescriptor.getName(),
                           createBeanPropertyDescriptor(propertyDescriptor.getName(), propertyDescriptor));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to get properties descriptors for class - [" + type + "]", e);
        }
        return result;
    }

    private boolean isConvertableToMap(Class type)
    {
        return !mappingProfile.isSimpleType(type);
    }

    private BeanMapping buildReverceMapping(BeanMapping beanMapping)
    {
        BeanMapping reverceMapping = new BeanMapping();
        List<PropertyMapping> reverceMappings = new ArrayList<PropertyMapping>();
        reverceMapping.setPropertyMappings(reverceMappings);
        reverceMapping.setSourceClass(beanMapping.getTargetClass());
        reverceMapping.setTargetClass(beanMapping.getSourceClass());
        List<PropertyMapping> propertyMappings = beanMapping.getPropertyMappings();
        for (PropertyMapping propertyMapping : propertyMappings)
        {
            PropertyMapping revercePropertyMapping = new PropertyMapping();
            revercePropertyMapping.setSrcProperty(propertyMapping.getTargetProperty());
            revercePropertyMapping.setTargetProperty(propertyMapping.getSrcProperty());
            reverceMappings.add(revercePropertyMapping);
        }
        return reverceMapping;
    }

}
