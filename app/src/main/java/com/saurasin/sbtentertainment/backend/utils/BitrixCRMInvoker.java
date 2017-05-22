/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.utils;

import com.saurasin.sbtentertainment.backend.model.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.Telephony;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by saurasin on 4/21/17.
 */

public class BitrixCRMInvoker {
    private static final String TAG = BitrixCRMInvoker.class.getSimpleName();
    
    private static String token = null;
    private static String refreshToken = null;
    
    private static final String BITRIX_AUTH_URL = "https://awesome.bitrix24.in/oauth/authorize/?response_type=code" + 
                    "&client_id=local.58eccdc4cf19f8.98218320&redirect_uri=app_URL";
    
    private static final String BITRIX_TOKEN_URL = "https://awesome.bitrix24.in/oauth/token/?" +
            "grant_type=authorization_code&client_id=local.58eccdc4cf19f8.98218320&" +
            "client_secret=XlbGZ9idRSCtwGcuzL48dG3C8vRGyoSJEWfjxskVBo3JBQddgE&code=%s&" +
            "scope=application_permissions&redirect_uri=application_URL";
    
    private static final String BITRIX_REFRESH_TOKEN_URL = "https://awesome.bitrix24.com/oauth/token/?" +  
            "grant_type=refresh_token&client_id=local.58eccdc4cf19f8.98218320&" +
            "client_secret=XlbGZ9idRSCtwGcuzL48dG3C8vRGyoSJEWfjxskVBo3JBQddgE&refresh_token=%s&" +
            "scope=granted_permission&redirect_uri=app_URL";
    
    private static final String BITRIX_CRM_PHONE_NUMBER = "https://awesome.bitrix24.in/rest/crm.lead.list?" +
            "auth=%s&filter[PHONE]=%s";

    private static final String BITRIX_CRM_BY_ID = "https://awesome.bitrix24.in/rest/crm.lead.get?" +
            "auth=%s&ID=%s";

    
    private static final String BITRIX_CRM_CREATE_CONTACT_POST = "https://awesome.bitrix24.in/rest/crm.lead.add";
    private static final String BITRIX_CRM_UPDATE_CONTACT_POST = "https://awesome.bitrix24.in/rest/crm.lead.update";

    
    
    private static String getIdForMobileNumber(final String mobileNumber) {
        String id = null;
        String contact = "";
        try {
            final String crmQuery = String.format(BITRIX_CRM_PHONE_NUMBER, token, mobileNumber);
            URL url = new URL(crmQuery);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            contact = builder.toString();
            JSONObject resultObj = new JSONObject(contact);
            JSONArray returnList = resultObj.getJSONArray("result");
            if (returnList.length() > 0) {
                JSONObject customer =  (JSONObject) returnList.get(0);
                id = customer.getString("ID");
            }
            reader.close();
            conn.disconnect();
        } catch (JSONException ex) {
            try {
                JSONObject errorObj = new JSONObject(contact);
                final String error = errorObj.getString("error");
                if ("expired_token".equals(error)) {
                    boolean result = refresh();
                    if (result) {
                        id = getIdForMobileNumber(mobileNumber);
                    } else {
                        Log.e(TAG, "Authentication failed");
                    }
                }
            } catch (JSONException jsonEx) {
                Log.e(TAG, "Error occured while fetching contact:: " + jsonEx.getMessage());
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error occured while fetching contact:: " + ex.getMessage());
        }
        return id;
    }
    
    public static Entry getEntryFromBackend(final String mobileNumber) {

        try {
                final String customerId = getIdForMobileNumber(mobileNumber);
                final String crmContactGetQuery = String.format(BITRIX_CRM_BY_ID, token, customerId);
                URL url = new URL(crmContactGetQuery);
                HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
                conn.setInstanceFollowRedirects(true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder contactStringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    contactStringBuilder.append(line);
                }
                JSONObject contactObj = new JSONObject(contactStringBuilder.toString()).getJSONObject("result");
                return Entry.createFromBitrixJson(contactObj);
        } catch (JSONException|IOException ex) {
                Log.e(TAG, "Error occured while fetching contact:: " + ex.getMessage());
        }
        
        return null;
    }
    
    public static String saveEntryToBackend(final Entry entry) {
        String crmId = ""; 
        final String customerId = getIdForMobileNumber(entry.getPhone());
        String crmQuery = "";
        
        JSONObject postDataObj = new JSONObject();
        JSONObject entryJson = entry.getEntryJson();
        try {
            entryJson.put("UF_CRM_1467901654", LeadConstants.getInstance().getLocationId());
            entryJson.put("SOURCE_ID", LeadConstants.getInstance().getSource());
            if (customerId != null) {
                crmQuery = BITRIX_CRM_UPDATE_CONTACT_POST + "?auth=" + token;
                postDataObj.put("ID", customerId);
            } else {
                crmQuery = BITRIX_CRM_CREATE_CONTACT_POST + "?auth=" + token;
            }
            postDataObj.put("fields", entryJson);
        } catch (JSONException jsEx) {
            Log.e(TAG, "Error adding ID to json:: " + jsEx.getMessage());
        }
        
        Log.e(TAG, postDataObj.toString());
        try {
            URL url = new URL(crmQuery);
            final byte[] postData = postDataObj.toString().getBytes(Charset.forName("UTF-8")); 
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty( "Content-Type", "application/json");
            conn.setRequestProperty( "charset", "utf-8");
            conn.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
            conn.setUseCaches( false );
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            try {
                wr.write( postData );
            } finally {
                wr.flush();
            }
            conn.getDoOutput();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            String result = builder.toString();
            if (customerId == null) {
                JSONObject jsonObject = new JSONObject(result);
                crmId = jsonObject.getString("result");
            } else {
                crmId = customerId;
            }
            Log.e(TAG, "Saved successfully:: " + result);
        } catch (IOException|JSONException ex) {
            Log.e(TAG, "Error while saving user:: " + ex.getMessage());
        }
        return crmId;
    }

   
    public static boolean initiate(final String username, final String password) {
        boolean result;
        try {
            URL url = new URL(BITRIX_AUTH_URL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            final String credentials = username + ":" + password;
            String encoded = Base64.encodeToString(credentials.getBytes(Charset.forName("UTF-8")), 0);
            conn.setRequestProperty("Authorization", "Basic "+encoded);

            conn.setInstanceFollowRedirects(true);
            final String location = conn.getHeaderField("Location");
            
            int startIndex = location.indexOf("code=") + 5;
            int endIndex = location.indexOf("&", startIndex);
            final String code = location.substring(startIndex, endIndex);
            
            final String tokenUrl = String.format(BITRIX_TOKEN_URL, code);
            url = new URL(tokenUrl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            result = setTokens(conn.getInputStream());
        } catch (Exception ioException) {
            result = false;
            Log.e(TAG, "Failed to get tokens:: " + ioException.getMessage());
        } 
        return result;
    }
    
    public static boolean refresh() {
        boolean result;
        try {
            final String tokenUrl = String.format(BITRIX_REFRESH_TOKEN_URL, refreshToken);
            URL url = new URL(tokenUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            result = setTokens(conn.getInputStream());
        } catch (IOException ioEx) {
            result = false;
            Log.e(TAG, "Error Occured while refreshing oken:: " + ioEx.getMessage());
        }
        return result;
    }
    
    private static boolean setTokens(InputStream inputStream) throws IOException {
        boolean result = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder builder = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String tokenLine = builder.toString();
        try {
            JSONObject tokenJsonObj = new JSONObject(tokenLine);
            token = tokenJsonObj.getString("access_token");
            refreshToken = tokenJsonObj.getString("refresh_token");
        } catch (JSONException ex) {
            result = false;
            Log.e(TAG, "Error Occured while parsing for token:: " + ex.getMessage());
        }
        return result;
    }
}
