package com.kar.transferup.model;

import com.kar.transferup.contacts.Contact;

import java.util.List;

/**
 * Created by praveenp on 09-02-2017.
 */

public class MatchedContacts {

    private String ownerName;
    private String ownerNumber;
    private List<Contact> contacts;

    private String lastSyncTime;
    private String syncType;

    public String getOwnerNumber() {
        return ownerNumber;
    }

    public void setOwnerNumber(String ownerNumber) {
        this.ownerNumber = ownerNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public void setOwnerSyncType(String ownerSyncType) {
        this.syncType = ownerSyncType;
    }
}
