package com.alo7.androidupdate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.alo7.android.update.UpdateAgent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.check_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateAgent.checkUpdate(MainActivity.this);
            }
        });

        findViewById(R.id.force_check_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateAgent.forceCheckUpdate(MainActivity.this);
            }
        });
    }
}
