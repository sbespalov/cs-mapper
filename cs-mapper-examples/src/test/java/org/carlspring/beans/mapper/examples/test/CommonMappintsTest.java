package org.carlspring.beans.mapper.examples.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.beans.mapper.CSBeanMapper;
import org.carlspring.beans.mapper.examples.api.dto.PetSotreDto;
import org.carlspring.beans.mapper.examples.domain.PetStore;
import org.carlspring.beans.mapper.examples.domain.PetStoreEntity;
import org.carlspring.beans.mapper.examples.repository.PetStoreReposytory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonMappintsTest extends ConfigurationTest
{
    private static final String COLL_PET_STORE = "Coll Pet Store";

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PetStoreReposytory petStoreReposytory;
    @Autowired
    private CSBeanMapper beanMapper;

    @Before
    public void setUp()
    {
        PetStoreEntity petStoreEntity = new PetStoreEntity();
        petStoreEntity.setName(COLL_PET_STORE);
        entityManager.persist(petStoreEntity);

    }

    @Test
    public void testNestedCollectionMapping()
    {
        PetStoreEntity petStoreEntity = petStoreReposytory.findByName(COLL_PET_STORE);
        PetSotreDto petStoreDto = (PetSotreDto) beanMapper.convertObject(petStoreEntity, PetSotreDto.class);
    }

}
