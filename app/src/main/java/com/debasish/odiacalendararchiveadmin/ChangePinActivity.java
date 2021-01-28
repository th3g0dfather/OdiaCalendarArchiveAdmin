package com.debasish.odiacalendararchiveadmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.debasish.odiacalendararchiveadmin.sharedprefs.PrefConstant;

public class ChangePinActivity extends AppCompatActivity {

    private EditText editTextNewPin;
    private EditText editTextConfirmPin;
    private Button buttonChangePin;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        editTextNewPin = findViewById(R.id.edit_text_new_pin);
        editTextConfirmPin = findViewById(R.id.edit_text_confirm_pin);
        buttonChangePin = findViewById(R.id.button_pin_change);

        setupSharedPreferences();

        /*
          the length of a pin set by user should contain atleast 4 digits
          if new pin and confirm pin do not match display error message
          else -> save pin in shared preference
         */

        buttonChangePin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPin = editTextNewPin.getText().toString();
                String confirmPin = editTextConfirmPin.getText().toString();
                if (newPin.length() >= 4) {
                    if (!newPin.equals(confirmPin)) {
                        Toast.makeText(ChangePinActivity.this, "Pin doesn't match", Toast.LENGTH_SHORT).show();
                    } else {
                        savePin(newPin);
                        //save the pin to shared preference then goto MainActivity
                        Toast.makeText(ChangePinActivity.this, "Pin changed successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePinActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(ChangePinActivity.this, "Pin must be atleast 4 characters long", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(PrefConstant.sharedPreferencesName, MODE_PRIVATE);
    }

    //open shared preference editor and safe pin to shared preference as key and value
    private void savePin(String newPin) {
        editor = sharedPreferences.edit();
        editor.putString(PrefConstant.savedPin, newPin);
        editor.apply();
    }
}