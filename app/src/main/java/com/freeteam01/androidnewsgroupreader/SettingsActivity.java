package com.freeteam01.androidnewsgroupreader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.freeteam01.androidnewsgroupreader.ModelsDatabase.UserSetting;
import com.freeteam01.androidnewsgroupreader.Services.AzureService;

import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatActivity {

    private AutoCompleteTextView emailView;
    private AutoCompleteTextView forenameView;
    private AutoCompleteTextView surnameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        emailView = (AutoCompleteTextView) findViewById(R.id.email);
        forenameView = (AutoCompleteTextView) findViewById(R.id.forename);
        surnameView = (AutoCompleteTextView) findViewById(R.id.surname);

        if (!AzureService.isInitialized()) {
            final Context context = this;
            AzureService.Initialize(context, AzureService.createClient(context));
        }
        UserSetting userSetting = AzureService.getInstance().getUserSetting();
        if(userSetting != null)
        {
            emailView.setText(userSetting.getEmail());
            forenameView.setText(userSetting.getForename());
            surnameView.setText(userSetting.getSurname());
        }

        Button saveSettingsButton = (Button) findViewById(R.id.save_settings_button);
        saveSettingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        emailView.setError(null);
        forenameView.setError(null);
        surnameView.setError(null);

        String email = emailView.getText().toString();
        String forename = forenameView.getText().toString();
        String surname = surnameView.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(forename)) {
            forenameView.setError(getString(R.string.error_field_required));
        }
        if (TextUtils.isEmpty(surname)) {
            surnameView.setError(getString(R.string.error_field_required));
        }

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(forename) && !TextUtils.isEmpty(surname)) {
            final UserSetting entry = new UserSetting(null, email, forename, surname);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    AzureService.getInstance().persist(entry);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            onBackPressed();
        }
    }
}

