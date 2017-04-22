package org.carlspring.beans.mapper.examples.api.dto;

import java.util.HashSet;
import java.util.Set;

import org.carlspring.beans.mapper.examples.domain.PetStore;
import org.carlspring.beans.mapper.examples.domain.PetStoreBaseDto;

public class PetStoreReadDto extends PetStoreBaseDto<PetStoreReadDto>
{

    private Set<PetReadDto> petSet = new HashSet<PetReadDto>();

    public PetStoreReadDto()
    {
        super();
    }

    public PetStoreReadDto(PetStore target)
    {
        super(target);
    }

    public Set<PetReadDto> getPetSet()
    {
        return petSet;
    }

    public void setPetSet(Set<PetReadDto> petSet)
    {
        this.petSet = petSet;
    }

}
