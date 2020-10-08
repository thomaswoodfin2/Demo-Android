package com.pinpoint.appointment.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.auth.api.Auth;
//import com.google.android.gms.auth.api.credentials.Credential;
//import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.CustomerDetails;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinpoint.appointment.utils.Util;
import com.pinpoint.appointment.validator.ValidationClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FirebasePhoneNumAuthActivity extends BaseActivity implements ClickEvent, DataObserver, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RESOLVE_HINT = 5052;
    private static String uniqueIdentifier = null;
    private static final String UNIQUE_ID = "UNIQUE_ID";
    private static final long ONE_HOUR_MILLI = 60 * 60 * 1000;

    private static final String TAG = "FirebasePhoneNumAuth";

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth firebaseAuth;
    GoogleApiClient googleApiClient;
    private String phoneNumber;
    private Button sendCodeButton;
    private Button verifyCodeButton;
    CustomerDetails details;
    //    private Button signOutButton;
    boolean fromSettings = false;
    private EditText phoneNum;
    private EditText verifyCodeET;

    private FirebaseFirestore firestoreDB;
    private FirebaseUser firebaseUser;
    public ImageView iv_back, iv_settings;
    public TextView tv_header, tv_unitedStates;
    private GoogleApiClient mCredentialsApiClient;
    private CountryCodePicker ccp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_phonenumber);
        if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        details = new CustomerDetails();
        sendCodeButton = findViewById(R.id.btn_send);
        verifyCodeButton = findViewById(R.id.verify_code_b);
//      initGoogleApiClient();
        TextInputLayout input_layout_Phone = (TextInputLayout) findViewById(R.id.input_layout_Phone);
        TextInputLayout input_layout_OTP = (TextInputLayout) findViewById(R.id.input_layout_OTP);

        sendCodeButton.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        verifyCodeButton.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));


        ccp = findViewById(R.id.ccp);
        ccp.setDefaultCountryUsingNameCode("US");

        iv_back = (ImageView) findViewById(R.id.iv_imgMenu_Back);
        iv_settings = (ImageView) findViewById(R.id.iv_imgSetting);
        tv_header = (TextView) findViewById(R.id.tv_txtTitle);
        tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_phone_authentication)));
        tv_header.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        tv_unitedStates = findViewById(R.id.tv_unitedStates);
        tv_unitedStates.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_united_states)));
        tv_unitedStates.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        iv_back.setVisibility(View.VISIBLE);
        iv_back.setImageResource(R.mipmap.ic_back);
        iv_settings.setVisibility(View.GONE);
        phoneNum = (EditText) findViewById(R.id.et_edtPhone);
        verifyCodeET = (EditText) findViewById(R.id.et_edtOTP);
        phoneNum.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        input_layout_Phone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        input_layout_OTP.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        verifyCodeET.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        //phoneNum.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_PHONE);

        input_layout_Phone.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_phone)));
        input_layout_OTP.setHint(MessageHelper.getInstance().getAppMessage(getString(R.string.str_verification_code)));
        sendCodeButton.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_send_otp)));
        verifyCodeButton.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_verify_code)));
        addOnClickListeners();
        getNumber();
        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        createCallback();
        getInstallationIdentifier();
        if (getIntent().getExtras() != null) {

            if (getIntent().hasExtra(BaseConstants.KEY_FROM) && getIntent().getStringExtra(BaseConstants.KEY_FROM).equalsIgnoreCase("settings")) {
                fromSettings = true;
            } else {
                fromSettings = false;
                details.setName(getIntent().getStringExtra(ApiList.KEY_NAME));
                details.setEmail(getIntent().getStringExtra(ApiList.KEY_EMAIL));
                details.setCompany(getIntent().getStringExtra(ApiList.KEY_COMPANY));
                details.setImagePath(getIntent().getStringExtra(ApiList.KEY_IMAGE));
                details.setPassword(getIntent().getStringExtra(ApiList.KEY_PASSWORD));
            }
        }

        mCredentialsApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId(); <-- E.164 format phone number on 10.2.+ devices
                Log.e("credential", "cc" + credential.getAccountType());
                Log.e("credential2", "cc2" + credential.getFamilyName());
                Log.e("credential3", "cc3" + credential.getName());
                Log.e("credential4", "cc4" + credential.getId());
                phoneNum.setText(credential.getId());
            }
        }

        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential cred = data.getParcelableExtra(Credential.EXTRA_KEY);
                if (cred != null && cred.getId() != null)
                    phoneNum.setText(cred.getId());
//                LeadDetails.setMessage(FirebasePhoneNumAuthActivity.this, " From : FirebasePhoneNnumAuthActivity onActivityResult Success,  Phonenumber: "  + phoneNum+ new Date());

//                ui.setPhoneNumber(cred.getId());
            } else {
//                LeadDetails.setMessage(FirebasePhoneNumAuthActivity.this, " From : FirebasePhoneNnumAuthActivity onActivityResult Failed,  ResultCode: "  + resultCode+ new Date());

//                try {
//                    String phoneNumber=getLineNumberPhone(FirebasePhoneNumAuthActivity.this);
//                    if(phoneNumber!=null)
//                        phoneNum.setText(phoneNumber);
//                }catch (Exception ex)
//                {}
//                ui.focusPhoneNumber();
            }
        }
    }

    private void addOnClickListeners() {
        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String orignalPhoneNumber = phoneNum.getText().toString();
                phoneNumber = orignalPhoneNumber.replace("(", "");
                phoneNumber = phoneNumber.replace(")", "");
                phoneNumber = phoneNumber.replace(" ", "");
                phoneNumber = phoneNumber.replace("-", "");

                if (phoneNumber.contains("+")) {
                    CustomDialog.getInstance().showAlert(FirebasePhoneNumAuthActivity.this, "Please Enter Phone number without country code.", MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
                } else {
                    phoneNumber = ccp.getSelectedCountryCodeWithPlus() + phoneNumber;
                    if (ValidationClass.isValidPhoneNumber(phoneNumber)) {
                        verifyPhoneNumberInit();
                    } else {
                        CustomDialog.getInstance().showAlert(FirebasePhoneNumAuthActivity.this, MessageHelper.getInstance().getAppMessage(getString(R.string.str_valid_phone)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_validation_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
                    }
                }
            }
        });
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDialog.getInstance().showProgressBar(FirebasePhoneNumAuthActivity.this);
                verifyPhoneNumberCode();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            phoneNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher("US"));
        } else {
            phoneNum.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        }
    }


    private void getNumber() {
        GoogleApiClient apiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        CredentialPickerConfig conf = new CredentialPickerConfig.Builder()
                .setShowAddAccountButton(true)
                .build();

        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .setHintPickerConfig(conf)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                apiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    1, null, 0, 0, 0);

        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClickEvent(View view) {
//        super.onClickEvent(view);
        switch (view.getId()) {
            case R.id.tv_txtBackToLogin:
                finish();
                break;
        }
    }

    private void createCallback() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "verification completed" + credential);

                String code = credential.getSmsCode();

                //sometime the code is not detected automatically
                //in this case the code will be null
                //so user has to manually enter the code
                if (code != null) {
                    verifyCodeET.setText(code);
                    //verifying the code
                }
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "verification failed", e);
                CustomDialog.getInstance().hide();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    phoneNum.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(FirebasePhoneNumAuthActivity.this,
                            "Trying too many timeS",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                CustomDialog.getInstance().hide();

                ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(getString(R.string.str_code_sent)));
                disableSendCodeButton(System.currentTimeMillis());
//                signInWithPhoneAuthCredential(credential);
                addVerificationDataToFirestore(phoneNumber, verificationId);
            }
        };
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNum.setError("Invalid phone number.");
            return false;
        }
        return true;
    }

    private void verifyPhoneNumberInit() {

        if (!validatePhoneNumber(phoneNumber)) {
            return;
        }
        CustomDialog.getInstance().showProgressBar(this);
        verifyPhoneNumber(phoneNumber);
    }

    private void verifyPhoneNumber(String phno) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phno, 70,
                TimeUnit.SECONDS, this, callbacks);
    }

    private void verifyPhoneNumberCode() {
        final String phone_code = verifyCodeET.getText().toString();
        getVerificationDataFromFirestoreAndVerify(phone_code);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        CustomDialog.getInstance().hide();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "code verified signIn successful");
                            firebaseUser = task.getResult().getUser();
                            firebaseUser.getPhoneNumber();
                            showSingInButtons();
                        } else {
                            Log.w(TAG, "code verification failed", task.getException());
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                verifyCodeET.setError("Invalid code.");
                            }
                        }
                    }
                });
    }

    private void createCredentialSignIn(String verificationId, String verifyCode) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.
                    getCredential(verificationId, verifyCode);
            signInWithPhoneAuthCredential(credential);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    private void signOut() {
        firebaseAuth.signOut();
        showSendCodeButton();
    }

    private void addVerificationDataToFirestore(String phone, String verificationId) {
        Map verifyMap = new HashMap();
        verifyMap.put("phone", phone);
        verifyMap.put("verificationId", verificationId);
        verifyMap.put("timestamp", System.currentTimeMillis());

        firestoreDB.collection("phoneAuth").document(uniqueIdentifier)
                .set(verifyMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "phone auth info added to db ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding phone auth info", e);
            }
        });
    }


    private void initGoogleApiClient() {
//        googleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Auth.CREDENTIALS_API)
//                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        String phoneNumber = getLineNumberPhone(FirebasePhoneNumAuthActivity.this);
        if (phoneNumber != null)
            phoneNum.setText(phoneNumber);
    }

    private void getVerificationDataFromFirestoreAndVerify(final String code) {
//        initButtons();
        firestoreDB.collection("phoneAuth").document(uniqueIdentifier)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot ds = task.getResult();
                            if (ds.exists()) {
                                disableSendCodeButton(ds.getLong("timestamp"));
                                if (code != null) {
                                    createCredentialSignIn(ds.getString("verificationId"),
                                            code);
                                } else {
                                    verifyPhoneNumber(ds.getString("phone"));
                                }
                            } else {
                                showSendCodeButton();
                                Log.d(TAG, "Code hasn't been sent yet");
                            }

                        } else {
                            Log.d(TAG, "Error getting document: ", task.getException());
                        }
                    }
                });
    }

    public synchronized String getInstallationIdentifier() {
        if (uniqueIdentifier == null) {
            SharedPreferences sharedPrefs = this.getSharedPreferences(
                    UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueIdentifier = sharedPrefs.getString(UNIQUE_ID, null);
            if (uniqueIdentifier == null) {
                uniqueIdentifier = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(UNIQUE_ID, uniqueIdentifier);
                editor.commit();
            }
        }
        return uniqueIdentifier;
    }

    private void disableSendCodeButton(long codeSentTimestamp) {
        long timeElapsed = System.currentTimeMillis() - codeSentTimestamp;
//        if(timeElapsed > ONE_HOUR_MILLI){
//            showSendCodeButton();
//        }else{
        findViewById(R.id.phone_auth_items).setVisibility(View.VISIBLE);
        findViewById(R.id.phone_auth_code_items).setVisibility(View.VISIBLE);
        findViewById(R.id.logout_items).setVisibility(View.GONE);
//        }
    }

    private void showSendCodeButton() {
        findViewById(R.id.phone_auth_items).setVisibility(View.VISIBLE);
        findViewById(R.id.phone_auth_code_items).setVisibility(View.GONE);
        findViewById(R.id.logout_items).setVisibility(View.GONE);
    }

    private void showSingInButtons() {
        findViewById(R.id.phone_auth_items).setVisibility(View.VISIBLE);
        findViewById(R.id.phone_auth_code_items).setVisibility(View.VISIBLE);
//        findViewById(R.id.logout_items).setVisibility(View.VISIBLE);
//        redirectToHome();
        details.setContact(phoneNumber);
        if (fromSettings) {
            CustomerDetails.updatePhone(this, this, phoneNumber);
        } else {
            CustomerDetails.addUser(this, details, getIntent().getStringExtra(ApiList.KEY_IMAGE), true, this);
        }

    }

    private void redirectToHome() {
        Intent ina = new Intent(FirebasePhoneNumAuthActivity.this, HomeActivity.class);
        ina.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(ina);
        finish();
    }

    private void initButtons() {
        findViewById(R.id.phone_auth_items).setVisibility(View.GONE);
        findViewById(R.id.phone_auth_code_items).setVisibility(View.GONE);
        findViewById(R.id.logout_items).setVisibility(View.GONE);
    }

    @Override
    public void OnSuccess(RequestCode requestCode, Object object) {
        switch (requestCode) {
            case signup:
                CustomDialog.getInstance().hide();
                try {
                    Intent i = new Intent(FirebasePhoneNumAuthActivity.this, HomeActivity.class);
                    i.putExtra("login", true);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    BaseConstants.SELECT_SENT_FRIEND = false;
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case updatephone:
                CustomDialog.getInstance().hide();

                String response = object.toString();
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    JSONObject jsonObject1=jsonObject.getJSONObject(ApiList.KEY_RESPONSE);
                    String message = jsonObject.getString(ApiList.KEY_MESSAGE);
//                    CustomDialog.getInstance().showAlert(ViewProfileActivity.this,message,Util.getAppKeyValue(parent,R.string.str_app_name), Util.getAppKeyValue(ViewProfileActivity.this, R.string.lblOk), Util.getAppKeyValue(ViewProfileActivity.this, R.string.str_dismiss),false);
                    ToastHelper.displayCustomToast(message);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    @Override
    public void OnFailure(RequestCode requestCode, String errorCode, String error) {
        switch (requestCode) {
            case signup:
                CustomDialog.getInstance().hide();
//                String response=object.toString();
                try {
                    CustomDialog.getInstance().showAlert(this, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
//                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
//                    startActivity(i);
//                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case updatephone:
                CustomDialog.getInstance().hide();


                try {
                    CustomDialog.getInstance().showAlert(this, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_dialog_header)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss)), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    @Override
    public void onOtherStatus(RequestCode requestCode, Object object) {
        switch (requestCode) {
            case signup:
                CustomDialog.getInstance().hide();
        }
    }

    @Override
    public void onRetryRequest(RequestCode requestCode) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //requestHint();
    }

    @Override
    public void onConnectionSuspended(int i) {
//        LeadDetails.setMessage(FirebasePhoneNumAuthActivity.this, " From : FirebasePhoneNnumAuthActivity onConnectionSuspended"  + new Date());

//        String phoneNumber=getLineNumberPhone(FirebasePhoneNumAuthActivity.this);
//        if(phoneNumber!=null)
//            phoneNum.setText(phoneNumber);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        LeadDetails.setMessage(FirebasePhoneNumAuthActivity.this, " From : FirebasePhoneNnumAuthActivity onConnectionFailed"  + new Date());
//        String phoneNumber=getLineNumberPhone(FirebasePhoneNumAuthActivity.this);
//        if(phoneNumber!=null)
//        phoneNum.setText(phoneNumber);
    }

    public String getLineNumberPhone(Context scenario) {
//        TelephonyManager tMgr = (TelephonyManager) scenario.getSystemService(Context.TELEPHONY_SERVICE);
//        @SuppressLint("MissingPermission")
//        String mPhoneNumber = tMgr.getLine1Number();
//        return mPhoneNumber;
        String phoneNumber = "";
        String main_data[] = {"data1", "is_primary", "data3", "data2", "data1", "is_primary", "photo_uri", "mimetype"};
        Object object = getContentResolver().query(Uri.withAppendedPath(android.provider.ContactsContract.Profile.CONTENT_URI, "data"),
                main_data, "mimetype=?",
                new String[]{"vnd.android.cursor.item/phone_v2"},
                "is_primary DESC");
        if (object != null) {
            do {
                if (!((Cursor) (object)).moveToNext())
                    break;
                // This is the phoneNumber
                String s1 = ((Cursor) (object)).getString(4);
                phoneNumber = s1;
            } while (true);
            ((Cursor) (object)).close();
        }
        return phoneNumber;
    }

    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                mCredentialsApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

}
