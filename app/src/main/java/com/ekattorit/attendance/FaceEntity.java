package com.ekattorit.attendance;

import android.graphics.Bitmap;

public class FaceEntity {

    public String userName;
    public String employeeId;
    public Bitmap headImg;
    public byte[] feature;
    public int user_id;

    public FaceEntity() {

    }

    public FaceEntity(int user_id, String userName, String employeeId, Bitmap headImg, byte[] feature) {
        this.user_id = user_id;
        this.employeeId = employeeId;
        this.userName = userName;
        this.headImg = headImg;
        this.feature = feature;
    }
}
