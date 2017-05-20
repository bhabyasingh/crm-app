/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by saurasin on 4/20/17.
 */

public class SmsSenderTask extends AsyncTask<String, Integer, Long> {
    private final Context ctx;
    private ProgressDialog progressDialog;
    
    public SmsSenderTask(final Context aCtx) {
        ctx = aCtx;
    }
    @Override
    protected Long doInBackground(String... params) {
        final String number = params[0];
        final String message = params[1];
        SmsSender.sendSms(message, number);
        return 0L;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setMessage("Saving data to CRM ...");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Long result){
        super.onPostExecute(result);
        progressDialog.dismiss();
    }
}