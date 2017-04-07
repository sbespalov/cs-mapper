package org.carlspring.beans.utils;

import java.util.ArrayList;
import java.util.List;

public class BeanProperty {

	private String name;
	
	private String type;

	private List<AnnotationDescriptor> annotations = new ArrayList<AnnotationDescriptor>();
	
	public String getName() {
		return name;
	}

	public void setName(
						String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(
						String type) {
		this.type = type;
	}

	public List<AnnotationDescriptor> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(
								List<AnnotationDescriptor> annotations) {
		this.annotations = annotations;
	}
	
}
