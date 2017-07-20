package com.kar.transferup.interfaces;

import retrofit2.Response;

/**
 * Created by praveenp on 21-04-2017.
 */

public interface NetworkRequestResponseListener {
    public void  onSuccess(Response response);

    public void onFailure(Throwable error);
}
