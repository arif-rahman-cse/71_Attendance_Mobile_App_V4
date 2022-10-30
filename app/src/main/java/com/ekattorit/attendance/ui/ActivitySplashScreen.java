package com.ekattorit.attendance.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.ekattorit.attendance.retrofit.RetrofitClient;
import com.ekattorit.attendance.ui.login.LoginActivity;
import com.ekattorit.attendance.ui.login.model.RpLogin;
import com.ekattorit.attendance.ui.login.model.RpLoginError;
import com.ekattorit.attendance.utils.UserCredentialPreference;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.databinding.ActivitySplashScreenBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("CustomSplashScreen")
public class ActivitySplashScreen extends AppCompatActivity {
    private static final String TAG = "ActivitySplashScreen";

    private ActivitySplashScreenBinding binding;
    private UserCredentialPreference userCredentialPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);
        userCredentialPreference = UserCredentialPreference.getPreferences(this);
        //clearAppData();
        clearApplicationData();

        if (userCredentialPreference.getUserPhone() != null && userCredentialPreference.getPassword() != null) {
            //Log.d(TAG, "onCreate: Shared Prefarance Value Phone: "+ userCredentialPreference.getUserPhone());
            //Log.d(TAG, "onCreate: Shared Prefarance Value Password: "+ userCredentialPreference.getPassword());

            String userPhone = userCredentialPreference.getUserPhone();
            String password = userCredentialPreference.getPassword();

            tryLogin(userPhone, password);

        } else if (userCredentialPreference.getUserPhone() == null && userCredentialPreference.getPassword() == null) {
            Log.d(TAG, "onCreate: Shared Prefarance Value Is Null");
            goLoginScreen();


        }

    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(Objects.requireNonNull(cache.getParent()));
        if (appDir.exists()) {
            String[] children = appDir.list();
            assert children != null;
            for (String s : children) {
                if (!s.equals("lib") && !s.equals("databases")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }else {
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " REMAIN *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            assert children != null;
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }



    private void tryLogin(String phone, String password) {
        binding.splashPb.setVisibility(View.VISIBLE);
        Call<RpLogin> loginCall = RetrofitClient.getInstance().getApi().login(phone, password);

        loginCall.enqueue(new Callback<RpLogin>() {
            @Override
            public void onResponse(Call<RpLogin> call, Response<RpLogin> response) {
                binding.splashPb.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onResponse: " + response.code());

                if (response.code() == 200 && response.isSuccessful()) {

                    RpLogin rpLogin = response.body();
                    assert rpLogin != null;

                    userCredentialPreference.setUserPhone(rpLogin.getUsername());
                    userCredentialPreference.setPassword(password);
                    userCredentialPreference.setName(rpLogin.getFirstName() + " " + rpLogin.getLastName());
                    userCredentialPreference.setUserId(rpLogin.getId());


                    userCredentialPreference.setProfileUrl(rpLogin.getProfile().getImage());
                    userCredentialPreference.setUserType(rpLogin.getProfile().getUsersType());
                    userCredentialPreference.setSuperVisorLatitude(String.valueOf(rpLogin.getProfile().getSupervisorLatitude()));
                    userCredentialPreference.setSuperVisorLongitude(String.valueOf(rpLogin.getProfile().getSupervisorLongitude()));
                    userCredentialPreference.setSuperVisorRange(rpLogin.getProfile().getRange());
                    userCredentialPreference.setSuperVisorWard(String.valueOf(rpLogin.getProfile().getSupervisorWard()));
                    userCredentialPreference.setIsFaceRemovePermission(rpLogin.getProfile().isFaceDeletePermission());
                    userCredentialPreference.setIsFaceAddPermission(rpLogin.getProfile().isFaceAddPermission());
                    userCredentialPreference.setAttendanceTimeDiff(rpLogin.getProfile().getAttendanceTimeDiff());

                    Intent intent = new Intent(ActivitySplashScreen.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    finish();
                    startActivity(intent);


                } else {
                    try {
                        Gson gson = new GsonBuilder().create();
                        RpLoginError loginError;
                        loginError = gson.fromJson(response.errorBody().string(), RpLoginError.class);
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                        if (loginError != null && !loginError.getNonFieldErrors().isEmpty()) {
                            showErrorLogin(loginError.getNonFieldErrors().get(0));
                            Log.d(TAG, "onResponse: Error: " + loginError.getNonFieldErrors());
                        }
                    } catch (IOException e) {
                        // handle failure at error parse
                        showErrorLogin(e.getMessage());
                    }

                    //goLoginScreen();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RpLogin> call, Throwable t) {
                binding.splashPb.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onResponse: Error");
                Log.e(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(ActivitySplashScreen.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                tryLogin(phone, password);
                //goLoginScreen();
                //Toast.makeText(SplashScreen.this, "User not found or Password doesn't match", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showErrorLogin(String error) {
        binding.splashPb.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar
                .make(binding.mainView, error, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", view -> {
                    binding.splashPb.setVisibility(View.VISIBLE);
                    String phone = userCredentialPreference.getUserPhone();
                    String password = userCredentialPreference.getPassword();
                    Log.d(TAG, "showErrorLogin: User: "+ phone+" Password: "+password);
                    tryLogin(phone, password);
                });

        snackbar.show();
    }

    private void goLoginScreen() {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}