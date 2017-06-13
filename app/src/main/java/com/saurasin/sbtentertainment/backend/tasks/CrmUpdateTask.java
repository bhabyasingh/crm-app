package com.saurasin.sbtentertainment.backend.tasks;

import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.utils.LocalDBRepository;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by saurasin on 6/13/17.
 */

public class CrmUpdateTask implements Runnable {

    private final String TAG = CrmUpdateTask.class.getSimpleName();
    // run on another Thread to avoid crash
    
    private Context appContext;
    
    public CrmUpdateTask(final Context aContext) {
        this.appContext = aContext;
    }

    public void run() {
        List<Entry> entries = LocalDBRepository.getInstance(appContext).getUnsyncedEntries();
        for(Entry entry : entries) {
            BitrixUpdateContactTask task = new BitrixUpdateContactTask(entry);
            task.execute();
            try {
                String crmId = task.get();
                if (!TextUtils.isEmpty(crmId)) {
                    entry.setSynced("YES");
                    entry.setEmailModified("NO");
                    entry.setId(crmId);
                    LocalDBRepository.getInstance(appContext).updateEntry(entry);
                }
            } catch (InterruptedException|ExecutionException ex) {
                Log.e(TAG, "Error saving to backend:: " + ex.getMessage() );
            }
        }
    }
}
