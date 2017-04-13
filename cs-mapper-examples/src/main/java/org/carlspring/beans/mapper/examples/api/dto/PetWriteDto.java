package org.carlspring.beans.mapper.examples.api.dto;

import org.carlspring.beans.mapper.examples.domain.Pet;

public class PetWriteDto extends PetDto<PetWriteDto>
{

    public PetWriteDto()
    {
        super();
    }

    public PetWriteDto(Pet target)
    {
        super(target);
    }

}
