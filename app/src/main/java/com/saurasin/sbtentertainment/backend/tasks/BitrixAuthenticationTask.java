/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;


/**
 * Created by saurasin on 4/20/17.
 */

public class BitrixAuthenticationTask extends AsyncTask<String, Integer, Boolean> {
    private final String username;
    private final String password;

    public BitrixAuthenticationTask(final String user, final String passwd) {
        this.username = user;
        this.password = passwd;
    }

    protected void onProgressUpdate(Integer... progress) {
        
    }


    @Override
    protected Boolean doInBackground(String... params) {
        Boolean res = BitrixCRMInvoker.initiate(username, password);
        return res;
    }
}