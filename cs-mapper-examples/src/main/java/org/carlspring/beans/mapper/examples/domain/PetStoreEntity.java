package org.carlspring.beans.mapper.examples.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class PetStoreEntity extends CommonEntity
{

    private String name;
    private Set<PetEntity> petSet;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @OneToMany
    public Set<PetEntity> getPetSet()
    {
        return petSet;
    }

    public void setPetSet(Set<PetEntity> petSet)
    {
        this.petSet = petSet;
    }

}
