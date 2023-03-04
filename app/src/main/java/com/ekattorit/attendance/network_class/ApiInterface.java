package com.ekattorit.attendance.network_class;

import com.ekattorit.attendance.ui.employee.modle.RpItemFace;
import com.ekattorit.attendance.ui.employee.modle.RpNewFace;
import com.ekattorit.attendance.ui.home.model.RpRecentScan;
import com.ekattorit.attendance.ui.login.model.RpLogin;
import com.ekattorit.attendance.ui.login.model.RpUpFace;
import com.ekattorit.attendance.ui.report.model.RpAttendance;
import com.ekattorit.attendance.ui.report.model.RpAttendanceSummary;
import com.ekattorit.attendance.ui.report.model.RpShift;
import com.ekattorit.attendance.ui.report.model.RpWord;
import com.ekattorit.attendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.attendance.ui.scan.model.RpNewScan;
import com.ekattorit.attendance.ui.scan.model.RpNewScan2;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    //@Headers("Content-Type: application/json")
    //@Headers("Content-Type: multipart/form-data")
    @FormUrlEncoded
    @POST("account/login/")
    Call<RpLogin> login(
            @Field("username") String phoneNumber,
            @Field("password") String password
    );

    @Multipart
    @POST("add-face/new")
    Call<RpNewFace> addNewFace(
            @Header("Authorization") String token,
            @Part("emp_id") String emp_id,
            @Part("emp_name") String emp_name,
            @Part("supervisor") int supervisor_id,
            @Part MultipartBody.Part faceImg

    );

//    user_id = models.AutoField(primary_key=True)
//    name = models.TextField(blank=True, null=True)
//    face = models.BinaryField(editable=True, blank=True, null=True, )
//    feature = models.BinaryField(editable=True, blank=True, null=True)

//    @Multipart
//    @POST("add-face/new")
//    Call<RpNewFace> addNewFace(
//            @Part("user_id") int emp_id,
//            @Part("name") String emp_name,
//            @Part("face") String faceImg,
//            @Part("feature") String feature
//            @Part MultipartBody.Part faceImg,
//            @Part MultipartBody.Part feature
//    );


    @GET("employee-face/list/{user_id}")
    Call<ArrayList<RpItemFace>> getEmployeeFace(
            @Header("Authorization") String token,
            @Path("user_id") int userId
    );

    @GET("moshok-employee/details/{employee_id}")
    Call<RpEmpDetails> getEmpDetails(
            @Header("Authorization") String token,
            @Path("employee_id") String empId
    );


    @GET("employee/list/{user_id}")
    Call<ArrayList<RpEmpDetails>> getEmployee(
            @Header("Authorization") String token,
            @Path("user_id") int userId
    );

    @GET("moshok-recent_attendance/list")
    Call<RpRecentScan> getRecentScan(
            @Header("Authorization") String token,
            @Query("page") int pageNumber,
            @Query("scan_by") int scanBy
    );

    @GET("shift/list")
    Call<List<RpShift>> getShift();

    @GET("current/shift")
    Call<RpShift> getCurrentShift();

    @GET("word/list/")
    Call<List<RpWord>> getWord(
            @Query("user") int supervisor_id
    );

    @GET("report/par_day_attendance/")
    Call<List<RpAttendance>> getPerDayAttendance(
            @Header("Authorization") String token,
            @Query("xdate") String date,
            @Query("user_id") int user_id
    );

    @GET("report/par_day_attendance")
    Call<List<RpAttendance>> getPerDayAttendanceByShift(
            @Query("user_id") int supervisor_id,
            @Query("shift_id") String shift
    );


    @Multipart
    @POST("moshok-attendance/new")
    Call<RpNewScan2> addNewAttendance(
            @Header("Authorization") String token,
            //@Part("business") String business_id,
            //@Part("user") int superVisor,
            //@Part("word_no") String wordNo,
            @Part("employee") String employeeId,
            @Part("latitude") double latitude,
            @Part("longitude") double longitude,
            //@Part("attendance_count") double attendance_count,
            @Part("address") String address,
            //@Part("scan_status") boolean scan_status,
            //@Part MultipartBody.Part employee_img,
            @Part("scan_by") int scan_by

    );

    @FormUrlEncoded
    @PUT("attendance/update")
    Call<RpNewScan> updateAttendance(
            @Field("employee") String employeeId,
            @Field("shift") String shiftName,
            @Field("attendance_date") String date

    );

    @GET("report/range_attendance_single/{start_date}/{end_date}/{user_id}/{emp_id}")
    Call<List<RpAttendance>> getSingleEmployeeAttendance(
            @Header("Authorization") String token,
            @Path("start_date") String start_date,
            @Path("end_date") String end_date,
            @Path("user_id") int user_id,
            @Path("emp_id") String emp_id
    );


    @GET("report/attendance_summery/")
    Call<RpAttendanceSummary> getAttendanceSummary(
            @Header("Authorization") String token,
            @Query("user_id") int supervisor_id,
            @Query("start_date") String start_date,
            @Query("end_date") String end_date
    );

    @FormUrlEncoded
    @PATCH("upload/face/{pk}")
    Call<RpUpFace> uploadFace(
            @Header("Authorization") String token,
            @Path("pk") int id,
            @Field("face_embeddings") String face
    );

    @GET("upload/face/{pk}")
    Call<RpUpFace> syncFace(
            @Header("Authorization") String token,
            @Path("pk") int id
    );



}
