package com.ekattorit.attendance.ui.employee.modle;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

public class RpNewFace{

	@SerializedName("face")
	private String face;

	@SerializedName("feature")
	private String feature;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("emp_name")
	private String empName;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("id")
	private int id;

	@SerializedName("supervisor")
	private int supervisor;

	@SerializedName("emp_id")
	private String empId;

	@SerializedName("status")
	private boolean status;

	public String getFace(){
		return face;
	}

	public String getFeature(){
		return feature;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public String getEmpName(){
		return empName;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public int getId(){
		return id;
	}

	public int getSupervisor(){
		return supervisor;
	}

	public String getEmpId(){
		return empId;
	}

	public boolean isStatus(){
		return status;
	}
}