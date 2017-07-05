package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

import android.accounts.AuthenticatorException;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.saurasin.sbtentertainment.SBTEntertainment;

/**
 * Created by saurasin on 5/7/17.
 */
public class BitrixGetContactTask extends AsyncTask<String, Integer, Entry> {
    final private String mobileNumber;
    final private onTaskCompleted<Entry> completionCallback;
    final private ProgressDialog progressDialog;
    

    public BitrixGetContactTask(final ProgressDialog pd, final onTaskCompleted<Entry> callback, final String mNumber) {
        progressDialog = pd;
        completionCallback = callback;
        this.mobileNumber = mNumber;
    }
    
    @Override
    protected Entry doInBackground(String... params) {
        Entry entry = null;
        try {
            entry = BitrixCRMInvoker.getEntryFromBackend(mobileNumber);
        } catch (AuthenticatorException ex) {
            SBTEntertainment.setTokenExpired(true);
        }
        return entry;
    }

    @Override
    protected void onPostExecute(Entry result) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        completionCallback.onTaskCompleted(result);
        super.onPostExecute(result);
    }
}