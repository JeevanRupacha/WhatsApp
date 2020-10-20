package com.jeevan.whatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.RotatingCircle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Log debug
    private static final String TAG = "LoginActivity";

    //main page views variables
    private TextView registerNewAccount, forgetPassword;
    private EditText email, password;
    private Button loginButton,phoneNumberButton, googleButton;

    //progress bar custom library spin-kit github
    private ProgressBar progressBar;


    /**
     * Firebase Firestore setup
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    /**
     * Firebase Firestore
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFields();
        setUpSpinkKit();

        registerNewAccount.setOnClickListener(this);
        loginButton.setOnClickListener(this);


    }

    private void initializeFields(){
        registerNewAccount = findViewById(R.id.registerNewAccountTextView);
        forgetPassword = findViewById(R.id.forgetPasswordTextView);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.loginButton);
        phoneNumberButton = findViewById(R.id.registerWithNumber);
        googleButton = findViewById(R.id.registerWithGoogle);
        progressBar = findViewById(R.id.spin_kit_login);
    }

    private void setUpSpinkKit()
    {
        Sprite rotatingCircle = new RotatingCircle();
        progressBar.setIndeterminateDrawable(rotatingCircle);
    }

    private void sendToRegisterAccount()
    {
        Intent intent = new Intent(LoginActivity.this, RegisterAccount.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.registerNewAccountTextView : sendToRegisterAccount();
                break;
            case R.id.loginButton : login();
                break;
            default:break;
        }
    }

    private void login() {

        progressBar.setVisibility(View.VISIBLE);

        String emailVal = email.getText().toString();
        String passwordVal = password.getText().toString();

        if(TextUtils.isEmpty(emailVal) && TextUtils.isEmpty(passwordVal))
        {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please provide the information above ", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(emailVal, passwordVal)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            // ...
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null)
        {
            sentToMainActivity();
        }
    }

    private void sentToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
