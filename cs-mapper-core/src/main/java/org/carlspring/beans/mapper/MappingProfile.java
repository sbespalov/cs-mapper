package org.carlspring.beans.mapper;

import org.apache.commons.beanutils.Converter;

public interface MappingProfile
{

    public boolean isSimpleType(Class type);

    public Converter lookupConverter(Class type);

    public boolean isAllowDefaultMapping();
}
