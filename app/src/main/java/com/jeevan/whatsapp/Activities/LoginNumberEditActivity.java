package com.jeevan.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;
import com.raycoarana.codeinputview.CodeInputView;
import com.raycoarana.codeinputview.OnCodeCompleteListener;
import com.tfcporciuncula.phonemoji.PhonemojiTextInputEditText;

import java.util.concurrent.TimeUnit;

public class LoginNumberEditActivity extends AppCompatActivity {

    //Debug string
    private static final String TAG = "LoginNumberEditActivity";

    //Fields variables
    private Button sendNotificationButton;
    private PhonemojiTextInputEditText phoneNumberEditText;
    private PinEntryEditText pinEntry;
    private TextView sendCodeMessageTextView;

    //values hold variables
    private String phoneNumber;

    private ProgressDialog loading;

    //Number Auth
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //firebase auth
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_number_edit);

        initializeFields();

        sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send the verification code to Number
                sendVerificationCode();
            }
        });

        if (pinEntry != null) {

            pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence code) {
                    Toast.makeText(LoginNumberEditActivity.this, "" + code, Toast.LENGTH_SHORT).show();
                    loadingBar();
                    verifyCode(code.toString());
                }
            });
        }




        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                //automatically login if the code is verified in same phone
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                Toast.makeText(LoginNumberEditActivity.this, "Something wrong with Phone Number ", Toast.LENGTH_SHORT).show();

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Log.d(TAG, "onVerificationFailed: Invalid request ");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Log.d(TAG, "onVerificationFailed: The SMS quota for the project has been exceeded");
                }

                // Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                /**
                 *  disable the phone number input for 60 sec
                 * and again enable after 60 sec in thread
                 */
                phoneNumberEditText.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        phoneNumberEditText.setEnabled(true);
                    }
                },6000);

                visibleCodeInput();
                clearFields();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(LoginNumberEditActivity.this, "Code has been sent!", Toast.LENGTH_SHORT).show();
                sendCodeMessageTextView.setText("The 6 digits code has been sent to number "+ phoneNumber + " enter below to verify your number.");
            }
        };

    }

    private void initializeFields()
    {

        sendNotificationButton = findViewById(R.id.send_notification_btn_num_verify);
        phoneNumberEditText = findViewById(R.id.phonemojiTextInputEditText);
        pinEntry = findViewById(R.id.txt_pin_entry);
        sendCodeMessageTextView = findViewById(R.id.codeSendMessageTextView);
    }


    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void sendVerificationCode() {

        phoneNumber = phoneNumberEditText.getText().toString().trim().replaceAll("\\s+","").replaceAll("-","");


        Toast.makeText(this, "" + phoneNumber, Toast.LENGTH_SHORT).show();
        if(TextUtils.isEmpty(phoneNumber))
        {
            Toast.makeText(this, "Number couldn't be empty !", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                LoginNumberEditActivity.this,  // Activity (for callback binding)
                callbacks);        // OnVerificationStateChangedCallbacks

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        loadingBar();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            if(loading != null)
                            {
                                loading.dismiss();
                            }
                            sendToMainActivity();
                            finish();

                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(LoginNumberEditActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
                                pinEntry.setError("Code Invalid !");
                                if(loading != null)
                                {
                                    loading.dismiss();
                                }
                            }
                        }
                    }
                });
    }

    private void sendToMainActivity() {
        startActivity(new Intent(LoginNumberEditActivity.this, MainActivity.class));
        finish();
    }

    private void visibleCodeInput() {
        //TODO animate when visible
      pinEntry.setVisibility(View.VISIBLE);
    }

    private void clearFields()
    {
        phoneNumberEditText.setText("");
    }

    private void loadingBar()
    {
        loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage("Checking Code...");
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.setTitle("Wait..");
        loading.show();
    }
}