/*
 * Copyright 2016 Tamir Shomer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kar.transferup.contacts;

import android.annotation.SuppressLint;
import android.provider.ContactsContract;

import com.kar.transferup.storage.columns.ChatColumns;
import com.kar.transferup.storage.columns.ChatHeaderColumns;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a compound contact. aggregating all phones, email and photo's a contact has.
 */
public final class Contact {
    private String displayName;
    private String givenName;
    private String familyName;
    private final Set<PhoneNumber> phoneNumbers = new HashSet<>();
    private String photoUri;
    private final Set<Email> emails = new HashSet<>();
    private final Set<Event> events = new HashSet<>();
    private long contactId;

    //For Sync Adapter
    private String serverId;
    private long rawContactId;

    @Override
    public String toString() {
        return "Contact{" +
            "displayName='" + displayName + '\'' +
            ", givenName='" + givenName + '\'' +
            ", familyName='" + familyName + '\'' +
            ", phoneNumbers=" + phoneNumbers +
            ", photoUri='" + photoUri + '\'' +
            ", emails=" + emails +
            ", events=" + events +
            ", contactId=" + contactId +
            ", serverId='" + serverId + '\'' +
            ", rawContactId=" + rawContactId +
            ", isDeleted=" + isDeleted +
            ", isDirty=" + isDirty +
            ", syncTime=" + syncTime +
            '}';
    }

    private boolean isDeleted;
    private boolean isDirty;
    private long syncTime;

    interface AbstractField {
        String getMimeType();
        String getColumn();
    }

    public enum Message implements AbstractField {
        Id(null, ChatColumns.KEY_ID),
        ChatId(null, ChatColumns.KEY_CHAT_ID),
        TimeStamp(null, ChatColumns.KEY_TIME_STAMP),
        Owner(null, ChatColumns.KEY_MSG_OWNER),
        Data(null, ChatColumns.KEY_MESSAGE);

        private final String column;
        private final String mimeType;

        Message(String mimeType, String column) {
            this.mimeType = mimeType;
            this.column = column;
        }

        @Override
        public String getColumn() {
            return column;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }

    public enum Messages implements AbstractField {
        ChatId(null, ChatHeaderColumns.KEY_CHAT_ID),
        SenderName(null, ChatHeaderColumns.KEY_SENDER_NAME),
        Senderphone(null, ChatHeaderColumns.KEY_SENDER_PHONE),
        ReceiverName(null, ChatHeaderColumns.KEY_RECEIVER_NAME),
        ReceiverPhone(null, ChatHeaderColumns.KEY_RECEIVER_PHONE);

        private final String column;
        private final String mimeType;

        Messages(String mimeType, String column) {
            this.mimeType = mimeType;
            this.column = column;
        }

        @Override
        public String getColumn() {
            return column;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }

    public enum Field implements AbstractField {
        DisplayName(null, ContactsContract.Data.DISPLAY_NAME),
        GivenName(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
           ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME),
        FamilyName(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
           ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME),
        PhoneNumber(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.NUMBER),
        PhoneType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE),
        PhoneLabel(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL),
        @SuppressLint("InlinedApi")
        PhoneNormalizedNumber(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER),
        Email(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.ADDRESS),
        EmailType(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE),
        EmailLabel(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.LABEL),
        PhotoUri(null, ContactsContract.Data.PHOTO_URI),
        EventStartDate(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Event.START_DATE),
        EventType(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Event.TYPE),
        EventLabel(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Event.LABEL);

        private final String column;
        private final String mimeType;

        Field(String mimeType, String column) {
            this.mimeType = mimeType;
            this.column = column;
        }

        @Override
        public String getColumn() {
            return column;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }

    public enum InternalField implements AbstractField {
        ContactId(null, ContactsContract.RawContacts.CONTACT_ID),
        RawContactId(null, ContactsContract.RawContacts._ID),
        MimeType(null, ContactsContract.Data.MIMETYPE);

        private final String column;
        private final String mimeType;

        InternalField(String mimeType, String column) {
            this.mimeType = mimeType;
            this.column = column;
        }

        @Override
        public String getColumn() {
            return column;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }

    public Contact() {}

    public Contact addDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Contact addGivenName(String givenName) {
        this.givenName = this.givenName;
        return this;
    }

    public Contact addFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public Contact addPhoneNumber(PhoneNumber phoneNumber) {
        phoneNumbers.add(phoneNumber);
        return this;
    }

    public Contact addPhotoUri(String photoUri) {
        this.photoUri = photoUri;
        return this;
    }

    public Contact addEmail(Email email) {
        emails.add(email);
        return this;
    }

    public Contact addEvent(Event event) {
        events.add(event);
        return this;
    }

    public Contact addContactId(long id) {
        this.contactId = id;
        return this;
    }

    public Contact addServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public Contact addRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
        return this;
    }

    public Contact addIsDeleted(boolean deleted){
        this.isDeleted = deleted;
        return this;
    }

    public Contact addSyncTime(long timeMillis){
        this.syncTime = timeMillis;
        return this;
    }

    public Contact addIsDirty(boolean dirtyFlag){
        this.isDirty = dirtyFlag;
        return this;
    }

    /**
     * Gets a the display name the contact.
     *
     * @return Display Name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets a the given name the contact.
     *
     * @return Given Name.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Gets a the Family name the contact.
     *
     * @return Family Name.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Gets a list of all phone numbers the contact has.
     *
     * @return A List of phone numbers.
     */
    public List<PhoneNumber> getPhoneNumbers() {
        return Arrays.asList(phoneNumbers.toArray(new PhoneNumber[phoneNumbers.size()]));
    }

    /**
     * Gets a contacts photo uri.
     *
     * @return Photo URI.
     */
    public String getPhotoUri() {
        return photoUri;
    }

    /**
     * Gets a list of all emails the contact has.
     *
     * @return A List of emails.
     */
    public List<Email> getEmails() {
        return Arrays.asList(emails.toArray(new Email[emails.size()]));
    }

    /**
     * Gets a list of all events the contact has.
     *
     * @return A List of emails.
     */
    public List<Event> getEvents() {
        return Arrays.asList(events.toArray(new Event[events.size()]));
    }

    /**
     * Gets the birthday event if exists.
     *
     * @return Birthday event or null.
     */
    public Event getBirthday() {
        return getEvent(Event.Type.BIRTHDAY);
    }

    /**
     * Gets the anniversary event if exists.
     *
     * @return Anniversary event or null.
     */
    public Event getAnniversary() {
        return getEvent(Event.Type.ANNIVERSARY);

    }

    private Event getEvent(Event.Type type) {
        for (Event event: events) {
            if (type.equals(event.getType())) {
                return event;
            }
        }

        return null;
    }

    public long getContactId() {
        return contactId;
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public String getServerId() {
        return serverId;
    }

    public boolean isDeleted(){
        return isDeleted;
    }

    public long getSyncTime(){
        return syncTime;
    }

    public boolean isDirty(){
        return isDirty;
    }
}
