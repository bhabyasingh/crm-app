
package com.saurasin.sbtentertainment.backend;

import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.tasks.BitrixUpdateContactTask;
import com.saurasin.sbtentertainment.backend.utils.LocalDBRepository;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by saurasin on 5/23/17.
 */

public class CrmUpdater extends Service {

    private final String TAG = CrmUpdater.class.getSimpleName();
    // constant
    public static final long NOTIFY_INTERVAL = 2 * 60 * 1000; // 2 minutes

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        // cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    private class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    List<Entry> entries = LocalDBRepository.getInstance(getApplicationContext()).getUnsyncedEntries();
                    for(Entry entry : entries) {
                        BitrixUpdateContactTask task = new BitrixUpdateContactTask(entry);
                        task.execute();
                        try {
                            String crmId = task.get();
                            if (!TextUtils.isEmpty(crmId)) {
                                entry.setSynced("YES");
                                entry.setEmailModified("NO");
                                entry.setId(crmId);
                                LocalDBRepository.getInstance(getApplicationContext()).updateEntry(entry);
                            }
                        } catch (InterruptedException|ExecutionException ex) {
                            Log.e(TAG, "Error saving to backend:: " + ex.getMessage() );
                        }
                    }
                }
            });
        }
    }
}
