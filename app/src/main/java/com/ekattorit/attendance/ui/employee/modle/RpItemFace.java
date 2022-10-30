package com.ekattorit.attendance.ui.employee.modle;

import com.google.gson.annotations.SerializedName;

public class RpItemFace{

	@SerializedName("face")
	private String face;

	@SerializedName("emp_name")
	private String empName;

	@SerializedName("supervisor")
	private int supervisor;

	@SerializedName("emp_id")
	private String empId;

	public String getFace(){
		return face;
	}

	public String getEmpName(){
		return empName;
	}

	public int getSupervisor(){
		return supervisor;
	}

	public String getEmpId(){
		return empId;
	}
}