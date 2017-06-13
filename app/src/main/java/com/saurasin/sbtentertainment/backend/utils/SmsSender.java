

package com.saurasin.sbtentertainment.backend.utils;

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
import java.util.TimeZone;

public class SmsSender {
    
    private final static String TAG = SmsSender.class.getSimpleName();
    private static final int TIMEOUT = 30000;
    
    public static void sendSms(final String message, final String phoneNumber) {
        try
        {
            String data="user=" + URLEncoder.encode("udayjose", "UTF-8");
            data +="&password=" + URLEncoder.encode("Welcome123!", "UTF-8");
            data +="&message=" + URLEncoder.encode(message, "UTF-8");
            data +="&sender=" + URLEncoder.encode("AWSPLC", "UTF-8");
            data +="&mobile=" + URLEncoder.encode(phoneNumber, "UTF-8");
            data +="&type=" + URLEncoder.encode("3", "UTF-8");
            URL url = new URL("http://login.bulksmsgateway.in/sendmessage.php?"+data);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
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
            Log.d(TAG, sResult);
        }
        catch (Exception e)
        {
           Log.e(TAG, "Error Sending SMS:: " + e.getMessage());
        }
    }
    
    public static String createMessage(final Entry entry) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Welcome to Awesome Place - ");
        stringBuilder.append(entry.getName());
        stringBuilder.append(",");
        stringBuilder.append(entry.getChildOneName());
        stringBuilder.append(",");
        stringBuilder.append(entry.getChildTwoName());
        
        SimpleDateFormat dateFormatter =  new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss");
        dateFormatter.setTimeZone(TimeZone.getDefault());
        stringBuilder.append(". Emergency - ");
        stringBuilder.append(entry.getPhone());
        stringBuilder.append(". Time - ");
        stringBuilder.append(dateFormatter.format(new Date()));
        stringBuilder.append(". Please remove phone from silent and receive our call if we reach out. Thank You.");
        return stringBuilder.toString();
    }
}