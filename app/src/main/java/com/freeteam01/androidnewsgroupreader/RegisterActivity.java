package com.freeteam01.androidnewsgroupreader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private UserRegisterTask mRegTask = null;
    private EditText tv_surename;
    private EditText tv_forename;
    private EditText tv_password;
    private EditText tv_passwordrep;
    private EditText tv_email;
    private Button btn_register;
    private TextView tv_alreadyregisterd;
    private ProgressBar pb_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pb_register = (ProgressBar) findViewById(R.id.register_progress);
        tv_surename = (EditText) findViewById(R.id.surname);
        tv_forename = (EditText) findViewById(R.id.forename);
        tv_password = (EditText) findViewById(R.id.password);
        tv_passwordrep = (EditText) findViewById(R.id.passwordrep);
        tv_email = (EditText) findViewById(R.id.email);
        btn_register = (Button) findViewById(R.id.register_button);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addeptRegister();
            }
        });

        tv_alreadyregisterd = (TextView) findViewById(R.id.alreadyregisterd);
        tv_alreadyregisterd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addeptRegister() {
        if (mRegTask != null) {
            return;
        }
        String surename = tv_surename.getText().toString();
        tv_surename.setError(null);
        String forename = tv_forename.getText().toString();
        tv_forename.setError(null);
        String password = tv_password.getText().toString();
        tv_password.setError(null);
        String passwordrep = tv_passwordrep.getText().toString();
        tv_passwordrep.setError(null);
        String email = tv_email.getText().toString();
        tv_email.setError(null);


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(surename)) {
            tv_surename.setError(getString(R.string.error_field_required));
            focusView = tv_surename;
            cancel = true;
        }

        if (TextUtils.isEmpty(forename)) {
            tv_forename.setError(getString(R.string.error_field_required));
            focusView = tv_forename;
            cancel = true;
        }

        if(TextUtils.isEmpty(password))
        {
            tv_password.setError(getString(R.string.error_field_required));
            focusView = tv_password;
            cancel = true;
        }
        else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            tv_password.setError(getString(R.string.error_invalid_password));
            focusView = tv_password;
            cancel = true;
        }

        if (!password.equals(passwordrep)) {
            tv_passwordrep.setError(getString(R.string.error_passwords_not_equal));
            focusView = tv_passwordrep;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            tv_email.setError(getString(R.string.error_field_required));
            focusView = tv_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tv_email.setError(getString(R.string.error_invalid_email));
            focusView = tv_email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mRegTask= new UserRegisterTask(surename, forename, email, password);
            mRegTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mSurname;
        private final String mForename;

        UserRegisterTask(String surename, String forename, String email, String password) {
            mSurname = surename;
            mForename = forename;
            mEmail = email;
            mPassword = password;
        }

        void setProgress(boolean status) {
            pb_register.setVisibility(status ? View.VISIBLE : View.GONE);
            tv_email.setEnabled(!status);
            tv_forename.setEnabled(!status);
            tv_surename.setEnabled(!status);
            tv_password.setEnabled(!status);
            tv_passwordrep.setEnabled(!status);
            tv_alreadyregisterd.setClickable(!status);
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

            boolean successfull = true;
            if (successfull) {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegTask = null;
            if (success) {
                finish();
            } else {
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
