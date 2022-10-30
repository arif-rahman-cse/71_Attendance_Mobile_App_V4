package com.ekattorit.attendance.ui.home.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RpRecentScan{

	@SerializedName("next")
	private String next;

	@SerializedName("previous")
	private String previous;

	@SerializedName("count")
	private int count;

	@SerializedName("current_page_number")
	private int currentPageNumber;

	@SerializedName("total_page_number")
	private int totalPageNumber;

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

	public int getCurrentPageNumber() {
		return currentPageNumber;
	}

	public int getTotalPageNumber() {
		return totalPageNumber;
	}

	public List<ScanItem> getResults(){
		return results;
	}
}