package com.ekattorit.attendance.ui.employee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;


import com.ekattorit.attendance.retrofit.RetrofitClient;
import com.ekattorit.attendance.ui.MainActivity;
import com.ekattorit.attendance.ui.ScanPreviewActivity;
import com.ekattorit.attendance.utils.AppConfig;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.attendance.ui.scan.model.RpError;
import com.ekattorit.attendance.utils.UserCredentialPreference;
import com.ekattorit.attendance.databinding.ActivityEmployeeCardScanBinding;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeCardScanActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeCardScan";
    private ActivityEmployeeCardScanBinding binding;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    UserCredentialPreference userCredentialPreference;
    boolean isQrDetected = false;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_card_scan);
        binding = DataBindingUtil.setContentView(EmployeeCardScanActivity.this, R.layout.activity_employee_card_scan);
        userCredentialPreference = UserCredentialPreference.getPreferences(EmployeeCardScanActivity.this);

    }


    // Function to check and request permission
    public boolean checkCameraPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(EmployeeCardScanActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(EmployeeCardScanActivity.this, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
            //Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        binding.surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {

                    if (checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) {
                        cameraSource.start(binding.surfaceView.getHolder());
                    } else {
                        checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                //Toast.makeText(getApplicationContext() , "To prevent memory leaks barcode scanner has been stopped" , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    Log.d(TAG, "receiveDetections: " + barcodes.valueAt(0).displayValue);
                    String qrCode = barcodes.valueAt(0).displayValue;
                    //cameraSource.release();
                    binding.progressBar.setVisibility(View.VISIBLE);
                    barcodeDetector.release();
                    getEmployeeDetails(qrCode.trim());
                }
            }
        });
    }


    private void getEmployeeDetails(String qrData) {
        Log.d(TAG, "getEmployeeDetails: Called");
        Call<RpEmpDetails> cardDetailsCall = RetrofitClient
                .getInstance()
                .getApi()
                .getEmpDetails(userCredentialPreference.getUserToken(),qrData);

        cardDetailsCall.enqueue(new Callback<RpEmpDetails>() {
            @Override
            public void onResponse(Call<RpEmpDetails> call, Response<RpEmpDetails> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.code() == 200) {

                    if (response.body() != null) {

                        RpEmpDetails empDetails = response.body();
                        Intent scanIntent;
                        if (getIntent().getBooleanExtra(AppConfig.IS_FROM_ADD_FACE, false)) {

                            scanIntent = new Intent(EmployeeCardScanActivity.this, AddEmployeeFace.class);
                        } else {
                            scanIntent = new Intent(EmployeeCardScanActivity.this, ScanPreviewActivity.class);
                        }

                        scanIntent.putExtra(AppConfig.EMP_ID, empDetails.getEmpId());
                        scanIntent.putExtra(AppConfig.EMP_NAME, empDetails.getEmpNameEn());
                        scanIntent.putExtra(AppConfig.DESIGNATION, empDetails.getDesignation());
                        scanIntent.putExtra(AppConfig.WORD_NO, empDetails.getWardNo());
                        scanIntent.putExtra(AppConfig.PHONE, empDetails.getMobileNumber());
                        scanIntent.putExtra(AppConfig.EMP_IMG, empDetails.getEmployeeImg());
                        scanIntent.putExtra(AppConfig.NID, empDetails.getNidBrn());
                        scanIntent.putExtra(AppConfig.BLOOD_GROUP, empDetails.getBloodGroup());
                        scanIntent.putExtra(AppConfig.ADDRESS, empDetails.getAddress());
                        finish();
                        startActivity(scanIntent);


                    }


                } else if (response.code() == 404) {
                    Log.d(TAG, "onResponse: Code: " + response.code());
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Gson gson = new GsonBuilder().create();
                    RpError loginError;
                    try {
                        loginError = gson.fromJson(response.errorBody().string(), RpError.class);
                        Log.d(TAG, "onResponse: msg: " + loginError);
                        //Toast.makeText(LoginActivity.this, loginError.getNonFieldErrors().get(0), Toast.LENGTH_LONG).show();
                        SweetAlertDialog sw = new SweetAlertDialog(EmployeeCardScanActivity.this, SweetAlertDialog.WARNING_TYPE);
                        sw.setCancelable(false);
                        sw.setTitleText(" নির্দেশনা !")
                                .setContentText(loginError.getError())
                                .setConfirmText("ঠিক আছে ")
                                .setConfirmClickListener(sDialog -> {
                                    sDialog.dismissWithAnimation();
                                    Intent intent;
                                    if (getIntent().getBooleanExtra(AppConfig.IS_FROM_ADD_FACE, false)) {
                                        intent = new Intent(EmployeeCardScanActivity.this, EmployeeListActivityV2.class);
                                    } else {
                                        intent = new Intent(EmployeeCardScanActivity.this, MainActivity.class);
                                    }
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                        Log.d(TAG, "onResponse: " + response.errorBody().string());
                    } catch (IOException e) {
                        // handle failure at error parse
                    }
                }
            }

            @Override
            public void onFailure(Call<RpEmpDetails> call, Throwable t) {
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
                binding.progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) {
                    try {
                        cameraSource.start(binding.surfaceView.getHolder());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    checkCameraPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
                }
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Camera Permission Denied");
                //Toast.makeText(EmployeeCardScanActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(EmployeeCardScanActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EmployeeCardScanActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


        if (getIntent().getBooleanExtra(AppConfig.IS_FROM_ADD_FACE, false)) {
            Intent intent = new Intent(this, EmployeeListActivityV2.class);
            startActivity(intent);
            finish();

        } else {
            Intent intent2 = new Intent(this, MainActivity.class);
            startActivity(intent2);
            finish();

        }
    }


}