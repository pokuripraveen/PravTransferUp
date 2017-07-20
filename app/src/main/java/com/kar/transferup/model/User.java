package com.kar.transferup.model;

import java.io.Serializable;

/**
 * Created by praveenp on 09-12-2016.
 */
public class User implements Serializable {

    private String name;
    private String mobileNumber;
    private String fcmId;
    private String eMail;
    private String countryCode;
    private String contactId;
    private String contactImageUrl;
    private String serverContactId;

    public User(){

    }

    public User(String name, String mobile, String email, String countryCode, String id, String contactImageUrl){
        this.name = name;
        this.mobileNumber = mobile;
        this.fcmId = id;
        this.eMail = email;
        this.countryCode = countryCode;
        this.contactImageUrl = contactImageUrl;
    }

    public String getFcmId() {
        return fcmId;
    }

    public void setFcmId(String fcmId) {
        this.fcmId = fcmId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setServerContactId(String serverId) {
        this.serverContactId = serverId;
    }

    public String getServerContactId(){
        return serverContactId;
    }

    public String getContactImageUrl() {
        return contactImageUrl;
    }

    public void setContactImageUrl(String contactImageUrl) {
        this.contactImageUrl = contactImageUrl;
    }

    @Override
    public String toString() {
        return "User{" +
            "name='" + name + '\'' +
            ", mobileNumber='" + mobileNumber + '\'' +
            ", fcmId='" + fcmId + '\'' +
            ", eMail='" + eMail + '\'' +
            ", countryCode='" + countryCode + '\'' +
            ", contactImageUrl='" + contactImageUrl + '\'' +
            ", contactId='" + contactId + '\'' +
            ", serverContactId='" + serverContactId + '\'' +
            '}';
    }

}
