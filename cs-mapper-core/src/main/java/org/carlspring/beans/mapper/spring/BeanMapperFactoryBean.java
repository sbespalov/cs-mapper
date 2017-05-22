package org.carlspring.beans.mapper.spring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import org.carlspring.beans.mapper.AnnotationMappingBuilder;
import org.carlspring.beans.mapper.BeanMapper;
import org.carlspring.beans.mapper.DefaultMappingProfile;
import org.carlspring.beans.mapper.MappingConfig;
import org.carlspring.beans.mapper.MappingProfile;
import org.carlspring.beans.mapper.markup.MappedBean;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.classloaderhandler.WebSphereClassLoaderHandler;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

/**
 * @author Sergey Bespalov
 */
public class BeanMapperFactoryBean implements FactoryBean<BeanMapper>, InitializingBean
{
    private static final Logger LOGGER = Logger.getLogger(BeanMapperFactoryBean.class.getName());

    private BeanMapper beanMapper;
    private MappingProfile mappingProfile;
    private Class<? extends BeanMapper> beanHelperClass;
    private String[] packagesToScan = new String[] {};
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public BeanMapperFactoryBean()
    {
        this(new DefaultMappingProfile());
    }

    public BeanMapperFactoryBean(EntityManagerFactory entityManagerFactory)
    {
        this(new DefaultMappingProfile(new TransactionAwareEntityManagerLocator(entityManagerFactory)));
    }

    public BeanMapperFactoryBean(MappingProfile mappingProfile)
    {
        super();
        this.beanHelperClass = BeanMapper.class;
        this.mappingProfile = mappingProfile;
    }

    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    public void setPackagesToScan(String... packagesToScan)
    {
        this.packagesToScan = packagesToScan;
    }

    public Class<? extends BeanMapper> getBeanHelperClass()
    {
        return beanHelperClass;
    }

    public void setBeanHelperClass(Class<? extends BeanMapper> beanHelperClass)
    {
        this.beanHelperClass = beanHelperClass;
    }

    public void setMappingProfile(MappingProfile mappingProfile)
    {
        this.mappingProfile = mappingProfile;
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

    private void addMappings(final MappingConfig mappingConfig)
    {
        List<Object> reflectionsConfig = new ArrayList<Object>();
        if (packagesToScan.length == 0)
        {
            return;
        }

        for (String packageName : packagesToScan)
        {
            reflectionsConfig.add(packageName);
        }

        ScanResult scanResult = new FastClasspathScanner(
                reflectionsConfig.toArray(new String[] {})).addClassLoader(classLoader)
                                                           .registerClassLoaderHandler(WebSphereClassLoaderHandler.class)
                                                           .verbose(true)
                                                           .scan();
        for (String mappedClassName : scanResult.getNamesOfClassesWithAnnotation(MappedBean.class))
        {
            Class<?> mappedClass;
            try
            {
                mappedClass = Class.forName(mappedClassName);
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            mappingConfig.registerMappings(new AnnotationMappingBuilder(
                    mappingProfile,
                    mappedClass));
            LOGGER.log(Level.INFO, String.format("DTO mapping registered: class-[%s]", mappedClass));

            Set<String> mappedSubClassNameSet = new HashSet<String>(scanResult.getNamesOfSubclassesOf(mappedClassName));
            mappedSubClassNameSet.addAll(scanResult.getNamesOfClassesImplementing(mappedClassName));

            for (String mappedSubClassName : mappedSubClassNameSet)
            {
                Class dtoClass;
                try
                {
                    dtoClass = Class.forName(mappedSubClassName);
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }

                mappingConfig.registerMappings(new AnnotationMappingBuilder(mappingProfile,
                        (Class) dtoClass));
                LOGGER.log(Level.INFO, String.format("DTO mapping registered: class-[%s]", dtoClass));

            }
        }

        // scanResult.getNamesOfSubclassesOf(superclass)

        // for (Class<?> mappedClass :
        // reflections.getTypesAnnotatedWith(MappedBean.class))
        // {
        // mappingConfig.registerMappings(new
        // AnnotationMappingBuilder(mappingProfile, mappedClass));
        //
        // Set<?> domainClasses = reflections.getSubTypesOf(mappedClass);
        // for (Object dtoClass : domainClasses)
        // {
        // mappingConfig.registerMappings(new
        // AnnotationMappingBuilder(mappingProfile,
        // (Class) dtoClass));
        // LOGGER.log(Level.FINE, String.format("DTO mapping registered:
        // class-[%s]", dtoClass));
        //
        // }
        // }
    }

    public BeanMapper getObject()
        throws Exception
    {
        return beanMapper;
    }

    public Class<?> getObjectType()
    {
        return BeanMapper.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

}
