package org.carlspring.beans.mapper.examples.api.dto;

import org.carlspring.beans.mapper.examples.domain.Pet;
import org.carlspring.beans.mapper.examples.domain.PetBaseDto;

public class PetWriteDto extends PetBaseDto<PetWriteDto>
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
