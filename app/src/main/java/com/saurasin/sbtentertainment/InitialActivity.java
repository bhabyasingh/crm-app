/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment;

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
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra(RegisterActivity.MOBILE_INTENT_EXTRA, mobileNumberET.getText().toString());
        startActivity(intent);
    }
    
    @Override
    public void onBackPressed() {
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
