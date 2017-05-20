/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by saurasin on 5/7/17.
 */
public class BitrixGetContactTask extends AsyncTask<String, Integer, Entry> {
    private String mobileNumber;
    private ProgressDialog progressDialog;
    private final Context ctx;

    public BitrixGetContactTask(final Context aCtx, final String mNumber) {
        ctx = aCtx;
        this.mobileNumber = mNumber;
    }
    
    @Override
    protected Entry doInBackground(String... params) {
        return BitrixCRMInvoker.getEntryFromBackend(mobileNumber);
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setMessage("Fetching contact from CRM ...");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Entry result){
        super.onPostExecute(result);
        progressDialog.dismiss();
    }
}