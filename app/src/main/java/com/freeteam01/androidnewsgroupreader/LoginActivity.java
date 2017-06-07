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
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private TextView mNoRegisterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
        mNoRegisterText = (TextView) findViewById(R.id.noregister);
        mNoRegisterText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launch = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(launch, 0);
            }
        });

        if (!AzureService.isInitialized())
        {
            AzureService.Initialize(this, AzureService.createClient(this));
            Log.d("AzureService", "LoginActivity - AzureService.Initialize(this)");
        }
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }


    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        void setProgress(boolean status) {
            mProgressView.setVisibility(status ? View.VISIBLE : View.GONE);
            mPasswordView.setEnabled(!status);
            mEmailView.setEnabled(!status);
            mNoRegisterText.setClickable(!status);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProgress(true);
                }
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (mEmail.equals("test@email.com") && mPassword.equals("testpassword")) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProgress(false);
                }
            });
        }
    }
}

