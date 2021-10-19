package com.example.telescopecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity2 extends AppCompatActivity {

    Button letstart, begin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        begin = (Button) findViewById(R.id.begin);
        letstart = (Button) findViewById(R.id.start);
        beginMethod();
        startMethod();
        letstart.setVisibility(View.INVISIBLE);

    }




    private void startMethod() {
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), PopActivity.class);
                startActivity(i);
                letstart.setVisibility(View.VISIBLE);
            }
        });
    }








   /* private void openPopUpWindow() {
        Intent popupwindow = new Intent (MainActivity.this, PopUpWindows.class);
        startActivity

    } */

    private void beginMethod() {
        letstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity();
            }
        });
    }

    private void openNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}