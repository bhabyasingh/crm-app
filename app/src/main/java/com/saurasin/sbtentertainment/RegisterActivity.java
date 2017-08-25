package com.saurasin.sbtentertainment;

import com.saurasin.sbtentertainment.backend.tasks.onTaskCompleted;
import com.saurasin.sbtentertainment.backend.utils.LocalDBRepository;
import com.saurasin.sbtentertainment.backend.model.Entry;
import com.saurasin.sbtentertainment.backend.tasks.BitrixGetContactTask;
import com.saurasin.sbtentertainment.backend.tasks.SmsSenderTask;
import com.saurasin.sbtentertainment.backend.utils.SmsSender;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by saurasin on 1/26/17.
 */
public class RegisterActivity extends AppCompatActivity implements onTaskCompleted<Entry> {
    
    public static final String MOBILE_INTENT_EXTRA = "MOBILE_INTENT_EXTRA";
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final static String TAG = RegisterActivity.class.getSimpleName();
    
    private EditText phoneET;
    private EditText emailET;
    private EditText nameET;
    private EditText childonenameET;
    private EditText childonedobET;
    private EditText childtwonameET;
    private EditText childtwodobET;
    private CheckBox bdayVenueCB;
    private CheckBox kidsActivitiesCB;
    private ImageView parentPhotoIM;
    
    private LocalDBRepository backend;
    private String mobileNumberFromIntent;
    
    private Bitmap photo;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final int THANK_YOU_REQUEST_CODE = 2;

    private String emailFromBackend = "";
    private String crmId = "";
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
        
        phoneET = (EditText) findViewById(R.id.parentPhoneEdit);
        emailET = (EditText) findViewById(R.id.parentEmailEdit);
        nameET = (EditText) findViewById(R.id.parentNameEdit);
        childonenameET = (EditText) findViewById(R.id.childone_name_edit);
        childonedobET  = (EditText) findViewById(R.id.childone_dob);
        childtwonameET = (EditText) findViewById(R.id.childtwo_name_edit);
        childtwodobET  = (EditText) findViewById(R.id.childtwo_dob);
        bdayVenueCB = (CheckBox) findViewById(R.id.checkbox_bday);
        bdayVenueCB.setChecked(true);
        kidsActivitiesCB = (CheckBox) findViewById(R.id.checkbox_kids_activities);
        kidsActivitiesCB.setChecked(true);
        parentPhotoIM = (ImageView) findViewById(R.id.parent_photo);

        backend = LocalDBRepository.getInstance(this);
        
        mobileNumberFromIntent = getIntent().getStringExtra(MOBILE_INTENT_EXTRA);
    }

    private ProgressDialog showProgressDialog(final String message) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage(message);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.show();
        return  pd;
    }
    
    private void getDataFromDB() {
        Entry entry = backend.getEntryByPhone(phoneET.getEditableText().toString());
        if (entry == null) {
            BitrixGetContactTask task = new BitrixGetContactTask(showProgressDialog("Loading .."), 
                    this, phoneET.getEditableText().toString());
            task.execute();
        } else {
            crmId = entry.getId();
            updateUIWithEntry(entry);
        }
    }
    
    private void updateUIWithEntry(final Entry entry) {
        emailET.setText(entry.getEmail());
        nameET.setText(entry.getName());
        bdayVenueCB.setChecked("YES".equals(entry.getInterestInBdayVenue()));
        kidsActivitiesCB.setChecked("YES".equals(entry.getInterestInKidsActivities()));
        childonenameET.setText(entry.getChildOneName());
        childonedobET.setText(entry.getChildOneDob());
        if (!TextUtils.isEmpty(entry.getChildTwoName())) {
            childtwonameET.setText(entry.getChildTwoName());
            childtwodobET.setText(entry.getChildTwoDob());
        }
        
        phoneET.setEnabled(false);
        emailFromBackend = entry.getEmail();
    }
    
    public void onSave(View v) {
        final String name = nameET.getEditableText().toString().trim();
        final String phone = phoneET.getEditableText().toString().trim();
        final String email = emailET.getEditableText().toString().trim();
        final String childonename = childonenameET.getEditableText().toString().trim();
        final String childonedob = childonedobET.getEditableText().toString().trim();
        final String childtwoname = childtwonameET.getEditableText().toString().trim();
        final String childtwodob = childtwodobET.getEditableText().toString().trim();

        if (TextUtils.isEmpty(childonename) || TextUtils.isEmpty(childonedob)) {
            Toast.makeText(this,
                    getResources().getText(R.string.childone_details_missing),
                    Toast.LENGTH_LONG).show();
            return;
        }

        Entry entry = new Entry(crmId, email, name, phone, bdayVenueCB.isChecked() ? "YES" : "NO",
                kidsActivitiesCB.isChecked() ? "YES" : "NO", "NO",
                !emailFromBackend.equals(email)?"YES":"NO", childonename, childonedob, childtwoname, childtwodob, 
                new Date());
        if (!TextUtils.isEmpty(crmId)) {
            backend.updateEntry(entry);
        } else {
            backend.addEntry(entry);
        }
        sendSms(entry);
    }
    
    private void sendSms(final Entry entry) {
        SmsSenderTask task  = new SmsSenderTask(showProgressDialog("Sending sms ..."), new onTaskCompleted<Long>() {
            @Override
            public void onTaskCompleted(Long aLong) {
                createLabel(entry);
                Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
                startActivityForResult(intent, THANK_YOU_REQUEST_CODE);
            }
        });
        task.execute(entry.getPhone(), SmsSender.createMessage(entry));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mobileNumberFromIntent)) {
            phoneET.setText(mobileNumberFromIntent);
            getDataFromDB();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == THANK_YOU_REQUEST_CODE) {
            finish();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data");
            parentPhotoIM.setImageBitmap(photo);
        }
    }
    
    public void onCancel(View v) {
        finish();
    }

    @Override
    public void onTaskCompleted(Entry entry) {
        if (entry != null) {
            boolean res = backend.addEntry(entry);
            if (res) {
                getDataFromDB();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Invalid date returned for child date of birth. Please input correct date.")
                        .setCancelable(false)
                        .setPositiveButton(getResources().getText(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        getParent().finish();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } else if (SBTEntertainment.isTokenExpired()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Token expired. Please login.")
                    .setCancelable(false)
                    .setPositiveButton(getResources().getText(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    getParent().finish();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
    
    public void onChildDOB(View v) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        final EditText dobControl = (EditText)v;
        final String date = dobControl.getEditableText().toString();
        Calendar newCalendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(date)) {
            String[] splitDates = date.split("/");
            newCalendar.set(Integer.parseInt(splitDates[2]),Integer.parseInt(splitDates[1])-1,
                    Integer.parseInt(splitDates[0]));
        }
        DatePickerDialog datePickerDlg = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog, 
                new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dobControl.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear+1, year));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDlg.getDatePicker().setForegroundGravity(11);
        datePickerDlg.show();
    }

    private void createLabel(final Entry entry) {
        Bitmap bitmap = Bitmap.createBitmap(325, 170, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAntiAlias(true);
        paint.setTextSize(12);
        canvas.drawLine(10, 0, 315, 0, paint);
        canvas.drawLine(10, 10, 315, 10, paint);

        canvas.drawLine(10, 140, 315, 140, paint);
        canvas.drawLine(10, 160, 315, 160, paint);
        canvas.drawLine(10, 0, 10, 160, paint);
        canvas.drawLine(315, 0, 315, 160, paint);
        canvas.drawLine(105, 10, 105, 140, paint);

        if (photo != null) {
            canvas.drawBitmap(photo, null, new RectF(13, 13, 102, 137), paint);
        }

        final int spacing = 20;
        int nextY = 30;
        canvas.drawText(entry.getName(), 115, nextY, paint);
        nextY += spacing;
        canvas.drawText(entry.getChildOneName(), 115, nextY, paint);
        nextY += spacing;
        if (!TextUtils.isEmpty(entry.getChildTwoName())) {
            canvas.drawText(entry.getChildTwoName(), 115, nextY, paint);
            nextY += spacing;
        }

        Date now = new Date();
        canvas.drawText(dateFormat.format(now), 115, nextY, paint);
        nextY += spacing;
        canvas.drawText(timeFormat.format(now), 115, nextY, paint);
        nextY += spacing;
        canvas.drawText(entry.getPhone(), 115, nextY, paint);

        paint.setColor(Color.BLUE);
        canvas.drawText("http://www.awesomeplace.in", 60, 155, paint);
        paint.setColor(Color.BLACK);
        
        if (childBDayWithinMonth(entry.getChildOneDob()) || childBDayWithinMonth(entry.getChildTwoDob())) {
            drawStar(canvas, 20, 145, paint);
        }
        
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        saveBitmap(bitmap);
    }
    
    private void drawStar(Canvas canvas, int x, int y, Paint paint) {
        Bitmap star = BitmapFactory.decodeResource(getResources(), R.drawable.blue_star);
        canvas.drawBitmap(star, null, new RectF(x, y, x+10, y+10), paint);
    }
    
    private boolean childBDayWithinMonth(final String dobStr) {
        boolean ret = false;
        if (!TextUtils.isEmpty(dobStr)) {
            Calendar cal = Calendar.getInstance();
            Calendar calNextMonth = Calendar.getInstance();
            calNextMonth.add(Calendar.MONTH, 1);
            String[] dates = dobStr.split("/");
            Calendar calDOB = Calendar.getInstance();
            calDOB.set(Calendar.MONTH, Integer.parseInt(dates[1])-1);
            calDOB.set(Calendar.DATE, Integer.parseInt(dates[0]));
            if (calDOB.get(Calendar.MONTH) < cal.get(Calendar.MONTH)) {
                calDOB.add(Calendar.YEAR, 1);
            }
            if (cal.before(calDOB) && calNextMonth.after(calDOB)) {
                ret = true;
            }
        }
        return ret;
    }

    private void saveBitmap(final Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            File f = new File(  getApplicationContext().getFilesDir(), "/temp_label.png");
            if (f.exists()) {
                final boolean del = f.delete();
                if (!del) {
                    Log.e(TAG, "Label File could not be deleted");
                }
            }
            fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException ex) {
            Log.e(TAG, "Error while printing label::" + ex.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException ex) {
                Log.e(TAG, "Error while printing label::" + ex.getMessage());
            }
        }
    }

    public void onTakePicture(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
}

