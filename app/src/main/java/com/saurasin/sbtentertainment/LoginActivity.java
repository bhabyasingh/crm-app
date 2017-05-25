package com.saurasin.sbtentertainment;

import com.saurasin.sbtentertainment.backend.tasks.BitrixAuthenticationTask;
import com.saurasin.sbtentertainment.backend.tasks.onTaskCompleted;
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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.concurrent.ExecutionException;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity 
        implements AdapterView.OnItemSelectedListener, onTaskCompleted<Boolean> {
    
    private static String TAG = LoginActivity.class.getSimpleName();
    // UI references.
    private TextView mEmailView;
    private EditText mPasswordView;
    private SharedPreferences  mPrefs;
    
    private static final String PREF_USER_EMAIL = "useremail";
    private static final String PREF_INVALID_USER_EMAIL = "Invalid";
    private static final String PREF_LOGIN_PREFERENCES = "AuthPreferences";

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
                    attemptLogin(textView);
                    return true;
                }
                return false;
            }
        });
        
        initializeSpinner();
        mPrefs = getSharedPreferences(PREF_LOGIN_PREFERENCES, MODE_PRIVATE);
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
    
    private ProgressDialog showProgressDialog() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Signin in ...");
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.show();
        return  pd;
    }
    
    public void attemptLogin(View view) {
        BitrixAuthenticationTask task = new BitrixAuthenticationTask(this, showProgressDialog(),
                mEmailView.getEditableText().toString(),
                mPasswordView.getEditableText().toString());
        task.execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
        final String location = (String)spinner.getItemAtPosition(position);
        LeadConstants.getInstance().setLocation(location);
    }

    @Override
    public void onNothingSelected(AdapterView<?> spinner) {

    }

    @Override
    public void onTaskCompleted(Boolean result) {
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
}

