/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 */

package com.saurasin.sbtentertainment.backend.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
    List<ChildEntry> children;
    
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
    public Entry(final String e, final String n, final String p, final String bdayV, 
                 final String kidsAct, final String s, final List<ChildEntry> ce) {
        this.email = e;
        this.name = n;
        this.phone = p;
        bdayVenue = bdayV;
        kidsActivity = kidsAct;
        synced = s;
        this.children = ce;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public String getName() {
        return name;
    }
    
    public List<ChildEntry> getChildren() {
        return children;
    }
    
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
    
    public static Entry createFromBitrixJson(final JSONObject entryJson) {
        try {
            final String name = entryJson.getString("NAME");
            final JSONArray phoneList = entryJson.getJSONArray("PHONE");
            final String phone = ((JSONObject)phoneList.get(0)).getString("VALUE");
            final JSONArray emailList = entryJson.getJSONArray("EMAIL");
            final String email = ((JSONObject)emailList.get(0)).getString("VALUE");
            final String bdayV = entryJson.getString("UF_CRM_1467822198");
            final String bDayYN = bdayV.equals("1")?"YES":"NO";
            final String kidsAct = entryJson.getString("UF_CRM_1467822240");
            final String kidsActYN = kidsAct.equals("1")?"YES":"NO";
            final String childsName = entryJson.getString("UF_CRM_1467906624");
            String childsBday = entryJson.getString("BIRTHDATE");
            
            try {
                Date dt = simpleDateFormat.parse(childsBday);
                GregorianCalendar cal = new GregorianCalendar();
                cal.setGregorianChange(dt);
                childsBday = String.format("%d/%d/%d", dt.getDate(), dt.getMonth(), 1900 + dt.getYear());
            } catch(ParseException ex) {
                Log.e(TAG, "Error parsing date:: " + ex.getMessage());
            }
            
            
            Log.e(TAG, "Saurabh:: " + email + name + phone + bDayYN + kidsActYN + childsName + childsBday);
            ChildEntry ce  = new ChildEntry(childsName, childsBday);
            List<ChildEntry> ceList = new ArrayList<ChildEntry>();
            ceList.add(ce);
            Entry entry = new Entry(email, name, phone, bDayYN, kidsActYN, "YES", ceList);
            return entry;
            
                    
        } catch (JSONException ex) {
            Log.e(TAG, "Error occured while getting data from backend "+ ex.getMessage());
        }
        return null;
    }
    
    public String getUpdateUrlString(final String urlFormat, final String token, final String id) {
        String childName = "";
        String birthdate = "";
        if (children.size() > 0) {
            ChildEntry ce = children.get(0);
            childName = ce.getName();
            String[] dob = ce.getDOB().split("/");
            birthdate = String.format("%s/%s/%s", dob[1], dob[0], dob[2]);
        }
        return String.format(urlFormat, token, id, name, name, phone, birthdate, childName, email, 
                bdayVenue.equals("YES")?"Y":"N", kidsActivity.equals("YES")?"Y":"N");
    }
    
    public String getAddUrlString(final String urlFormat, final String token) {
        String childName = "";
        String birthdate = "";
        if (children.size() > 0) {
            ChildEntry ce = children.get(0);
            childName = ce.getName();
            String[] dob = ce.getDOB().split("/");
            birthdate = String.format("%s/%s/%s", dob[1], dob[0], dob[2]);
        }
        return String.format(urlFormat, token, name, name, phone, birthdate, childName, email,
                bdayVenue.equals("YES")?"Y":"N", kidsActivity.equals("YES")?"Y":"N");
    }

    
    public JSONObject getEntryJson(final boolean isUpdate, final boolean emailChanged) {
        String childName = "";
        String birthdate = "";
        if (children.size() > 0) {
            ChildEntry ce = children.get(0);
            childName = ce.getName();
            String[] dob = ce.getDOB().split("/");
            birthdate = String.format("%s/%s/%s", dob[1], dob[0], dob[2]);
        }
        JSONObject fields = new JSONObject();
        try {
            
            fields.put("TITLE", name);
            fields.put("NAME", name);
            if (!isUpdate) {
                JSONObject phoneObject = new JSONObject();
                JSONArray phoneNums = new JSONArray();
                phoneObject.put("VALUE", phone);
                phoneObject.put("VALUE_TYPE", "MOBILE");
                phoneNums.put(0, phoneObject);
                fields.put("PHONE", phoneNums);
            }

            if (emailChanged) {
                JSONObject emailObject = new JSONObject();
                JSONArray emails = new JSONArray();
                emailObject.put("VALUE", email);
                emailObject.put("VALUE_TYPE", "HOME");
                emails.put(0, emailObject);
                fields.put("EMAIL", emails);
            }

            fields.put("BIRTHDATE", birthdate);
            fields.put("UF_CRM_1467906624", childName);

            fields.put("UF_CRM_1467822240", bdayVenue.equals("YES") ? "Y" : "N");
            fields.put("UF_CRM_1467822198", kidsActivity.equals("YES") ? "Y" : "N");
        } catch (JSONException jsonEx) {
            Log.e(TAG, "Unable to serialize object:: " + jsonEx.getMessage());
        }
        
        return fields;
    }
}
