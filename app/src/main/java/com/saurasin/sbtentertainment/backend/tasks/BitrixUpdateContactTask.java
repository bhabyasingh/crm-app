/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by saurasin on 5/10/17.
 */

public class BitrixUpdateContactTask extends AsyncTask<Entry, Integer, String> {
    private Entry entry;
    private final Context ctx;

    public BitrixUpdateContactTask(final Context aCtx, final Entry contact) {
        ctx = aCtx;
        entry = contact;
    }
    
    @Override
    protected String doInBackground(Entry... params) {
        return BitrixCRMInvoker.saveEntryToBackend(entry);
    }
    
    @Override
    protected void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result){
        super.onPostExecute(result);
    }
}
