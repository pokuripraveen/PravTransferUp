package com.kar.transferup.interfaces;

import com.kar.transferup.model.MatchedContacts;
import com.kar.transferup.model.Message;
import com.kar.transferup.model.TransferUpContacts;
import com.kar.transferup.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by praveenp on 09-12-2016.
 */

public interface RFNetworkInterface {
    @POST("sendMessage/")
    Call<Void> sendMessage(@Header("action") String action, @Body Message message);

    @Headers("Content-Type: application/json")
    @POST("fcm/")
    Call<Void> updateFcmId(@Header("action") String action, @Body User user);

    @Headers("Content-Type: application/json")
    @POST("saveInfo/")
    Call<Void> updateUser(@Body User user);

    @Headers("Content-Type: application/json")
    @POST("matchedContacts/")
    Call<TransferUpContacts> syncContacts(@Body MatchedContacts contacts);

    @Headers("Content-Type: application/json")
    @POST("matchedContactsSync/")
    Call<TransferUpContacts> matchedContactsSync(@Body MatchedContacts contacts);
}
