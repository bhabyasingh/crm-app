/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment;

import com.saurasin.sbtentertainment.backend.utils.AgreementBackend;
import com.saurasin.sbtentertainment.backend.model.ChildEntry;
import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.tasks.BitrixGetContactTask;
import com.saurasin.sbtentertainment.backend.tasks.BitrixUpdateContactTask;
import com.saurasin.sbtentertainment.backend.tasks.SmsSenderTask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by saurasin on 1/26/17.
 */
public class RegisterActivity extends AppCompatActivity {
    
    final static String TAG = RegisterActivity.class.getSimpleName();
    
    public static final String MOBILE_INTENT_EXTRA = "MOBILE_INTENT_EXTRA";
    
    private EditText phoneET;
    private EditText emailET;
    private EditText nameET;
    private EditText childonenameET;
    private EditText getChildonedobET;
    private EditText childtwonameET;
    private EditText getChildtwodobET;
    private CheckBox bdayVenueCB;
    private CheckBox kidsActivitiesCB;
    private CheckBox termsAndConditionCB;
    private Button doneButon;
    
    private List<ChildEntry> children;
    private AgreementBackend backend;
    private boolean updateMode;
    private String mobileNumberFromIntent;
    
    private final int AGREEMENT_REQUEST_CODE = 1;
    
    private boolean agreementAccepted = true;
    
    private String emailFromBackend = "";
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateMode = false;
        setContentView(R.layout.register_layout);
        
        phoneET = (EditText) findViewById(R.id.parentPhoneEdit);
        emailET = (EditText) findViewById(R.id.parentEmailEdit);
        nameET = (EditText) findViewById(R.id.parentNameEdit);
        childonenameET = (EditText) findViewById(R.id.childone_name_edit);
        getChildonedobET = (EditText) findViewById(R.id.childone_dob_edit);
        childtwonameET = (EditText) findViewById(R.id.childtwo_name_edit);
        getChildtwodobET = (EditText) findViewById(R.id.childtwo_dob_edit);
        bdayVenueCB = (CheckBox) findViewById(R.id.checkbox_bday);
        kidsActivitiesCB = (CheckBox) findViewById(R.id.checkbox_kids_activities);
        termsAndConditionCB = (CheckBox) findViewById(R.id.checkbox_terms);
        doneButon = (Button) findViewById(R.id.submit_button);
        children = new ArrayList<>();

        backend = new AgreementBackend(this);

        Calendar newCalendar = Calendar.getInstance();
        final DatePickerDialog childOneDOB = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                getChildonedobET.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear, year));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        final DatePickerDialog childTwoDOB = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                getChildtwodobET.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear, year));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        
        getChildonedobET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    childOneDOB.show();
                }
            }
        });
        getChildtwodobET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    childTwoDOB.show();
                }
            }
        });
        
        mobileNumberFromIntent = getIntent().getStringExtra(MOBILE_INTENT_EXTRA);
        Toast.makeText(this,
                "MobileNumber::" + mobileNumberFromIntent,
                Toast.LENGTH_LONG).show();
    }
    
    
    private void getDataFromDB() {
        Entry entry = backend.getEntryByPhone(phoneET.getEditableText().toString());
        if (entry == null) {
            BitrixGetContactTask task =
                    new BitrixGetContactTask(this, "Fetching data from CRM", phoneET.getEditableText().toString());
            task.execute();
            try {
                entry = task.get();
            }catch (InterruptedException|ExecutionException e) {
                Log.e(TAG, "Error while fetching data from CRM" + e.getMessage());
            }
            if (entry != null) {
                backend.addEntry(entry);
                getDataFromDB();
            } else {
                updateMode = false;
            }
        } else {
            updateUIWithEntry(entry);
        }
    }
    
    private void updateUIWithEntry(final Entry entry) {
        updateMode = true;
        emailET.setText(entry.getEmail());
        nameET.setText(entry.getName());
        bdayVenueCB.setChecked("YES".equals(entry.getInterestInBdayVenue()));
        kidsActivitiesCB.setChecked("YES".equals(entry.getInterestInKidsActivities()));
        ChildEntry childOne = entry.getChildren().get(0);
        childonenameET.setText(childOne.getName());
        getChildonedobET.setText(childOne.getDOB());
        if (entry.getChildren().size() > 1) {
            ChildEntry childTwo = entry.getChildren().get(1);
            childtwonameET.setText(childTwo.getName());
            getChildtwodobET.setText(childTwo.getDOB());
        }
        termsAndConditionCB.setChecked(agreementAccepted);
        doneButon.setText(R.string.done_button_text);
        phoneET.setEnabled(false);
        updateMode = true;
        emailFromBackend = entry.getEmail();
    }
    
    public void onSave(View v) {
        if (termsAndConditionCB.isChecked()) {
            children.clear();
            final String name = nameET.getEditableText().toString();
            final String phone = phoneET.getEditableText().toString();
            final String email = emailET.getEditableText().toString();
            final String childonename = childonenameET.getEditableText().toString();
            final String childonedob = getChildonedobET.getEditableText().toString();
            final String childtwoname = childtwonameET.getEditableText().toString();
            final String childtwodob = getChildtwodobET.getEditableText().toString();

            if (!TextUtils.isEmpty(childtwoname) && !TextUtils.isEmpty(childtwodob)) {
                ChildEntry ce = new ChildEntry(childtwoname, childtwodob);
                children.add(ce);
            }

            if (!TextUtils.isEmpty(childonename) && !TextUtils.isEmpty(childonedob)) {
                ChildEntry ce = new ChildEntry(childonename, childonedob);
                children.add(ce);
            } else {
                Toast.makeText(this,
                        getResources().getText(R.string.childone_details_missing),
                        Toast.LENGTH_LONG).show();
                return;
            }

            Entry entry = new Entry(email, name, phone, bdayVenueCB.isChecked() ? "YES" : "NO",
                    kidsActivitiesCB.isChecked() ? "YES" : "NO", "YES", children);
            BitrixUpdateContactTask task = 
                    new BitrixUpdateContactTask(this, "Saving data to backend", entry, 
                            updateMode, !emailFromBackend.equals(email));
            task.execute();
            Boolean update = false;
            try {
                update = task.get();
            } catch(InterruptedException|ExecutionException iex) {
                Toast.makeText(this, "Error while syncing with backend", Toast.LENGTH_LONG).show();
            }
            if (update) {
                if (updateMode) {
                    backend.updateEntry(entry);
                } else {
                    backend.addEntry(entry);
                }
                new SmsSenderTask(this, "Processing ...").execute(phone, name);
            }
        } else {
            Toast.makeText(this,
                    getResources().getText(R.string.terms_not_accepted),
                    Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mobileNumberFromIntent)) {
            phoneET.setText(mobileNumberFromIntent);
            getDataFromDB();
        }
    }
    
    public void onViewTerms(View v) {
        Intent intent = new Intent(this, AgreementActivity.class);
        startActivityForResult(intent, AGREEMENT_REQUEST_CODE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AGREEMENT_REQUEST_CODE) {
                agreementAccepted = (resultCode == Activity.RESULT_OK);
        }
    }
}

