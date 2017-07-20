package com.kar.transferup.model;

/**
 * Created by praveenp on 20-04-2017.
 */

public class UserChat {

    private String mName;
    private String mPhone;
    private String mMessage;
    private String mCreatedAt;
    private String mContactUri;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public String getContactUri() {
        return mContactUri;
    }

    public void setContactUri(String mContactUri) {
        this.mContactUri = mContactUri;
    }


}
