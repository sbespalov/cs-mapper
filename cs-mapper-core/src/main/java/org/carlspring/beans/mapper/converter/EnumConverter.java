package org.carlspring.beans.mapper.converter;

import org.apache.commons.beanutils.Converter;
import org.carlspring.beans.Identifiable;

public class EnumConverter implements Converter
{

    public <T> T convert(Class<T> type,
                         Object value)
    {
        if (value instanceof String)
        {
            return (T) convertString((Class<Enum>) type, (String) value);
        }
        else if (value instanceof Number)
        {
            return (T) convertNumber((Class<Enum>) type, value);
        }
        return null;
    }

    protected <T extends Enum<T>> T convertNumber(Class<T> type,
                                                  Object value)
    {
        if (Identifiable.class.isAssignableFrom(type))
        {
            for (T enumVal : type.getEnumConstants())
            {
                if (((Identifiable) enumVal).getId().intValue() == ((Number) value).intValue())
                {
                    return enumVal;
                }
            }
        }
        else
        {
            for (T enumVal : type.getEnumConstants())
            {
                if (enumVal.ordinal() == ((Number) value).intValue())
                {
                    return enumVal;
                }
            }
        }
        return null;
    }

    protected <T extends Enum<T>> T convertString(Class<T> type,
                                                  String value)
    {
        return Enum.valueOf(type, value);
    }

}
