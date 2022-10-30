package com.ekattorit.attendance;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ekattorit.attendance.ui.MainActivity;
import com.ttv.face.FaceEngine;
import com.ttv.face.FaceFeatureInfo;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "users.db";
    public static final String CONTACTS_TABLE_NAME = "users";
    public static final String CONTACTS_COLUMN_ID = "user_id";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_FACE = "face";
    public static final String CONTACTS_COLUMN_FEATURE = "feature";
    public static final String CONTACTS_COLUMN_EMPLOYEE_ID = "employee_id";
    public static final String CONTACTS_COLUMN_EMPLOYEE_ADDRESS = "employee_address";

    private HashMap hp;
    private Context appCtx;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 5);
        appCtx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("create table users " + "(user_id integer primary key autoincrement,  name text, employee_id text unique,  face blob, feature blob)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public int insertUser (String name, String empId, Bitmap faceImg, byte[] feature) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        faceImg.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] face = byteArrayOutputStream.toByteArray();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("employee_id", empId);
        contentValues.put("face", face);
        contentValues.put("feature", feature);
        db.insert("users", null, contentValues);

        Cursor res =  db.rawQuery( "select last_insert_rowid() from users", null );
        res.moveToFirst();

        int user_id = 0;
        while(!res.isAfterLast()){
            user_id = res.getInt(0);
            res.moveToNext();
        }
        return user_id;
    }

    public int insertUser2 (String name, String empId, byte[] face, byte[] feature) {


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("employee_id", empId);
        contentValues.put("face", face);
        contentValues.put("feature", feature);
        db.insert("users", null, contentValues);

        Cursor res =  db.rawQuery( "select last_insert_rowid() from users", null );
        res.moveToFirst();

        int user_id = 0;
        while(!res.isAfterLast()){
            user_id = res.getInt(0);
            res.moveToNext();
        }
        return user_id;
    }

    public Cursor getData(String employee_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from users where employee_id="+employee_id, null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public Integer deleteUser (String empId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("users",
                "employee_id = ? ",
                new String[] { empId });
    }

    public Integer deleteAllUser () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ CONTACTS_TABLE_NAME);
        return 0;
    }

    @SuppressLint("Range")
    public void getAllUsers() {
        MainActivity.userLists.clear();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from users", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            int user_id = res.getInt(res.getColumnIndex(CONTACTS_COLUMN_ID));
            String userName = res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME));
            String employeeId = res.getString(res.getColumnIndex(CONTACTS_COLUMN_EMPLOYEE_ID));
            byte[] faceData = res.getBlob(res.getColumnIndex(CONTACTS_COLUMN_FACE));
            byte[] featureData = res.getBlob(res.getColumnIndex(CONTACTS_COLUMN_FEATURE));
            Bitmap faceImg = BitmapFactory.decodeByteArray(faceData, 0, faceData.length);

            FaceEntity face = new FaceEntity(user_id, userName, employeeId,faceImg, featureData);
            MainActivity.userLists.add(face);

            FaceFeatureInfo faceFeatureInfo = new FaceFeatureInfo(user_id, featureData);
            FaceEngine.getInstance(appCtx).registerFaceFeature(faceFeatureInfo);

            res.moveToNext();
        }
    }
}