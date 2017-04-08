package org.carlspring.beans.mapper.examples.domain;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.carlspring.beans.Identifiable;

@MappedSuperclass
public class CommonEntity implements Identifiable
{

    private Long id;

    @Id
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

}
