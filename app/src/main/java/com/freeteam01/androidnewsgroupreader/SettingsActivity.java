package com.freeteam01.androidnewsgroupreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.freeteam01.androidnewsgroupreader.Services.AzureService;

public class SettingsActivity extends AppCompatActivity {

    private AutoCompleteTextView mEmailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        Button saveSettingsButton = (Button) findViewById(R.id.save_settings_button);
        saveSettingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        mEmailView.setError(null);

        String email = mEmailView.getText().toString();

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
        }

        if (!TextUtils.isEmpty(email)) {
            // TODO save settings here
            onBackPressed();
        }
    }
}

