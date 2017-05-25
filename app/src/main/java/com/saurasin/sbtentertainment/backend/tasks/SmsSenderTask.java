
package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.os.AsyncTask;

/**
 * Created by saurasin on 4/20/17.
 */

public class SmsSenderTask extends AsyncTask<String, Integer, Long> {
    @Override
    protected Long doInBackground(String... params) {
        final String number = params[0];
        final String message = params[1];
        SmsSender.sendSms(message, number);
        return 0L;
    }
}