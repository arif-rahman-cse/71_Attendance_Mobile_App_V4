package com.ekattorit.attendance.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.ekattorit.attendance.utils.AppConfig;
import com.ekattorit.attendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.databinding.ActivityScanPreviewBinding;

import java.io.File;


public class ScanPreviewActivity extends AppCompatActivity {
    private static final String TAG = "ScanPreviewActivity";
    ActivityScanPreviewBinding binding;
    private File employeeImgFile;
    private UserCredentialPreference userCredentialPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_preview);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_preview);
        userCredentialPreference = UserCredentialPreference.getPreferences(ScanPreviewActivity.this);

        Intent scanIntent = getIntent();
        String empId = scanIntent.getStringExtra(AppConfig.EMP_ID);
        String empName = scanIntent.getStringExtra(AppConfig.EMP_NAME);
        String empDesignation = scanIntent.getStringExtra(AppConfig.DESIGNATION);
        String wordNo = scanIntent.getStringExtra(AppConfig.WORD_NO);
        String phone = scanIntent.getStringExtra(AppConfig.PHONE);
        String emp_img = scanIntent.getStringExtra(AppConfig.EMP_IMG);
        String nid = scanIntent.getStringExtra(AppConfig.NID);
        String bloodGroup = scanIntent.getStringExtra(AppConfig.BLOOD_GROUP);
        String address = scanIntent.getStringExtra(AppConfig.ADDRESS);

        setScanData(empId, empName, empDesignation, wordNo, phone, emp_img, nid, bloodGroup, address);
        Log.d(TAG, "onCreate: Link: " + emp_img);


        binding.btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);

        });

//        binding.btnCompleteScan.setOnClickListener(view -> {
//            if (employeeImgFile != null) {
//                binding.attendancePb.setVisibility(View.VISIBLE);
//                binding.btnCompleteScan.setAlpha(0.3f);
//                binding.btnCompleteScan.setClickable(false);
//
//                //addNewAttendance(empId, empName, wordNo, businessId, supervisorId, attendanceType, userCurrentAddress, latitude, longitude);
//
//                //addNewAttendance(empId, empName, wordNo);
//                //getShiftName(empId, empName, wordNo, businessId, supervisorId, attendanceType, userCurrentAddress, latitude, longitude);
//
//            } else {
//                Snackbar.make(binding.mainView, "????????????????????? ????????? ???????????????????????????  ????????? ???????????????", BaseTransientBottomBar.LENGTH_LONG).show();
//            }
//
//        });

    }


    private void setScanData(String empId, String empName, String empDesignation, String wordNo, String phone, String imgLink, String nid, String bloodGroup, String address) {

        binding.tvCardId.setText(empId);
        binding.tvCardOwner.setText(empName);
        binding.tvDesignation.setText(empDesignation);
        binding.tvWordNo.setText(wordNo);
        binding.tvPhone.setText(phone);
        binding.tvNid.setText(nid);
        binding.tvBloodGroup.setText(bloodGroup);
        binding.tvAddress.setText(address);

        if ((phone != null)) {
            binding.rowMobile.setVisibility(View.VISIBLE);
            binding.separatorMobile.setVisibility(View.VISIBLE);
        } else {
            binding.rowMobile.setVisibility(View.GONE);
            binding.separatorMobile.setVisibility(View.GONE);
        }

        if ((nid != null)) {
            binding.rowNid.setVisibility(View.VISIBLE);
            binding.separatorNid.setVisibility(View.VISIBLE);
        } else {
            binding.rowNid.setVisibility(View.GONE);
            binding.separatorNid.setVisibility(View.GONE);
        }

        if ((bloodGroup != null)) {
            binding.rowBloodGroup.setVisibility(View.VISIBLE);
            binding.separatorBloodGroup.setVisibility(View.VISIBLE);
        } else {
            binding.rowBloodGroup.setVisibility(View.GONE);
            binding.separatorBloodGroup.setVisibility(View.GONE);
        }

        if ((address != null)) {
            binding.rowAddress.setVisibility(View.VISIBLE);
        } else {
            binding.rowAddress.setVisibility(View.GONE);
        }


        Glide.with(this)
                .load(AppConfig.Base_URL_ONLINE_IMG + imgLink)
                .placeholder(R.drawable.loading_01)
                .centerInside()
                .error(R.drawable.default_profile_pic)
                .into(binding.ivEmpImgFixed);

    }

    /**
     *
    private void addNewAttendance(String empId, String empName, String wordNo, String businessId, int supervisorId, boolean attendanceType,
                                  String userCurrentAddress, double latitude, double longitude) {

        RequestBody requestInnerFile = RequestBody.create(MediaType.parse("multipart/form-data"), employeeImgFile);
        MultipartBody.Part scanImage = MultipartBody.Part.createFormData("scan_img", employeeImgFile.getName(), requestInnerFile);

        double attendance_count = attendanceType ? 0.5 : 1;

        Call<RpNewScan> newScanCall = RetrofitClient
                .getInstance()
                .getApi()
                .addNewAttendance(
                        businessId, supervisorId, wordNo, empId, latitude, longitude, attendance_count, userCurrentAddress, true, scanImage, userCredentialPreference.getUserId());

        newScanCall.enqueue(new Callback<RpNewScan>() {
            @Override
            public void onResponse(Call<RpNewScan> call, Response<RpNewScan> response) {
                Log.d(TAG, "onResponse: " + response.code());
                binding.attendancePb.setVisibility(View.GONE);
                binding.btnCompleteScan.setAlpha(1f);
                binding.btnCompleteScan.setClickable(true);
                if (response.code() == 201) {
                    final SweetAlertDialog sDialog = AppProgressBar.userActionSuccessPb(ScanPreviewActivity.this, empName + " ?????? ?????????????????? ????????? ??????????????? ");
                    sDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sDialog.dismissWithAnimation();
                        Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });

                }
                else if (response.code() == 200) {

                    final SweetAlertDialog sDialog = AppProgressBar.userActionSuccessPb(ScanPreviewActivity.this, empName + " ?????? ?????????????????? ??????????????? ????????? ??????????????? ");
                    sDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sDialog.dismissWithAnimation();
                        Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });

                } else {
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                        final SweetAlertDialog sDialog = AppProgressBar.userActionErrorPb(ScanPreviewActivity.this, " ???????????? ???????????? ?????????????????? ??????????????? " + response.errorBody().string());
                        sDialog.setConfirmClickListener(sweetAlertDialog -> {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<RpNewScan> call, Throwable t) {
                binding.attendancePb.setVisibility(View.GONE);
                binding.btnCompleteScan.setClickable(true);
                binding.btnCompleteScan.setAlpha(1f);
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
                final SweetAlertDialog sDialog = AppProgressBar.userActionErrorPb(ScanPreviewActivity.this, " ???????????? ???????????? ?????????????????? ??????????????? " + t.getMessage());
                sDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sDialog.dismissWithAnimation();
                    Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (resultCode == Activity.RESULT_OK) {
//
//            //Image Uri will not be null for RESULT_OK
//            Uri selectedImageUri = data.getData();
//            binding.ivEmployeeImg.setImageURI(selectedImageUri);
//            //You can get File object from intent
//            employeeImgFile = ImagePicker.Companion.getFile(data);
//
//        } else if (resultCode == ImagePicker.RESULT_ERROR) {
//            Toast.makeText(this, ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
//        }
//
//
    }

}