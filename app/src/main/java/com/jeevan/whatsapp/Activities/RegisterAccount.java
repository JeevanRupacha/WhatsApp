package com.jeevan.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.RotatingCircle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;

public class RegisterAccount extends AppCompatActivity {


    //tag name for debug
    private static final String TAG= "RegisterActivity";

    private EditText emailInput, passwordInput;
    private TextView alreadyHaveAccount;
    private Button createAccountButton;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();
        setUpSpinkkit();

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void setUpSpinkkit()
    {
        Sprite rotatingCircle = new RotatingCircle();
        progressBar.setIndeterminateDrawable(rotatingCircle);
    }

    private void createNewAccount()
    {

        progressBar.setVisibility(View.VISIBLE);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please provide the information above ", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateDatabase();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterAccount.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }

    private void initializeFields() {
        emailInput = findViewById(R.id.editTextEmailForRegister);
        passwordInput = findViewById(R.id.editTextPasswordForRegister);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
        createAccountButton = findViewById(R.id.createNewAccountButton);

        progressBar = (ProgressBar)findViewById(R.id.spin_kit);
    }

    private void updateUI(FirebaseUser user)
    {
        if(user != null) {
            if(getUsername() != null) {
                sendToMainActivity();
            }else{
                sendToSettingActivity();
            }
        }
    }

    private void sendToSettingActivity() {
        startActivity(new Intent(RegisterAccount.this, SettingActivity.class));
    }

    private String getUsername() {

        final String[] userName = new String[1];

        DocumentReference docRef = db.collection("Users")
                .document(firebaseAuth.getCurrentUser().getUid());


        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                //update Fields
                                 userName[0] = (String) document.getData().get(FeedDataEntry.USERNAME);

                            } else {
                                Log.d(TAG, "No such document");
                                userName[0] = null;
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
        return userName[0];
    }

    private void updateDatabase()
    {
        //TODO this is not implemented because in this project
        //I am not getting other information like name , address etc
        //if any then update user information schema in  here
    }

    private void sendToLoginActivity() {
        Intent intent = new Intent(RegisterAccount.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(RegisterAccount.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}