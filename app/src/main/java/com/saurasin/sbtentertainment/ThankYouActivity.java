package com.saurasin.sbtentertainment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;


public class ThankYouActivity extends AppCompatActivity {
    private Bitmap entryLabelBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);
        File imgFile = new File(  getApplicationContext().getFilesDir(), "/temp_label.png");
        if(imgFile.exists()){
            entryLabelBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView myImage = (ImageView) findViewById(R.id.entry_label);
            myImage.setImageBitmap(entryLabelBitmap);
        }
    }
    
    public void onSubmit(View v) {
        finish();
    }
    
    public void onPrint(View v) {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.printBitmap("Entry Label", entryLabelBitmap);
    }

}
