package org.carlspring.beans.mapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

public class MappingConfig
{

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
                                      Class sourceClass,
                                      String mappingId)
    {
        String key = createMappingKey(sourceClass.getName(), targetClass.getName(), mappingId);
        BeanMapping result = mappings.get(key);
        if (result != null)
        {
            return result;
        }

        key = createMappingKey(sourceClass.getName(), targetClass.getName(), null);
        result = mappings.get(key);
        if (result != null)
        {
            return result;
        }
        if (!mappingProfile.isAllowDefaultMapping() && !sourceClass.isAssignableFrom(targetClass)
                && !targetClass.isAssignableFrom(sourceClass))
        {
            throw new RuntimeException(
                    String.format("Mapping [%s] to [%s] not allowed. You can avoid this with 'MappingProfile.isAllowDefaultMapping()'",
                                  sourceClass, targetClass));
        }

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
        AnnotationMappingBuilder mappingBuilder = targetClass.isAssignableFrom(sourceClass)
                ? new AnnotationMappingBuilder(mappingProfile, targetClass, sourceClass)
                : new AnnotationMappingBuilder(mappingProfile, sourceClass, targetClass);
        return mappingBuilder;
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
        return createMappingKey(beanMapping.getSourceClass().getName(), beanMapping.getTargetClass().getName(),
                                beanMapping.getMappingId());
    }

    private String createMappingKey(String sourceClass,
                                    String targetClass,
                                    String mappingId)
    {
        return (StringUtils.isBlank(mappingId) ? "" : "[" + mappingId + "]-") + "[" + sourceClass + "]-[" + targetClass
                + "]";
    }

}
