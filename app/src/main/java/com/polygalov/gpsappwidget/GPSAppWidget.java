package com.polygalov.gpsappwidget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class GPSAppWidget extends AppCompatActivity {

    private MapInFragment mapInFragment;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fragmentManager = getSupportFragmentManager();

        fragmentTransaction = fragmentManager.beginTransaction();

        mapInFragment = new MapInFragment();

        fragmentTransaction.replace(R.id.container, mapInFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();


    }
}
