package com.saurasin.sbtentertainment.backend.model;

import com.saurasin.sbtentertainment.backend.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by saurasin on 1/26/17.
 */
public class Entry {
    
    final static String TAG = Entry.class.getSimpleName();
    private String id;
    private String email;
    private String phone;
    private String name;
    private String bdayVenue;
    private String kidsActivity;
    private String synced;
    private String emailModified;
    private String childOneName;
    private String childOneDob;
    private String childTwoName;
    private String childTwoDob;
    
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ssZ");
    
    public Entry(final String i, final String e, final String n, final String p, final String bdayV, 
                 final String kidsAct, final String s, final String eMod, final String chOneName, 
                 final String chOneDob, final String chTwoName, final String chTwoDob) {
        id = i;
        this.email = e;
        this.name = n;
        this.phone = p;
        bdayVenue = bdayV;
        kidsActivity = kidsAct;
        synced = s;
        emailModified = eMod;
        childOneName = chOneName;
        childOneDob = chOneDob;
        childTwoName = chTwoName;
        childTwoDob = chTwoDob;
    }
    
    public String getId() { return id; }
    public void setId(final String crmId) { id = crmId; }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getName() {
        return name;
    }
    
    public String getChildOneName() { return childOneName; }
    
    public String getChildOneDob() { return childOneDob; }
    
    public String getChildTwoName() { return childTwoName; }
    
    public String getChildTwoDob() { return childTwoDob; }
    
    public String getInterestInBdayVenue() {
        return bdayVenue;
    }
    
    public String getInterestInKidsActivities() {
        return kidsActivity;
    }
    
    
    public boolean isSynced() {
        return "YES".equals(synced);
    }
    
    public void setSynced(final String isSynced) {
        synced = isSynced;
    }
    
    public boolean isEmailModified() { return "YES".equals(emailModified); }
    
    public void setEmailModified(final String emlModified) { emailModified = emlModified; }
    
    public static Entry createFromBitrixJson(final JSONObject entryJson) {
        try {
            final String id = entryJson.getString("ID");
            final String name = entryJson.getString("NAME");
            final JSONArray phoneList = entryJson.getJSONArray("PHONE");
            final String phone = ((JSONObject)phoneList.get(0)).getString("VALUE");
            String email = "";
            if (entryJson.has("EMAIL")) {
                final JSONArray emailList = entryJson.getJSONArray("EMAIL");
                email = ((JSONObject) emailList.get(0)).getString("VALUE");
            }
            final String bdayV = entryJson.getString("UF_CRM_1467822198");
            final String bDayYN = bdayV.equals("1")?"YES":"NO";
            final String kidsAct = entryJson.getString("UF_CRM_1467822240");
            final String kidsActYN = kidsAct.equals("1")?"YES":"NO";
            final String childOneName = entryJson.getString("UF_CRM_1467906624");
            String childOneBday = entryJson.getString("BIRTHDATE");
            String childTwoName = entryJson.getString("UF_CRM_1496121945");
            String childTwoBday = entryJson.getString("UF_CRM_1496122753");
            
            try {
                Date dt = simpleDateFormat.parse(childOneBday);
                childOneBday = String.format("%d/%d/%d", dt.getDate(), dt.getMonth()+1, 1900 + dt.getYear());
                dt = simpleDateFormat.parse(childTwoBday);
                childTwoBday = String.format("%d/%d/%d", dt.getDate(), dt.getMonth()+1, 1900 + dt.getYear());
            } catch(ParseException ex) {
                Log.e(TAG, "Error parsing date:: " + ex.getMessage());
            }
            
            return new Entry(id, email, name, phone, bDayYN, kidsActYN, "YES", "NO", 
                    childOneName, childOneBday, childTwoName, childTwoBday);
        } catch (JSONException ex) {
            Log.e(TAG, "Error occured while getting data from backend "+ ex.getMessage());
        }
        return null;
    }
    
    public JSONObject getEntryJson() {
        JSONObject fields = new JSONObject();
        try {
            
            fields.put("TITLE", name);
            fields.put("NAME", name);
            if (TextUtils.isEmpty(id)) {
                JSONObject phoneObject = new JSONObject();
                JSONArray phoneNums = new JSONArray();
                phoneObject.put("VALUE", phone);
                phoneObject.put("VALUE_TYPE", "MOBILE");
                phoneNums.put(0, phoneObject);
                fields.put("PHONE", phoneNums);
            }

            if (isEmailModified()) {
                JSONObject emailObject = new JSONObject();
                JSONArray emails = new JSONArray();
                emailObject.put("VALUE", email);
                emailObject.put("VALUE_TYPE", "HOME");
                emails.put(0, emailObject);
                fields.put("EMAIL", emails);
            }

            String[] dob = childOneDob.split("/");
            String formatDOB = String.format("%s/%s/%s", dob[1], dob[0], dob[2]);
            fields.put("BIRTHDATE", formatDOB);
            fields.put("UF_CRM_1467906624", childOneName);
            
            final String oneMonthIndex = Constants.oneMonthIndexMap.get(dob[1]);
            final String oneYearIndex = Constants.oneYearIndexMap.get(dob[2]);
            if (oneMonthIndex != null) {
                fields.put("UF_CRM_1478418831", oneMonthIndex);
            }
            if (oneYearIndex != null) {
                fields.put("UF_CRM_1478418918", oneYearIndex);
            }

            if (!TextUtils.isEmpty(childTwoName)) {
                fields.put("UF_CRM_1496121945", childTwoName);
                if (!TextUtils.isEmpty(childTwoDob)) {
                    dob = childTwoDob.split("/");
                    if (dob.length == 3) {
                        formatDOB = String.format("%s/%s/%s", dob[1], dob[0], dob[2]);

                        fields.put("UF_CRM_1496122753", formatDOB);
                        final String twoMonthIndex = Constants.twoMonthIndexMap.get(dob[1]);
                        final String twoYearIndex = Constants.twoYearIndexMap.get(dob[2]);
                        if (twoMonthIndex != null) {
                            fields.put("UF_CRM_1496121978", twoMonthIndex);
                        }
                        if (twoYearIndex != null) {
                            fields.put("UF_CRM_1496122130", twoYearIndex);
                        }
                    }
                }
            }
            
            fields.put("UF_CRM_1467822240", bdayVenue.equals("YES") ? "Y" : "N");
            fields.put("UF_CRM_1467822198", kidsActivity.equals("YES") ? "Y" : "N");
        } catch (JSONException jsonEx) {
            Log.e(TAG, "Unable to serialize object:: " + jsonEx.getMessage());
        }
        
        return fields;
    }
}
