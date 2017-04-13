package org.carlspring.beans.mapper.examples.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class PetEntity extends CommonEntity
{

    private String name;
    private PetStoreEntity petStore;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @ManyToOne(optional = true)
    public PetStoreEntity getPetStore()
    {
        return petStore;
    }

    public void setPetStore(PetStoreEntity petStore)
    {
        this.petStore = petStore;
    }

}
