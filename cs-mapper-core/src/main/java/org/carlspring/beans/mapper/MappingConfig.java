package org.carlspring.beans.mapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MappingConfig
{

    private static final Logger LOGGER = Logger.getLogger(MappingConfig.class.getName());

    private MappingProfile mappingProfile = new DefaultMappingProfile();
    private Map<String, BeanMapping> mappings = new ConcurrentHashMap<String, BeanMapping>();

    public MappingConfig()
    {
        this(new DefaultMappingProfile());
    }

    public MappingConfig(MappingProfile mappingProfile)
    {
        super();
        this.mappingProfile = mappingProfile;
    }

    public MappingProfile getMappingProfile()
    {
        return mappingProfile;
    }

    public void setMappingProfile(MappingProfile mappingProfile)
    {
        this.mappingProfile = mappingProfile;
    }

    public BeanMapping getBeanMapping(Class targetClass,
                                      Class sourceClass)
    {
        String key = createMappingKey(sourceClass.getName(), targetClass.getName());
        BeanMapping result = mappings.get(key);
        if (result != null)
        {
            return result;
        }

        key = createMappingKey(sourceClass.getName(), targetClass.getName());
        result = mappings.get(key);
        if (result != null)
        {
            return result;
        }
        if (!mappingProfile.isAllowDefaultMapping() && !sourceClass.isAssignableFrom(targetClass)
                && !targetClass.isAssignableFrom(sourceClass))
        {
            throw new RuntimeException(String.format(
                                                     "Mapping [%s] to [%s] not allowed. You can avoid this with 'MappingProfile.isAllowDefaultMapping()'",
                                                     sourceClass, targetClass));
        }

        LOGGER.log(Level.INFO, String.format("Mapping not found, create: source-[%s]; target-[%s]",
                                             sourceClass.getName(), targetClass.getName()));

        synchronized (mappings)
        {
            MappingBuilder mappingBuilder = createMappingBuilder(targetClass, sourceClass);

            List<BeanMapping> beanMappings = mappingBuilder.buildMappings();
            for (BeanMapping beanMapping : beanMappings)
            {
                String keyLocal = createMappingKey(beanMapping);
                mappings.put(keyLocal, beanMapping);
                if (key.equals(keyLocal))
                {
                    result = beanMapping;
                }
            }
        }
        return result;
    }

    protected MappingBuilder createMappingBuilder(Class targetClass,
                                                  Class sourceClass)
    {
        return new AnnotationMappingBuilder(mappingProfile, targetClass, sourceClass);
    }

    public void registerMappings(MappingBuilder mappingBuilder)
    {
        synchronized (mappings)
        {
            List<BeanMapping> beanMappings = mappingBuilder.buildMappings();
            for (BeanMapping beanMapping : beanMappings)
            {
                String key = createMappingKey(beanMapping);
                mappings.put(key, beanMapping);
            }
        }

    }

    private String createMappingKey(BeanMapping beanMapping)
    {
        return createMappingKey(beanMapping.getSourceClass().getName(), beanMapping.getTargetClass().getName());
    }

    private String createMappingKey(String sourceClass,
                                    String targetClass)
    {
        return "[" + sourceClass + "]-[" + targetClass + "]";
    }

}
