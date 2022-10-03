package com.ttv.facerecog.ui.login.model;

import com.google.gson.annotations.SerializedName;

public class RpUpFace {

	@SerializedName("face_embeddings")
	private String faceEmbeddings;

	public String getFaceEmbeddings(){
		return faceEmbeddings;
	}
}