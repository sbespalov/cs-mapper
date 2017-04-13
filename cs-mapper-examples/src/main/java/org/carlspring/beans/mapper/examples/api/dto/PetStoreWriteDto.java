package org.carlspring.beans.mapper.examples.api.dto;

import java.util.HashSet;
import java.util.Set;

import org.carlspring.beans.mapper.examples.domain.PetStore;
import org.carlspring.beans.mapper.markup.MappedProperty;

public class PetStoreWriteDto extends PetStoreDto<PetStoreWriteDto>
{

    private Set<Long> petIdSet = new HashSet<Long>();

    public PetStoreWriteDto()
    {
        super();
    }

    public PetStoreWriteDto(PetStore target)
    {
        super(target);
    }

    @MappedProperty("petSet")
    public Set<Long> getPetIdSet()
    {
        return petIdSet;
    }

    public void setPetIdSet(Set<Long> petSet)
    {
        this.petIdSet = petSet;
    }

}
