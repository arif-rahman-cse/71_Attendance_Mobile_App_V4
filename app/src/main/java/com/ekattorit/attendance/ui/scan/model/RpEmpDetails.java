package com.ekattorit.attendance.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class RpEmpDetails{

	@SerializedName("nid_brn")
	private String nidBrn;

	@SerializedName("designation_name")
	private int designationName;

	@SerializedName("address")
	private String address;

	@SerializedName("belongs_to")
	private String belongsTo;

	@SerializedName("emp_name_en")
	private String empNameEn;

	@SerializedName("ward_no")
	private String wardNo;

	@SerializedName("gender")
	private String gender;

	@SerializedName("is_resigned")
	private boolean isResigned;

	@SerializedName("join_date")
	private String joinDate;

	@SerializedName("mother_name")
	private Object motherName;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("supervisor_name")
	private int supervisorName;

	@SerializedName("emp_name_bn")
	private String empNameBn;

	@SerializedName("employee_img")
	private String employeeImg;

	@SerializedName("driving_license")
	private Object drivingLicense;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("father_name")
	private Object fatherName;

	@SerializedName("resign_date")
	private Object resignDate;

	@SerializedName("blood_group")
	private String bloodGroup;

	@SerializedName("designation")
	private String designation;

	@SerializedName("mobile_number")
	private String mobileNumber;

	@SerializedName("emp_id")
	private String empId;

	@SerializedName("status")
	private boolean status;

	public String getNidBrn(){
		return nidBrn;
	}

	public int getDesignationName(){
		return designationName;
	}

	public String getAddress(){
		return address;
	}

	public String getBelongsTo(){
		return belongsTo;
	}

	public String getEmpNameEn(){
		return empNameEn;
	}

	public String getWardNo(){
		return wardNo;
	}

	public String getGender(){
		return gender;
	}

	public boolean isIsResigned(){
		return isResigned;
	}

	public String getJoinDate(){
		return joinDate;
	}

	public Object getMotherName(){
		return motherName;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public int getSupervisorName(){
		return supervisorName;
	}

	public String getEmpNameBn(){
		return empNameBn;
	}

	public String getEmployeeImg(){
		return employeeImg;
	}

	public Object getDrivingLicense(){
		return drivingLicense;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public Object getFatherName(){
		return fatherName;
	}

	public Object getResignDate(){
		return resignDate;
	}

	public String getBloodGroup(){
		return bloodGroup;
	}

	public String getDesignation(){
		return designation;
	}

	public String getMobileNumber(){
		return mobileNumber;
	}

	public String getEmpId(){
		return empId;
	}

	public boolean isStatus(){
		return status;
	}
}