package org.carlspring.beans.mapper.examples.api.dto;

import java.util.HashSet;
import java.util.Set;

import org.carlspring.beans.mapper.CSBeanUtils;
import org.carlspring.beans.mapper.examples.domain.PetStore;

public class PetSotreDto implements PetStore
{

    private PetStore target = CSBeanUtils.createBeanInstance(PetStore.class);
    private Set<PetDto> petSet = new HashSet<PetDto>();

    public Set<PetDto> getPetSet()
    {
        return petSet;
    }

    public void setPetSet(Set<PetDto> petSet)
    {
        this.petSet = petSet;
    }

    public String getName()
    {
        return target.getName();
    }

    public void setName(String pName)
    {
        target.setName(pName);
    }

    public Long getPetStoreId()
    {
        return target.getPetStoreId();
    }

    public void setPetStoreId(Long pPetStoreId)
    {
        target.setPetStoreId(pPetStoreId);
    }

}
