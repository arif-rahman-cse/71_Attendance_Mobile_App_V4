package com.ekattorit.attendance.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class Data{

	@SerializedName("second_in_time")
	private String secondInTime;

	@SerializedName("address")
	private String address;

	@SerializedName("latitude")
	private String latitude;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("attendance_date")
	private String attendanceDate;

	@SerializedName("employee")
	private String employee;

	@SerializedName("attendance_count")
	private String attendanceCount;

	@SerializedName("first_out_time")
	private Object firstOutTime;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("first_in_time")
	private Object firstInTime;

	@SerializedName("second_out_time")
	private Object secondOutTime;

	@SerializedName("id")
	private int id;

	@SerializedName("attendance_status")
	private boolean attendanceStatus;

	@SerializedName("longitude")
	private String longitude;

	@SerializedName("scan_by")
	private int scanBy;

	public String getSecondInTime(){
		return secondInTime;
	}

	public String getAddress(){
		return address;
	}

	public String getLatitude(){
		return latitude;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public String getAttendanceDate(){
		return attendanceDate;
	}

	public String getEmployee(){
		return employee;
	}

	public String getAttendanceCount(){
		return attendanceCount;
	}

	public Object getFirstOutTime(){
		return firstOutTime;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public Object getFirstInTime(){
		return firstInTime;
	}

	public Object getSecondOutTime(){
		return secondOutTime;
	}

	public int getId(){
		return id;
	}

	public boolean isAttendanceStatus(){
		return attendanceStatus;
	}

	public String getLongitude(){
		return longitude;
	}

	public int getScanBy(){
		return scanBy;
	}
}