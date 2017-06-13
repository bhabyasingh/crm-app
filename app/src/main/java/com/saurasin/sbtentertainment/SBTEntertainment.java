package com.saurasin.sbtentertainment;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by saurasin on 6/13/17.
 */

class SBTEntertainment  {
    private static final String TAG = SBTEntertainment.class.getSimpleName();
    private static ExecutorService pool = Executors.newFixedThreadPool(1);
    
     static void submitTask(Runnable r) {
        try {
            pool.submit(r);
        } catch (RejectedExecutionException ex) {
          Log.e(TAG, "Unable to excute task:: " + ex.getMessage());  
        }
    } 
}
