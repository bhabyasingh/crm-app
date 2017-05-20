/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class SmsSender {
    
    private final static String TAG = SmsSender.class.getSimpleName();
    public static void sendSms(final String message, final String phoneNumber) {
        try
        {
            String data="user=" + URLEncoder.encode("ssisodia", "UTF-8");
            data +="&password=" + URLEncoder.encode("gudds260", "UTF-8");
            data +="&message=" + URLEncoder.encode(message, "UTF-8");
            data +="&sender=" + URLEncoder.encode("AWSPLC", "UTF-8");
            data +="&mobile=" + URLEncoder.encode(phoneNumber, "UTF-8");
            data +="&type=" + URLEncoder.encode("3", "UTF-8");
            URL url = new URL("http://login.bulksmsgateway.in/sendmessage.php?"+data);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            String sResult="";
            while ((line = rd.readLine()) != null)
            {
                sResult = sResult + line + " ";
            }
            wr.close();
            rd.close();
            Log.e(TAG, sResult);
        }
        catch (Exception e)
        {
           Log.e(TAG, "Error SMS " + e);
        }
    }
}