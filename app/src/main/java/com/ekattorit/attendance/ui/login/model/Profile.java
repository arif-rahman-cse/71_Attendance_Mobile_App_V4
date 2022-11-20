package com.ekattorit.attendance.ui.login.model;

import com.google.gson.annotations.SerializedName;

public class Profile{

	@SerializedName("image")
	private String image;

	@SerializedName("report_access")
	private String reportAccess;

	@SerializedName("face_delete_permission")
	private boolean faceDeletePermission;

	@SerializedName("access")
	private String access;

	@SerializedName("business")
	private String business;

	@SerializedName("range")
	private int range;

	@SerializedName("users_type")
	private String usersType;

	@SerializedName("attendance_time_diff")
	private float attendanceTimeDiff;

	@SerializedName("id")
	private int id;

	@SerializedName("designation")
	private String designation;

	@SerializedName("face_add_permission")
	private boolean faceAddPermission;

	@SerializedName("supervisor_longitude")
	private float supervisorLongitude;

	@SerializedName("supervisor_ward")
	private String supervisorWard;

	@SerializedName("user")
	private int user;

	@SerializedName("supervisor_latitude")
	private float supervisorLatitude;

	public String getImage(){
		return image;
	}

	public String getReportAccess(){
		return reportAccess;
	}

	public boolean isFaceDeletePermission(){
		return faceDeletePermission;
	}

	public String getAccess(){
		return access;
	}

	public String getBusiness(){
		return business;
	}

	public int getRange(){
		return range;
	}

	public String getUsersType(){
		return usersType;
	}

	public float getAttendanceTimeDiff(){
		return attendanceTimeDiff;
	}

	public int getId(){
		return id;
	}

	public String getDesignation(){
		return designation;
	}

	public boolean isFaceAddPermission(){
		return faceAddPermission;
	}

	public float getSupervisorLongitude(){
		return supervisorLongitude;
	}

	public String getSupervisorWard(){
		return supervisorWard;
	}

	public int getUser(){
		return user;
	}

	public float getSupervisorLatitude(){
		return supervisorLatitude;
	}
}