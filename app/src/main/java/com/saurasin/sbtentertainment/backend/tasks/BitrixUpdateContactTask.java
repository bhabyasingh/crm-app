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

public class BitrixUpdateContactTask extends AsyncTask<Entry, Integer, Boolean> {
    private Entry entry;
    private boolean isUpdate;
    private boolean emailChanged;
    private ProgressDialog progressDialog;
    private final Context ctx;

    public BitrixUpdateContactTask(final Context aCtx, final Entry contact, 
                                   final boolean update, final boolean hasEmailChanged) {
        ctx = aCtx;
        entry = contact;
        isUpdate = update;
        emailChanged = hasEmailChanged;
    }
    
    @Override
    protected Boolean doInBackground(Entry... params) {
        return BitrixCRMInvoker.saveEntryToBackend(entry, isUpdate, emailChanged);
    }
    
    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setMessage("Saving data to CRM ...");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean result){
        super.onPostExecute(result);
        progressDialog.dismiss();
    }
}
