package org.carlspring.beans.mapper.examples;

import java.beans.PropertyVetoException;

import javax.persistence.EntityManagerFactory;

import org.carlspring.beans.mapper.CSBeanMapper;
import org.carlspring.beans.mapper.spring.BeanHelperFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories
public class CSMapperExamplesConfiguration
{

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf)
    {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory()
        throws ClassNotFoundException,
        PropertyVetoException
    {
        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setPersistenceUnitName("cs-mapper-examples");
        result.setPackagesToScan("org.carlspring.beans.mapper.examples");
        return result;
    }

    @Bean
    public BeanHelperFactoryBean beanMapper()
    {
        return new BeanHelperFactoryBean();
    }
}
