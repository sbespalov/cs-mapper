package org.carlspring.beans.mapper.converter;

import java.math.BigDecimal;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.NumberConverter;
import org.carlspring.beans.Identifiable;

public class LongConverter implements Converter {

	private Long defaultValue;

	public LongConverter() {
		super();
	}

	public LongConverter(Long defaultValue) {
		super();
		this.defaultValue = defaultValue;
	}

	private Converter longConverter = new NumberConverter(false) {

		@Override
		protected Object convertToType(Class targetType, Object value) throws Throwable {
			try {
				return super.convertToType(targetType, value);
			} catch (NumberFormatException e) {
				String stringValue = value.toString().trim();
				return super.convert(targetType, new BigDecimal(stringValue));
			} catch (org.apache.commons.beanutils.ConversionException e) {
				if (value == null || value.toString().trim().length() == 0) {
					return defaultValue;
				}
				throw e;
			}
		}

		@Override
		protected Class getDefaultType() {
			return Long.class;
		}

	};

	public Object convert(Class type, Object value) {
		if (value != null) {
			Class<? extends Object> clazz = value.getClass();
			if (Identifiable.class.isAssignableFrom(clazz)) {
				return ((Identifiable) value).getId();
			} else if (Enum.class.isAssignableFrom(clazz)) {
				return ((Enum) value).ordinal();
			}
		}
		return longConverter.convert(type, value);
	}
}
