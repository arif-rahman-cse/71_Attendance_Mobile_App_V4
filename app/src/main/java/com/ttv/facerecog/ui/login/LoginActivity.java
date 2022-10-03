package com.ttv.facerecog.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ttv.facerecog.R;
import com.ttv.facerecog.databinding.ActivityLoginBinding;
import com.ttv.facerecog.retrofit.RetrofitClient;
import com.ttv.facerecog.ui.MainActivity;
import com.ttv.facerecog.ui.login.model.RpLogin;
import com.ttv.facerecog.ui.login.model.RpLoginError;
import com.ttv.facerecog.ui.login.model.RpUpFace;
import com.ttv.facerecog.utils.EmployeeFacePreference;
import com.ttv.facerecog.utils.HideKeyboard;
import com.ttv.facerecog.utils.UserCredentialPreference;

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
                    userCredentialPreference.setIsFaceRemovePermission(rpLogin.getProfile().isFaceRemovePermission());

                    if (rpLogin.getProfile().getSupervisorWard() == null || rpLogin.getProfile().getSupervisorWard().isEmpty()) {
                        userCredentialPreference.setSuperVisorWard("0");
                    } else {
                        userCredentialPreference.setSuperVisorWard(rpLogin.getProfile().getSupervisorWard());
                    }

                    if (!userCredentialPreference.getIsSyncFace()) {
                        Log.d(TAG, "onResponse: sync Face..");
                        syncFace(rpLogin.getId());
                    } else {
                        Log.d(TAG, "onResponse: face already synced...");
                        binding.loginPb.setVisibility(View.GONE);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        startActivity(intent);
                    }


                } else if (response.code() == 400) {
                    enableBtn();
                    Gson gson = new GsonBuilder().create();
                    RpLoginError loginError;
                    try {
                        loginError = gson.fromJson(response.errorBody().string(), RpLoginError.class);
                        if (loginError != null && !loginError.getNonFieldErrors().isEmpty()){
                            showErrorLogin(loginError.getNonFieldErrors().get(0));
                        }

                    } catch (IOException e) {
                        // handle failure at error parse
                    }


                }
            }

            @Override
            public void onFailure(Call<RpLogin> call, Throwable t) {
                enableBtn();
                showErrorLogin(t.getMessage());
            }
        });


    }

    private void showErrorLogin(String error) {
        Log.d(TAG, "showErrorLogin: "+error);
        Snackbar snackbar = Snackbar
                .make(binding.mainView, "Unable to connect server " + error, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", view -> {
                    binding.loginPb.setVisibility(View.VISIBLE);
                    String userPhone = userCredentialPreference.getUserPhone();
                    tryLogin(userPhone, userCredentialPreference.getPassword());
                });

        snackbar.show();
    }

    private void syncFace(int id) {

        Call<RpUpFace> newScanCall = RetrofitClient
                .getInstance()
                .getApi()
                .syncFace(id);

        newScanCall.enqueue(new Callback<RpUpFace>() {
            @Override
            public void onResponse(Call<RpUpFace> call, Response<RpUpFace> response) {
                Log.d(TAG, "onResponse: " + response.code());
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getFaceEmbeddings() !=null && !response.body().getFaceEmbeddings().isEmpty()){
                        employeeFacePreference.setEmpFace(response.body().getFaceEmbeddings());
                    }else {
                        employeeFacePreference.setEmpFace(getString(R.string.face_default_value));
                    }
                    userCredentialPreference.setIsSyncFace(true);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    finish();
                    startActivity(intent);

                } else {
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                        Toast.makeText(LoginActivity.this, " কিছু একটা সমস্যা হয়েছে " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<RpUpFace> call, Throwable t) {
                Toast.makeText(LoginActivity.this, " কিছু একটা সমস্যা হয়েছে " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
            }
        });
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