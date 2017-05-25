package com.saurasin.sbtentertainment;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


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
