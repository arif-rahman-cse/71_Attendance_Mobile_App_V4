package com.ekattorit.attendance.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class ScanItem{

	@SerializedName("second_in_time")
	private String secondInTime;

	@SerializedName("address")
	private String address;

	@SerializedName("latitude")
	private String latitude;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("employee")
	private Employee employee;

	@SerializedName("attendance_date")
	private String attendanceDate;

	@SerializedName("attendance_count")
	private String attendanceCount;

	@SerializedName("first_out_time")
	private String firstOutTime;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("first_in_time")
	private String firstInTime;

	@SerializedName("second_out_time")
	private String secondOutTime;

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

	public Employee getEmployee(){
		return employee;
	}

	public String getAttendanceDate(){
		return attendanceDate;
	}

	public String getAttendanceCount(){
		return attendanceCount;
	}

	public String getFirstOutTime(){
		return firstOutTime;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public String getFirstInTime(){
		return firstInTime;
	}

	public String getSecondOutTime(){
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