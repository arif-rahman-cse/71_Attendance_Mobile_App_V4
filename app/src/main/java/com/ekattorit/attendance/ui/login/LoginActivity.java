package com.ekattorit.attendance.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import com.ekattorit.attendance.retrofit.RetrofitClient;
import com.ekattorit.attendance.ui.login.model.RpLogin;
import com.ekattorit.attendance.utils.EmployeeFacePreference;
import com.ekattorit.attendance.utils.HideKeyboard;
import com.ekattorit.attendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.ui.MainActivity;
import com.ekattorit.attendance.ui.login.model.RpLoginError;
import com.ekattorit.attendance.ui.login.model.RpUpFace;
import com.ekattorit.attendance.databinding.ActivityLoginBinding;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    ActivityLoginBinding binding;
    UserCredentialPreference userCredentialPreference;
    EmployeeFacePreference employeeFacePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        userCredentialPreference = UserCredentialPreference.getPreferences(LoginActivity.this);
        employeeFacePreference = EmployeeFacePreference.getPreferences(LoginActivity.this);


        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString();
            String password = binding.etPassword.getText().toString();

            if (validateData(phone, password)) {
                HideKeyboard.hideKeyboard(LoginActivity.this);
                binding.loginPb.setVisibility(View.VISIBLE);
                tryLogin(phone, password);
                //tryLoginV2(phone, password);
                disableBtn();

            }

        });

        binding.etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableBtn();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableBtn();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }



    private void enableBtn() {
        binding.btnLogin.setClickable(true);
        binding.btnLogin.setBackgroundColor(getResources().getColor(R.color.green));
    }

    private void disableBtn() {
        binding.btnLogin.setClickable(false);
        binding.btnLogin.setBackgroundColor(getResources().getColor(R.color.disable_color));
    }

    private void tryLogin(String phone, String password) {
        Call<RpLogin> loginCall = RetrofitClient.getInstance().getApi().login(phone, password);

        loginCall.enqueue(new Callback<RpLogin>() {
            @Override
            public void onResponse(Call<RpLogin> call, Response<RpLogin> response) {

                Log.d(TAG, "onResponse: " + response.code());

                if (response.code() == 200 && response.isSuccessful()) {

                    RpLogin rpLogin = response.body();
                    userCredentialPreference.setUserPhone(rpLogin.getUsername());
                    userCredentialPreference.setPassword(password);
                    userCredentialPreference.setName(rpLogin.getFirstName() + " " + rpLogin.getLastName());
                    userCredentialPreference.setUserId(rpLogin.getId());


                    userCredentialPreference.setProfileUrl(rpLogin.getProfile().getImage());
                    userCredentialPreference.setUserType(rpLogin.getProfile().getUsersType());
                    userCredentialPreference.setSuperVisorLatitude(rpLogin.getProfile().getSupervisorLatitude());
                    userCredentialPreference.setSuperVisorLongitude(rpLogin.getProfile().getSupervisorLongitude());
                    userCredentialPreference.setSuperVisorRange(rpLogin.getProfile().getRange());
                    userCredentialPreference.setSuperVisorWard(String.valueOf(rpLogin.getProfile().getSupervisorWard()));
                    userCredentialPreference.setIsFaceRemovePermission(rpLogin.getProfile().isFaceDeletePermission());
                    userCredentialPreference.setIsFaceAddPermission(rpLogin.getProfile().isFaceAddPermission());
                    userCredentialPreference.setAttendanceTimeDiff(rpLogin.getProfile().getAttendanceTimeDiff());
                    userCredentialPreference.setUserToken("Token "+rpLogin.getToken());

                    binding.loginPb.setVisibility(View.GONE);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();


                } else if (response.code() == 400) {
                    enableBtn();

                    try {
                        //assert response.errorBody() != null;
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


                }
            }

            @Override
            public void onFailure(Call<RpLogin> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.getMessage());
                enableBtn();
                showErrorLogin(t.getMessage());
                //tryLogin(phone, password);
                //showErrorLogin(t.getMessage());
            }
        });


    }

    private void showErrorLogin(String error) {
        binding.loginPb.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar
                .make(binding.mainView, " "+error, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", view -> {
                    binding.loginPb.setVisibility(View.VISIBLE);
                    String phone = binding.etPhone.getText().toString();
                    String password = binding.etPassword.getText().toString();
                    Log.d(TAG, "showErrorLogin: User: "+ phone+" Password: "+password);
                    tryLogin(phone, password);
                });

        snackbar.show();
    }


    private boolean validateData(String phone, String password) {
        if (phone.isEmpty()) {
            binding.etPhone.setError("ফোন নম্বর দিন ");
            binding.etPhone.requestFocus();
            return false;

        } else if (phone.length() != 11) {
            binding.etPhone.setError("ফোন নম্বর ১১ ডিজিট হতে হবে");
            binding.etPhone.requestFocus();
            return false;

        } else if (password.isEmpty()) {
            binding.etPassword.setError("পাসওয়ার্ড দিন");
            binding.etPassword.requestFocus();
            return false;
        }
        return true;
    }


}