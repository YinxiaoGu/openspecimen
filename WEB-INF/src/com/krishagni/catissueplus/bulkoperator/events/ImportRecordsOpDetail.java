package com.krishagni.catissueplus.bulkoperator.events;

import java.io.File;

public class ImportRecordsOpDetail {
	private String operationName;
	
	private File fileIn;

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public File getFileIn() {
		return fileIn;
	}

	public void setFileIn(File fileIn) {
		this.fileIn = fileIn;
	}
}