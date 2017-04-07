package org.carlspring.beans.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationDescriptor {

	private String className;

	private Map<String, List<String>> attributes = new HashMap<String, List<String>>();

	public String getClassName() {
		return className;
	}

	public void setClassName(
								String className) {
		this.className = className;
	}

	public Map<String, List<String>> getAttributes() {
		return attributes;
	}

	public void setAttributes(
								Map<String, List<String>> attributes) {
		this.attributes = attributes;
	}

}
