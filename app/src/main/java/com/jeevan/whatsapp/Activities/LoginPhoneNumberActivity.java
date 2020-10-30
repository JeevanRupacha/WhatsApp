package com.jeevan.whatsapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jeevan.whatsapp.R;

public class LoginPhoneNumberActivity extends AppCompatActivity {

    //Fields variables
    private Button proceedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_number);

        initializeFields();
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLoginNumberEdit();
            }
        });
    }

    private void sendToLoginNumberEdit() {
        startActivity(new Intent(LoginPhoneNumberActivity.this, LoginNumberEditActivity.class));
        finish();
    }

    private void initializeFields() {
        proceedButton = findViewById(R.id.loginButtonWithNumber);
    }
}