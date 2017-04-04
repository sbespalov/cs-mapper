package org.carlspring.beans.mapper;

public class BeanPropertyDescriptor {

	private boolean writable = true;
	
	private String propertyName;

	private Class propertyType;

	private Class[] typeArgs = new Class[] {};

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(
							boolean writable) {
		this.writable = writable;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(
								String propertyName) {
		this.propertyName = propertyName;
	}

	public Class getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(
								Class propertyType) {
		this.propertyType = propertyType;
	}

	public Class[] getTypeArgs() {
		return typeArgs;
	}

	public void setTypeArgs(
							Class[] typeArgs) {
		this.typeArgs = typeArgs;
	}

}
