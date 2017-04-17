package org.carlspring.beans.mapper.spring;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.carlspring.beans.jpa.EntityManagerLocator;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

public class TransactionAwareEntityManagerLocator implements EntityManagerLocator
{

    private EntityManagerFactory emf;

    public TransactionAwareEntityManagerLocator(EntityManagerFactory emf)
    {
        super();
        this.emf = emf;
    }

    public EntityManager lookupEntityManager()
    {
        return EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
    }

}
