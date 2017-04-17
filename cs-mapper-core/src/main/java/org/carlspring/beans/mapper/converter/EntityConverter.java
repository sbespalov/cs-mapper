package org.carlspring.beans.mapper.converter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.beanutils.converters.AbstractConverter;
import org.carlspring.beans.Identifiable;
import org.carlspring.beans.jpa.EntityManagerLocator;

public class EntityConverter extends AbstractConverter
{
    private EntityManagerLocator entityManagerLocator;
    private Class<? extends Identifiable> defaultType;

    public EntityConverter(EntityManagerLocator entityManagerLocator,
                           Class<? extends Identifiable> defaultType)
    {
        super();
        this.entityManagerLocator = entityManagerLocator;
        this.defaultType = defaultType;
    }

    @Override
    protected Object convertToType(Class type,
                                   Object value)
        throws Throwable
    {
        EntityManager entityManager = entityManagerLocator.lookupEntityManager();
        
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
