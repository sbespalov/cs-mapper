package org.carlspring.beans.mapper.examples.repository;

import org.carlspring.beans.mapper.examples.domain.PetStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetStoreReposytory extends JpaRepository<PetStoreEntity, Long>
{

    PetStoreEntity findByName(String name);
    
}
