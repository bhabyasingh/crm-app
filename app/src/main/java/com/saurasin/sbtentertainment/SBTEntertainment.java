package com.saurasin.sbtentertainment;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by saurasin on 6/13/17.
 */

public class SBTEntertainment  {
    private static final String TAG = SBTEntertainment.class.getSimpleName();
    private static ExecutorService pool = Executors.newFixedThreadPool(1);
    private static boolean tokenExpired = true;
    private static SharedPreferences sharedPreferences;
    final static String AWESOME_PREFERENCES = "AwesomePrefs"; 
    
     public static void submitTask(Runnable r) {
        try {
            pool.submit(r);
        } catch (RejectedExecutionException ex) {
          Log.e(TAG, "Unable to excute task:: " + ex.getMessage());  
        }
    }
    
    public static void initSharedPreference(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(AWESOME_PREFERENCES, Context.MODE_PRIVATE);
        }
    }
    
    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
    
    public static void addStringToSharedPreferences(final String key, final String value) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString(key, value);
        prefEditor.apply();
    }
    
    public static boolean isTokenExpired() {
        return tokenExpired;
    }
    
    public static void setTokenExpired(final boolean aTokenExpired) {
        tokenExpired = aTokenExpired;
    }
}
