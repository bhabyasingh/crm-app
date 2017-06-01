package com.saurasin.sbtentertainment;

import com.saurasin.sbtentertainment.backend.tasks.onTaskCompleted;
import com.saurasin.sbtentertainment.backend.utils.LocalDBRepository;
import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.tasks.BitrixGetContactTask;
import com.saurasin.sbtentertainment.backend.tasks.SmsSenderTask;
import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by saurasin on 1/26/17.
 */
public class RegisterActivity extends AppCompatActivity implements onTaskCompleted<Entry> {
    
    public static final String MOBILE_INTENT_EXTRA = "MOBILE_INTENT_EXTRA";
    
    private EditText phoneET;
    private EditText emailET;
    private EditText nameET;
    private EditText childonenameET;
    private EditText childonedobET;
    private EditText childtwonameET;
    private EditText childtwodobET;
    private CheckBox bdayVenueCB;
    private CheckBox kidsActivitiesCB;
    
    private LocalDBRepository backend;
    private String mobileNumberFromIntent;
    
    private final int THANK_YOU_REQUEST_CODE = 2;
    
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
        childonedobET  = (EditText) findViewById(R.id.childone_dob);
        childtwonameET = (EditText) findViewById(R.id.childtwo_name_edit);
        childtwodobET  = (EditText) findViewById(R.id.childtwo_dob);
        bdayVenueCB = (CheckBox) findViewById(R.id.checkbox_bday);
        kidsActivitiesCB = (CheckBox) findViewById(R.id.checkbox_kids_activities);

        backend = LocalDBRepository.getInstance(this);
        
        
        mobileNumberFromIntent = getIntent().getStringExtra(MOBILE_INTENT_EXTRA);
    }

    private ProgressDialog showProgressDialog(final String message) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.show();
        return  pd;
    }
    
    private void getDataFromDB() {
        Entry entry = backend.getEntryByPhone(phoneET.getEditableText().toString());
        if (entry == null) {
            BitrixGetContactTask task = new BitrixGetContactTask(showProgressDialog("Loading .."), 
                    this, phoneET.getEditableText().toString());
            task.execute();
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
        childonenameET.setText(entry.getChildOneName());
        childonedobET.setText(entry.getChildOneDob());
        if (!TextUtils.isEmpty(entry.getChildTwoName())) {
            childtwonameET.setText(entry.getChildTwoName());
            childtwodobET.setText(entry.getChildTwoDob());
        }
        
        phoneET.setEnabled(false);
        emailFromBackend = entry.getEmail();
    }
    
    public void onSave(View v) {
        final String name = nameET.getEditableText().toString().trim();
        final String phone = phoneET.getEditableText().toString().trim();
        final String email = emailET.getEditableText().toString().trim();
        final String childonename = childonenameET.getEditableText().toString().trim();
        final String childonedob = childonedobET.getEditableText().toString().trim();
        final String childtwoname = childtwonameET.getEditableText().toString().trim();
        final String childtwodob = childtwodobET.getEditableText().toString().trim();

        if (TextUtils.isEmpty(childonename) || TextUtils.isEmpty(childonedob)) {
            Toast.makeText(this,
                    getResources().getText(R.string.childone_details_missing),
                    Toast.LENGTH_LONG).show();
            return;
        }

        Entry entry = new Entry(crmId, email, name, phone, bdayVenueCB.isChecked() ? "YES" : "NO",
                kidsActivitiesCB.isChecked() ? "YES" : "NO", "NO",
                !emailFromBackend.equals(email)?"YES":"NO", childonename, childonedob, childtwoname, childtwodob);
        if (!TextUtils.isEmpty(crmId)) {
            backend.updateEntry(entry);
        } else {
            backend.addEntry(entry);
        }
        sendSms(entry);
    }
    
    private void sendSms(final Entry entry) {
        SmsSenderTask task  = new SmsSenderTask(showProgressDialog("Sending sms ..."), new onTaskCompleted<Long>() {
            @Override
            public void onTaskCompleted(Long aLong) {
                Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
                startActivityForResult(intent, THANK_YOU_REQUEST_CODE);
            }
        });
        task.execute(entry.getPhone(), SmsSender.createMessage(entry));
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mobileNumberFromIntent)) {
            phoneET.setText(mobileNumberFromIntent);
            getDataFromDB();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == THANK_YOU_REQUEST_CODE) {
            finish();
        }
    }
    
    public void onCancel(View v) {
        finish();
    }

    @Override
    public void onTaskCompleted(Entry entry) {
        if (entry != null) {
            boolean res = backend.addEntry(entry);
            if (res) {
                getDataFromDB();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Invalid date returned for child date of birth. Please input correct date.")
                        .setCancelable(false)
                        .setPositiveButton(getResources().getText(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        getParent().finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
    
    public void onChildDOB(View v) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        final EditText dobControl = (EditText)v;
        final String date = dobControl.getEditableText().toString();
        Calendar newCalendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(date)) {
            String[] splitDates = date.split("/");
            newCalendar.set(Integer.parseInt(splitDates[2]),Integer.parseInt(splitDates[1])-1,
                    Integer.parseInt(splitDates[0]));
        }
        DatePickerDialog datePickerDlg = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog, 
                new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dobControl.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear+1, year));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDlg.getDatePicker().setForegroundGravity(11);
        datePickerDlg.show();
    }
}

