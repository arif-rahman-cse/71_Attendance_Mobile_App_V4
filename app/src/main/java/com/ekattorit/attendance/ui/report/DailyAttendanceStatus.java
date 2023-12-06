package com.ekattorit.attendance.ui.report;

import static java.util.Locale.US;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ekattorit.attendance.retrofit.RetrofitClient;
import com.ekattorit.attendance.ui.report.adapter.DailyAttendanceAdapter;
import com.ekattorit.attendance.ui.report.model.RpAttendance;
import com.ekattorit.attendance.ui.report.model.RpShift;
import com.ekattorit.attendance.ui.report.model.RpWord;
import com.ekattorit.attendance.utils.AppProgressBar;
import com.ekattorit.attendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.databinding.ActivityDailyAttendanceStatusBinding;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DailyAttendanceStatus extends AppCompatActivity {
    private static final String TAG = "DailyAttendanceStatus";
    private ActivityDailyAttendanceStatusBinding binding;
    private UserCredentialPreference userCredentialPreference;


    private ArrayList<String> spinnerShiftList = null;
    private ArrayList<String> spinnerWordList = null;
    private List<Map<String, Object>> shitMap = new ArrayList<>();
    private List<Map<String, Object>> wordMap = new ArrayList<>();
    private List<RpAttendance> attendanceList = new ArrayList<>();
    private DailyAttendanceAdapter dailyAttendanceAdapter;
    final Calendar cCalendar = Calendar.getInstance();
    private String startDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_attendance_status);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_daily_attendance_status);
        binding.toolbar.title.setText(getString(R.string.daily_attendance_status));
        userCredentialPreference = UserCredentialPreference.getPreferences(DailyAttendanceStatus.this);

        String currentDate = java.text.DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
        binding.tvCurrentDate.setText(currentDate);
        formatDate();

        initRecyclerView();

        binding.tvCurrentDate.setOnClickListener(view -> {

            final DatePickerDialog.OnDateSetListener sDate = (view1, year, monthOfYear, dayOfMonth) -> {
                cCalendar.set(Calendar.YEAR, year);
                cCalendar.set(Calendar.MONTH, monthOfYear);
                cCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDate();
            };
            new DatePickerDialog(
                    this,
                    sDate,
                    cCalendar.get(Calendar.YEAR),
                    cCalendar.get(Calendar.MONTH),
                    cCalendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        /*
            getShift();
            getWord(userCredentialPreference.getUserId());
         */


        binding.toolbar.backButton.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.btnViewReport.setOnClickListener(view -> {
            AppProgressBar.showMessageProgress(DailyAttendanceStatus.this, "Data Loading...");
            getAttendance();
        });


    }

    private void formatDate() {

        int year = cCalendar.get(Calendar.YEAR);
        int month = cCalendar.get(Calendar.MONTH) + 1;
        int day = cCalendar.get(Calendar.DAY_OF_MONTH);

        startDate = year + "-" + month + "-" + day;

    }


    private void setDate() {
        String myFormat = "MMM-dd-yyyy"; //Date Format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, US);


        binding.tvCurrentDate.setText(sdf.format(cCalendar.getTime()));

        int year = cCalendar.get(Calendar.YEAR);
        int month = cCalendar.get(Calendar.MONTH) + 1;
        int day = cCalendar.get(Calendar.DAY_OF_MONTH);

        startDate = year + "-" + month + "-" + day;

        Log.d(TAG, "setDate: Start Date: " + startDate);


    }


    private void initRecyclerView() {
        dailyAttendanceAdapter = new DailyAttendanceAdapter(this, attendanceList);
        binding.rvDailyAttendance.setAdapter(dailyAttendanceAdapter);
        binding.rvDailyAttendance.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }




    private void getAttendance() {
        Log.d(TAG, "getAttendance: Called ");
        Log.d(TAG, "getAttendance: Date: " + startDate + " User Id: " + userCredentialPreference.getUserId());
        Call<List<RpAttendance>> rpAttendance = RetrofitClient
                .getInstance()
                .getApi()
                .getPerDayAttendance(userCredentialPreference.getUserToken(),startDate, userCredentialPreference.getUserId());


        rpAttendance.enqueue(new Callback<List<RpAttendance>>() {
            @Override
            public void onResponse(Call<List<RpAttendance>> call, Response<List<RpAttendance>> response) {
                AppProgressBar.hideMessageProgress();
                Log.d(TAG, "onResponse: " + response.code());
                if (response.isSuccessful() && response.code() == 200) {
                    binding.itemTitleBar.setVisibility(View.VISIBLE);
                    attendanceList.clear();
                    assert response.body() != null;
                    if (response.body().size() > 0) {
                        int present = 0, absent = 0;
                        binding.errorView.setVisibility(View.GONE);
                        attendanceList.addAll(response.body());
                        dailyAttendanceAdapter.notifyDataSetChanged();
                        binding.tvTotalEmp.setText(getString(R.string.total_employee, response.body().size()));
                        for (RpAttendance attendance : attendanceList) {
                            if (attendance.getStatus().equals("P") || attendance.getStatus().equals("OFFDAY")) {
                                present = present + 1;
                            } else {
                                absent = absent + 1;
                            }
                        }
                        binding.tvPresent.setText(getString(R.string.present, present));
                        binding.tvAbsent.setText(getString(R.string.absent, absent));

                    } else {
                        binding.errorView.setVisibility(View.VISIBLE);
                    }
                } else {
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RpAttendance>> call, Throwable t) {
                AppProgressBar.hideMessageProgress();
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });


    }
}