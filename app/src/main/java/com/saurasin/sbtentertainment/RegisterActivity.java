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
import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private String mobileNumberFromIntent;
    
    private final int AGREEMENT_REQUEST_CODE = 1;
    private final int THANK_YOU_REQUEST_CODE = 2;
    
    private boolean agreementAccepted = true;
    
    private String emailFromBackend = "";
    private String crmId = "";
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        backend = AgreementBackend.getInstance(this);

        Calendar newCalendar = Calendar.getInstance();
        final DatePickerDialog childOneDOB = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                getChildonedobET.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear+1, year));
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        final DatePickerDialog childTwoDOB = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                getChildtwodobET.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear+1, year));
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
    }
    
    
    private void getDataFromDB() {
        Entry entry = backend.getEntryByPhone(phoneET.getEditableText().toString());
        if (entry == null) {
            BitrixGetContactTask task = new BitrixGetContactTask(this, phoneET.getEditableText().toString());
            task.execute();
            try {
                entry = task.get();
            } catch (InterruptedException|ExecutionException e) {
                Log.e(TAG, "Error while fetching data from CRM" + e.getMessage());
            }
            if (entry != null) {
                backend.addEntry(entry);
                getDataFromDB();
            }
        } else {
            crmId = entry.getId();
            updateUIWithEntry(entry);
        }
    }
    
    private void updateUIWithEntry(final Entry entry) {
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

            Entry entry = new Entry(crmId, email, name, phone, bdayVenueCB.isChecked() ? "YES" : "NO",
                    kidsActivitiesCB.isChecked() ? "YES" : "NO", "NO",
                    !emailFromBackend.equals(email)?"YES":"NO", children);
            if (!TextUtils.isEmpty(crmId)) {
                backend.updateEntry(entry);
            } else {
                backend.addEntry(entry);
            }
            sendSms(entry);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please accept the terms and conditions to proceed.")
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
    
    private void sendSms(final Entry entry) {
        SmsSenderTask task  = new SmsSenderTask(this);
        task.execute(entry.getPhone(), SmsSender.createMessage(entry));
        try {
            task.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException ex) {
            Log.e(TAG, "Error while sending SMS:: " + ex.getMessage());
        } catch (TimeoutException timeoutException) {
            Log.e(TAG, "Timed out while sending sms:: " + timeoutException.getMessage());
        }
        
        Intent intent = new Intent(this, ThankYouActivity.class);
        startActivityForResult(intent, THANK_YOU_REQUEST_CODE);
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
        } else if (requestCode == THANK_YOU_REQUEST_CODE) {
            finish();
        }
    }
}

