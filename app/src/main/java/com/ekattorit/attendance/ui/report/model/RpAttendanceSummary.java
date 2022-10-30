package com.ekattorit.attendance.ui.report.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RpAttendanceSummary{

	@SerializedName("end_date")
	private String endDate;

	@SerializedName("start_date")
	private String startDate;

	@SerializedName("attendance_list")
	private List<AttendanceListItem> attendanceList;

	public String getEndDate(){
		return endDate;
	}

	public String getStartDate(){
		return startDate;
	}

	public List<AttendanceListItem> getAttendanceList(){
		return attendanceList;
	}
}