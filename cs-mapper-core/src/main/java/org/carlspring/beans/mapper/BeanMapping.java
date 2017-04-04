package org.carlspring.beans.mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Bespalov
 */
public class BeanMapping {

	private Class sourceClass;

	private Class targetClass;

	private String mappingId;

	private List<PropertyMapping> propertyMappings;

	public String getMappingId() {
		return mappingId;
	}

	public void setMappingId(
								String mappingId) {
		this.mappingId = mappingId;
	}

	public Class getSourceClass() {
		return sourceClass;
	}

	public void setSourceClass(
								Class sourceClass) {
		this.sourceClass = sourceClass;
	}

	public Class getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(
								Class targetClass) {
		this.targetClass = targetClass;
	}

	public List<PropertyMapping> getPropertyMappings() {
		return propertyMappings;
	}

	public void setPropertyMappings(
									List<PropertyMapping> propertyMappings) {
		this.propertyMappings = propertyMappings;
	}

	public void addPropertyMapping(
									PropertyMapping mapping) {
		if (propertyMappings == null) {
			propertyMappings = new ArrayList<PropertyMapping>();
		}
		propertyMappings.add(mapping);
	}

}
