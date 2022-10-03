package com.ttv.facerecog.retrofit;
import com.ttv.facerecog.network_class.ApiInterface;
import com.ttv.facerecog.utils.AppConfig;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = AppConfig.Base_URL_ONLINE;
    //private static final String BASE_URL = AppConfig.Base_URL_DEV;
    private static RetrofitClient mInstance;
    private final Retrofit retrofit;

    private RetrofitClient() {

        OkHttpClient client = new OkHttpClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    public ApiInterface getApi() {
        return retrofit.create(ApiInterface.class);
    }
}
