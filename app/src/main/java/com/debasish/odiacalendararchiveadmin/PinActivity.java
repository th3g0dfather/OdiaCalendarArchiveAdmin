package com.debasish.odiacalendararchiveadmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.debasish.odiacalendararchiveadmin.sharedprefs.PrefConstant;

public class PinActivity extends AppCompatActivity {

    private EditText editTextPin;
    private Button buttonSubmit;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        editTextPin = findViewById(R.id.edit_text_pin);
        buttonSubmit = findViewById(R.id.button_submit);

        setupSharedPreferences();

        /*
          if shared preference is not empty i.e pin has been set by a user -> get pin from shared preference
          else when pin is not set by user or app is installed for the first time -> default pin 1234
         */

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sharedPin = sharedPreferences.getString(PrefConstant.savedPin, "");
                String pin = editTextPin.getText().toString();
                if(!TextUtils.isEmpty(sharedPin)) {
                    if(pin.equals(sharedPin)) {
                        //if pin matches goto MainActivity else display wrong pin
                        forwardRequest();
                    }
                    else {
                        wrongPin();
                    }
                } else {
                    //when a pin is not created by user
                    if (pin.equals("1234")) {
                        forwardRequest();
                    } else {
                        wrongPin();
                    }

                }
            }
        });
    }

    private void forwardRequest() {
        Intent intent = new Intent(PinActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void wrongPin() {
        Toast.makeText(PinActivity.this, "Incorrect Pin", Toast.LENGTH_SHORT).show();
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PrefConstant.sharedPreferencesName, MODE_PRIVATE);
    }
}