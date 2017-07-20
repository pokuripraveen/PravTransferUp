package com.kar.transferup.net;

import com.kar.transferup.interfaces.RFNetworkInterface;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by praveenp on 07-09-2016.
 */
public class RetrofitApiClient {

    public static final String BASE_DEBUG_URL = "http://192.168.230.150";
    public static final String BASE_PRODUCTION_URL = "http://52.58.108.7:7126/transferUp/";
    private static Retrofit mRetrofit;

    public static RFNetworkInterface getClient() {
        if (mRetrofit == null) {
            String baseUrl;

            mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_PRODUCTION_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }

        return mRetrofit.create(RFNetworkInterface.class);
    }

}
