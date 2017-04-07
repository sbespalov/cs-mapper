package org.carlspring.beans.utils;

import java.util.ArrayList;
import java.util.List;

public class BeanDescriptor {

	private String className;
	
	private String packageName;
	
	private List<BeanProperty> properties = new ArrayList<BeanProperty>();
	
	private List<AnnotationDescriptor> annotations = new ArrayList<AnnotationDescriptor>();

	public String getClassName() {
		return className;
	}

	public void setClassName(
								String className) {
		this.className = className;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(
								String packageName) {
		this.packageName = packageName;
	}

	public List<BeanProperty> getProperties() {
		return properties;
	}

	public void setProperties(
								List<BeanProperty> properties) {
		this.properties = properties;
	}

	public List<AnnotationDescriptor> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(
								List<AnnotationDescriptor> annotations) {
		this.annotations = annotations;
	}
	
}
