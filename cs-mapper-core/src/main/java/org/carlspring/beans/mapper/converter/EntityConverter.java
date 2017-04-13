package org.carlspring.beans.mapper.converter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.beanutils.converters.AbstractConverter;
import org.carlspring.beans.Identifiable;

public class EntityConverter extends AbstractConverter
{
    private EntityManagerFactory entityManagerFactory;
    private Class<? extends Identifiable> defaultType;

    public EntityConverter(EntityManagerFactory entityManagerFactory,
                           Class<? extends Identifiable> defaultType)
    {
        super();
        this.entityManagerFactory = entityManagerFactory;
        this.defaultType = defaultType;
    }

    @Override
    protected Object convertToType(Class type,
                                   Object value)
        throws Throwable
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        if (value != null)
        {
            return entityManager.find(type, (Long) value);
        }
        return null;
    }

    @Override
    protected Class getDefaultType()
    {
        return defaultType;
    }

}
