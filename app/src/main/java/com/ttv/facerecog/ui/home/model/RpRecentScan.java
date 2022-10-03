package com.ttv.facerecog.ui.home.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RpRecentScan{

	@SerializedName("next")
	private Object next;

	@SerializedName("previous")
	private Object previous;

	@SerializedName("count")
	private int count;

	@SerializedName("results")
	private List<ScanItem> results;

	public Object getNext(){
		return next;
	}

	public Object getPrevious(){
		return previous;
	}

	public int getCount(){
		return count;
	}

	public List<ScanItem> getResults(){
		return results;
	}
}