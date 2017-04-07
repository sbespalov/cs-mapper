package org.carlspring.beans.mapper.spring;

import java.util.List;

import org.carlspring.beans.mapper.AnnotationMappingBuilder;
import org.carlspring.beans.mapper.CSBeanMapper;
import org.carlspring.beans.mapper.DefaultMappingProfile;
import org.carlspring.beans.mapper.MappingConfig;
import org.carlspring.beans.mapper.MappingProfile;
import org.carlspring.beans.mapper.XMLMappingBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Sergey Bespalov
 */
public class BeanHelperFactoryBean implements FactoryBean<CSBeanMapper>, InitializingBean
{

    private List<Class<?>> mappedClasses;
    private List<String> mappingLocations;
    private CSBeanMapper beanMapper;
    private MappingProfile mappingProfile;
    private ClassLoader beanClassLoader;
    private Class<? extends CSBeanMapper> beanHelperClass;

    public BeanHelperFactoryBean()
    {
        super();
        beanClassLoader = Thread.currentThread().getContextClassLoader();
        beanHelperClass = CSBeanMapper.class;
        mappingProfile = new DefaultMappingProfile();
    }

    public Class<? extends CSBeanMapper> getBeanHelperClass()
    {
        return beanHelperClass;
    }

    public void setBeanHelperClass(Class<? extends CSBeanMapper> beanHelperClass)
    {
        this.beanHelperClass = beanHelperClass;
    }

    public void setBeanClassLoader(ClassLoader beanClassLoader)
    {
        this.beanClassLoader = beanClassLoader;
    }

    public void setMappingProfile(MappingProfile mappingProfile)
    {
        this.mappingProfile = mappingProfile;
    }

    public void setMappedClasses(List<Class<?>> mappedClasses)
    {
        this.mappedClasses = mappedClasses;
    }

    public List<String> getMappingLocations()
    {
        return mappingLocations;
    }

    public void setMappingLocations(
                                    List<String> mappingLocations)
    {
        this.mappingLocations = mappingLocations;
    }

    public void afterPropertiesSet()
        throws Exception
    {
        MappingConfig mappingConfig = createMappingConfig();
        try
        {
            beanMapper = beanHelperClass.getConstructor(MappingConfig.class).newInstance(mappingConfig);
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    String.format("Failed to create [%s] instance.", beanHelperClass.getName()), e);
        }
    }

    private MappingConfig createMappingConfig()
    {
        MappingConfig mappingConfig = new MappingConfig();
        if (mappedClasses != null)
        {
            for (Class mappedClass : mappedClasses)
            {
                mappingConfig.registerMappings(new AnnotationMappingBuilder(mappingProfile, mappedClass));
            }
        }
        if (mappingLocations != null)
        {
            for (String mappingLocation : mappingLocations)
            {
                mappingConfig.registerMappings(new XMLMappingBuilder(mappingProfile, beanClassLoader, mappingLocation));
            }
        }
        if (mappingProfile != null)
        {
            mappingConfig.setMappingProfile(mappingProfile);
        }
        else
        {
            mappingConfig.setMappingProfile(new DefaultMappingProfile());
        }
        return mappingConfig;
    }

    public CSBeanMapper getObject()
        throws Exception
    {
        return beanMapper;
    }

    public Class<?> getObjectType()
    {
        return CSBeanMapper.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

}
