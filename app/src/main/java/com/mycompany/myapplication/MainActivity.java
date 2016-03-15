package com.mycompany.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    public void login(View view){
        Intent myIntent = new Intent(MainActivity.this, SpotifyActivity.class);
        MainActivity.this.startActivity(myIntent);
    }



}