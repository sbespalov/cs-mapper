package org.carlspring.beans.mapper.examples.api.dto;

import java.util.HashSet;
import java.util.Set;

import org.carlspring.beans.mapper.examples.domain.PetStore;

public class PetStoreReadDto extends PetStoreDto<PetStoreReadDto>
{

    private Set<PetDto> petSet = new HashSet<PetDto>();

    public PetStoreReadDto()
    {
        super();
    }

    public PetStoreReadDto(PetStore target)
    {
        super(target);
    }

    public Set<PetDto> getPetSet()
    {
        return petSet;
    }

    public void setPetSet(Set<PetDto> petSet)
    {
        this.petSet = petSet;
    }

}
