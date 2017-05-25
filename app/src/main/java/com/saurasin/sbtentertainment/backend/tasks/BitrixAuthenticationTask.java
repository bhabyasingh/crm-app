package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

import android.app.ProgressDialog;
import android.os.AsyncTask;


/**
 * Created by saurasin on 4/20/17.
 */

public class BitrixAuthenticationTask extends AsyncTask<String, Integer, Boolean> {
    private final String username;
    private final String password;
    private final ProgressDialog progressDialog;
    private final onTaskCompleted<Boolean> completionCallback;

    public BitrixAuthenticationTask(final onTaskCompleted<Boolean> callback, 
                                    final ProgressDialog pd, final String user, final String passwd) {
        completionCallback = callback;
        progressDialog = pd;
        this.username = user;
        this.password = passwd;
    }


    @Override
    protected Boolean doInBackground(String... params) {
        return BitrixCRMInvoker.initiate(username, password);
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        completionCallback.onTaskCompleted(result);
        super.onPostExecute(result);
    }
}