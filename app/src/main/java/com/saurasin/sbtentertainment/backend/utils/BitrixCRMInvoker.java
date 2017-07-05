package com.saurasin.sbtentertainment.backend.utils;

import com.saurasin.sbtentertainment.SBTEntertainment;
import com.saurasin.sbtentertainment.backend.model.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AuthenticatorException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by saurasin on 4/21/17.
 */

public class BitrixCRMInvoker {
    private static final String TAG = BitrixCRMInvoker.class.getSimpleName();
    
    private static final int TIMEOUT = 30000;
    private static final String PREF_TOKEN_KEY = "TokenPref";
    private static final String PREF_REFRESH_TOKEN_KEY = "RefreshPref";
    
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

    private static boolean allowRefresh;
    
    private static HttpsURLConnection setupHttpConnection(final URL url) throws IOException{
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        return conn;
    }
    
    private static String retry(final String mobileNumber) throws AuthenticatorException {
        String id;
        boolean result = refresh();
        if (result) {
            id = getIdForMobileNumber(mobileNumber);
        } else {
            Log.e(TAG, "Authentication failed");
            throw new AuthenticatorException("Failed to refresh token, relogin required.");
        }
        return id;
    }
    
    private static String getIdForMobileNumber(final String mobileNumber) throws AuthenticatorException {
        String id = null;
        final String token = SBTEntertainment.getSharedPreferences().getString(PREF_TOKEN_KEY, "");
        if (!TextUtils.isEmpty(token)) {
            String contact = "";
            try {
                final String crmQuery = String.format(BITRIX_CRM_PHONE_NUMBER, token, mobileNumber);
                URL url = new URL(crmQuery);
                HttpsURLConnection conn = setupHttpConnection(url);
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
                    JSONObject customer = (JSONObject) returnList.get(0);
                    id = customer.getString("ID");
                }
                reader.close();
                conn.disconnect();
            } catch (JSONException ex) {
                try {
                    JSONObject errorObj = new JSONObject(contact);
                    final String error = errorObj.getString("error");
                    if ("expired_token".equals(error)) {
                        id = retry(mobileNumber);
                    }
                } catch (JSONException jsonEx) {
                    Log.e(TAG, "Error occured while fetching contact:: " + jsonEx.getMessage());
                }
            } catch (IOException ex) {
                Log.e(TAG, "Error occured while fetching contact:: " + ex.getMessage());
                if (allowRefresh) {
                    id = retry(mobileNumber);
                }
            }
        }
        return id;
    }
    
    public static Entry getEntryFromBackend(final String mobileNumber) throws AuthenticatorException {
        allowRefresh = true;
        try {
            final String customerId = getIdForMobileNumber(mobileNumber);
            final String token = SBTEntertainment.getSharedPreferences().getString(PREF_TOKEN_KEY, "");
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(customerId)) {
                final String crmContactGetQuery = String.format(BITRIX_CRM_BY_ID, token, customerId);
                URL url = new URL(crmContactGetQuery);
                HttpsURLConnection conn = setupHttpConnection(url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder contactStringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    contactStringBuilder.append(line);
                }
                JSONObject contactObj = new JSONObject(contactStringBuilder.toString()).getJSONObject("result");
                return Entry.createFromBitrixJson(contactObj);
            }
        } catch (JSONException | IOException ex) {
            Log.e(TAG, "Error occured while fetching contact:: " + ex.getMessage());
        }

        return null;
    }
    
    private static void setupPostRequest(HttpsURLConnection conn, int datalen) throws ProtocolException {
        conn.setRequestMethod("POST");
        conn.setRequestProperty( "Content-Type", "application/json");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty( "Content-Length", Integer.toString( datalen ));
        conn.setUseCaches( false );
    }
    
    public static String saveEntryToBackend(final Entry entry) throws AuthenticatorException {
        String crmId = "";
        final String token = SBTEntertainment.getSharedPreferences().getString(PREF_TOKEN_KEY, "");
        if (!TextUtils.isEmpty(token)) {
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

            Log.d(TAG, postDataObj.toString());
            try {
                URL url = new URL(crmQuery);
                HttpsURLConnection conn = setupHttpConnection(url);
                final byte[] postData = postDataObj.toString().getBytes(Charset.forName("UTF-8"));
                setupPostRequest(conn, postData.length);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                try {
                    wr.write(postData);
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
                JSONObject jsonObject = new JSONObject(result);
                final String res = jsonObject.getString("result");
                if (customerId == null) {
                    crmId = res;
                } else {
                    final Boolean success = Boolean.parseBoolean(res);
                    if (success) {
                        crmId = customerId;
                    } else {
                        crmId = "";
                    }
                }
                Log.d(TAG, "Saved successfully:: " + result);
            } catch (IOException | JSONException ex) {
                Log.e(TAG, "Error while saving user:: " + ex.getMessage());
            }
        }
        return crmId;
    }

   
    public static boolean initiate(final String username, final String password) {
        boolean result;
        try {
            URL url = new URL(BITRIX_AUTH_URL);
            HttpsURLConnection conn = setupHttpConnection(url);
            final String credentials = username + ":" + password;
            String encoded = Base64.encodeToString(credentials.getBytes(Charset.forName("UTF-8")), 0);
            conn.setRequestProperty("Authorization", "Basic "+encoded);
            conn.getDoOutput();
            final String location = conn.getHeaderField("Location");

            String code = null;
            if (!TextUtils.isEmpty(location)) {
                int startIndex = location.indexOf("code=") + 5;
                int endIndex = location.indexOf("&", startIndex);
                if (startIndex < endIndex) {
                    code = location.substring(startIndex, endIndex);
                }
            }
            
            if (!TextUtils.isEmpty(code)) {
                final String tokenUrl = String.format(BITRIX_TOKEN_URL, code);
                url = new URL(tokenUrl);
                conn = setupHttpConnection(url);
                result = setTokens(conn.getInputStream());
            } else {
                result = false;
            }
        } catch (Exception ioException) {
            result = false;
            Log.e(TAG, "Failed to get tokens:: " + ioException.getMessage());
        } 
        return result;
    }
    
    private static boolean refresh() {
        boolean result;
        allowRefresh = false;
        final String refreshToken = SBTEntertainment.getSharedPreferences().getString(PREF_REFRESH_TOKEN_KEY, "");
        if (TextUtils.isEmpty(refreshToken)) {
            result = false;
        } else {
            try {
                final String tokenUrl = String.format(BITRIX_REFRESH_TOKEN_URL, refreshToken);
                URL url = new URL(tokenUrl);
                HttpsURLConnection conn = setupHttpConnection(url);
                result = setTokens(conn.getInputStream());
            } catch (IOException ioEx) {
                result = false;
                Log.e(TAG, "Error Occured while refreshing token:: " + ioEx.getMessage());
            }
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
            SBTEntertainment.addStringToSharedPreferences(PREF_TOKEN_KEY, tokenJsonObj.getString("access_token"));
            SBTEntertainment.addStringToSharedPreferences(PREF_REFRESH_TOKEN_KEY, 
                    tokenJsonObj.getString("refresh_token"));
            SBTEntertainment.setTokenExpired(false);
        } catch (JSONException ex) {
            result = false;
            Log.e(TAG, "Error Occured while parsing for token:: " + ex.getMessage());
        }
        return result;
    }
}
