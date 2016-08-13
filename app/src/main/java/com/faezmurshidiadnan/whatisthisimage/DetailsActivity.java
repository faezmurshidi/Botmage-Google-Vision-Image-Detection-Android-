package com.faezmurshidiadnan.whatisthisimage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

/**
 * Created by root on 11/08/16.
 */
public class DetailsActivity extends MainActivity {

    private String details;
    private TextView det;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stub);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        det = (TextView)findViewById(R.id.details);

        Intent intent = getIntent();
        details = intent.getStringExtra("det");
        det.setText(details);




    }
}
