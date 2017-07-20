package com.kar.transferup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.kar.transferup.R;
import com.kar.transferup.base.BaseActivity;
import com.kar.transferup.contacts.Contacts;
import com.kar.transferup.logger.Logger;
import com.kar.transferup.model.User;
import com.kar.transferup.storage.PreferenceManager;
import com.kar.transferup.util.AppUtil;
import com.kar.transferup.util.NetworkUtils;

import retrofit2.Response;

import static com.kar.transferup.storage.PreferenceManager.KEY_USER_COUNTRY_CODE;


/**
 * Created by praveenp on 06-01-2017.
 */
public class TransferUpLogin extends BaseActivity implements AdapterView.OnItemSelectedListener {

    private static final int GET_ACCOUNT_PERMISSION_REQUEST_CODE = 100;

    private Spinner mSpinner;
    private AutoCompleteTextView mUsername;
    private AutoCompleteTextView mEmail;
    private EditText mMobile;
    private View mProgress;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i("onCreate ");

        setContentView(R.layout.activity_login);
        mUsername = (AutoCompleteTextView) findViewById(R.id.user);
        mEmail = (AutoCompleteTextView) findViewById(R.id.email);
        mMobile = (EditText) findViewById(R.id.mobile);
        mSpinner = (Spinner) findViewById(R.id.countries_spinner);

        initView();
        initLogin();
    }

    private void initLogin() {
        User user = PreferenceManager.getInstance().getUser();
        if (user != null){
            if(hasContactsPermissions()) {
                launchMainActivity();
            } else{
                Logger.e("There was no permissions granted to launch the App..!!!");
            }
        }else{
            Logger.e("User is NULL.unable to AutoLogin..!!!");
        }
    }

    private void initView() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.country_codes_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);

        Button signIn = (Button) findViewById(R.id.login_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        mUser = new User();
        mUser.setCountryCode(mSpinner.getSelectedItem().toString());
    }

    private void signIn() {
        if(hasAllPermissions() && isDataValid()) {
            AppUtil.getInstance().forceSync(getResources());
            String fcmId = PreferenceManager.getInstance().getFcmId();
            storeUserData(fcmId);
            if(fcmId != null ){
                NetworkUtils.updateUser(mUser, this);
            }
        }
    }

    private void storeUserData(String fcmId) {
        String mobile = mMobile.getText().toString();
        String photoUrl = Contacts.getQuery().getPhotoUri(mobile);
        mUser.setFcmId(fcmId);
        mUser.setEMail(mEmail.getText().toString());
        mUser.setMobileNumber(mobile);
        mUser.setName(mUsername.getText().toString());
        mUser.setContactImageUrl(photoUrl);
        PreferenceManager.getInstance().storeUser(mUser);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        PreferenceManager.getInstance().put(KEY_USER_COUNTRY_CODE, parent.getItemAtPosition(position).toString());
        mUser.setCountryCode(parent.getItemAtPosition(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void launchMainActivity() {
        Intent i = new Intent(TransferUpLogin.this, TransferUpActivity.class);
        if(getIntent().getExtras() != null) {
            i.putExtras(getIntent().getExtras());
        }
        startActivity(i);
        finish();
    }

    @Override
    public void onAcquiredAccountPermission() {
        Logger.i("onAcquiredAccountPermission ..!! ");
        addAccount();
    }

    private void addAccount() {
        Logger.i("AFter Permissions Acquired adding Account ..!! ");
        AppUtil utils = AppUtil.getInstance();
        if(!utils.isAccountExist(this)){
            Logger.i("Account does not exists");
            utils.createAndSync(this);
        } else {
            Logger.i("Account already exist");
        }
    }

    @Override
    public void onSuccess(Response response) {
        Logger.i("updateUser Success : "+response.body());
        if(AppUtil.getInstance().isAccountExist(this)) {
            launchMainActivity();
        } else {
            Logger.i("TransferUp Account Does not exists ");
        }

    }

    @Override
    public void onFailure(Throwable error) {
        Toast.makeText(TransferUpLogin.this,"Unable to update the USer ", Toast.LENGTH_LONG).show();
        Logger.log(Logger.ERROR, "SAVE_USER", error.getMessage(), error);
    }

    public boolean isDataValid() {
        String username = mUsername.getText().toString();
        String email = mEmail.getText().toString();
        String mobile = mMobile.getText().toString();
        if(username != null && username.length() > 0 && AppUtil.isValidUser(email, mobile)){
            return true;
        }
        Logger.e("User Input Data Validation falied.");
        return false;
    }
}
