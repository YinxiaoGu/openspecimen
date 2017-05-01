package com.krishagni.catissueplus.core.exporter.events;

import java.util.Map;

public class ExportDetail {
	private String objectType;

	private Map<String, String> params;

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
