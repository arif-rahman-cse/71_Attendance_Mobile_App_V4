package com.ekattorit.attendance.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class Employee{

	@SerializedName("emp_name_en")
	private String empNameEn;

	@SerializedName("emp_id")
	private String empId;

	public String getEmpNameEn(){
		return empNameEn;
	}

	public String getEmpId(){
		return empId;
	}
}