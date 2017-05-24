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

    public BitrixGetContactTask(final String mNumber) {
        this.mobileNumber = mNumber;
    }
    
    @Override
    protected Entry doInBackground(String... params) {
        return BitrixCRMInvoker.getEntryFromBackend(mobileNumber);
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Entry result){
        super.onPostExecute(result);
    }
}