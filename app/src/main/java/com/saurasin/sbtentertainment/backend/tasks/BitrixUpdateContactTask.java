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
    private ProgressDialog progressDialog;
    private Entry entry;
    private boolean isUpdate;
    private boolean emailChanged;

    public BitrixUpdateContactTask(final Context ctx, final String message, 
                                   final Entry contact, final boolean update, final boolean hasEmailChanged) {
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setMessage(message);
        entry = contact;
        isUpdate = update;
        emailChanged = hasEmailChanged;
    }

    protected void onProgressUpdate(Integer... progress) {
        progressDialog.show();
    }

    protected void onPostExecute(Entry result) {
        progressDialog.dismiss();
    }

    @Override
    protected Boolean doInBackground(Entry... params) {
        return BitrixCRMInvoker.saveEntryToBackend(entry, isUpdate, emailChanged);
    }
}
