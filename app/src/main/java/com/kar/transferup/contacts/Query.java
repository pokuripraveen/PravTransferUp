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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.Chat;
import com.kar.transferup.providers.MessageContract;
import com.kar.transferup.storage.columns.ChatColumns;
import com.kar.transferup.util.DBUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.kar.transferup.interfaces.CommonColumns.KEY_CHAT_ID;

/**
 * The Query class defines a query that is used to fetch Contact objects.
 */
public final class Query {
    private static final String TAG = Query.class.getSimpleName();
    private final Context context;
    private final Map<String, Where> mimeWhere = new HashMap<>();
    private Where defaultWhere = null;
    private Set<Contact.Field> include = new HashSet<>();
    private List<Query> innerQueries;

    Query(Context context) {
        this.context = context;
        include.addAll(Arrays.asList(Contact.Field.values()));
    }

    /**
     * Add a constraint to the query for finding string values that contain the provided string.
     *
     * @param field     The field that the string to match is stored in.
     * @param value     The substring that the value must contain.
     * @return          this, so you can chain this call.
     */
    public Query whereContains(Contact.Field field, Object value) {
        addNewConstraint(field, Where.contains(field.getColumn(), value));
        return this;
    }

    /**
     * Add a constraint to the query for finding string values that start with the provided string.
     *
     * @param field     The field that the string to match is stored in.
     * @param value     The substring that the value must start with.
     * @return          this, so you can chain this call.
     */
    public Query whereStartsWith(Contact.Field field, Object value) {
        addNewConstraint(field, Where.startsWith(field.getColumn(), value));
        return this;
    }

    /**
     * Add a constraint to the query for finding values that equal the provided value.
     *
     * @param field     The field that the value to match is stored in.
     * @param value     The value that the field value must be equal to.
     * @return          this, so you can chain this call.
     */
    public Query whereEqualTo(Contact.Field field, Object value) {
        addNewConstraint(field, Where.equalTo(field.getColumn(), value));
        return this;
    }


    /**
     * Add a constraint to the query for finding values that NOT equal the provided value.
     *
     * @param field     The field that the value to match is stored in.
     * @param value     The value that the field value must be NOT equal to.
     * @return          this, so you can chain this call.
     */
    public Query whereNotEqualTo(Contact.Field field, Object value) {
        addNewConstraint(field, Where.notEqualTo(field.getColumn(), value));
        return this;
    }

    /**
     * Restrict the return contacts to only include contacts with a phone number.
     *
     * @return this, so you can chain this call.
     */
    public Query hasPhoneNumber() {
        defaultWhere = addWhere(defaultWhere, Where.notEqualTo(ContactsContract.Data.HAS_PHONE_NUMBER, 0));
        return this;
    }

    /**
     * Constructs a query that is the or of the given queries.
     * Previous calls to include are disregarded for the inner queries.
     * Calling those functions on the returned query will have the desired effect.
     * Calling where* functions on the return query is not permitted.
     *
     * @param queries The list of Queries to 'or' together.
     * @return A query that is the 'or' of the passed in queries.
     */
    public Query or(List<Query> queries) {
        innerQueries = queries;
        return this;
    }

    /**
     * Restrict the fields of returned Contacts to only include the provided fields.
     *
     * @param fields The array of keys to include in the result.
     * @return this, so you can chain this call.
     */
    public Query include(Contact.Field... fields) {
        include.clear();
        include.addAll(Arrays.asList(fields));
        return this;
    }

    /**
     * Retrieves a list of contacts that satisfy this query.
     *
     * @return A list of all contacts obeying the conditions set in this query.
     */
    public List<Contact> find() {
        List<Long> ids = new ArrayList<>();

        if (innerQueries != null) {
            for (Query query : innerQueries) {
                ids.addAll(query.findInner());
            }
        } else {
            if (mimeWhere.isEmpty()) {
                return find(null);
            }

            for (Map.Entry<String, Where> entry : mimeWhere.entrySet()) {
                ids = findIds(ids, entry.getKey(), entry.getValue());
            }
        }

        return find(ids);
    }

    /**
     * Retrieves a list of contacts that satisfy this query.
     *
     * @return A list of all contacts obeying the conditions set in this query.
     */
    public Cursor findAll() {
        Where where;
        where = defaultWhere;
        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
            buildProjection(),
            addWhere(where, buildWhereFromInclude()).toString(),
            null,
            ContactsContract.Data.DISPLAY_NAME);
        return c;
    }

    /**
     * Retrieves a list of contacts that satisfy this query.
     *
     * @return A list of all contacts obeying the conditions set in this query.
     */
    public Cursor getMessagesCursor(String phoneNo) {

        Where where;
        int chatId = getChatID(phoneNo);
        where = addWhere(defaultWhere, Where.equalTo(KEY_CHAT_ID, String.valueOf(chatId)));
        Cursor c = context.getApplicationContext().getContentResolver().query(MessageContract.Chat.CONTENT_URI,
            buildMessageProjection(),
            where.toString(),
            null,
            ChatColumns.KEY_TIME_STAMP);
        if(c == null){
            Log.i("ERROR", " Messages count is ZERO" );
        } else {
            Log.i(TAG, " Messages count " + c.getCount());
            c.moveToFirst();
            while(!c.isAfterLast()){
                for(int i=0;i< c.getColumnCount();i++){
                    Log.i("PRAV"," Column Name "+c.getColumnName(i) + " : "+c.getString(c.getColumnIndex(c.getColumnName(i))));
                }
                c.moveToNext();
            }

        }
        return c;
    }

    private int getChatID(String phoneNo) {
        return DBUtil.getInstance().getChatID(phoneNo);
    }

    /**
     * Retrieves a list of contacts that satisfy this query.
     *
     * @return A list of all contacts obeying the conditions set in this query.
     */
    public List<Chat> getMessages(String phoneNo) {
        List<Chat> messages = new LinkedList<>();
        Where where;
        int chatId = getChatID(phoneNo);
        where = addWhere(defaultWhere, Where.equalTo(KEY_CHAT_ID, String.valueOf(chatId)));
        Cursor c = context.getApplicationContext().getContentResolver().query(MessageContract.Chat.CONTENT_URI,
            buildMessageProjection(),
            where.toString(),
            null,
            ChatColumns.KEY_TIME_STAMP);
        if(c == null){
            Log.i("ERROR", " Messages count is ZERO" );
        } else {
            Log.i(TAG, " Messages count " + c.getCount());
            messages = getMessagesFromCursor(c);

        }
        return messages;
    }

    public List<Chat> getMessagesFromCursor(Cursor cursor){

        List<Chat> messages = new LinkedList<>();

        if (cursor != null) {
            Log.i("PRAV","message cursor Size "+cursor.getCount() +" can Move ");
            while (cursor.moveToNext()) {
                String Message = cursor.getString(cursor.getColumnIndexOrThrow(ChatColumns.KEY_MESSAGE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(ChatColumns.KEY_TIME_STAMP));
                String woner = cursor.getString(cursor.getColumnIndexOrThrow(ChatColumns.KEY_MSG_OWNER));
                String chatId = cursor.getString(cursor.getColumnIndexOrThrow(ChatColumns.KEY_MSG_CHATID));
                Chat  chat= new Chat();
                chat.setChatId(chatId);
                chat.setCreatedAt(timestamp);
                chat.setMessage(Message);
                Log.i("PRAV","message : "+Message +"  timestamp "+timestamp);
                chat.setOwner(woner);
                messages.add(chat);
            }

            //cursor.close();
            //cursor = null;
        } else {
            Log.i("PRAV","message cursor NULL !!!!");
        }

        return messages;
    }

    private List<Long> findIds(List<Long> ids, String mimeType, Where innerWhere) {
        String[] projection = { ContactsContract.RawContacts.CONTACT_ID};
        Where where = Where.equalTo(ContactsContract.Data.MIMETYPE, mimeType);
        where = addWhere(where, innerWhere);
        if (!ids.isEmpty()) {
            where = addWhere(where, Where.in(ContactsContract.RawContacts.CONTACT_ID, new ArrayList<Object>(ids)));
        }

        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                projection,
                where.toString(),
                null,
                ContactsContract.RawContacts.CONTACT_ID);

        List<Long> returnIds = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                CursorHelper helper = new CursorHelper(c);
                returnIds.add(helper.getContactId());
            }

            c.close();
        }

        return returnIds;
    }


    private List<Long> findInner() {
        List<Long> ids = new ArrayList<>();

        if (mimeWhere.isEmpty()) {
            Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.RawContacts.CONTACT_ID},
                    defaultWhere.toString(),
                    null,
                    ContactsContract.RawContacts.CONTACT_ID);
            if (c != null) {
                while (c.moveToNext()) {
                    CursorHelper helper = new CursorHelper(c);
                    ids.add(helper.getContactId());
                }

                c.close();
            }
        } else {
            for (Map.Entry<String, Where> entry : mimeWhere.entrySet()) {
                ids = findIds(ids, entry.getKey(), entry.getValue());
            }
        }
        return ids;
    }

    private List<Contact> find(List<Long> ids) {
        Where where;
        if (ids == null) {
            where = defaultWhere;
        } else if (ids.isEmpty()) {
            return new ArrayList<>();
        } else {
            where = Where.in(ContactsContract.RawContacts.CONTACT_ID, new ArrayList<>(ids));
        }

        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                buildProjection(),
                addWhere(where, buildWhereFromInclude()).toString(),
                null,
                ContactsContract.Data.DISPLAY_NAME);

        Map<Long, Contact> contactsMap = new LinkedHashMap<>();

        if (c != null) {
            while (c.moveToNext()) {
                CursorHelper helper = new CursorHelper(c);
                Long contactId = helper.getContactId();
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    contact = new Contact();
                    contactsMap.put(contactId, contact);
                }

                updateContact(contact, helper);
            }

            c.close();
        }

        return new ArrayList<>(contactsMap.values());
    }

     public List<Contact> getContactsFromCursor(Cursor cursor){

         Map<Long, Contact> contactsMap = new LinkedHashMap<>();
         try {
             if (cursor != null) {
                 while (cursor.moveToNext()) {
                     CursorHelper helper = new CursorHelper(cursor);
                     Long contactId = helper.getContactId();
                     Contact contact = contactsMap.get(contactId);
                     if (contact == null) {
                         contact = new Contact();
                         contactsMap.put(contactId, contact);
                     }

                     updateContact(contact, helper);
                 }

                 //cursor.close();
                 //cursor =null;
             }
         }catch(Exception e){
             Logger.log(Logger.ERROR,"QUERY ", e.getMessage(),e);
         }
         return new ArrayList<>(contactsMap.values());
     }

    public List<Contact> getContactsFromRawCursor(Cursor cursor){

        Map<Long, Contact> contactsMap = new LinkedHashMap<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                CursorHelper helper = new CursorHelper(cursor);
                Long contactId = helper.getContactId();
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    contact = new Contact();
                    contactsMap.put(contactId, contact);
                }

                updateRawContact(contact, helper);
            }

            //cursor.close();
           // cursor = null;
        }

        return new ArrayList<>(contactsMap.values());
    }

    private void updateRawContact(Contact contact, CursorHelper helper) {
        String displayName = helper.getDisplayName();
        if (displayName != null) {
            contact.addDisplayName(displayName);
        }

        long id = helper.getContactId();
        contact.addContactId(id);

        String photoUri = helper.getPhotoUri();
        if (photoUri != null) {
            contact.addPhotoUri(photoUri);
        }

        String mimeType = helper.getMimeType();
        Log.i("PRAV3","mimeType "+mimeType);
        if (mimeType.equals(Constants.ACCOUNT_MIME_TYPE)) {
            PhoneNumber phoneNumber = helper.getPhoneNumber();
            Log.i("PRAV3","phoneNumber" + phoneNumber);
            if (phoneNumber != null) {
                contact.addPhoneNumber(phoneNumber);
            }
        } else if (mimeType.equals(Constants.ACCOUNT_MIME_TYPE)) {
            Email email = helper.getEmail();
            if (email != null) {
                contact.addEmail(email);
            }
        } else if (mimeType.equals(Constants.ACCOUNT_MIME_TYPE)) {
            Event event = helper.getEvent();
            if (event != null) {
                contact.addEvent(event);
            }
        } else if (mimeType.equals(Constants.ACCOUNT_MIME_TYPE)) {
            String givenName = helper.getGivenName();
            if (givenName != null) {
                contact.addGivenName(givenName);
            }

            String familyName = helper.getFamilyName();
            if (familyName != null) {
                contact.addFamilyName(familyName);
            }
        }
    }

    private Cursor findByIds(List<Long> ids) {
        Where where;
        if (ids == null) {
            where = defaultWhere;
        } else if (ids.isEmpty()) {
            return null;
        } else {
            where = Where.in(ContactsContract.RawContacts.CONTACT_ID, new ArrayList<>(ids));
        }

        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
            buildProjection(),
            addWhere(where, buildWhereFromInclude()).toString(),
            null,
            ContactsContract.Data.DISPLAY_NAME);

        return c;
    }



    public List<Contact> getContactById(long cId) {
        Where where = Where.equalTo(ContactsContract.RawContacts.CONTACT_ID, cId);

        Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
            buildProjection(),
            where.toString(),
            null,
            ContactsContract.Data.DISPLAY_NAME);
        return getContactsFromCursor(c);
    }

    private Where buildWhereFromInclude() {
        Set<String> mimes = new HashSet<>();
        for (Contact.Field field : include) {
            if (field.getMimeType() != null) {
                mimes.add(field.getMimeType());
            }
        }
        return Where.in(ContactsContract.Data.MIMETYPE, new ArrayList<Object>(mimes));
    }

    private void addNewConstraint(Contact.Field field, Where where)  {
        if (field.getMimeType() == null) {
            defaultWhere = addWhere(defaultWhere, where);
        } else {
            Where existingWhere = mimeWhere.get(field.getMimeType());
            mimeWhere.put(field.getMimeType(), addWhere(existingWhere, where));
        }
    }

    private void updateContact(Contact contact, CursorHelper helper) {
        String displayName = helper.getDisplayName();
        if (displayName != null) {
            contact.addDisplayName(displayName);
        }

        long id = helper.getContactId();
        contact.addContactId(id);

        String photoUri = helper.getPhotoUri();
        if (photoUri != null) {
            contact.addPhotoUri(photoUri);
        }

        String mimeType = helper.getMimeType();
        if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
            PhoneNumber phoneNumber = helper.getPhoneNumber();
            Log.i("PRAV3","phoneNumber" + phoneNumber);
            if (phoneNumber != null) {
                contact.addPhoneNumber(phoneNumber);
            }
        } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            Email email = helper.getEmail();
            if (email != null) {
                contact.addEmail(email);
            }
        } else if (mimeType.equals(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)) {
            Event event = helper.getEvent();
            if (event != null) {
                contact.addEvent(event);
            }
        } else if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
            String givenName = helper.getGivenName();
            if (givenName != null) {
                contact.addGivenName(givenName);
            }

            String familyName = helper.getFamilyName();
            if (familyName != null) {
                contact.addFamilyName(familyName);
            }
        }
    }

    private String[] buildProjection() {
        Set<String> projection = new HashSet<>();

        for (Contact.AbstractField field : Contact.InternalField.values()) {
            projection.add(field.getColumn());
        }
        Log.i("PRAV3","include "+include);
        for (Contact.AbstractField field : include) {
            projection.add(field.getColumn());
        }

        return projection.toArray(new String[projection.size()]);
    }

    private String[] buildMessageProjection() {
        Set<String> projection = new HashSet<>();

        for (Contact.AbstractField field : Contact.Message.values()) {
            projection.add(field.getColumn());
        }

        return projection.toArray(new String[projection.size()]);
    }
    private String[] buildMessagesProjection() {
        Set<String> projection = new HashSet<>();

        for (Contact.AbstractField field : Contact.Messages.values()) {
            projection.add(field.getColumn());
        }

        return projection.toArray(new String[projection.size()]);
    }

    public String[] getProjection() {
        Set<String> projection = new HashSet<>();
        for (Contact.AbstractField field : Contact.InternalField.values()) {
            projection.add(field.getColumn());
        }

        for (Contact.AbstractField field : include) {
            Log.i("PRAV3","include  "+field.getColumn());
            projection.add(field.getColumn());
        }
        Log.i("PRAV3","String proj  "+projection);
        return projection.toArray(new String[projection.size()]);
    }

    private Where addWhere(Where where, Where otherWhere) {
        return where == null ? otherWhere : where.and(otherWhere);
    }

    public Cursor getTransferUpContacts() {
        List<Long> transferUpIds = new ArrayList<>();

        ContentResolver cr = context.getContentResolver();
        Cursor contactCursor = cr.query(ContactsContract.RawContacts.CONTENT_URI, getTransferUpSelection(),
            ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?", new String[]{Constants.ACCOUNT_TYPE},
            null);
        if (contactCursor != null) {
            if (contactCursor.getCount() > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        transferUpIds.add(contactCursor.getLong(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)));
                    } while (contactCursor.moveToNext());
                   // contactCursor.close();
                    //contactCursor =null;
                }
            }
        }
        return findByIds(transferUpIds);
    }

    private String[] getTransferUpSelection(){
        return new String[]{
            ContactsContract.RawContacts._ID,
            ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY };
    }

    public List<Contact> getContactByUri(Uri uri) {
        return getContactsFromRawCursor(context.getContentResolver().query(uri, getProjection(), null, null, null));
    }

    public String getPhotoUri(String phone) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor c = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.PHOTO_URI},null,null,null);
        if(c != null && c.moveToFirst()){
            return c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI));
        }
        return null;
    }

    public String[] getDisplayNameAndPhoto(String phone){
        String[] contactInfo = new String[2];
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor c = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI},null,null,null);
        if(c != null && c.moveToFirst()){
            contactInfo[0] =  c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            contactInfo[1] =  c.getString(c.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI));
        }
        return contactInfo;
    }
}
