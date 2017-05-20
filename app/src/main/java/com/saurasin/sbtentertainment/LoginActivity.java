/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment;

import com.saurasin.sbtentertainment.backend.tasks.BitrixAuthenticationTask;
import com.saurasin.sbtentertainment.backend.utils.LeadConstants;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.concurrent.ExecutionException;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    
    private static String TAG = LoginActivity.class.getSimpleName();
    // UI references.
    private TextView mEmailView;
    private EditText mPasswordView;
    private SharedPreferences  mPrefs;
    
    private static final String PREF_USER_EMAIL = "useremail";
    private static final String PREF_INVALID_USER_EMAIL = "Invalid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (TextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        initializeSpinner();
        mPrefs = getPreferences(MODE_PRIVATE);
        final String userEmail = mPrefs.getString(PREF_USER_EMAIL, PREF_INVALID_USER_EMAIL);
        if (!PREF_INVALID_USER_EMAIL.equals(userEmail)) {
            mEmailView.setText(userEmail);
        }
    }
    
    private void initializeSpinner() {
        Spinner locationSpinner = (Spinner) findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.location_array, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setOnItemSelectedListener(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    private void attemptLogin() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing In");
        progressDialog.show();
        BitrixAuthenticationTask task = new BitrixAuthenticationTask(this, 
                mEmailView.getEditableText().toString(),
                mPasswordView.getEditableText().toString());
        task.execute();
        boolean result = true;
        try {
            result = task.get();
        } catch (InterruptedException|ExecutionException iex) {
            Log.e(TAG, "Error logging in: " + iex.getMessage());
            result = false;
        }
        progressDialog.dismiss();
        if (result) {
            SharedPreferences.Editor prefEditor = mPrefs.edit();
            prefEditor.putString(PREF_USER_EMAIL, mEmailView.getEditableText().toString());
            prefEditor.apply();
            LeadConstants.getInstance().setSource("SELF");
            Intent initialIntent = new Intent(this, InitialActivity.class);
            startActivity(initialIntent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Invalid credentials")
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
    public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
        final String location = (String)spinner.getItemAtPosition(position);
        LeadConstants.getInstance().setLocation(location);
    }

    @Override
    public void onNothingSelected(AdapterView<?> spinner) {

    }
}

