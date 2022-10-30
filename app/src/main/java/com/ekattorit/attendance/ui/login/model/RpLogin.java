package com.ekattorit.attendance.ui.login.model;

import com.google.gson.annotations.SerializedName;

public class RpLogin{

	@SerializedName("id")
	private int id;

	@SerializedName("username")
	private String username;

	@SerializedName("email")
	private String email;

	@SerializedName("last_name")
	private String lastName;

	@SerializedName("first_name")
	private String firstName;

	@SerializedName("is_active")
	private boolean isActive;


	@SerializedName("profile")
	private Profile profile;

	public boolean isIsActive(){
		return isActive;
	}

	public Profile getProfile(){
		return profile;
	}

	public String getLastName(){
		return lastName;
	}

	public int getId(){
		return id;
	}

	public String getFirstName(){
		return firstName;
	}

	public String getEmail(){
		return email;
	}

	public String getUsername(){
		return username;
	}
}