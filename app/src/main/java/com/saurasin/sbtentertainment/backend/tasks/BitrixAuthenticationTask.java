package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

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


    @Override
    protected Boolean doInBackground(String... params) {
        return BitrixCRMInvoker.initiate(username, password);
    }
}