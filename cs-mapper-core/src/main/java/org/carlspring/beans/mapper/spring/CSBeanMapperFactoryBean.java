package org.carlspring.beans.mapper.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import org.carlspring.beans.mapper.AnnotationMappingBuilder;
import org.carlspring.beans.mapper.CSBeanMapper;
import org.carlspring.beans.mapper.DefaultMappingProfile;
import org.carlspring.beans.mapper.MappingConfig;
import org.carlspring.beans.mapper.MappingProfile;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Sergey Bespalov
 */
public class CSBeanMapperFactoryBean implements FactoryBean<CSBeanMapper>, InitializingBean
{
    private static final Logger LOGGER = Logger.getLogger(CSBeanMapperFactoryBean.class.getName());

    private List<Class<?>> mappedClasses;
    private CSBeanMapper beanMapper;
    private MappingProfile mappingProfile;
    private Class<? extends CSBeanMapper> beanHelperClass;
    private String[] packagesToScan = new String[] {};

    public CSBeanMapperFactoryBean()
    {
        this(new DefaultMappingProfile());
    }

    public CSBeanMapperFactoryBean(EntityManagerFactory entityManagerFactory)
    {
        this(new DefaultMappingProfile(entityManagerFactory));
    }

    public CSBeanMapperFactoryBean(MappingProfile mappingProfile)
    {
        super();
        this.beanHelperClass = CSBeanMapper.class;
        this.mappingProfile = mappingProfile;
    }

    public void setPackagesToScan(String... packagesToScan)
    {
        this.packagesToScan = packagesToScan;
    }

    public Class<? extends CSBeanMapper> getBeanHelperClass()
    {
        return beanHelperClass;
    }

    public void setBeanHelperClass(Class<? extends CSBeanMapper> beanHelperClass)
    {
        this.beanHelperClass = beanHelperClass;
    }

    public void setMappingProfile(MappingProfile mappingProfile)
    {
        this.mappingProfile = mappingProfile;
    }

    public void setMappedClasses(List<Class<?>> mappedClasses)
    {
        this.mappedClasses = mappedClasses;
    }

    public void afterPropertiesSet()
        throws Exception
    {
        if (mappingProfile == null)
        {
            throw new BeanInitializationException(String.format("%s required.", MappingProfile.class.getSimpleName()));
        }

        MappingConfig mappingConfig = createMappingConfig();
        try
        {
            beanMapper = beanHelperClass.getConstructor(MappingConfig.class).newInstance(mappingConfig);
        }
        catch (Exception e)
        {
            throw new BeanInitializationException(
                    String.format("Failed to create [%s] instance.", beanHelperClass.getName()), e);
        }
    }

    private MappingConfig createMappingConfig()
    {
        MappingConfig mappingConfig = new MappingConfig();
        mappingConfig.setMappingProfile(mappingProfile);

        addMappings(mappingConfig);

        return mappingConfig;
    }

    private void addMappings(MappingConfig mappingConfig)
    {
        if (mappedClasses == null)
        {
            return;
        }
        List<Object> reflectionsConfig = new ArrayList<Object>();
        for (String packageName : packagesToScan)
        {
            reflectionsConfig.add(packageName);
        }
        reflectionsConfig.add(Thread.currentThread().getContextClassLoader());

        Reflections reflections = new Reflections(reflectionsConfig.toArray(new Object[] {}));

        for (Class<?> mappedClass : mappedClasses)
        {
            mappingConfig.registerMappings(new AnnotationMappingBuilder(mappingProfile, mappedClass));

            Set<?> domainClasses = reflections.getSubTypesOf(mappedClass);
            for (Object dtoClass : domainClasses)
            {
                mappingConfig.registerMappings(new AnnotationMappingBuilder(mappingProfile,
                        (Class) dtoClass));
                LOGGER.log(Level.FINE, String.format("DTO mapping registered: class-[%s]", dtoClass));

            }
            // String dtoClassName = mappedClass.getCanonicalName() + "Dto";
        }
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
