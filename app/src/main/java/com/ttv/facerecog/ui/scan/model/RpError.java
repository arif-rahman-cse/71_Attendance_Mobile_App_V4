package com.ttv.facerecog.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class RpError {

	@SerializedName("status")
	private String error;

	public String getError(){
		return error;
	}
}