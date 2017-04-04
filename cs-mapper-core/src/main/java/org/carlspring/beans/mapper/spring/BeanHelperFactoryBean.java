package org.carlspring.beans.mapper.spring;

import java.util.List;

import org.carlspring.beans.mapper.AnnotationMappingBuilder;
import org.carlspring.beans.mapper.BeanHelper;
import org.carlspring.beans.mapper.DefaultMappingProfile;
import org.carlspring.beans.mapper.MappingConfig;
import org.carlspring.beans.mapper.MappingProfile;
import org.carlspring.beans.mapper.XMLMappingBuilder;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Sergey Bespalov
 */
public class BeanHelperFactoryBean implements FactoryBean<BeanHelper>, InitializingBean
{

    private List<Class<?>> mappedClasses;
    private List<String> mappingLocations;
    private BeanHelper beanMapper;
    private MappingProfile mappingProfile;
    private ClassLoader beanClassLoader;
    private Class<? extends BeanHelper> beanHelperClass;

    public BeanHelperFactoryBean()
    {
        super();
        beanClassLoader = Thread.currentThread().getContextClassLoader();
        beanHelperClass = BeanHelper.class;
        mappingProfile = new DefaultMappingProfile();
    }

    public Class<? extends BeanHelper> getBeanHelperClass()
    {
        return beanHelperClass;
    }

    public void setBeanHelperClass(
                                   Class<? extends BeanHelper> beanHelperClass)
    {
        this.beanHelperClass = beanHelperClass;
    }

    public ClassLoader getBeanClassLoader()
    {
        return beanClassLoader;
    }

    public void setBeanClassLoader(
                                   ClassLoader beanClassLoader)
    {
        this.beanClassLoader = beanClassLoader;
    }

    public MappingProfile getMappingProfile()
    {
        return mappingProfile;
    }

    public void setMappingProfile(
                                  MappingProfile mappingProfile)
    {
        this.mappingProfile = mappingProfile;
    }

    public List<Class<?>> getMappedClasses()
    {
        return mappedClasses;
    }

    public void setMappedClasses(
                                 List<Class<?>> mappedClasses)
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
        try
        {
            beanMapper = beanHelperClass.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create [" + beanHelperClass + "] instance.", e);
        }
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
        beanMapper.setMappingConfig(mappingConfig);
    }

    public BeanHelper getObject()
        throws Exception
    {
        return beanMapper;
    }

    public Class<?> getObjectType()
    {
        return BeanHelper.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

}
