/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by saurasin on 1/26/17.
 */
public class AgreementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agreement_layout);
    }
    
    public void onAgree(View v) {
        setResult(Activity.RESULT_OK);
        finish();
    }
    
    public void onDisagree(View v) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
