package org.carlspring.beans.mapper.examples.api.dto;

import org.carlspring.beans.mapper.CSBeanUtils;
import org.carlspring.beans.mapper.examples.domain.PetStore;

public class PetStoreDto<T extends PetStoreDto<T>> implements PetStore
{

    private PetStore target = CSBeanUtils.createBeanInstance(PetStore.class);

    public PetStoreDto()
    {
        super();
    }

    public PetStoreDto(PetStore target)
    {
        super();
        this.target = target;
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