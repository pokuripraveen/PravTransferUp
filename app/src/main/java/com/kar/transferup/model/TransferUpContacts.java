package com.kar.transferup.model;

import com.kar.transferup.contacts.Contact;

import java.util.List;

/**
 * Created by praveenp on 10-02-2017.
 */

public class TransferUpContacts {

    private String statusCode;
    private String value;
    private List<Contact> installedContacts;

    public List<Contact> getInstalledContacts() {
        return installedContacts;
    }

    public void setInstalledContacts(List<Contact> installedContacts) {
        this.installedContacts = installedContacts;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
