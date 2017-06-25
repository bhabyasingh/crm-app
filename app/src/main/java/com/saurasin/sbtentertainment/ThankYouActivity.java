package com.saurasin.sbtentertainment;

import com.saurasin.sbtentertainment.backend.tasks.CrmUpdateTask;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Timer;


public class ThankYouActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);
    }
    
    public void onSubmit(View v) {
        finish();
    }

}
