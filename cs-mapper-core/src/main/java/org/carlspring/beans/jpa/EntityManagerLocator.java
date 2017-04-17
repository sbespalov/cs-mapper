package org.carlspring.beans.jpa;

import javax.persistence.EntityManager;

public interface EntityManagerLocator
{

    EntityManager lookupEntityManager();

}
