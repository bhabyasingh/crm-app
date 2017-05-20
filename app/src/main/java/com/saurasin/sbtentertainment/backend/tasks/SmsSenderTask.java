/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by saurasin on 4/20/17.
 */

public class SmsSenderTask extends AsyncTask<String, Integer, Long> {
    private Activity parentActivity;
    private ProgressDialog progressDialog;

    public SmsSenderTask(final Activity pActivity, final String message) {
        parentActivity = pActivity;
        progressDialog = new ProgressDialog(parentActivity);
        progressDialog.setMessage(message);
    }

    protected void onProgressUpdate(Integer... progress) {
        progressDialog.show();
    }

    protected void onPostExecute(Long result) {
        progressDialog.dismiss();
        parentActivity.finish();
    }


    @Override
    protected Long doInBackground(String... params) {
        final String number = params[0];
        final String message = params[1];
        SmsSender.sendSms(message, number);
        return 0L;
    }
}