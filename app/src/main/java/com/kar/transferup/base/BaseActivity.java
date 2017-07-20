package com.kar.transferup.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.kar.transferup.interfaces.AppPermissionListener;
import com.kar.transferup.interfaces.NetworkRequestResponseListener;
import com.kar.transferup.logger.Logger;

import retrofit2.Response;

/**
 * Created by praveenp on 21-04-2017.
 */

public class BaseActivity extends AppCompatActivity implements  AppPermissionListener, NetworkRequestResponseListener {

    protected static final int GET_ALL_PERMISSION_REQUEST_CODE = 100;
    private static final int GET_ACCOUNT_PERMISSION_REQUEST_CODE = 101;
    protected static final int READ_CONTACT_PERMISSION_REQUEST_CODE = 102;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        verifyAppPermissions();
        super.onCreate(savedInstanceState);
    }

    protected void verifyAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!hasAccountPermissions() && !hasAccountPermissions()) {
                Logger.i("requesting for multiple permissions");
                requestRequiredPermissions();
            } else if (!hasContactsPermissions()) {
                Logger.i("requesting for Contacts permissions");
                requestContactPermission();
            } else if (!hasAccountPermissions()) {
                Logger.i("requesting for Accounts permissions");
                requestAccountPermission();
            }
        } else {
            onAcquiredAccountPermission();
        }
    }

    private void requestRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS},
                GET_ALL_PERMISSION_REQUEST_CODE);
        }
    }

    protected boolean hasAllPermissions(){
        return (hasContactsPermissions() && hasAccountPermissions());
    }

    protected boolean hasContactsPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    protected boolean hasAccountPermissions(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) ==
            PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;

    }
    protected void requestContactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                READ_CONTACT_PERMISSION_REQUEST_CODE);

        }
    }


    protected void requestAccountPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                    GET_ACCOUNT_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Logger.i("onRequestPermissionsResult grantResults %s length %s", grantResults, grantResults.length);
        if(requestCode == GET_ALL_PERMISSION_REQUEST_CODE){
            if(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                onAcquiredContactsPermission();
            }
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                onAcquiredAccountPermission();
            }
        }
        if (requestCode == GET_ACCOUNT_PERMISSION_REQUEST_CODE){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                onAcquiredAccountPermission();
            }
        }
        if(requestCode == READ_CONTACT_PERMISSION_REQUEST_CODE){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                onAcquiredContactsPermission();
            }
        }
    }

    @Override
    public void onAcquiredAccountPermission() {

    }

    @Override
    public void onAcquiredContactsPermission() {

    }


    @Override
    public void onSuccess(Response reponse) {

    }

    @Override
    public void onFailure(Throwable error) {

    }
}
