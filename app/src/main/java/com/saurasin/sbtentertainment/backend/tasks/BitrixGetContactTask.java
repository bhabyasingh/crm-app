/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by saurasin on 5/7/17.
 */
public class BitrixGetContactTask extends AsyncTask<String, Integer, Entry> {
    private ProgressDialog progressDialog;
    private String mobileNumber;

    public BitrixGetContactTask(final Activity pActivity, final String message, final String mNumber) {
        progressDialog = new ProgressDialog(pActivity);
        progressDialog.setMessage(message);
        this.mobileNumber = mNumber;
    }

    protected void onProgressUpdate(Integer... progress) {
        progressDialog.show();
    }

    protected void onPostExecute(Entry result) {
        progressDialog.dismiss();
    }
    
    @Override
    protected Entry doInBackground(String... params) {
        return BitrixCRMInvoker.getEntryFromBackend(mobileNumber);
    }
}