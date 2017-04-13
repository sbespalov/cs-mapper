package org.carlspring.beans.mapper.examples.api;

import org.carlspring.beans.mapper.examples.api.dto.PetStoreReadDto;
import org.carlspring.beans.mapper.examples.api.dto.PetStoreWriteDto;

public interface PetStoreApi
{

    public PetStoreReadDto getPetStore(Long id);

    public PetStoreReadDto putPetStore(PetStoreWriteDto petStore);

}
