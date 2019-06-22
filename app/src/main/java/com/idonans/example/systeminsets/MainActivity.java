package com.idonans.example.systeminsets;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.idonans.systeminsets.SystemUiHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemUiHelper.from(getWindow())
                .layoutStatusBar()
                .layoutStable()
                .setLightStatusBar(true)
                .apply();
    }

}
