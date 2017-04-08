package org.carlspring.beans.mapper.examples.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.beans.mapper.examples.domain.PetStoreEntity;
import org.junit.Before;
import org.junit.Test;

public class CommonMappintsTest extends ConfigurationTest
{
    @PersistenceContext
    private EntityManager entityManager;

    @Before
    private void setUp()
    {
        PetStoreEntity petStoreEntity = new PetStoreEntity();
        
    }

    @Test
    public void testNestedCollectionMapping()
    {

    }

}
