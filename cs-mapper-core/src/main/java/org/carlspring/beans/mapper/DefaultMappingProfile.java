/**
 * 
 */
package org.carlspring.beans.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BigIntegerConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.ByteConverter;
import org.apache.commons.beanutils.converters.CharacterConverter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.ShortConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.carlspring.beans.jpa.EntityManagerLocator;

/**
 * @author Sergey Bespalov
 */
public class DefaultMappingProfile implements MappingProfile
{

    private static final Logger LOGGER = Logger.getLogger(DefaultMappingProfile.class.getName());

    private Map<Class, Converter> converters = new HashMap<Class, Converter>();
    private boolean allowDefaultMapping = true;
    private EntityManagerLocator entityManagerLocator;

    public DefaultMappingProfile()
    {
        super();
    }

    public DefaultMappingProfile(EntityManagerLocator entityManagerLocator)
    {
        super();
        this.entityManagerLocator = entityManagerLocator;
    }

    public boolean isSimpleType(Class type)
    {
        return (type.isPrimitive() || type.isEnum() || Number.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type) || Character.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type) || java.util.Date.class.isAssignableFrom(type));
    }

    public Converter lookupConverter(Class type)
    {
        Converter converter = converters.get(type);
        if (converter == null)
        {
            throw new RuntimeException("Converter not found: target class-[" + type + "]");
        }
        return converter;
    }

    public boolean isAllowDefaultMapping()
    {
        return allowDefaultMapping;
    }

    public void setAllowDefaultMapping(boolean allowDefaultMapping)
    {
        this.allowDefaultMapping = allowDefaultMapping;
    }

    public void registerConverter(Class type,
                                  Converter converter)
    {
        Converter mappedConverter = converters.get(type);
        if (mappedConverter == null || mappedConverter.getClass() != converter.getClass())
        {
            converters.put(type, converter);
            LOGGER.log(Level.INFO,
                       String.format("Converter registered: class-[%s]; converter-[%s];", type, converter));
        }
    }

    public void registerDefault()
    {
        registerDefault(false, true);
    }

    public void registerDefault(boolean throwException,
                                boolean defaultNull)
    {
        Number defaultNumber = defaultNull ? null : 0;
        BigDecimal bigDecDeflt = defaultNull ? null : new BigDecimal("0.0");
        BigInteger bigIntDeflt = defaultNull ? null : new BigInteger("0");
        Boolean booleanDefault = defaultNull ? null : Boolean.FALSE;
        Character charDefault = defaultNull ? null : ' ';
        String stringDefault = defaultNull ? null : "";

        registerConverter(BigDecimal.class, throwException ? new BigDecimalConverter()
                : new BigDecimalConverter(bigDecDeflt));
        registerConverter(BigInteger.class, throwException ? new BigIntegerConverter()
                : new BigIntegerConverter(bigIntDeflt));
        registerConverter(Boolean.class,
                          throwException ? new BooleanConverter() : new BooleanConverter(booleanDefault));
        registerConverter(Byte.class, throwException ? new ByteConverter() : new ByteConverter(defaultNumber));
        registerConverter(Character.class, throwException ? new CharacterConverter()
                : new CharacterConverter(charDefault));
        registerConverter(Double.class, throwException ? new DoubleConverter() : new DoubleConverter(defaultNumber));
        registerConverter(Float.class, throwException ? new FloatConverter() : new FloatConverter(defaultNumber));
        registerConverter(Integer.class, throwException ? new IntegerConverter() : new IntegerConverter(defaultNumber));
        registerConverter(Long.class, throwException ? new LongConverter() : new LongConverter(defaultNumber));
        registerConverter(Short.class, throwException ? new ShortConverter() : new ShortConverter(defaultNumber));
        registerConverter(String.class, throwException ? new StringConverter() : new StringConverter(stringDefault));

        registerConverter(Boolean.TYPE, throwException ? new BooleanConverter() : new BooleanConverter(Boolean.FALSE));
        registerConverter(Byte.TYPE, throwException ? new ByteConverter() : new ByteConverter(0));
        registerConverter(Character.TYPE, throwException ? new CharacterConverter() : new CharacterConverter(' '));
        registerConverter(Double.TYPE, throwException ? new DoubleConverter() : new DoubleConverter(0));
        registerConverter(Float.TYPE, throwException ? new FloatConverter() : new FloatConverter(0));
        registerConverter(Integer.TYPE, throwException ? new IntegerConverter() : new IntegerConverter(0));
        registerConverter(Long.TYPE, throwException ? new LongConverter() : new LongConverter(0));
        registerConverter(Short.TYPE, throwException ? new ShortConverter() : new ShortConverter(0));

    }

    public Map<Class, Converter> getConverters()
    {
        return converters;
    }

    public void setConverters(Map<Class, Converter> converters)
    {
        this.converters = converters;
    }

    public EntityManagerLocator getEntityManagerLocator()
    {
        return entityManagerLocator;
    }

    public void setEntityManagerLocator(EntityManagerLocator entityManagerLocator)
    {
        this.entityManagerLocator = entityManagerLocator;
    }

}
