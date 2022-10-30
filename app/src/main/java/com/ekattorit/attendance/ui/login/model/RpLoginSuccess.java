package com.ekattorit.attendance.ui.login.model;

import com.google.gson.annotations.SerializedName;

public class RpLoginSuccess{

	@SerializedName("image")
	private String image;

	@SerializedName("is_active")
	private boolean isActive;

	@SerializedName("last_name")
	private String lastName;

	@SerializedName("range")
	private int range;

	@SerializedName("id")
	private int id;

	@SerializedName("users_type")
	private String usersType;

	@SerializedName("supervisor_longitude")
	private double supervisorLongitude;

	@SerializedName("supervisor_ward")
	private int supervisorWard;

	@SerializedName("first_name")
	private String firstName;

	@SerializedName("email")
	private String email;

	@SerializedName("supervisor_latitude")
	private double supervisorLatitude;

	@SerializedName("username")
	private String username;

	public String getImage(){
		return image;
	}

	public boolean isIsActive(){
		return isActive;
	}

	public String getLastName(){
		return lastName;
	}

	public int getRange(){
		return range;
	}

	public int getId(){
		return id;
	}

	public String getUsersType(){
		return usersType;
	}

	public double getSupervisorLongitude(){
		return supervisorLongitude;
	}

	public int getSupervisorWard(){
		return supervisorWard;
	}

	public String getFirstName(){
		return firstName;
	}

	public String getEmail(){
		return email;
	}

	public double getSupervisorLatitude(){
		return supervisorLatitude;
	}

	public String getUsername(){
		return username;
	}
}