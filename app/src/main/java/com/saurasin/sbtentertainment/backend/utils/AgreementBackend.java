package com.saurasin.sbtentertainment.backend.utils;

import com.saurasin.sbtentertainment.backend.model.ChildEntry;
import com.saurasin.sbtentertainment.backend.model.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saurasin on 1/26/17.
 */
public class AgreementBackend extends SQLiteOpenHelper {
    
    final private static String TAG = AgreementBackend.class.getSimpleName();
    
    final private static String DATABASE_NAME = "awesomeplace.db";
    final private static String PARENT_TABLE_NAME = "parents";
    final private static String CRM_ID = "id";
    final private static String PARENT_NAME = "name";
    final private static String PARENT_EMAIL = "email";
    final private static String PARENT_PHONE = "phone";
    final private static String BDAY_INTEREST = "bday_interest";
    final private static String SYNCED = "synced";
    final private static String KIDS_ACT_INTEREST = "kids_activity_interest";
    final private static String EMAIL_MODIFIED = "email_modified";
    
    final private static String CHILDREN_TABLE_NAME = "children";
    final private static String CHILDREN_NAME = "name";
    final private static String CHILDREN_DOB = "dob";
    final private static String CHILDREN_PARENT_ID = "parent_id";
    
    private static volatile AgreementBackend instance;
    
    public static synchronized AgreementBackend getInstance(Context context) {
        if (instance == null) {
            instance =  new AgreementBackend(context);
        }
        return instance;
    }
    
    private AgreementBackend(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "  + PARENT_TABLE_NAME  +
                " (" + PARENT_PHONE + " text primary_key," + PARENT_EMAIL + " text," + BDAY_INTEREST + " text, " +
                KIDS_ACT_INTEREST + " text, " + PARENT_NAME +" text, " + SYNCED + " text," + EMAIL_MODIFIED + " text," 
                + CRM_ID + " text);");
        db.execSQL("create table children " +
                "(name text, dob text, parent_id text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
    
    private ContentValues getContentForParentInfo(final Entry entry) {
        ContentValues cv = new ContentValues();
        cv.put(CRM_ID, entry.getId());
        cv.put(PARENT_NAME, entry.getName());
        cv.put(PARENT_EMAIL, entry.getEmail());
        cv.put(PARENT_PHONE, entry.getPhone());
        cv.put(BDAY_INTEREST, entry.getInterestInBdayVenue());
        cv.put(KIDS_ACT_INTEREST, entry.getInterestInKidsActivities());
        cv.put(SYNCED, entry.isSynced()?"YES":"NO");
        cv.put(EMAIL_MODIFIED, entry.isEmailModified()?"YES":"NO");
        return cv;
    }
    
    private ContentValues getContentForChild(final ChildEntry ce, final String phone) {
        ContentValues cv = new ContentValues();
        cv.put(CHILDREN_PARENT_ID, phone);
        cv.put(CHILDREN_NAME, ce.getName());
        cv.put(CHILDREN_DOB, ce.getDOB());
        return  cv;
    }
    
    
    public void addEntry(final Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = getContentForParentInfo(entry);
        db.insert(PARENT_TABLE_NAME, null, cv);
        
        for (ChildEntry ce : entry.getChildren()) {
            cv = getContentForChild(ce, entry.getPhone());
            db.insert(CHILDREN_TABLE_NAME, null, cv);
        }
        db.close();
    }
    
    public void updateEntry(final Entry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = getContentForParentInfo(entry);
        db.update(PARENT_TABLE_NAME, cv, PARENT_PHONE + " = ?", new String[]{entry.getPhone()});

        for (ChildEntry ce : entry.getChildren()) {
            cv = getContentForChild(ce, entry.getPhone());
            int n = db.update(CHILDREN_TABLE_NAME, cv, CHILDREN_PARENT_ID + " = ? AND "+ CHILDREN_NAME + " = ?", 
                    new String[]{ entry.getPhone(), ce.getName()});
            if (n == 0) {
                db.insert(CHILDREN_TABLE_NAME, null, cv);
            }
        }
        db.close();
    }
    
    public Entry getEntryByPhone(final String phone) {
        Entry entry = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor parentCursor =  db.rawQuery( "select * from parents where phone=\'" + phone + "\'", null );
        if (parentCursor.getCount() > 0) {
            parentCursor.moveToFirst();
            entry = createEntryFromCursor(db, parentCursor);
        }
        parentCursor.close();
        db.close();
        return entry;
    }
    
    public List<Entry> getUnsyncedEntries() {
        List<Entry> entries = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor parentCursor = db.rawQuery("select * from parents where synced=\'NO\'", null);
            if (parentCursor.getCount() > 0) {
                parentCursor.moveToFirst();
                while (!parentCursor.isAfterLast()) {
                    Entry entry = createEntryFromCursor(db, parentCursor);
                    entries.add(entry);
                    parentCursor.moveToNext();
                }
            }
            parentCursor.close();
            db.close();
        } catch (Exception ex) {
            Log.e(TAG, "Error while get entries form db:: " + ex.getMessage());
        }
        return entries;
    }
    
    private Entry createEntryFromCursor(SQLiteDatabase db, Cursor parentCursor) {
        Entry entry = null;
        
        String crmId = null;
        String parentName = null;
        String parentEmail = null;
        String interestInBday = null;
        String interestInKidsAct = null;
        String synced = null;
        String emailModified = null;
        String phone = null;
        List<ChildEntry> children = new ArrayList<>();
        if (!parentCursor.isAfterLast()) {
            crmId = parentCursor.getString(parentCursor.getColumnIndex(CRM_ID));
            parentName = parentCursor.getString(parentCursor.getColumnIndex(PARENT_NAME));
            parentEmail = parentCursor.getString(parentCursor.getColumnIndex(PARENT_EMAIL));
            interestInBday = parentCursor.getString(parentCursor.getColumnIndex(BDAY_INTEREST));
            interestInKidsAct = parentCursor.getString(parentCursor.getColumnIndex(KIDS_ACT_INTEREST));
            synced = parentCursor.getString(parentCursor.getColumnIndex(SYNCED));
            emailModified = parentCursor.getString(parentCursor.getColumnIndex(EMAIL_MODIFIED));
            phone = parentCursor.getString(parentCursor.getColumnIndex((PARENT_PHONE)));
        }

        Cursor childrenCursor = db.rawQuery("select * from children where parent_id=\'" + phone + "\'", null);
        childrenCursor.moveToFirst();
        while (!childrenCursor.isAfterLast()) {
            final String childName = childrenCursor.getString(childrenCursor.getColumnIndex(CHILDREN_NAME));
            final String dob = childrenCursor.getString(childrenCursor.getColumnIndex(CHILDREN_DOB));
            ChildEntry ce = new ChildEntry(childName, dob);
            children.add(ce);
            childrenCursor.moveToNext();
        }
        childrenCursor.close();
        
        if (parentName != null) {
            entry = new Entry(crmId, parentEmail, parentName, phone, interestInBday,
                    interestInKidsAct, synced, emailModified, children);
        }
        return entry;
    }
}
