package com.ekattorit.attendance.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class RpNewScan2{

	@SerializedName("data")
	private Data data;

	@SerializedName("employee_name")
	private String employeeName;

	@SerializedName("message")
	private String message;

	public Data getData(){
		return data;
	}

	public String getEmployeeName(){
		return employeeName;
	}

	public String getMessage() {
		return message;
	}
}