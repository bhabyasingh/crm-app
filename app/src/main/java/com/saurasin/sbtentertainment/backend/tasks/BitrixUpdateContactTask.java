package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.utils.BitrixCRMInvoker;

import android.os.AsyncTask;

/**
 * Created by saurasin on 5/10/17.
 */

public class BitrixUpdateContactTask extends AsyncTask<Entry, Integer, String> {
    private Entry entry;

    public BitrixUpdateContactTask(final Entry contact) {
        entry = contact;
    }
    
    @Override
    protected String doInBackground(Entry... params) {
        return BitrixCRMInvoker.saveEntryToBackend(entry);
    }
}
