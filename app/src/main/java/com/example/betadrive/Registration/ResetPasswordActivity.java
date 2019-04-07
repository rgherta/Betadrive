package com.example.betadrive.Registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.betadrive.LoginActivity;
import com.example.betadrive.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView       backButton;
    Button          resetButton;
    EditText        mEmail;
    FirebaseAuth    auth;
    ProgressBar     progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();

        backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(this);

        resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(this);

        mEmail = findViewById(R.id.my_email);
        progressBar = findViewById(R.id.progress);

    }

    @Override
    public void onClick(View v) {

        switch ( v.getId() ) {
            case R.id.backbutton:
                finish();
                break;
            case R.id.reset_button:
                attemptReset();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;
        }

    }

    private void attemptReset() {

        String email = mEmail.getText().toString();

        if( email.isEmpty() ){
            mEmail.setError("Please enter a valid email");
            return;
        };

        progressBar.setVisibility(View.VISIBLE);

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
            }

            progressBar.setVisibility(View.GONE);
        });



    }




}
