package com.saurasin.sbtentertainment;


import com.saurasin.sbtentertainment.backend.tasks.CrmUpdateTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class InitialActivity extends AppCompatActivity {
    
    private EditText mobileNumberET;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        mobileNumberET = (EditText) findViewById(R.id.phone_number_input);
        mobileNumberET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    openNextScreen();
                    handled = true;
                }
                return handled;
            }
        });
    }
    
    public void onRegister(View v) {
        openNextScreen();
    }
    
    private void openNextScreen() {
        final String mobileNumber = mobileNumberET.getText().toString().trim();
        if (mobileNumber.length() == 10) {
            Intent intent = new Intent(this, RegisterActivity.class);
            intent.putExtra(RegisterActivity.MOBILE_INTENT_EXTRA, mobileNumber);
            mobileNumberET.setText("");
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("The number entered is invalid. Please enter a 10 digit valid mobile number")
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
    
    @Override
    public void onBackPressed() {
        
    }
    
    @Override
    public void onResume() {
        if (SBTEntertainment.isTokenExpired()) {
            finish();
        } else {
            SBTEntertainment.submitTask(new CrmUpdateTask(getApplicationContext()));
        }
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
