
package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by saurasin on 4/20/17.
 */

public class SmsSenderTask extends AsyncTask<String, Integer, Long> {
    final ProgressDialog progressDialog;
    final onTaskCompleted<Long> completionCallback;
    
    public SmsSenderTask(final ProgressDialog pd, final onTaskCompleted<Long> callback) {
        progressDialog = pd;
        completionCallback = callback;
    }
    @Override
    protected Long doInBackground(String... params) {
        final String number = params[0];
        final String message = params[1];
        SmsSender.sendSms(message, number);
        return 0L;
    }

    @Override
    protected void onPostExecute(Long result) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        completionCallback.onTaskCompleted(result);
        super.onPostExecute(result);
    }
}