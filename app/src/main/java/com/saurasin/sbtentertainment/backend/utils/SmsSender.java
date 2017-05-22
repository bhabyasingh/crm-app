/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.utils;

import com.saurasin.sbtentertainment.backend.model.ChildEntry;
import com.saurasin.sbtentertainment.backend.model.Entry;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

    public static String createMessage(final Entry entry) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Welcome to Awesome Place - ");
        stringBuilder.append(entry.getName());
        List<ChildEntry> ce =  entry.getChildren();
        for (int i = 0; i < ce.size(); i++) {
            stringBuilder.append(",");
            stringBuilder.append(ce.get(i).getName());
        }

        SimpleDateFormat dateFormatter =  new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getDefault());
        stringBuilder.append(". Emergency - ");
        stringBuilder.append(entry.getPhone());
        stringBuilder.append(". Time - ");
        stringBuilder.append(dateFormatter.format(new Date()));
        stringBuilder.append(". PLEASE REMOVE PHONE FROM SILENT AND STAY ALERT. THANK YOU.");
        stringBuilder.append(" Buy one membership, use multiple locations - ");
        stringBuilder.append("Elements Mall Nagawara, Forum Mall Whitefield.");
        return stringBuilder.toString();
    }
}