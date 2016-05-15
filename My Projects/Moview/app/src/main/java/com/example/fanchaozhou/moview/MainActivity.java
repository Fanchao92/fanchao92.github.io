package com.example.fanchaozhou.moview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null/*When the app has just started*/) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainActivityFragment())  //Start a main fragment
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {  //Popping the previous fragments out when the BACK button is pressed
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }
}
