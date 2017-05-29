package com.saurasin.sbtentertainment;

import com.saurasin.sbtentertainment.backend.tasks.onTaskCompleted;
import com.saurasin.sbtentertainment.backend.utils.AgreementBackend;
import com.saurasin.sbtentertainment.backend.model.ChildEntry;
import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.tasks.BitrixGetContactTask;
import com.saurasin.sbtentertainment.backend.tasks.SmsSenderTask;
import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by saurasin on 1/26/17.
 */
public class RegisterActivity extends AppCompatActivity implements onTaskCompleted<Entry> {
    
    public static final String MOBILE_INTENT_EXTRA = "MOBILE_INTENT_EXTRA";
    
    private EditText phoneET;
    private EditText emailET;
    private EditText nameET;
    private EditText childonenameET;
    private Spinner childonedobMonth;
    private Spinner childonedobDay;
    private Spinner childonedobYear;
    private EditText childtwonameET;
    private Spinner childtwodobMonth;
    private Spinner childtwodobDay;
    private Spinner childtwodobYear;
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
        childonedobMonth = (Spinner) findViewById(R.id.chileone_dob_month);
        childonedobDay = (Spinner) findViewById(R.id.chileone_dob_date);
        childonedobYear = (Spinner) findViewById(R.id.chileone_dob_year);
        childtwonameET = (EditText) findViewById(R.id.childtwo_name_edit);
        childtwodobMonth = (Spinner) findViewById(R.id.chiletwo_dob_month);
        childtwodobDay = (Spinner) findViewById(R.id.chiletwo_dob_date);
        childtwodobYear = (Spinner) findViewById(R.id.chiletwo_dob_year);
        bdayVenueCB = (CheckBox) findViewById(R.id.checkbox_bday);
        kidsActivitiesCB = (CheckBox) findViewById(R.id.checkbox_kids_activities);
        termsAndConditionCB = (CheckBox) findViewById(R.id.checkbox_terms);
        doneButon = (Button) findViewById(R.id.submit_button);
        children = new ArrayList<>();

        backend = AgreementBackend.getInstance(this);
        
        
        mobileNumberFromIntent = getIntent().getStringExtra(MOBILE_INTENT_EXTRA);
        initializeDOBSpinners();
    }
    
    private void initializeDOBSpinners() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, R.layout.spinner_layout);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        childonedobMonth.setAdapter(adapter);
        childtwodobMonth.setAdapter(adapter);
        ArrayList<String> years = new ArrayList<>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2005; i <= thisYear; i++)
        {
            years.add(String.valueOf(i));
        }

        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, years);
        yearsAdapter.setDropDownViewResource(R.layout.spinner_layout);
        childonedobYear.setAdapter(yearsAdapter);
        childtwodobYear.setAdapter(yearsAdapter);

        ArrayList<String> dates = new ArrayList<>();
        for (int i = 1; i <= 31; i++)
        {
            dates.add(String.valueOf(i));
        }

        ArrayAdapter<String> datesAdapter = new ArrayAdapter<>(this, R.layout.spinner_layout, dates);
        datesAdapter.setDropDownViewResource(R.layout.spinner_layout);
        childonedobDay.setAdapter(datesAdapter);
        childtwodobDay.setAdapter(datesAdapter);
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
            BitrixGetContactTask task = new BitrixGetContactTask(showProgressDialog("Fetching data from backend .."), 
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
        ChildEntry childOne = entry.getChildren().get(0);
        childonenameET.setText(childOne.getName());
        setDateSpinners(childOne.getDOB(), childonedobDay, childonedobMonth, childonedobYear);
        if (entry.getChildren().size() > 1) {
            ChildEntry childTwo = entry.getChildren().get(1);
            childtwonameET.setText(childTwo.getName());

            setDateSpinners(childTwo.getDOB(), childtwodobDay, childtwodobMonth, childtwodobYear);
        } else {
            setDateSpinners("1/1/1900", childtwodobDay, childtwodobMonth, childtwodobYear);
        }
        termsAndConditionCB.setChecked(agreementAccepted);
        doneButon.setText(R.string.done_button_text);
        phoneET.setEnabled(false);
        emailFromBackend = entry.getEmail();
    }
    
    private void setDateSpinners(final String dob, Spinner dSpinner, Spinner mSpinner, Spinner ySpinner) {
        String[] dobSplit = dob.split("/");
        int date = Integer.parseInt(dobSplit[0]);
        int month = Integer.parseInt(dobSplit[1]);
        int year = Integer.parseInt(dobSplit[2]);
        if (date > 31 || month > 12  || year > Calendar.getInstance().get(Calendar.YEAR)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Invalid date returned for child date of birth. Please input correct date.")
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            dSpinner.setSelection(date - 1);
            mSpinner.setSelection(month - 1);
            ySpinner.setSelection(year - 2005);
        }
    }
    
    
    public void onSave(View v) {
        if (termsAndConditionCB.isChecked()) {
            children.clear();
            final String name = nameET.getEditableText().toString();
            final String phone = phoneET.getEditableText().toString();
            final String email = emailET.getEditableText().toString();
            final String childonename = childonenameET.getEditableText().toString();
            final String childonedob = String.format("%d/%d/%d", 
                    childonedobDay.getSelectedItemPosition() + 1, childonedobMonth.getSelectedItemPosition() + 1,
                    childonedobYear.getSelectedItemPosition() + 2005);
            final String childtwoname = childtwonameET.getEditableText().toString();
            final String childtwodob = String.format("%d/%d/%d",
                    childtwodobDay.getSelectedItemPosition() + 1, childtwodobMonth.getSelectedItemPosition() + 1,
                    childtwodobYear.getSelectedItemPosition() + 2005);

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
    
    public void onCancel(View v) {
        finish();
    }

    @Override
    public void onTaskCompleted(Entry entry) {
        if (entry != null) {
            backend.addEntry(entry);
            getDataFromDB();
        }
    }
}

