package org.carlspring.beans.mapper.examples.api.dto;

import org.carlspring.beans.mapper.examples.domain.Pet;
import org.carlspring.beans.mapper.markup.MappedProperty;

public class PetReadDto extends PetDto<PetReadDto>
{
    private String petStoreName;

    public PetReadDto()
    {
        super();
    }

    public PetReadDto(Pet target)
    {
        super(target);
    }

    @MappedProperty("petStore.name")
    public String getPetStoreName()
    {
        return petStoreName;
    }

    public void setPetStoreName(String petStoreName)
    {
        this.petStoreName = petStoreName;
    }

}
