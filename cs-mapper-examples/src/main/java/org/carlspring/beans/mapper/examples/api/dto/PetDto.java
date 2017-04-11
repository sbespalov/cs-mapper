package org.carlspring.beans.mapper.examples.api.dto;

import org.carlspring.beans.mapper.CSBeanUtils;
import org.carlspring.beans.mapper.examples.domain.Pet;

public class PetDto implements Pet
{
    private Pet target = CSBeanUtils.createBeanInstance(Pet.class);

    public Long getPetStoreId()
    {
        return target.getPetStoreId();
    }

    public void setPetStoreId(Long pPetStoreId)
    {
        target.setPetStoreId(pPetStoreId);
    }

    public String getName()
    {
        return target.getName();
    }

    public void setName(String pName)
    {
        target.setName(pName);
    }

    public Long getPetId()
    {
        return target.getPetId();
    }

    public void setPetId(Long pPetId)
    {
        target.setPetId(pPetId);
    }

}